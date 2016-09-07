/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

package org.apache.taverna.workbench.iterationstrategy.contextview;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.iterationstrategy.IterationStrategyStack;
import org.apache.taverna.scufl2.api.iterationstrategy.IterationStrategyTopNode;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.helper.HelpEnabledDialog;
import org.apache.taverna.workbench.iterationstrategy.editor.IterationStrategyTree;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

/**
 * Contextual view of an {@link IterationStrategyStack}.
 *
 */
public class IterationStrategyContextualView extends ContextualView {

	private static Logger logger = Logger.getLogger(IterationStrategyContextualView.class);

	private EditManager editManager;

	private FileManager fileManager;

	private IterationStrategyStack iterationStack;

	private final Processor processor;

	private IterationStrategyTree strategyTree = new IterationStrategyTree();

	static {

		// This should be enabled and modified for T2-822
		/*
		 * editManager.addObserver(new Observer<EditManagerEvent> () {
		 * 
		 * private void examineEdit(Edit edit) { if (edit instanceof
		 * ConnectDatalinkEdit) {
		 * processConnectDatalinkEdit((ConnectDatalinkEdit) edit); } if (edit
		 * instanceof CompoundEdit) { processCompoundEdit((CompoundEdit) edit);
		 * } }
		 * 
		 * private void processConnectDatalinkEdit(ConnectDatalinkEdit edit) {
		 * Datalink d = ((ConnectDatalinkEdit) edit).getSubject();
		 * EventHandlingInputPort sink = d.getSink(); if (sink instanceof
		 * ProcessorInputPort) { ProcessorInputPort pip = (ProcessorInputPort)
		 * sink; Processor p = pip.getProcessor(); final HelpEnabledDialog
		 * dialog = new IterationStrategyConfigurationDialog(null, p,
		 * copyIterationStrategyStack(p.getIterationStrategy()));
		 * dialog.setVisible(true); } }
		 * 
		 * private void processCompoundEdit(CompoundEdit edit) { for (Edit e :
		 * edit.getChildEdits()) { examineEdit(e); } }
		 * 
		 * @Override public void notify(Observable<EditManagerEvent> sender,
		 * EditManagerEvent message) throws Exception { if (!(message instanceof
		 * DataflowEditEvent)) { return; } examineEdit(message.getEdit()); }});
		 */
	}

	public IterationStrategyContextualView(Processor processor, EditManager editManager, FileManager fileManager) {
		if (processor == null) {
			throw new NullPointerException("Iteration strategy stack can't be null");
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

	public static IterationStrategyStack copyIterationStrategyStack(IterationStrategyStack stack) {
		Element asXML = ((IterationStrategyStack) stack).asXML();
		stripEmptyElements(asXML);
		IterationStrategyStack copyStack = new IterationStrategyStack();
		copyStack.configureFromElement(asXML);
		if (copyStack.getStrategies().isEmpty()) {
			copyStack.addStrategy(new IterationStrategy());
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
				} else if (childElement.getDescendants(new ElementFilter("port")).hasNext()) {
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
		List<? extends IterationStrategy> strategies = iStack.getStrategies();
		if (strategies.isEmpty()) {
			throw new IllegalStateException("Empty iteration stack");
		}
		IterationStrategy strategy = strategies.get(0);
		if (!(strategy instanceof IterationStrategy)) {
			throw new IllegalStateException("Can't edit unknown iteration strategy implementation " + strategy);
		}
		return (IterationStrategy) strategy;
	}

	private void refreshIterationStrategyStack() {
		IterationStrategyStack originalIterationStrategy = processor.getIterationStrategy();
		if (!(originalIterationStrategy instanceof IterationStrategyStack)) {
			throw new IllegalStateException("Unknown iteration strategy implementation " + originalIterationStrategy);
		}
		this.iterationStack = copyIterationStrategyStack((IterationStrategyStack) originalIterationStrategy);
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
			final HelpEnabledDialog dialog = new IterationStrategyConfigurationDialog(owner, processor, iterationStack,
					editManager, fileManager);
			dialog.setVisible(true);
			refreshView();
		}

	}

	@Override
	public int getPreferredPosition() {
		return 200;
	}
}
