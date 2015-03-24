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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.iterationstrategy.editor.IterationStrategyTree;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workflowmodel.Processor;
import org.apache.taverna.workflowmodel.processor.iteration.IterationStrategy;
import org.apache.taverna.workflowmodel.processor.iteration.IterationStrategyStack;
import org.apache.taverna.workflowmodel.processor.iteration.impl.IterationStrategyImpl;
import org.apache.taverna.workflowmodel.processor.iteration.impl.IterationStrategyStackImpl;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

/**
 * Contextual view of an {@link IterationStrategyStack}.
 *
 * @author Stian Soiland-Reyes
 *
 */
public class IterationStrategyContextualView extends ContextualView {

	private static Logger logger = Logger
			.getLogger(IterationStrategyContextualView.class);

	private EditManager editManager;

	private FileManager fileManager;

	private IterationStrategyStack iterationStack;

	private final Processor processor;

	private IterationStrategyTree strategyTree = new IterationStrategyTree();

	static {

// This should be enabled and modified for T2-822
/*		editManager.addObserver(new Observer<EditManagerEvent> () {

			private void examineEdit(Edit edit) {
				if (edit instanceof ConnectDatalinkEdit) {
					processConnectDatalinkEdit((ConnectDatalinkEdit) edit);
				}
				if (edit instanceof CompoundEdit) {
					processCompoundEdit((CompoundEdit) edit);
				}
			}

			private void processConnectDatalinkEdit(ConnectDatalinkEdit edit) {
				Datalink d = ((ConnectDatalinkEdit) edit).getSubject();
				EventHandlingInputPort sink = d.getSink();
				if (sink instanceof ProcessorInputPort) {
					ProcessorInputPort pip = (ProcessorInputPort) sink;
					Processor p = pip.getProcessor();
					final HelpEnabledDialog dialog = new IterationStrategyConfigurationDialog(null, p, copyIterationStrategyStack(p.getIterationStrategy()));
					dialog.setVisible(true);
				}
			}

			private void processCompoundEdit(CompoundEdit edit) {
				for (Edit e : edit.getChildEdits()) {
					examineEdit(e);
				}
			}

			@Override
			public void notify(Observable<EditManagerEvent> sender,
					EditManagerEvent message) throws Exception {
				if (!(message instanceof DataflowEditEvent)) {
					return;
				}
				examineEdit(message.getEdit());
			}});*/
	}

	public IterationStrategyContextualView(Processor processor, EditManager editManager, FileManager fileManager) {
		if (processor == null || processor.getIterationStrategy() == null) {
			throw new NullPointerException(
					"Iteration strategy stack can't be null");
		}
		this.processor = processor;
		this.editManager = editManager;
		this.fileManager = fileManager;
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
		strategyTree.setIterationStrategy(getIterationStrategy(iterationStack));
	}

	public static IterationStrategyStack copyIterationStrategyStack(
			IterationStrategyStack stack) {
		Element asXML = ((IterationStrategyStackImpl)stack).asXML();
		stripEmptyElements(asXML);
		IterationStrategyStackImpl copyStack = new IterationStrategyStackImpl();
		copyStack.configureFromElement(asXML);
		if (copyStack.getStrategies().isEmpty()) {
			copyStack.addStrategy(new IterationStrategyImpl());
		}
		return copyStack;
	}

	private static void stripEmptyElements(Element asXML) {
		int childCount = asXML.getContent().size();
		int index = 0;
		while (index < childCount) {
			Content child = asXML.getContent(index);
			if (child instanceof Element) {
				Element childElement = (Element) child;
				if (childElement.getName().equals("port")) {
					index++;
				}
				else if (childElement.getDescendants(new ElementFilter("port")).hasNext()) {
					stripEmptyElements(childElement);
					index++;
				} else {
					asXML.removeContent(childElement);
					childCount--;
				}
			}
		}
	}

	public static IterationStrategy getIterationStrategy(IterationStrategyStack iStack) {
		List<? extends IterationStrategy> strategies = iStack
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
			super("Configure");
			this.owner = owner;
		}

		public void actionPerformed(ActionEvent e) {
			final HelpEnabledDialog dialog = new IterationStrategyConfigurationDialog(owner, processor, iterationStack, editManager, fileManager);
			dialog.setVisible(true);
			refreshView();
		}



	}

	@Override
	public int getPreferredPosition() {
		return 200;
	}
}
