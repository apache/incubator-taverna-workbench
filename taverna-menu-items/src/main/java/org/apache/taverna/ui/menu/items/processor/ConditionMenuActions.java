/**********************************************************************
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
 **********************************************************************/
package org.apache.taverna.ui.menu.items.processor;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.taverna.lang.ui.ShadedLabel;
import org.apache.taverna.ui.menu.AbstractMenuCustom;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.ContextualSelection;
import org.apache.taverna.ui.menu.items.contextualviews.ConfigureSection;
import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.workbench.design.actions.AddConditionAction;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.icons.WorkbenchIcons;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;

public class ConditionMenuActions extends AbstractMenuCustom implements
		ContextualMenuComponent {

	private ContextualSelection contextualSelection;
	private EditManager editManager;
	private SelectionManager selectionManager;
	private ActivityIconManager activityIconManager;
	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	public ConditionMenuActions() {
		super(ConfigureSection.configureSection, 80 );
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof Processor
				&& getContextualSelection().getParent() instanceof Workflow;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
		this.customComponent = null;
	}

	@Override
	protected Component createCustomComponent() {

		Workflow workflow = (Workflow) getContextualSelection().getParent();
		Processor processor = (Processor) getContextualSelection()
				.getSelection();
		Component component = getContextualSelection().getRelativeToComponent();

		List<AddConditionAction> conditions = getAddConditionActions(workflow,
				processor, component);
		if (conditions.isEmpty()) {
			return null;
		}
		JMenu conditionMenu = new JMenu("Run after");
		conditionMenu.setIcon(WorkbenchIcons.controlLinkIcon);
		conditionMenu.add(new ShadedLabel("Services:", ShadedLabel.ORANGE));
		conditionMenu.addSeparator();
		for (AddConditionAction addConditionAction : conditions) {
			conditionMenu.add(new JMenuItem(addConditionAction));
		}
		return conditionMenu;
	}

	protected List<AddConditionAction> getAddConditionActions(
			Workflow workflow, Processor targetProcessor, Component component) {
		List<AddConditionAction> actions = new ArrayList<AddConditionAction>();
		for (Processor processor : scufl2Tools.possibleUpStreamProcessors(workflow, targetProcessor)) {
			actions.add(new AddConditionAction(workflow, processor,
					targetProcessor, component, editManager, selectionManager, activityIconManager));
		}
		return actions;
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setActivityIconManager(ActivityIconManager activityIconManager) {
		this.activityIconManager = activityIconManager;
	}

}
