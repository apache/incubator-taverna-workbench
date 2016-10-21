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
package org.apache.taverna.workbench.selection.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.taverna.lang.observer.MultiCaster;
import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.workbench.edits.CompoundEdit;
import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.edits.EditManager.DataFlowUndoEvent;
import org.apache.taverna.workbench.edits.EditManager.EditManagerEvent;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.events.ClosedDataflowEvent;
import org.apache.taverna.workbench.file.events.FileManagerEvent;
import org.apache.taverna.workbench.file.events.OpenedDataflowEvent;
import org.apache.taverna.workbench.file.events.SetCurrentDataflowEvent;
import org.apache.taverna.workbench.selection.DataflowSelectionModel;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.selection.events.DataflowSelectionMessage;
import org.apache.taverna.workbench.selection.events.PerspectiveSelectionEvent;
import org.apache.taverna.workbench.selection.events.ProfileSelectionEvent;
import org.apache.taverna.workbench.selection.events.SelectionManagerEvent;
import org.apache.taverna.workbench.selection.events.WorkflowBundleSelectionEvent;
import org.apache.taverna.workbench.selection.events.WorkflowRunSelectionEvent;
import org.apache.taverna.workbench.selection.events.WorkflowSelectionEvent;
import org.apache.taverna.workbench.ui.zaria.PerspectiveSPI;
import org.apache.taverna.workflow.edits.AddChildEdit;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.InputWorkflowPort;
import org.apache.taverna.scufl2.api.port.OutputWorkflowPort;
import org.apache.taverna.scufl2.api.profiles.Profile;

/**
 * Implementation of the {@link SelectionManager}.
 *
 * @author David Withers
 */
public class SelectionManagerImpl implements SelectionManager {
	private static final String RESULTS_PERSPECTIVE_ID = "net.sf.taverna.t2.ui.perspectives.results.ResultsPerspective";

	private static final String DESIGN_PERSPECTIVE_ID = "net.sf.taverna.t2.ui.perspectives.design.DesignPerspective";

	private static final Logger logger = Logger.getLogger(SelectionManagerImpl.class);

	private WorkflowBundle selectedWorkflowBundle;
	private Map<WorkflowBundle, DataflowSelectionModel> workflowSelectionModels = new IdentityHashMap<>();
	private Map<WorkflowBundle, Workflow> selectedWorkflows = new IdentityHashMap<>();
	private Map<WorkflowBundle, Profile> selectedProfiles = new IdentityHashMap<>();
	private String selectedWorkflowRun;
	private Map<String, DataflowSelectionModel> workflowRunSelectionModels = new HashMap<>();
	private PerspectiveSPI selectedPerspective;
	private MultiCaster<SelectionManagerEvent> observers = new MultiCaster<>(this);
	private FileManager fileManager;
	private List<PerspectiveSPI> perspectives;

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
	public WorkflowBundle getSelectedWorkflowBundle() {
		return selectedWorkflowBundle;
	}

	@Override
	public void setSelectedWorkflowBundle(WorkflowBundle workflowBundle) {
		setSelectedWorkflowBundle(workflowBundle, true);
	}

	private void setSelectedWorkflowBundle(WorkflowBundle workflowBundle, boolean notifyFileManager) {
		if (workflowBundle == null || workflowBundle == selectedWorkflowBundle)
			return;
		if (notifyFileManager) {
			fileManager.setCurrentDataflow(workflowBundle);
			return;
		}
		if (selectedWorkflows.get(workflowBundle) == null)
			selectedWorkflows.put(workflowBundle,
					workflowBundle.getMainWorkflow());
		if (selectedProfiles.get(workflowBundle) == null)
			selectedProfiles.put(workflowBundle,
					workflowBundle.getMainProfile());
		SelectionManagerEvent selectionManagerEvent = new WorkflowBundleSelectionEvent(
				selectedWorkflowBundle, workflowBundle);
		selectedWorkflowBundle = workflowBundle;
		notify(selectionManagerEvent);
		selectDesignPerspective();
	}

	private void removeWorkflowBundle(WorkflowBundle dataflow) {
		synchronized (workflowSelectionModels) {
			DataflowSelectionModel selectionModel = workflowSelectionModels.remove(dataflow);
			if (selectionModel != null)
				for (Observer<DataflowSelectionMessage> observer : selectionModel.getObservers())
					selectionModel.removeObserver(observer);
		}
		synchronized (selectedWorkflows) {
			selectedWorkflows.remove(dataflow);
		}
		synchronized (selectedProfiles) {
			selectedProfiles.remove(dataflow);
		}
	}

	@Override
	public Workflow getSelectedWorkflow() {
		return selectedWorkflows.get(selectedWorkflowBundle);
	}

	@Override
	public void setSelectedWorkflow(Workflow workflow) {
		if (workflow != null) {
			Workflow selectedWorkflow = selectedWorkflows.get(workflow
					.getParent());
			if (selectedWorkflow != workflow) {
				SelectionManagerEvent selectionManagerEvent = new WorkflowSelectionEvent(
						selectedWorkflow, workflow);
				selectedWorkflows.put(workflow.getParent(), workflow);
				notify(selectionManagerEvent);
			}
		}
	}

	@Override
	public Profile getSelectedProfile() {
		return selectedProfiles.get(selectedWorkflowBundle);
	}

	@Override
	public void setSelectedProfile(Profile profile) {
		if (profile != null) {
			Profile selectedProfile = selectedProfiles.get(profile.getParent());
			if (selectedProfile != profile) {
				SelectionManagerEvent selectionManagerEvent = new ProfileSelectionEvent(
						selectedProfile, profile);
				selectedProfiles.put(profile.getParent(), profile);
				notify(selectionManagerEvent);
			}
		}
	}

	@Override
	public String getSelectedWorkflowRun() {
		return selectedWorkflowRun;
	}

	@Override
	public void setSelectedWorkflowRun(String workflowRun) {
		if ((selectedWorkflowRun == null && workflowRun != null)
				|| !selectedWorkflowRun.equals(workflowRun)) {
			SelectionManagerEvent selectionManagerEvent = new WorkflowRunSelectionEvent(
					selectedWorkflowRun, workflowRun);
			selectedWorkflowRun = workflowRun;
			notify(selectionManagerEvent);
			selectResultsPerspective();
		}
	}

	@Override
	public DataflowSelectionModel getWorkflowRunSelectionModel(String workflowRun) {
		DataflowSelectionModel selectionModel;
		synchronized (workflowRunSelectionModels) {
			selectionModel = workflowRunSelectionModels.get(workflowRun);
			if (selectionModel == null) {
				selectionModel = new DataflowSelectionModelImpl();
				workflowRunSelectionModels.put(workflowRun, selectionModel);
			}
		}
		return selectionModel;
	}

	@SuppressWarnings("unused")
	private void removeWorkflowRun(String workflowRun) {
		synchronized (workflowRunSelectionModels) {
			DataflowSelectionModel selectionModel = workflowRunSelectionModels
					.remove(workflowRun);
			if (selectionModel != null)
				for (Observer<DataflowSelectionMessage> observer : selectionModel
						.getObservers())
					selectionModel.removeObserver(observer);
		}
	}

	@Override
	public PerspectiveSPI getSelectedPerspective() {
		return selectedPerspective;
	}

	@Override
	public void setSelectedPerspective(PerspectiveSPI perspective) {
		if (selectedPerspective != perspective) {
			SelectionManagerEvent selectionManagerEvent = new PerspectiveSelectionEvent(
					selectedPerspective, perspective);
			selectedPerspective = perspective;
			notify(selectionManagerEvent);
		}
	}

	private void selectDesignPerspective() {
		for (PerspectiveSPI perspective : perspectives)
			if (DESIGN_PERSPECTIVE_ID.equals(perspective.getID())) {
				setSelectedPerspective(perspective);
				break;
			}
	}

	private void selectResultsPerspective() {
		for (PerspectiveSPI perspective : perspectives)
			if (RESULTS_PERSPECTIVE_ID.equals(perspective.getID())) {
				setSelectedPerspective(perspective);
				break;
			}
	}

	@Override
	public void addObserver(Observer<SelectionManagerEvent> observer) {
		synchronized (observers) {
			WorkflowBundle selectedWorkflowBundle = getSelectedWorkflowBundle();
			Workflow selectedWorkflow = getSelectedWorkflow();
			Profile selectedProfile = getSelectedProfile();
			String selectedWorkflowRun = getSelectedWorkflowRun();
			PerspectiveSPI selectedPerspective = getSelectedPerspective();
			try {
				if (selectedWorkflowBundle != null)
					observer.notify(this, new WorkflowBundleSelectionEvent(
							null, selectedWorkflowBundle));
				if (selectedWorkflow != null)
					observer.notify(this, new WorkflowSelectionEvent(null,
							selectedWorkflow));
				if (selectedProfile != null)
					observer.notify(this, new ProfileSelectionEvent(null,
							selectedProfile));
				if (selectedWorkflowRun != null)
					observer.notify(this, new WorkflowRunSelectionEvent(null,
							selectedWorkflowRun));
				if (selectedPerspective != null)
					observer.notify(this, new PerspectiveSelectionEvent(null,
							selectedPerspective));
			} catch (Exception e) {
				logger.warn("Could not notify " + observer, e);
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

	public void setPerspectives(List<PerspectiveSPI> perspectives) {
		this.perspectives = perspectives;
	}

	public class FileManagerObserver implements Observer<FileManagerEvent> {
		@Override
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			if (message instanceof ClosedDataflowEvent) {
				WorkflowBundle workflowBundle = ((ClosedDataflowEvent) message).getDataflow();
				removeWorkflowBundle(workflowBundle);
			} else if (message instanceof OpenedDataflowEvent) {
				WorkflowBundle workflowBundle = ((OpenedDataflowEvent) message).getDataflow();
				setSelectedWorkflowBundle(workflowBundle, false);
			} else if (message instanceof SetCurrentDataflowEvent) {
				WorkflowBundle workflowBundle = ((SetCurrentDataflowEvent) message).getDataflow();
				setSelectedWorkflowBundle(workflowBundle, false);
			}
		}
	}

	private class EditManagerObserver implements Observer<EditManagerEvent> {
		@Override
		public void notify(Observable<EditManagerEvent> sender, EditManagerEvent message)
				throws Exception {
			Edit<?> edit = message.getEdit();
			considerEdit(edit, message instanceof DataFlowUndoEvent);
		}

		private void considerEdit(Edit<?> edit, boolean undoing) {
			if (edit instanceof CompoundEdit) {
				CompoundEdit compound = (CompoundEdit) edit;
				for (Edit<?> e : compound.getChildEdits())
					considerEdit(e, undoing);
			} else if (edit instanceof AddChildEdit
					&& edit.getSubject() instanceof Workflow) {
				Workflow subject = (Workflow) edit.getSubject();
				DataflowSelectionModel selectionModel = getDataflowSelectionModel(subject
						.getParent());
				Object child = ((AddChildEdit<?>) edit).getChild();
				if (child instanceof Processor
						|| child instanceof InputWorkflowPort
						|| child instanceof OutputWorkflowPort) {
					if (undoing
							&& selectionModel.getSelection().contains(child))
						selectionModel.clearSelection();
					else {
						Set<Object> selection = new HashSet<>();
						selection.add(child);
						selectionModel.setSelection(selection);
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
