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

package org.apache.taverna.workbench.ui.actions;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.VK_P;
import static java.awt.event.KeyEvent.VK_V;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.pasteIcon;
import static org.apache.taverna.workbench.ui.workflowview.WorkflowView.pasteTransferable;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.services.ServiceRegistry;

/**
 * An action that pastes a graph component
 *
 * @author Alan R Williams
 */
@SuppressWarnings("serial")
//TODO this class appears to be non-OSGi-fied
public class PasteGraphComponentAction extends AbstractAction {
	private static PasteGraphComponentAction instance = null;

	private static boolean enabled = false;

	private final EditManager editManager;
	private final MenuManager menuManager;
	private final SelectionManager selectionManager;
	private final ServiceRegistry serviceRegistry;

	private PasteGraphComponentAction(EditManager editManager,
			MenuManager menuManager, SelectionManager selectionManager,
			ServiceRegistry serviceRegistry) {
		super();
		this.editManager = editManager;
		this.menuManager = menuManager;
		this.selectionManager = selectionManager;
		this.serviceRegistry = serviceRegistry;
		putValue(SMALL_ICON, pasteIcon);
		putValue(NAME, "Paste");
		putValue(SHORT_DESCRIPTION, "Paste");
		putValue(MNEMONIC_KEY, VK_P);

		putValue(
				ACCELERATOR_KEY,
				getKeyStroke(VK_V, getDefaultToolkit().getMenuShortcutKeyMask()));
		setEnabled(enabled);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		pasteTransferable(editManager, menuManager, selectionManager,
				serviceRegistry);
	}

	public static Action getInstance(EditManager editManager,
			MenuManager menuManager, SelectionManager selectionManager,
			ServiceRegistry serviceRegistry) {
		if (instance == null)
			instance = new PasteGraphComponentAction(editManager, menuManager,
					selectionManager, serviceRegistry);
		return instance;
	}

	public static void setEnabledStatic(boolean enabled) {
		if (instance == null) {
			PasteGraphComponentAction.enabled = enabled;
		} else {
			instance.setEnabled(enabled);
		}
	}
}
