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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.taverna.workbench.file.AbstractDataflowPersistenceHandler;
import org.apache.taverna.workbench.file.DataflowInfo;
import org.apache.taverna.workbench.file.DataflowPersistenceHandler;
import org.apache.taverna.workbench.file.FileType;
import org.apache.taverna.workbench.file.exceptions.OpenException;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Workflow;

/**
 * @author alanrw
 */
public class DataflowFromDataflowPersistenceHandler extends
		AbstractDataflowPersistenceHandler implements
		DataflowPersistenceHandler {
	private static final WorkflowBundleFileType WORKFLOW_BUNDLE_FILE_TYPE = new WorkflowBundleFileType();

	@Override
	public DataflowInfo openDataflow(FileType fileType, Object source)
			throws OpenException {
		if (!getOpenFileTypes().contains(fileType))
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);

		WorkflowBundle workflowBundle = (WorkflowBundle) source;
		Date lastModified = null;
		Object canonicalSource = null;
		return new DataflowInfo(WORKFLOW_BUNDLE_FILE_TYPE, canonicalSource,
				workflowBundle, lastModified);
	}

	@Override
	public List<FileType> getOpenFileTypes() {
		return Arrays.<FileType> asList(WORKFLOW_BUNDLE_FILE_TYPE);
	}

	@Override
	public List<Class<?>> getOpenSourceTypes() {
		return Arrays.<Class<?>> asList(Workflow.class);
	}
}
