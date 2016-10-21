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
package org.apache.taverna.workbench.file;

import java.util.Collections;
import java.util.List;

import org.apache.taverna.workbench.file.exceptions.OpenException;
import org.apache.taverna.workbench.file.exceptions.SaveException;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;

public abstract class AbstractDataflowPersistenceHandler implements
		DataflowPersistenceHandler {
	@Override
	public List<FileType> getOpenFileTypes() {
		return Collections.emptyList();
	}

	@Override
	public List<FileType> getSaveFileTypes() {
		return Collections.emptyList();
	}

	@Override
	public List<Class<?>> getOpenSourceTypes() {
		return Collections.emptyList();
	}

	@Override
	public List<Class<?>> getSaveDestinationTypes() {
		return Collections.emptyList();
	}

	@Override
	public DataflowInfo openDataflow(FileType fileType, Object source)
			throws OpenException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DataflowInfo saveDataflow(WorkflowBundle workflowBundle, FileType fileType,
			Object destination) throws SaveException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean wouldOverwriteDataflow(WorkflowBundle workflowBundle, FileType fileType,
			Object destination, DataflowInfo lastDataflowInfo) {
		throw new UnsupportedOperationException();
	}
}
