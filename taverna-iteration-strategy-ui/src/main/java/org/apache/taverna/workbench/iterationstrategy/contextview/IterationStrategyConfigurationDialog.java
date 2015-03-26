/**
 *
 */
package org.apache.taverna.workbench.iterationstrategy.contextview;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.helper.HelpEnabledDialog;
import org.apache.taverna.workbench.iterationstrategy.editor.IterationStrategyEditorControl;
import org.apache.taverna.workflowmodel.Edit;
import org.apache.taverna.workflowmodel.EditException;
import org.apache.taverna.workflowmodel.Edits;
import org.apache.taverna.workflowmodel.Processor;
import org.apache.taverna.workflowmodel.processor.iteration.IterationStrategy;
import org.apache.taverna.workflowmodel.processor.iteration.IterationStrategyStack;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
@SuppressWarnings("serial")
public class IterationStrategyConfigurationDialog extends HelpEnabledDialog {

	private static Logger logger = Logger
	.getLogger(IterationStrategyConfigurationDialog.class);

	private final EditManager editManager;
	private final FileManager fileManager;


	private final Frame owner;
	private final Processor processor;
	private final IterationStrategyStack originalStack;

	private IterationStrategyStack workingStack;

	public IterationStrategyConfigurationDialog(Frame owner, Processor processor, IterationStrategyStack iStack, EditManager editManager, FileManager fileManager) {
		super (owner, "List handling for " + processor.getLocalName(), true, null);
		this.owner = owner;
		this.processor = processor;
		this.originalStack = iStack;
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.workingStack = IterationStrategyContextualView.copyIterationStrategyStack(originalStack);
		IterationStrategy iterationStrategy = IterationStrategyContextualView.getIterationStrategy(workingStack);
		IterationStrategyEditorControl iterationStrategyEditorControl = new IterationStrategyEditorControl(
				iterationStrategy);
		this.add(iterationStrategyEditorControl, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton okButton = new JButton(new OKAction(this));
		buttonPanel.add(okButton);

		JButton resetButton = new JButton(new ResetAction(
				iterationStrategyEditorControl));
		buttonPanel.add(resetButton);

		JButton cancelButton = new JButton(new CancelAction(this));
		buttonPanel.add(cancelButton);

		this.add(buttonPanel, BorderLayout.SOUTH);
		this.pack();
		this.setSize(new Dimension(getPreferredSize().width, getPreferredSize().height > 400 ? 400 : getPreferredSize().height));
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
						IterationStrategyContextualView.copyIterationStrategyStack(workingStack));
				editManager.doDataflowEdit(
						fileManager.getCurrentDataflow(), edit);
				dialog.setVisible(false);
			} catch (RuntimeException ex) {
				logger.warn("Could not set list handling", ex);
				JOptionPane.showMessageDialog(owner,
						"Can't set list handling",
						"An error occured when setting list handling: "
								+ ex.getMessage(),
						JOptionPane.ERROR_MESSAGE);
			} catch (EditException ex) {
				logger.warn("Could not set list handling", ex);
				JOptionPane.showMessageDialog(owner,
						"Can't set list handling",
						"An error occured when setting list handling: "
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
			workingStack = IterationStrategyContextualView.copyIterationStrategyStack(originalStack);
			strategyEditorControl
					.setIterationStrategy(IterationStrategyContextualView.getIterationStrategy(workingStack));
		}

	}

	private final class CancelAction extends AbstractAction {
		private final JDialog dialog;

		private CancelAction(JDialog dialog) {
			super("Cancel");
			this.dialog = dialog;
		}

		public void actionPerformed(ActionEvent e) {
			dialog.setVisible(false);
		}

	}

}
