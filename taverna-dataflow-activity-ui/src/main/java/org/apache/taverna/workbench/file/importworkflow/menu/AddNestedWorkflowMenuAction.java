/*******************************************************************************
 * Copyright (C) 2007-2009 The University of Manchester
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
package org.apache.taverna.workbench.file.importworkflow.menu;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.Action;
import javax.swing.KeyStroke;

import org.apache.taverna.ui.menu.AbstractMenuAction;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.importworkflow.actions.AddNestedWorkflowAction;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.views.graph.menu.InsertMenu;

/**
 * An action to add a nested workflow activity + a wrapping processor to the
 * workflow.
 *
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 *
 */
public class AddNestedWorkflowMenuAction extends AbstractMenuAction {

	private static final String ADD_NESTED_WORKFLOW = "Nested workflow";

	private static final URI ADD_NESTED_WORKFLOW_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#graphMenuAddNestedWorkflow");

	private EditManager editManager;
	private FileManager fileManager;
	private MenuManager menuManager;
	private ColourManager colourManager;
	private WorkbenchConfiguration workbenchConfiguration;
	private SelectionManager selectionManager;

	public AddNestedWorkflowMenuAction() {
		super(InsertMenu.INSERT, 400, ADD_NESTED_WORKFLOW_URI);
	}

	@Override
	protected Action createAction() {
		AddNestedWorkflowAction a = new AddNestedWorkflowAction(editManager, fileManager,
				menuManager, colourManager, workbenchConfiguration, selectionManager);
		// Override name to avoid "Add "
		a.putValue(Action.NAME, ADD_NESTED_WORKFLOW);
		a.putValue(Action.SHORT_DESCRIPTION, ADD_NESTED_WORKFLOW);
		a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_N, InputEvent.SHIFT_DOWN_MASK
						| InputEvent.ALT_DOWN_MASK));
		return a;

	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setColourManager(ColourManager colourManager) {
		this.colourManager = colourManager;
	}

	public void setWorkbenchConfiguration(WorkbenchConfiguration workbenchConfiguration) {
		this.workbenchConfiguration = workbenchConfiguration;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

}
