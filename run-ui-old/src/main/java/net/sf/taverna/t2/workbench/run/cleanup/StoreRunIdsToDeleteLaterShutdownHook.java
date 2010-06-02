/*******************************************************************************
 * Copyright (C) 2009-2010 The University of Manchester   
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

import net.sf.taverna.t2.workbench.ShutdownSPI;

/**
 * Store identifiers of workflow runs that are scheduled for deletion, but not
 * yet deleted to a file, so that they can be deleted on startup instead.
 * 
 * @see DatabaseCleanup
 * @see LoadRunIdsToDeleteStartupHook
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class StoreRunIdsToDeleteLaterShutdownHook implements ShutdownSPI {

	/**
	 * Quite late, after {@link RemoveDataflowRunsShutdownHook} and
	 * {@link ReferenceDatabaseCleanUpShutdownHook}
	 * 
	 */
	public int positionHint() {
		return 700;
	}

	public boolean shutdown() {
		DatabaseCleanup.getInstance().persist();
		return true;
	}

}
