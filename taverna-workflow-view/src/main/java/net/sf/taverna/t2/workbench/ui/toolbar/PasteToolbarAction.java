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
package net.sf.taverna.t2.workbench.ui.toolbar;

import java.net.URI;

import javax.swing.Action;

import org.apache.taverna.commons.services.ServiceRegistry;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.actions.PasteGraphComponentAction;

public class PasteToolbarAction extends AbstractMenuAction {
	private static final URI EDIT_TOOLBAR_SECTION = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#editToolbarSection");
	private static final URI EDIT_TOOLBAR_PASTE_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#editToolbarPaste");
	private EditManager editManager;
	private MenuManager menuManager;
	private SelectionManager selectionManager;
	private ServiceRegistry serviceRegistry;

	public PasteToolbarAction() {
		super(EDIT_TOOLBAR_SECTION, 50, EDIT_TOOLBAR_PASTE_URI);
	}

	@Override
	protected Action createAction() {
		return PasteGraphComponentAction.getInstance(editManager, menuManager,
				selectionManager, serviceRegistry);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
}
