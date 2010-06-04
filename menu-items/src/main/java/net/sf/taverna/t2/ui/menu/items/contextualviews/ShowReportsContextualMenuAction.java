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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.t2.lang.ui.icons.Icons;
import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public class ShowReportsContextualMenuAction extends AbstractContextualMenuAction {
	private static final ReportManager reportManager = ReportManager.getInstance();
	private static final FileManager fileManager = FileManager.getInstance();
	
	private static final String SHOW_REPORTS = "Show reports";
	private String namedComponent = "reportView";

	@SuppressWarnings("unused")
	private static Log logger = Log.getLogger(ShowReportsContextualMenuAction.class);

	public ShowReportsContextualMenuAction() {
		/** Right below ShowDetailsContextualMenuAction 
		 */		
		super(ConfigureSection.configureSection, 41);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled();
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		Dataflow parent;
		if (getContextualSelection().getParent() instanceof Dataflow) {
			parent = (Dataflow)getContextualSelection().getParent();
		} else {
			parent = fileManager.getCurrentDataflow();
		}
		Status status = reportManager.getStatus(parent, getContextualSelection().getSelection());
		
		Icon icon = null;
		if (status == Status.WARNING) {
			icon = Icons.warningIcon;
		} else if (status == Status.SEVERE) {
			icon = Icons.severeIcon;
		} 
		
		return new AbstractAction(SHOW_REPORTS, icon) {
			public void actionPerformed(ActionEvent e) {
				Workbench workbench = Workbench.getInstance();
				workbench.getPerspectives().setWorkflowPerspective();
				workbench.makeNamedComponentVisible(namedComponent);
			}
		};
	}

}
