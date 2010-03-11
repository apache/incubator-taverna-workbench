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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workbench.run.DataflowRun;
import net.sf.taverna.t2.workbench.run.DataflowRunsComponent;

import org.apache.log4j.Logger;

/**
 * Perform database cleanup such as deleting old run data and provenance.
 * <p>
 * The deletion queue is consumed by the {@link DeleteWorkflowRunsThread}.
 * <p>
 * Workflows to delete are stored in a file on Taverna shutdown so that they can
 * be deleted on startup.
 * 
 * @see LoadRunIdsToDeleteStartupHook
 * @see StoreRunIdsToDeleteLaterShutdownHook
 * @see DeleteWorkflowRunsThread
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class DatabaseCleanup {
	private static final String UTF_8 = "UTF-8";

	private static class Singleton {
		private static DatabaseCleanup instance = new DatabaseCleanup();
	}

	protected DatabaseCleanup() {
	}

	public static DatabaseCleanup getInstance() {
		return Singleton.instance;
	}

	private static Logger logger = Logger.getLogger(DatabaseCleanup.class);

	protected Queue<String> deletionQueue = new ConcurrentLinkedQueue<String>();

	protected Set<String> inQueueOrDeleted = Collections
			.synchronizedSet(new HashSet<String>());

	protected Map<String, ReferenceService> runToReferenceService = Collections
			.synchronizedMap(new HashMap<String, ReferenceService>());

	protected DeleteWorkflowRunsThread deleteWorkflowRunsThread;

	public void scheduleDeleteDataflowRun(DataflowRun run, boolean startDeletion) {
		String runId = run.getRunId();
		runToReferenceService.put(runId, run.getReferenceService());
		scheduleDeleteDataflowRun(runId, startDeletion);
	}

	public void scheduleDeleteDataflowRun(String workflowRunId,
			boolean startDeletion) {
		addToDeletionQueue(workflowRunId, startDeletion);
	}

	protected void addToDeletionQueue(String workflowRunId,
			boolean startDeletion) {
		if (inQueueOrDeleted.add(workflowRunId)) {
			deletionQueue.offer(workflowRunId);
		}
		synchronized (this) {
			if (startDeletion
					&& (deleteWorkflowRunsThread == null || !deleteWorkflowRunsThread
							.isAlive()
							&& deleteWorkflowRunsThread.active)) {
				// Start listening for requests for previous workflow runs to be
				// deleted
				deleteWorkflowRunsThread = new DeleteWorkflowRunsThread();
				deleteWorkflowRunsThread.start();
			}
		}
	}

	protected void persist() {
		File runToDeleteFile = getRunToDeleteFile();
		if (deletionQueue.isEmpty()) {
			runToDeleteFile.delete();
			return;
		}
		OutputStream outStream;
		try {
			outStream = new FileOutputStream(runToDeleteFile);
		} catch (FileNotFoundException e) {
			logger.warn("Could not write workflow runs to delete to "
					+ runToDeleteFile, e);
			return;
		}
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(
					new OutputStreamWriter(outStream, UTF_8));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Can't find encoding " + UTF_8, e);
		}
		try {
			for (String wfRunId : deletionQueue) {
				writer.write(wfRunId);
				writer.newLine();
			}
		} catch (IOException e) {
			logger.warn("Could not write workflow runs to delete to "
					+ runToDeleteFile, e);
			return;
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				logger.warn("Could not write workflow runs to delete to "
						+ runToDeleteFile, e);
				return;
			}
		}

	}

	protected File getRunToDeleteFile() {
		File confDir = new File(ApplicationRuntime.getInstance()
				.getApplicationHomeDir(), "var");
		confDir.mkdir();
		return new File(confDir, "runs_to_delete.txt");
	}

	protected void load() {
		File runToDeleteFile = getRunToDeleteFile();
		if (!runToDeleteFile.isFile()) {
			return;
		}
		FileInputStream inStream;
		try {
			inStream = new FileInputStream(runToDeleteFile);
		} catch (FileNotFoundException e) {
			logger.warn("Could not read workflow runs to delete from "
					+ runToDeleteFile, e);
			return;
		}
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(inStream, UTF_8));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Can't find encoding " + UTF_8, e);
		}
		String workflowRunId;
		try {
			workflowRunId = reader.readLine();
			while (workflowRunId != null) {
				addToDeletionQueue(workflowRunId, true);
				workflowRunId = reader.readLine();

			}
		} catch (IOException e) {
			logger.warn("Could not read workflow runs to delete from "
					+ runToDeleteFile, e);
			return;
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				logger.warn("Could not read workflow runs to delete from "
						+ runToDeleteFile, e);
				return;
			}
		}

	}

	public boolean isDeletedOrScheduledForDeletion(String workflowRunId) {
		return inQueueOrDeleted.contains(workflowRunId);
	}

	public ReferenceService getLikelyReferenceService(String runToDelete) {
		ReferenceService refService = runToReferenceService.get(runToDelete);
		if (refService == null) {
			refService = DataflowRunsComponent.getInstance()
					.getReferenceServiceWithDatabase();
		}
		return refService;

	}

	public boolean isDeleteThreadAlive() {
		return (deleteWorkflowRunsThread != null && deleteWorkflowRunsThread
				.isAlive());
	}

	public void requestStopDeletionThread() {
		if (!isDeleteThreadAlive()) {
			return;
		}
		deleteWorkflowRunsThread.requestStop();
		synchronized (deletionQueue) {
			deletionQueue.notifyAll();
		}
	}
}
