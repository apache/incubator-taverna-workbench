/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

package org.apache.taverna.workbench.file.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.taverna.workbench.file.AbstractDataflowPersistenceHandler;
import org.apache.taverna.workbench.file.DataflowInfo;
import org.apache.taverna.workbench.file.DataflowPersistenceHandler;
import org.apache.taverna.workbench.file.FileType;
import org.apache.taverna.workbench.file.exceptions.OpenException;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.io.ReaderException;
import org.apache.taverna.scufl2.api.io.WorkflowBundleIO;

public class T2DataflowOpener extends AbstractDataflowPersistenceHandler
		implements DataflowPersistenceHandler {
	private static final T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();
	private static Logger logger = Logger.getLogger(T2DataflowOpener.class);

	private WorkflowBundleIO workflowBundleIO;

	@SuppressWarnings("resource")
	@Override
	public DataflowInfo openDataflow(FileType fileType, Object source)
			throws OpenException {
		if (!getOpenFileTypes().contains(fileType))
			throw new OpenException("Unsupported file type "
					+ fileType);
		InputStream inputStream;
		Date lastModified = null;
		Object canonicalSource = source;
		if (source instanceof InputStream)
			inputStream = (InputStream) source;
		else if (source instanceof File)
			try {
				inputStream = new FileInputStream((File) source);
			} catch (FileNotFoundException e) {
				throw new OpenException("Could not open file " + source + ":\n" + e.getLocalizedMessage(), e);
			}
		else if (source instanceof URL) {
			URL url = ((URL) source);
			try {
				URLConnection connection = url.openConnection();
				connection.setRequestProperty("Accept", "text/xml");
				inputStream = connection.getInputStream();
				if (connection.getLastModified() != 0)
					lastModified = new Date(connection.getLastModified());
			} catch (IOException e) {
				throw new OpenException("Could not open connection to URL "
						+ source+ ":\n" + e.getLocalizedMessage(), e);
			}
			try {
				if (url.getProtocol().equalsIgnoreCase("file"))
					canonicalSource = new File(url.toURI());
			} catch (URISyntaxException e) {
				logger.warn("Invalid file URI created from " + url);
			}
		} else {
			throw new OpenException("Unsupported source type "
					+ source.getClass());
		}

		final WorkflowBundle workflowBundle;
		try {
			workflowBundle = openDataflowStream(inputStream);
		} finally {
			try {
				if (!(source instanceof InputStream))
					// We created the stream, we'll close it
					inputStream.close();
			} catch (IOException ex) {
				logger.warn("Could not close inputstream " + inputStream, ex);
			}
		}
		if (canonicalSource instanceof File)
			return new FileDataflowInfo(T2_FLOW_FILE_TYPE,
					(File) canonicalSource, workflowBundle);
		return new DataflowInfo(T2_FLOW_FILE_TYPE, canonicalSource,
				workflowBundle, lastModified);
	}

	protected WorkflowBundle openDataflowStream(InputStream workflowXMLstream)
			throws OpenException {
		WorkflowBundle workflowBundle;
		try {
			workflowBundle = workflowBundleIO.readBundle(workflowXMLstream, null);
		} catch (ReaderException e) {
			throw new OpenException("Could not read the workflow", e);
		} catch (IOException e) {
			throw new OpenException("Could not open the workflow file for parsing", e);
		} catch (Exception e) {
			throw new OpenException("Error while opening workflow", e);
		}

		return workflowBundle;
	}

	@Override
	public List<FileType> getOpenFileTypes() {
		return Arrays.<FileType> asList(new T2FlowFileType());
	}

	@Override
	public List<Class<?>> getOpenSourceTypes() {
		return Arrays.<Class<?>> asList(InputStream.class, URL.class,
				File.class);
	}

	public void setWorkflowBundleIO(WorkflowBundleIO workflowBundleIO) {
		this.workflowBundleIO = workflowBundleIO;
	}
}
