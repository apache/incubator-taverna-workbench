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

package org.apache.taverna.activities.stringconstant.menu;

import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_S;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.taverna.workbench.ui.workflowview.WorkflowView.importServiceDescription;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;
import org.apache.taverna.ui.menu.AbstractMenuAction;
import org.apache.taverna.ui.menu.DesignOnlyAction;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.services.ServiceRegistry;

/**
 * An action to add a string constant activity + a wrapping processor to the
 * workflow.
 * 
 * @author Alex Nenadic
 * @author Alan R Williams
 * @author David Withers
 */
@SuppressWarnings("serial")
public class AddStringConstantTemplateMenuAction extends AbstractMenuAction {
	private static final URI ACTIVITY_TYPE = URI
			.create("http://ns.taverna.org.uk/2010/activity/constant");
	private static final URI INSERT = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#insert");
	private static final String ADD_STRING_CONSTANT = "Text constant";
	private static final URI ADD_STRING_CONSTANT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#graphMenuAddStringConstant");

	private EditManager editManager;
	private MenuManager menuManager;
	private SelectionManager selectionManager;
	private ActivityIconManager activityIconManager;
	private ServiceDescriptionRegistry serviceDescriptionRegistry;
	private ServiceRegistry serviceRegistry;

	public AddStringConstantTemplateMenuAction() {
		super(INSERT, 800, ADD_STRING_CONSTANT_URI);
	}

	@Override
	protected Action createAction() {
		return new AddStringConstantMenuAction();
	}

	protected class AddStringConstantMenuAction extends AbstractAction
			implements DesignOnlyAction {
		AddStringConstantMenuAction() {
			super();
			putValue(SMALL_ICON,
					activityIconManager.iconForActivity(ACTIVITY_TYPE));
			putValue(NAME, ADD_STRING_CONSTANT);
			putValue(SHORT_DESCRIPTION, ADD_STRING_CONSTANT);
			putValue(ACCELERATOR_KEY,
					getKeyStroke(VK_S, SHIFT_DOWN_MASK | ALT_DOWN_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			importServiceDescription(
					serviceDescriptionRegistry
							.getServiceDescription(ACTIVITY_TYPE),
					false, editManager, menuManager, selectionManager,
					serviceRegistry);
		}
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

	public void setActivityIconManager(ActivityIconManager activityIconManager) {
		this.activityIconManager = activityIconManager;
	}

	public void setServiceDescriptionRegistry(
			ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
}
