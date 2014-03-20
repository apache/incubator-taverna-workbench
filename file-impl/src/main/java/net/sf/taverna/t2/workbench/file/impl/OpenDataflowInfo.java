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

import java.util.Date;

import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.FileType;

/**
 * Information about an open dataflow.
 * <p>
 * 
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class OpenDataflowInfo {

	private DataflowInfo dataflowInfo;
	private boolean isChanged;
	private Date openedAt;

	public OpenDataflowInfo() {
	}

	public FileType getFileType() {
		if (dataflowInfo == null) {
			return null;
		}
		return dataflowInfo.getFileType();
	}

	public Date getLastModified() {
		if (dataflowInfo == null) {
			return null;
		}
		return dataflowInfo.getLastModified();
	}

	public Date getOpenedAtDate() {
		return openedAt;
	}

	public Object getSource() {
		if (dataflowInfo == null) {
			return null;
		}
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
