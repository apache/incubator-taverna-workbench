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

import java.util.ArrayList;

import net.sf.taverna.t2.workbench.ShutdownSPI;
import net.sf.taverna.t2.workbench.run.DataflowRun;
import net.sf.taverna.t2.workbench.run.DataflowRunsComponent;

/**
 * Performs clean up of the Reference Manager's database by deleting references
 * for runs with no provenance, and removing provenance for runs without
 * database.
 * <p>
 * Actual database deletion is scheduled and stored by
 * {@link StoreRunIdsToDeleteLaterShutdownHook} so that they are deleted at
 * next Taverna startup.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public class ReferenceDatabaseCleanUpShutdownHook implements ShutdownSPI {

	public int positionHint() {
		// Run before RemoveDataflowRunsShutdownHook
		return 250;
	}

	public boolean shutdown() {
		final ArrayList<DataflowRun> wfRunList = DataflowRunsComponent
				.getInstance().getPreviousWFRuns();

		DatabaseCleanup databaseCleanup = DatabaseCleanup.getInstance();
		for (int i = wfRunList.size() - 1; i >= 0; i--) {
			DataflowRun dataflowRun = wfRunList.get(i);
			if (!dataflowRun.isProvenanceEnabledForRun()) {
				// provenance was not enabled for the wf run
				if (dataflowRun.isDataSavedInDatabase()) {
					// was data for the wf run stored in database
					// Delete all the referenced data for the run
					// DataflowRunsComponent.getInstance().getReferenceService().deleteReferencesForWorkflowRun(wfRunList.get(i).getRunId());
					databaseCleanup.scheduleDeleteDataflowRun(dataflowRun,
							false);
				}
			} else if (!dataflowRun.isDataSavedInDatabase()) {
				// data was stored in memory
				// Delete the run from provenance database
				databaseCleanup.scheduleDeleteDataflowRun(dataflowRun, false);

			} else {
				// Don't delete it, either in-memory-no-provenance or
				// in-database-with-provenance.
			}
		}
		return true;
	}
}
