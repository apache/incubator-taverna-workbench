/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.file.impl;

import static org.apache.taverna.workbench.file.impl.WorkflowBundleFileType.APPLICATION_VND_TAVERNA_SCUFL2_WORKFLOW_BUNDLE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.taverna.workbench.file.AbstractDataflowPersistenceHandler;
import org.apache.taverna.workbench.file.DataflowInfo;
import org.apache.taverna.workbench.file.DataflowPersistenceHandler;
import org.apache.taverna.workbench.file.FileType;
import org.apache.taverna.workbench.file.exceptions.SaveException;

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
