/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.views.graph.actions;

import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_I;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.inputIcon;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.taverna.ui.menu.DesignOnlyAction;
import org.apache.taverna.workbench.design.actions.AddDataflowInputAction;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.scufl2.api.core.Workflow;

/**
 * An action that adds a workflow input.
 * 
 * @author Alex Nenadic
 * @author Alan R Williams
 */
@SuppressWarnings("serial")
public class AddWFInputAction extends AbstractAction implements
		DesignOnlyAction {
	private final EditManager editManager;
	private final SelectionManager selectionManager;

	public AddWFInputAction(EditManager editManager,
			SelectionManager selectionManager) {
		super();
		this.editManager = editManager;
		this.selectionManager = selectionManager;
		putValue(SMALL_ICON, inputIcon);
		putValue(NAME, "Workflow input port");
		putValue(SHORT_DESCRIPTION, "Workflow input port");
		putValue(ACCELERATOR_KEY,
				getKeyStroke(VK_I, SHIFT_DOWN_MASK | ALT_DOWN_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Workflow workflow = selectionManager.getSelectedWorkflow();
		new AddDataflowInputAction(workflow, null, editManager,
				selectionManager).actionPerformed(e);
	}
}
