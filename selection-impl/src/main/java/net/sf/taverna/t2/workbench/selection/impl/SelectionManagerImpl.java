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
package net.sf.taverna.t2.workbench.selection.impl;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.edits.CompoundEdit;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.DataFlowUndoEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.OpenedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.SetCurrentDataflowEvent;
import net.sf.taverna.t2.workbench.selection.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.events.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.selection.events.PerspectiveSelectionEvent;
import net.sf.taverna.t2.workbench.selection.events.ProfileSelectionEvent;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowBundleSelectionEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowSelectionEvent;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import net.sf.taverna.t2.workflow.edits.AddChildEdit;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;
import uk.org.taverna.scufl2.api.profiles.Profile;

/**
 * Implementation of the {@link SelectionManager}.
 *
 * @author David Withers
 */
public class SelectionManagerImpl implements SelectionManager {

	private static final Logger logger = Logger.getLogger(SelectionManagerImpl.class);

	private Map<WorkflowBundle, DataflowSelectionModel> workflowSelectionModels = new IdentityHashMap<WorkflowBundle, DataflowSelectionModel>();
	private Map<WorkflowBundle, Workflow> selectedWorkflows = new IdentityHashMap<WorkflowBundle, Workflow>();
	private Map<WorkflowBundle, Profile> selectedProfiles = new IdentityHashMap<WorkflowBundle, Profile>();
	private WorkflowBundle selectedWorkflowBundle;
	private PerspectiveSPI selectedPerspective;

	private MultiCaster<SelectionManagerEvent> observers = new MultiCaster<SelectionManagerEvent>(this);

	private FileManager fileManager;

	@Override
	public DataflowSelectionModel getDataflowSelectionModel(WorkflowBundle dataflow) {
		DataflowSelectionModel selectionModel;
		synchronized (workflowSelectionModels) {
			selectionModel = workflowSelectionModels.get(dataflow);
			if (selectionModel == null) {
				selectionModel = new DataflowSelectionModelImpl();
				workflowSelectionModels.put(dataflow, selectionModel);
			}
		}
		return selectionModel;
	}

	@Override
	public void removeDataflowSelectionModel(WorkflowBundle dataflow) {
		DataflowSelectionModel selectionModel = workflowSelectionModels
				.get(dataflow);
		if (selectionModel == null) {
			return;
		}
		for (Observer<DataflowSelectionMessage> observer : selectionModel
				.getObservers()) {
			selectionModel.removeObserver(observer);
		}
		workflowSelectionModels.remove(dataflow);
	}

	@Override
	public WorkflowBundle getSelectedWorkflowBundle() {
		return selectedWorkflowBundle;
	}

	@Override
	public void setSelectedWorkflowBundle(WorkflowBundle workflowBundle) {
		setSelectedWorkflowBundle(workflowBundle, true);
	}

	private void setSelectedWorkflowBundle(WorkflowBundle workflowBundle, boolean notifyFileManager) {
		if (workflowBundle != null && workflowBundle != selectedWorkflowBundle) {
			Workflow selectedWorkflow = selectedWorkflows.get(workflowBundle);
			if (selectedWorkflow == null) {
				setSelectedWorkflow(workflowBundle.getMainWorkflow(), notifyFileManager);
			} else {
				setSelectedWorkflow(selectedWorkflow, notifyFileManager);
			}
			Profile selectedProfile = selectedProfiles.get(workflowBundle);
			if (selectedProfile == null) {
				setSelectedProfile(workflowBundle.getMainProfile());
			} else {
				setSelectedProfile(selectedProfile);
			}
		}
	}

	@Override
	public Workflow getSelectedWorkflow() {
		return selectedWorkflows.get(selectedWorkflowBundle);
	}

	@Override
	public void setSelectedWorkflow(Workflow workflow) {
		setSelectedWorkflow(workflow, true);
	}

	private void setSelectedWorkflow(Workflow workflow, boolean notifyFileManager) {
		if (workflow != null) {
			if (notifyFileManager) {
				fileManager.setCurrentDataflow(workflow.getParent());
			} else {
				Workflow selectedWorkflow = selectedWorkflows.get(selectedWorkflowBundle);
				if (selectedWorkflowBundle != workflow.getParent()) {
					SelectionManagerEvent workflowManagerEvent = new WorkflowBundleSelectionEvent(selectedWorkflowBundle, workflow.getParent());
					selectedWorkflowBundle = workflow.getParent();
					notify(workflowManagerEvent);
				}
				if (selectedWorkflow != workflow) {
					SelectionManagerEvent workflowManagerEvent = new WorkflowSelectionEvent(selectedWorkflow, workflow);
					selectedWorkflows.put(workflow.getParent(), workflow);
					notify(workflowManagerEvent);
				}
			}
		}
	}

	@Override
	public Profile getSelectedProfile() {
		return selectedProfiles.get(selectedWorkflowBundle);
	}

	@Override
	public void setSelectedProfile(Profile profile) {
		Profile selectedProfile = selectedProfiles.get(profile.getParent());
		if (selectedProfile != profile) {
			SelectionManagerEvent workflowManagerEvent = new ProfileSelectionEvent(selectedProfile, profile);
			selectedProfiles.put(profile.getParent(), profile);
			notify(workflowManagerEvent);
		}
	}

	@Override
	public PerspectiveSPI getSelectedPerspective() {
		return selectedPerspective;
	}

	@Override
	public void setSelectedPerspective(PerspectiveSPI perspective) {
		if (selectedPerspective != perspective) {
			SelectionManagerEvent workflowManagerEvent = new PerspectiveSelectionEvent(selectedPerspective, perspective);
			selectedPerspective = perspective;
			notify(workflowManagerEvent);
		}
	}

	@Override
	public void addObserver(Observer<SelectionManagerEvent> observer) {
		synchronized (observers) {
			if (selectedWorkflowBundle != null) {
				Workflow selectedWorkflow = getSelectedWorkflow();
				try {
					observer.notify(this, new WorkflowBundleSelectionEvent(null, selectedWorkflowBundle));
					if (selectedWorkflow != null) {
						observer.notify(this, new WorkflowSelectionEvent(null, selectedWorkflow));
					}
				} catch (Exception e) {
					logger.warn("Could not notify " + observer, e);
				}
			}
			observers.addObserver(observer);
		}
	}

	@Override
	public void removeObserver(Observer<SelectionManagerEvent> observer) {
		observers.removeObserver(observer);
	}

	@Override
	public List<Observer<SelectionManagerEvent>> getObservers() {
		return observers.getObservers();
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
		setSelectedWorkflowBundle(fileManager.getCurrentDataflow());
		fileManager.addObserver(new FileManagerObserver());
	}

	public void setEditManager(EditManager editManager) {
		editManager.addObserver(new EditManagerObserver());
	}

	public class FileManagerObserver implements Observer<FileManagerEvent> {
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			if (message instanceof ClosedDataflowEvent) {
				WorkflowBundle workflowBundle = ((ClosedDataflowEvent) message).getDataflow();
				removeDataflowSelectionModel(workflowBundle);
			} else if (message instanceof OpenedDataflowEvent) {
				WorkflowBundle workflowBundle = ((OpenedDataflowEvent) message).getDataflow();
				setSelectedWorkflowBundle(workflowBundle);
			} else if (message instanceof SetCurrentDataflowEvent) {
				WorkflowBundle workflowBundle = ((SetCurrentDataflowEvent) message).getDataflow();
				setSelectedWorkflowBundle(workflowBundle, false);
			}
		}
	}

	private class EditManagerObserver implements Observer<EditManagerEvent> {

		public void notify(Observable<EditManagerEvent> sender, EditManagerEvent message)
				throws Exception {
			Edit<?> edit = message.getEdit();
			considerEdit(edit, message instanceof DataFlowUndoEvent);
		}

		private void considerEdit(Edit<?> edit, boolean undoing) {
			if (edit instanceof CompoundEdit) {
				CompoundEdit compound = (CompoundEdit) edit;
				for (Edit<?> e : compound.getChildEdits()) {
					considerEdit(e, undoing);
				}
			} else {
				Object subject = edit.getSubject();
				if (subject instanceof Workflow) {
					DataflowSelectionModel selectionModel = getDataflowSelectionModel(((Workflow) edit.getSubject()).getParent());
					if (edit instanceof AddChildEdit) {
						Object child = ((AddChildEdit<?>) edit).getChild();
						if (child instanceof Processor || child instanceof InputWorkflowPort || child instanceof OutputWorkflowPort) {
							if (undoing && selectionModel.getSelection().contains(child)) {
								selectionModel.clearSelection();
							} else {
								HashSet<Object> selection = new HashSet<Object>();
								selection.add(child);
								selectionModel.setSelection(selection);
							}
						}
					}
				}
			}
		}

	}

	private void notify(SelectionManagerEvent event) {
		synchronized (observers) {
			observers.notify(event);
		}
	}

}
