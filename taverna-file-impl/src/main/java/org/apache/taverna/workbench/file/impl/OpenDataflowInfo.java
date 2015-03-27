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

import java.util.Date;

import org.apache.taverna.workbench.file.DataflowInfo;
import org.apache.taverna.workbench.file.FileType;

/**
 * Information about an open dataflow.
 * 
 * @author Stian Soiland-Reyes
 */
public class OpenDataflowInfo {
	private DataflowInfo dataflowInfo;
	private boolean isChanged;
	private Date openedAt;

	public OpenDataflowInfo() {
	}

	public FileType getFileType() {
		if (dataflowInfo == null)
			return null;
		return dataflowInfo.getFileType();
	}

	public Date getLastModified() {
		if (dataflowInfo == null)
			return null;
		return dataflowInfo.getLastModified();
	}

	public Date getOpenedAtDate() {
		return openedAt;
	}

	public Object getSource() {
		if (dataflowInfo == null)
			return null;
		return dataflowInfo.getCanonicalSource();
	}

	public boolean isChanged() {
		return isChanged;
	}

	public void setIsChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}

	public synchronized void setOpenedFrom(DataflowInfo dataflowInfo) {
		setDataflowInfo(dataflowInfo);
		setOpenedAt(new Date());
		setIsChanged(false);
	}

	public synchronized void setSavedTo(DataflowInfo dataflowInfo) {
		setDataflowInfo(dataflowInfo);
		setIsChanged(false);
	}

	private void setDataflowInfo(DataflowInfo dataflowInfo) {
		this.dataflowInfo = dataflowInfo;
	}

	private void setOpenedAt(Date openedAt) {
		this.openedAt = openedAt;
	}

	public DataflowInfo getDataflowInfo() {
		return dataflowInfo;
	}
}
