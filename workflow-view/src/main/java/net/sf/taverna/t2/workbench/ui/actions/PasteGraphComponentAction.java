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
package net.sf.taverna.t2.workbench.ui.actions;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.VK_P;
import static java.awt.event.KeyEvent.VK_V;
import static javax.swing.KeyStroke.getKeyStroke;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.pasteIcon;
import static net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView.pasteTransferable;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import uk.org.taverna.commons.services.ServiceRegistry;

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
