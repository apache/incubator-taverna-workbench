/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
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

import javax.swing.SwingWorker;

import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

public class OpenDataflowSwingWorker extends
		SwingWorker<WorkflowBundle, Object> {
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(OpenDataflowSwingWorker.class);
	private FileType fileType;
	private Object source;
	private FileManagerImpl fileManagerImpl;
	private WorkflowBundle workflowBundle;
	private OpenException e = null;

	public OpenDataflowSwingWorker(FileType fileType, Object source,
			FileManagerImpl fileManagerImpl) {
		this.fileType = fileType;
		this.source = source;
		this.fileManagerImpl = fileManagerImpl;
	}

	@Override
	protected WorkflowBundle doInBackground() throws Exception {
		try {
			workflowBundle = fileManagerImpl.performOpenDataflow(fileType,
					source);
		} catch (OpenException e) {
			this.e = e;
		}
		return workflowBundle;
	}

	public WorkflowBundle getDataflow() {
		return workflowBundle;
	}

	public OpenException getException() {
		return e;
	}
}
