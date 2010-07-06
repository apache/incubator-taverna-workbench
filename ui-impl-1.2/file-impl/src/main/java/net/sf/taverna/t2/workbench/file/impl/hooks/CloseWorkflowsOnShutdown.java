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
package net.sf.taverna.t2.workbench.file.impl.hooks;

import net.sf.taverna.t2.workbench.ShutdownSPI;
import net.sf.taverna.t2.workbench.file.impl.actions.CloseAllWorkflowsAction;

/**
 * Close open workflows (and ask the user if she wants to save changes) on
 * shutdown.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class CloseWorkflowsOnShutdown implements ShutdownSPI {

	public int positionHint() {
		// Quite early, we don't want to do various clean-up in case the
		// user clicks Cancel
		return 50;
	}

	public boolean shutdown() {
		return new CloseAllWorkflowsAction().closeAllWorkflows(null);
	}

}
