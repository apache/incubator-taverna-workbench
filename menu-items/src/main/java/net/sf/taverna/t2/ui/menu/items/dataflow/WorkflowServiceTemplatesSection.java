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
package net.sf.taverna.t2.ui.menu.items.dataflow;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.ui.menu.AbstractMenuSection;
import net.sf.taverna.t2.ui.menu.ContextualMenuComponent;
import net.sf.taverna.t2.ui.menu.ContextualSelection;
import net.sf.taverna.t2.ui.menu.DefaultContextualMenu;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * Menu section containing the actions to add service templates, i.e. activities 
 * than are not readily runnable but need to be configured first. The actual actions that
 * go into this menu can be found in the ui modules for the activities.
 * 
 * @author Alex Nenadic
 *
 */
public class WorkflowServiceTemplatesSection extends AbstractMenuSection
		implements ContextualMenuComponent {

	private static final String SERVICE_TEMPLATES = "Service templates";
	public static final URI serviceTemplatesSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/serviceTemplates");
	private ContextualSelection contextualSelection;

	public WorkflowServiceTemplatesSection() {
		super(DefaultContextualMenu.DEFAULT_CONTEXT_MENU, 30, serviceTemplatesSection);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof Dataflow;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
	}

	@Override
	protected Action createAction() {
		DummyAction action = new DummyAction(SERVICE_TEMPLATES);
		// Set the colour for the section
		action.putValue(AbstractMenuSection.SECTION_COLOR, ShadedLabel.ORANGE);
		return action;
	}
}
