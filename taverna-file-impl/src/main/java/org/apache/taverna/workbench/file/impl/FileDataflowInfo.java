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

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.taverna.workbench.file.DataflowInfo;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.FileType;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * Information about an open dataflow that was opened from or saved to a
 * {@link File}.
 * 
 * @see DataflowInfo
 * @see FileManager
 * @author Stian Soiland-Reyes
 */
public class FileDataflowInfo extends DataflowInfo {
	private static Logger logger = Logger.getLogger(FileDataflowInfo.class);

	public FileDataflowInfo(FileType fileType, File source,
			WorkflowBundle workflowBundle) {
		super(fileType, canonicalFile(source), workflowBundle,
				lastModifiedFile(source));
	}

	protected static Date lastModifiedFile(File file) {
		long lastModifiedLong = file.lastModified();
		if (lastModifiedLong == 0)
			return null;
		return new Date(lastModifiedLong);
	}

	public static File canonicalFile(File file) {
		try {
			return file.getCanonicalFile();
		} catch (IOException e) {
			logger.warn("Could not find canonical file for " + file);
			return file;
		}
	}
}
