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

import java.util.Queue;

import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;

import org.apache.log4j.Logger;

/**
 * Thread that deletes provenance for previous workflow runs placed in a special
 * queue.
 */
public class DeleteWorkflowRunsThread extends Thread {

	private static Logger logger = Logger
			.getLogger(DeleteWorkflowRunsThread.class);
	protected boolean active = true;

	public DeleteWorkflowRunsThread() {
		super("Deleting old workflow runs");
		setDaemon(true);
		setPriority(MIN_PRIORITY + 1);
	}
	
	public void requestStop() {
		active = false;
	}

	public void run() {
		DatabaseCleanup databaseCleanup = DatabaseCleanup.getInstance();
		String connectorType = DataManagementConfiguration.getInstance()
				.getConnectorType();
		ProvenanceAccess provenanceAccess = new ProvenanceAccess(connectorType);

		try {
			while (active) {
				Queue<String> queue = databaseCleanup.deletionQueue;
				synchronized (queue) {
					// Wait until an element is placed in the queue
					while (queue.isEmpty()) {
						queue.wait();
						if (! active) {
							return;
						}
					}
				}
				// Retrieve the first element from the queue (but do not
				// remove it)
				String runToDelete = queue.peek();

				// Remove provenance data for the run (if any) and all
				// references held by the workflow run from the Reference
				// Manager's store
				try {
					logger.info("Starting deletion of workflow run '"
							+ runToDelete.toString() + "' (run id "
							+ runToDelete + ").");

					ReferenceService refService = databaseCleanup
							.getLikelyReferenceService(runToDelete);
					// Remove the run from provenance database (if it is
					// stored there at all)
					provenanceAccess.removeRun(runToDelete);

					// Remove references from the Reference Manager's store
					// (regardless if in-memory or database)
					refService.deleteReferencesForWorkflowRun(runToDelete);
					String message = "Deletion of workflow run '"
							+ runToDelete
							+ "' from provenance database and Reference Manager's store completed.";
					logger.info(message);
				} catch (Exception ex) {
					String message = "Failed to delete workflow run '"
							+ runToDelete
							+ "' from provenance database and Reference Manager's store.";
					logger.error(message, ex);
				} finally {
					queue.poll(); // Remove from queue, even if deletion failed
				}
			}
		} catch (InterruptedException ignored) {
			return;
		}
	}
}