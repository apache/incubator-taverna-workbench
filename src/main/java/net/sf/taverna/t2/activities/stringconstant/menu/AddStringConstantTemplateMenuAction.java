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
package net.sf.taverna.t2.activities.stringconstant.menu;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.servicedescriptions.StringConstantTemplateService;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.ui.menu.DesignOnlyAction;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;

/**
 * An action to add a string constant activity + a wrapping processor to the workflow.
 *
 * @author Alex Nenadic
 * @author Alan R Williams
 *
 */
@SuppressWarnings("serial")
public class AddStringConstantTemplateMenuAction extends AbstractMenuAction {

	private static final URI INSERT = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#insert");

	private static final String ADD_STRING_CONSTANT = "String constant";

	private static final URI ADD_STRING_CONSTANT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#graphMenuAddStringConstant");

	private EditManager editManager;

	private MenuManager menuManager;

	private DataflowSelectionManager dataflowSelectionManager;

	private ActivityIconManager activityIconManager;

	// private static Logger logger = Logger.getLogger(AddStringConstantTemplateMenuAction.class);

	public AddStringConstantTemplateMenuAction() {
		super(INSERT, 800, ADD_STRING_CONSTANT_URI);
	}

	@Override
	protected Action createAction() {
		return new AddStringConstantMenuAction();
	}

	protected class AddStringConstantMenuAction extends DesignOnlyAction {
		AddStringConstantMenuAction() {
			super();
			putValue(SMALL_ICON, activityIconManager.iconForActivity(new StringConstantActivity()));
			putValue(NAME, ADD_STRING_CONSTANT);
			putValue(SHORT_DESCRIPTION, ADD_STRING_CONSTANT);
			putValue(
					Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK
							| InputEvent.ALT_DOWN_MASK));

		}

		public void actionPerformed(ActionEvent e) {

			WorkflowView.importServiceDescription(
					StringConstantTemplateService.getServiceDescription(), false, editManager,
					menuManager, dataflowSelectionManager);

		}
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setDataflowSelectionManager(DataflowSelectionManager dataflowSelectionManager) {
		this.dataflowSelectionManager = dataflowSelectionManager;
	}

	public void setActivityIconManager(ActivityIconManager activityIconManager) {
		this.activityIconManager = activityIconManager;
	}

}
