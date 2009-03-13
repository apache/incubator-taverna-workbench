/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
/**
 * 
 */
package net.sf.taverna.t2.workbench.iterationstrategy.contextview;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.iterationstrategy.editor.IterationStrategyEditorControl;
import net.sf.taverna.t2.workbench.iterationstrategy.editor.IterationStrategyTree;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategy;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyStack;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyStackImpl;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * Contextual view of an {@link IterationStrategyStack}.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class IterationStrategyContextualView extends ContextualView {

	private static Logger logger = Logger
			.getLogger(IterationStrategyContextualView.class);

	private EditManager editManager = EditManager.getInstance();

	private FileManager fileManager = FileManager.getInstance();

	private IterationStrategyStackImpl iterationStack;

	private final Processor processor;

	private IterationStrategyTree strategyTree = new IterationStrategyTree();

	public IterationStrategyContextualView(Processor processor) {
		if (processor == null || processor.getIterationStrategy() == null) {
			throw new NullPointerException(
					"Iteration strategy stack can't be null");
		}
		this.processor = processor;
		refreshIterationStrategyStack();
		initView();
	}

	@Override
	public Action getConfigureAction(final Frame owner) {
		return new ConfigureIterationStrategyAction(owner);
	}

	public Processor getProcessor() {
		return processor;
	}

	@Override
	public void refreshView() {
		refreshIterationStrategyStack();
		strategyTree.setIterationStrategy(getIterationStrategy());
	}

	private IterationStrategyStackImpl copyIterationStrategyStack(
			IterationStrategyStackImpl stack) {
		Element asXML = stack.asXML();
		IterationStrategyStackImpl copyStack = new IterationStrategyStackImpl();
		copyStack.configureFromElement(asXML);
		return copyStack;
	}

	private IterationStrategyImpl getIterationStrategy() {
		List<? extends IterationStrategy> strategies = iterationStack
				.getStrategies();
		if (strategies.isEmpty()) {
			throw new IllegalStateException("Empty iteration stack");
		}
		IterationStrategy strategy = strategies.get(0);
		if (!(strategy instanceof IterationStrategyImpl)) {
			throw new IllegalStateException(
					"Can't edit unknown iteration strategy implementation "
							+ strategy);
		}
		return (IterationStrategyImpl) strategy;
	}

	private void refreshIterationStrategyStack() {
		IterationStrategyStack originalIterationStrategy = processor
				.getIterationStrategy();
		if (!(originalIterationStrategy instanceof IterationStrategyStackImpl)) {
			throw new IllegalStateException(
					"Unknown iteration strategy implementation "
							+ originalIterationStrategy);
		}
		this.iterationStack = copyIterationStrategyStack((IterationStrategyStackImpl) originalIterationStrategy);
	}

	@Override
	public JComponent getMainFrame() {
		refreshView();
		return strategyTree;
	}

	@Override
	public String getViewTitle() {
		return "List handling";
	}

	private final class ConfigureIterationStrategyAction extends AbstractAction {
		private final Frame owner;

		private ConfigureIterationStrategyAction(Frame owner) {
			super("Configure list handling");
			this.owner = owner;
		}

		public void actionPerformed(ActionEvent e) {
			String title = "Iteration strategy for " + processor.getLocalName();
			final HelpEnabledDialog dialog = new HelpEnabledDialog(owner, title, true, null);
			IterationStrategyImpl iterationStrategy = getIterationStrategy();
			IterationStrategyEditorControl iterationStrategyEditorControl = new IterationStrategyEditorControl(
					iterationStrategy);
			dialog.add(iterationStrategyEditorControl, BorderLayout.CENTER);

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout());

			JButton okButton = new JButton(new OKAction(dialog));
			buttonPanel.add(okButton);

			JButton resetButton = new JButton(new ResetAction(
					iterationStrategyEditorControl));
			buttonPanel.add(resetButton);

			JButton cancelButton = new JButton(new CancelAction(dialog));
			buttonPanel.add(cancelButton);

			dialog.add(buttonPanel, BorderLayout.SOUTH);
			dialog.setSize(400, 400);
			dialog.setVisible(true);
		}

		private final class CancelAction extends AbstractAction {
			private final JDialog dialog;

			private CancelAction(JDialog dialog) {
				super("Cancel");
				this.dialog = dialog;
			}

			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				refreshView();
			}

		}

		private final class OKAction extends AbstractAction {
			private final JDialog dialog;

			private OKAction(JDialog dialog) {
				super("OK");
				this.dialog = dialog;
			}

			public void actionPerformed(ActionEvent e) {
				Edits edits = editManager.getEdits();
				try {
					Edit<?> edit = edits.getSetIterationStrategyStackEdit(
							processor,
							copyIterationStrategyStack(iterationStack));
					editManager.doDataflowEdit(
							fileManager.getCurrentDataflow(), edit);
					dialog.setVisible(false);
					refreshView();
				} catch (RuntimeException ex) {
					logger.warn("Could not set iteration strategy", ex);
					JOptionPane.showMessageDialog(owner,
							"Can't set iteration strategy",
							"An error occured when setting iteration strategy: "
									+ ex.getMessage(),
							JOptionPane.ERROR_MESSAGE);
				} catch (EditException ex) {
					logger.warn("Could not set iteration strategy", ex);
					JOptionPane.showMessageDialog(owner,
							"Can't set iteration strategy",
							"An error occured when setting iteration strategy: "
									+ ex.getMessage(),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}

		private final class ResetAction extends AbstractAction {
			private final IterationStrategyEditorControl strategyEditorControl;

			private ResetAction(
					IterationStrategyEditorControl strategyEditorControl) {
				super("Reset");
				this.strategyEditorControl = strategyEditorControl;
			}

			public void actionPerformed(ActionEvent e) {
				refreshView();
				strategyEditorControl
						.setIterationStrategy(getIterationStrategy());
			}

		}
	}
}
