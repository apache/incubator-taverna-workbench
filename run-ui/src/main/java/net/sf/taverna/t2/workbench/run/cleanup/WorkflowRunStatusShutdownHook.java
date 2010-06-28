/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
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
package net.sf.taverna.t2.workbench.run.cleanup;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade.State;
import net.sf.taverna.t2.workbench.ShutdownSPI;
import net.sf.taverna.t2.workbench.run.ResultsPerspectiveComponent;
import net.sf.taverna.t2.workbench.run.WorkflowRun;

/**
 * Shutdown hook that detects running and paused workflows.
 *
 * @author David Withers
 */
public class WorkflowRunStatusShutdownHook implements ShutdownSPI {

	private ResultsPerspectiveComponent resultsPerspectiveComponent = ResultsPerspectiveComponent.getInstance();
	
	public int positionHint() {
		return 40;
	}

	public boolean shutdown() {
		boolean shutdown = true;
		List<WorkflowRun> workflowRuns = resultsPerspectiveComponent.getPreviousWorkflowRuns();
		List<WorkflowRun> runningWorkflows = new ArrayList<WorkflowRun>();
		List<WorkflowRun> pausedWorkflows = new ArrayList<WorkflowRun>();
		for (WorkflowRun workflowRun : workflowRuns) {
			WorkflowInstanceFacade facade = workflowRun.getFacade();
			if (facade != null) {
				State state = facade.getState();
				switch (state) {
				case paused 	: pausedWorkflows.add(workflowRun);
								  break;
				case prepared 	:
				case running	: runningWorkflows.add(workflowRun);
								  break;				
				}
			}
		}
		if (runningWorkflows.size() + pausedWorkflows.size() > 0) {
			WorkflowRunStatusShutdownDialog dialog = new WorkflowRunStatusShutdownDialog(runningWorkflows.size(), pausedWorkflows.size());
			dialog.setVisible(true);
			shutdown = dialog.confirmShutdown();
		}
		if (shutdown) {
			for (WorkflowRun workflowRun : pausedWorkflows) {
				WorkflowInstanceFacade facade = workflowRun.getFacade();
				if (facade != null) {
					facade.cancelWorkflowRun();
				}
			}
			for (WorkflowRun workflowRun : runningWorkflows) {
				WorkflowInstanceFacade facade = workflowRun.getFacade();
				if (facade != null) {
					facade.cancelWorkflowRun();
				}
			}
		}
		return shutdown;
	}

}
