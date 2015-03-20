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
package net.sf.taverna.t2.workbench.iterationstrategy.menu;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.iterationstrategy.contextview.IterationStrategyConfigurationDialog;
import net.sf.taverna.t2.workbench.iterationstrategy.contextview.IterationStrategyContextualView;
import net.sf.taverna.t2.workflowmodel.Processor;

public class IterationStrategyConfigureMenuAction extends AbstractContextualMenuAction {
	
	
	
	public static final URI configureRunningSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/configureRunning");
	
	private static final URI ITERATION_STRATEGY_CONFIGURE_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/iterationStrategyConfigure");

	public IterationStrategyConfigureMenuAction() {
		super(configureRunningSection, 40, ITERATION_STRATEGY_CONFIGURE_URI);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("List handling...") {
			public void actionPerformed(ActionEvent e) {
				Processor p = (Processor) getContextualSelection().getSelection();
				final HelpEnabledDialog dialog = new IterationStrategyConfigurationDialog(null, p, IterationStrategyContextualView.copyIterationStrategyStack(p.getIterationStrategy()));		
				dialog.setVisible(true);
			}
		};
	}
	
	public boolean isEnabled() {
		return super.isEnabled() && (getContextualSelection().getSelection() instanceof Processor);
	}

}
