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
package org.apache.taverna.ui.perspectives.results;

import javax.swing.AbstractListModel;

import org.apache.taverna.platform.run.api.RunService;

/**
 * @author David Withers
 */
public class WorkflowRunListModel extends AbstractListModel<String> {
	private static final long serialVersionUID = 6899849120823569185L;

	private final RunService runService;

	public WorkflowRunListModel(RunService runService) {
		this.runService = runService;
	}

	@Override
	public int getSize() {
		return runService.getRuns().size();
	}

	@Override
	public String getElementAt(int index) {
		return runService.getRuns().get(index);
	}

	/**
	 * @param runID
	 */
	public void runAdded(String runID) {
		int index = runService.getRuns().indexOf(runID);
		if (index >= 0)
			fireIntervalAdded(this, index, index);
	}

	/**
	 * @param runID
	 */
	public void runRemoved(String runID) {
		int index = runService.getRuns().indexOf(runID);
		if (index >= 0)
			fireIntervalRemoved(this, index, index);
	}
}
