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
package net.sf.taverna.t2.workbench.ui.impl;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;

/**
 * Manages the mapping between Dataflows and DataflowSelectionModels.
 *
 * @author David Withers
 * @author Stian Soiland-Reyes
 */
public class DataflowSelectionManagerImpl implements DataflowSelectionManager {

	private Map<WorkflowBundle, DataflowSelectionModel> dataflowSelectionModelMap = new HashMap<WorkflowBundle, DataflowSelectionModel>();

	private FileManagerObserver fileManagerObserver = new FileManagerObserver();

	public DataflowSelectionManagerImpl(FileManager fileManager) {
		fileManager.addObserver(fileManagerObserver);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager#getDataflowSelectionModel(net.sf.taverna.t2.workflowmodel.Dataflow)
	 */
	@Override
	public DataflowSelectionModel getDataflowSelectionModel(WorkflowBundle dataflow) {
		DataflowSelectionModel selectionModel;
		synchronized (dataflowSelectionModelMap) {
			selectionModel = dataflowSelectionModelMap.get(dataflow);
			if (selectionModel == null) {
				// Create it
				selectionModel = new DataflowSelectionModelImpl();
				dataflowSelectionModelMap.put(dataflow, selectionModel);
			}
		}
		return selectionModel;
	}


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager#removeDataflowSelectionModel(net.sf.taverna.t2.workflowmodel.Dataflow)
	 */
	@Override
	public void removeDataflowSelectionModel(WorkflowBundle dataflow) {
		DataflowSelectionModel selectionModel = dataflowSelectionModelMap
				.get(dataflow);
		if (selectionModel == null) {
			return;
		}
		for (Observer<DataflowSelectionMessage> observer : selectionModel
				.getObservers()) {
			selectionModel.removeObserver(observer);
		}
		dataflowSelectionModelMap.remove(dataflow);
	}


	public class FileManagerObserver implements
			Observer<FileManagerEvent> {
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			if (message instanceof ClosedDataflowEvent) {
				removeDataflowSelectionModel(((ClosedDataflowEvent) message)
						.getDataflow());
			}
		}
	}

}
