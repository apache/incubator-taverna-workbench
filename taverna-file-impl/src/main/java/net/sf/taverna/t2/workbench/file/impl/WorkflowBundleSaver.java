/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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
package net.sf.taverna.t2.workbench.file.impl;

import static net.sf.taverna.t2.workbench.file.impl.WorkflowBundleFileType.APPLICATION_VND_TAVERNA_SCUFL2_WORKFLOW_BUNDLE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.sf.taverna.t2.workbench.file.AbstractDataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.DataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.io.WorkflowBundleIO;

public class WorkflowBundleSaver extends AbstractDataflowPersistenceHandler
		implements DataflowPersistenceHandler {
	private static final WorkflowBundleFileType WF_BUNDLE_FILE_TYPE = new WorkflowBundleFileType();
	private static Logger logger = Logger.getLogger(WorkflowBundleSaver.class);
	private WorkflowBundleIO workflowBundleIO;

	@Override
	public DataflowInfo saveDataflow(WorkflowBundle workflowBundle, FileType fileType,
			Object destination) throws SaveException {
		if (!getSaveFileTypes().contains(fileType))
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		OutputStream outStream;
		if (destination instanceof File)
			try {
				outStream = new FileOutputStream((File) destination);
			} catch (FileNotFoundException e) {
				throw new SaveException("Can't create workflow file "
						+ destination + ":\n" + e.getLocalizedMessage(), e);
			}
		else if (destination instanceof OutputStream)
			outStream = (OutputStream) destination;
		else
			throw new SaveException("Unsupported destination type "
					+ destination.getClass());

		try {
			saveDataflowToStream(workflowBundle, outStream);
		} finally {
			try {
				// Only close if we opened the stream
				if (!(destination instanceof OutputStream))
					outStream.close();
			} catch (IOException e) {
				logger.warn("Could not close stream", e);
			}
		}

		if (destination instanceof File)
			return new FileDataflowInfo(WF_BUNDLE_FILE_TYPE, (File) destination,
					workflowBundle);
		return new DataflowInfo(WF_BUNDLE_FILE_TYPE, destination, workflowBundle);
	}

	protected void saveDataflowToStream(WorkflowBundle workflowBundle,
			OutputStream fileOutStream) throws SaveException {
		try {
			workflowBundleIO.writeBundle(workflowBundle, fileOutStream,
					APPLICATION_VND_TAVERNA_SCUFL2_WORKFLOW_BUNDLE);
		} catch (Exception e) {
			throw new SaveException("Can't write workflow:\n"
					+ e.getLocalizedMessage(), e);
		}
	}

	@Override
	public List<FileType> getSaveFileTypes() {
		return Arrays.<FileType> asList(WF_BUNDLE_FILE_TYPE);
	}

	@Override
	public List<Class<?>> getSaveDestinationTypes() {
		return Arrays.<Class<?>> asList(File.class, OutputStream.class);
	}

	@Override
	public boolean wouldOverwriteDataflow(WorkflowBundle workflowBundle, FileType fileType,
			Object destination, DataflowInfo lastDataflowInfo) {
		if (!getSaveFileTypes().contains(fileType))
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		if (!(destination instanceof File))
			return false;

		File file;
		try {
			file = ((File) destination).getCanonicalFile();
		} catch (IOException e) {
			return false;
		}
		if (!file.exists())
			return false;
		if (lastDataflowInfo == null)
			return true;
		Object lastDestination = lastDataflowInfo.getCanonicalSource();
		if (!(lastDestination instanceof File))
			return true;
		File lastFile = (File) lastDestination;
		if (!lastFile.getAbsoluteFile().equals(file))
			return true;

		Date lastModified = new Date(file.lastModified());
		if (lastModified.equals(lastDataflowInfo.getLastModified()))
			return false;
		return true;
	}

	public void setWorkflowBundleIO(WorkflowBundleIO workflowBundleIO) {
		this.workflowBundleIO = workflowBundleIO;
	}
}
