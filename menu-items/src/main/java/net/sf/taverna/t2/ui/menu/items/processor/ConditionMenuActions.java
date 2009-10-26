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
package net.sf.taverna.t2.ui.menu.items.processor;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;
import net.sf.taverna.t2.ui.menu.ContextualMenuComponent;
import net.sf.taverna.t2.ui.menu.ContextualSelection;
import net.sf.taverna.t2.ui.menu.items.contextualviews.ConfigureSection;
import net.sf.taverna.t2.workbench.design.actions.AddConditionAction;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.utils.NamedWorkflowEntityComparator;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

public class ConditionMenuActions extends AbstractMenuCustom implements
		ContextualMenuComponent {

	private ContextualSelection contextualSelection;
	private ArrayList<Processor> processors;

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
				&& getContextualSelection().getParent() instanceof Dataflow;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
		this.customComponent = null;
	}

	@Override
	protected Component createCustomComponent() {

		Dataflow dataflow = (Dataflow) getContextualSelection().getParent();
		Processor processor = (Processor) getContextualSelection()
				.getSelection();
		Component component = getContextualSelection().getRelativeToComponent();

		List<AddConditionAction> conditions = getAddConditionActions(dataflow,
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
			Dataflow dataflow, Processor targetProcessor, Component component) {
		List<AddConditionAction> actions = new ArrayList<AddConditionAction>();
		processors = new ArrayList<Processor>(Tools.possibleUpStreamProcessors(
				dataflow, targetProcessor));
		Collections.sort(processors, new NamedWorkflowEntityComparator());
		for (Processor processor : processors) {
			actions.add(new AddConditionAction(dataflow, processor,
					targetProcessor, component));
		}
		return actions;
	}

}
