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
package net.sf.taverna.t2.workbench.file;

import java.util.Collections;
import java.util.List;

import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;

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
