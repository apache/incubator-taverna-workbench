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
package net.sf.taverna.t2.ui.menu.items.contextualviews;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.ui.menu.DefaultContextualMenu;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workbench.views.graph.actions.DesignOnlyAction;

public class ShowDetailsMenuAction extends AbstractMenuAction {
	private static final URI SHOW_DETAILS_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/menu#graphMenuShowDetailsComponent");

	private static final String SHOW_DETAILS = "Details";
	private String namedComponent = "contextualView";

	public ShowDetailsMenuAction() {
		super(ShowConfigureMenuAction.GRAPH_DETAILS_MENU_SECTION, 20, SHOW_DETAILS_URI);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled();
		// FIXME: Should we list all the applicable types here?
		// && getContextualSelection().getSelection() instanceof Processor;
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new ShowDetailsAction();
	}

	protected class ShowDetailsAction extends DesignOnlyAction {
		
		ShowDetailsAction() {
			super();
			putValue(NAME, "Show details");	
			putValue(SHORT_DESCRIPTION, "Show details of selected component");
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			Workbench workbench = Workbench.getInstance();
			workbench.getPerspectives().setWorkflowPerspective();
			workbench.makeNamedComponentVisible(namedComponent);			
		}
		
	}
}
