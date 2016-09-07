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

package org.apache.taverna.workbench.ui.actions;

import static java.awt.event.KeyEvent.VK_C;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.cutIcon;
import static org.apache.taverna.workbench.ui.workflowview.WorkflowView.cutProcessor;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;

/**
 * Action for copying a processor.
 * 
 * @author Alan R Williams
 */
@SuppressWarnings("serial")
public class CutProcessorAction extends AbstractAction {
	private Processor processor;
	private Workflow dataflow;
	private Component component;

	private final EditManager editManager;
	private final SelectionManager selectionManager;

	public CutProcessorAction(Workflow dataflow, Processor processor,
			Component component, EditManager editManager,
			SelectionManager selectionManager) {
		this.dataflow = dataflow;
		this.processor = processor;
		this.component = component;
		this.editManager = editManager;
		this.selectionManager = selectionManager;
		putValue(SMALL_ICON, cutIcon);
		putValue(NAME, "Cut");
		putValue(MNEMONIC_KEY, VK_C);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		cutProcessor(dataflow, processor, component, editManager,
				selectionManager);
	}
}
