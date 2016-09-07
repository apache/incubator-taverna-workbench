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

import static java.awt.GraphicsEnvironment.isHeadless;
import static java.util.Collections.singleton;
import static javax.swing.SwingUtilities.invokeAndWait;
import static javax.swing.SwingUtilities.isEventDispatchThread;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import org.apache.taverna.lang.observer.MultiCaster;
import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.edits.EditManager.AbstractDataflowEditEvent;
import org.apache.taverna.workbench.edits.EditManager.EditManagerEvent;
import org.apache.taverna.workbench.file.DataflowInfo;
import org.apache.taverna.workbench.file.DataflowPersistenceHandler;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.FileType;
import org.apache.taverna.workbench.file.events.ClosedDataflowEvent;
import org.apache.taverna.workbench.file.events.ClosingDataflowEvent;
import org.apache.taverna.workbench.file.events.FileManagerEvent;
import org.apache.taverna.workbench.file.events.OpenedDataflowEvent;
import org.apache.taverna.workbench.file.events.SavedDataflowEvent;
import org.apache.taverna.workbench.file.events.SetCurrentDataflowEvent;
import org.apache.taverna.workbench.file.exceptions.OpenException;
import org.apache.taverna.workbench.file.exceptions.OverwriteException;
import org.apache.taverna.workbench.file.exceptions.SaveException;
import org.apache.taverna.workbench.file.exceptions.UnsavedException;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.profiles.Profile;

/**
 * Implementation of {@link FileManager}
 * 
 * @author Stian Soiland-Reyes
 */
public class FileManagerImpl implements FileManager {
	private static Logger logger = Logger.getLogger(FileManagerImpl.class);
	private static int nameIndex = 1;

	/**
	 * The last blank workflowBundle created using #newDataflow() until it has
	 * been changed - when this variable will be set to null again. Used to
	 * automatically close unmodified blank workflowBundles on open.
	 */
	private WorkflowBundle blankWorkflowBundle = null;
	@SuppressWarnings("unused")
	private EditManager editManager;
	private EditManagerObserver editManagerObserver = new EditManagerObserver();
	protected MultiCaster<FileManagerEvent> observers = new MultiCaster<>(this);
	/**
	 * Ordered list of open WorkflowBundle
	 */
	private LinkedHashMap<WorkflowBundle, OpenDataflowInfo> openDataflowInfos = new LinkedHashMap<>();
	private DataflowPersistenceHandlerRegistry dataflowPersistenceHandlerRegistry;
	private Scufl2Tools scufl2Tools = new Scufl2Tools();
	private WorkflowBundle currentWorkflowBundle;

	public DataflowPersistenceHandlerRegistry getPersistanceHandlerRegistry() {
		return dataflowPersistenceHandlerRegistry;
	}

	public FileManagerImpl(EditManager editManager) {
		this.editManager = editManager;
		editManager.addObserver(editManagerObserver);
	}

	/**
	 * Add an observer to be notified of {@link FileManagerEvent}s, such as
	 * {@link OpenedDataflowEvent} and {@link SavedDataflowEvent}.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void addObserver(Observer<FileManagerEvent> observer) {
		observers.addObserver(observer);
	}

	@Override
	public boolean canSaveWithoutDestination(WorkflowBundle workflowBundle) {
		OpenDataflowInfo dataflowInfo = getOpenDataflowInfo(workflowBundle);
		if (dataflowInfo.getSource() == null)
			return false;
		Set<?> handlers = getPersistanceHandlerRegistry()
				.getSaveHandlersForType(
						dataflowInfo.getFileType(),
						dataflowInfo.getDataflowInfo().getCanonicalSource()
								.getClass());
		return !handlers.isEmpty();
	}

	@Override
	public boolean closeDataflow(WorkflowBundle workflowBundle,
			boolean failOnUnsaved) throws UnsavedException {
		if (workflowBundle == null)
			throw new NullPointerException("Dataflow can't be null");
		ClosingDataflowEvent message = new ClosingDataflowEvent(workflowBundle);
		observers.notify(message);
		if (message.isAbortClose())
			return false;
		if ((failOnUnsaved && getOpenDataflowInfo(workflowBundle).isChanged()))
			throw new UnsavedException(workflowBundle);
		if (workflowBundle.equals(getCurrentDataflow())) {
			// We'll need to change current workflowBundle
			// Find best candidate to the left or right
			List<WorkflowBundle> workflowBundles = getOpenDataflows();
			int openIndex = workflowBundles.indexOf(workflowBundle);
			if (openIndex == -1)
				throw new IllegalArgumentException("Workflow was not opened "
						+ workflowBundle);

			if (openIndex > 0)
				setCurrentDataflow(workflowBundles.get(openIndex - 1));
			else if (openIndex == 0 && workflowBundles.size() > 1)
				setCurrentDataflow(workflowBundles.get(1));
			else
				// If it was the last one, start a new, empty workflowBundle
				newDataflow();
		}
		if (workflowBundle == blankWorkflowBundle)
			blankWorkflowBundle = null;
		openDataflowInfos.remove(workflowBundle);
		observers.notify(new ClosedDataflowEvent(workflowBundle));
		return true;
	}

	@Override
	public WorkflowBundle getCurrentDataflow() {
		return currentWorkflowBundle;
	}

	@Override
	public WorkflowBundle getDataflowBySource(Object source) {
		for (Entry<WorkflowBundle, OpenDataflowInfo> infoEntry : openDataflowInfos
				.entrySet()) {
			OpenDataflowInfo info = infoEntry.getValue();
			if (source.equals(info.getSource()))
				return infoEntry.getKey();
		}
		// Not found
		return null;
	}

	@Override
	public String getDataflowName(WorkflowBundle workflowBundle) {
		Object source = null;
		if (isDataflowOpen(workflowBundle))
			source = getDataflowSource(workflowBundle);
		// Fallback
		String name;
		Workflow workflow = workflowBundle.getMainWorkflow();
		if (workflow != null)
			name = workflow.getName();
		else
			name = workflowBundle.getName();
		if (source == null)
			return name;
		if (source instanceof File)
			return ((File) source).getAbsolutePath();
		else if (source instanceof URL)
			return source.toString();

		// Check if it has implemented a toString() method
		Method toStringMethod = null;
		Method toStringMethodFromObject = null;
		try {
			toStringMethod = source.getClass().getMethod("toString");
			toStringMethodFromObject = Object.class.getMethod("toString");
		} catch (Exception e) {
			throw new IllegalStateException(
					"Source did not implement Object.toString() " + source);
		}
		if (!toStringMethod.equals(toStringMethodFromObject))
			return source.toString();
		return name;
	}

	@Override
	public String getDefaultWorkflowName() {
		return "Workflow" + (nameIndex++);
	}

	@Override
	public Object getDataflowSource(WorkflowBundle workflowBundle) {
		return getOpenDataflowInfo(workflowBundle).getSource();
	}

	@Override
	public FileType getDataflowType(WorkflowBundle workflowBundle) {
		return getOpenDataflowInfo(workflowBundle).getFileType();
	}

	@Override
	public List<Observer<FileManagerEvent>> getObservers() {
		return observers.getObservers();
	}

	/**
	 * Get the {@link OpenDataflowInfo} for the given WorkflowBundle
	 * 
	 * @throws NullPointerException
	 *             if the WorkflowBundle was <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the WorkflowBundle was not open.
	 * @param workflowBundle
	 *            WorkflowBundle which information is to be found
	 * @return The {@link OpenDataflowInfo} describing the WorkflowBundle
	 */
	protected synchronized OpenDataflowInfo getOpenDataflowInfo(
			WorkflowBundle workflowBundle) {
		if (workflowBundle == null)
			throw new NullPointerException("Dataflow can't be null");
		OpenDataflowInfo info = openDataflowInfos.get(workflowBundle);
		if (info == null)
			throw new IllegalArgumentException("Workflow was not opened "
					+ workflowBundle);
		return info;
	}

	@Override
	public List<WorkflowBundle> getOpenDataflows() {
		return new ArrayList<>(openDataflowInfos.keySet());
	}

	@Override
	public List<FileFilter> getOpenFileFilters() {
		List<FileFilter> fileFilters = new ArrayList<>();

		Set<FileType> fileTypes = getPersistanceHandlerRegistry()
				.getOpenFileTypes();
		if (!fileTypes.isEmpty())
			fileFilters.add(new MultipleFileTypes(fileTypes,
					"All supported workflows"));
		for (FileType fileType : fileTypes)
			fileFilters.add(new FileTypeFileFilter(fileType));
		return fileFilters;
	}

	@Override
	public List<FileFilter> getOpenFileFilters(Class<?> sourceClass) {
		List<FileFilter> fileFilters = new ArrayList<>();
		for (FileType fileType : getPersistanceHandlerRegistry()
				.getOpenFileTypesFor(sourceClass))
			fileFilters.add(new FileTypeFileFilter(fileType));
		return fileFilters;
	}

	@Override
	public List<FileFilter> getSaveFileFilters() {
		List<FileFilter> fileFilters = new ArrayList<>();
		for (FileType fileType : getPersistanceHandlerRegistry()
				.getSaveFileTypes())
			fileFilters.add(new FileTypeFileFilter(fileType));
		return fileFilters;
	}

	@Override
	public List<FileFilter> getSaveFileFilters(Class<?> destinationClass) {
		List<FileFilter> fileFilters = new ArrayList<>();
		for (FileType fileType : getPersistanceHandlerRegistry()
				.getSaveFileTypesFor(destinationClass))
			fileFilters.add(new FileTypeFileFilter(fileType));
		return fileFilters;
	}

	@Override
	public boolean isDataflowChanged(WorkflowBundle workflowBundle) {
		return getOpenDataflowInfo(workflowBundle).isChanged();
	}

	@Override
	public boolean isDataflowOpen(WorkflowBundle workflowBundle) {
		return openDataflowInfos.containsKey(workflowBundle);
	}

	@Override
	public WorkflowBundle newDataflow() {
		WorkflowBundle workflowBundle = new WorkflowBundle();
		workflowBundle.setMainWorkflow(new Workflow());
		workflowBundle.getMainWorkflow().setName(getDefaultWorkflowName());
		workflowBundle.setMainProfile(new Profile());
		scufl2Tools.setParents(workflowBundle);
		blankWorkflowBundle = null;
		openDataflowInternal(workflowBundle);
		blankWorkflowBundle = workflowBundle;
		observers.notify(new OpenedDataflowEvent(workflowBundle));
		return workflowBundle;
	}

	@Override
	public void openDataflow(WorkflowBundle workflowBundle) {
		openDataflowInternal(workflowBundle);
		observers.notify(new OpenedDataflowEvent(workflowBundle));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorkflowBundle openDataflow(FileType fileType, Object source)
			throws OpenException {
		if (isHeadless())
			return performOpenDataflow(fileType, source);

		OpenDataflowRunnable r = new OpenDataflowRunnable(this, fileType,
				source);
		if (isEventDispatchThread()) {
			r.run();
		} else
			try {
				invokeAndWait(r);
			} catch (InterruptedException | InvocationTargetException e) {
				throw new OpenException("Opening was interrupted", e);
			}
		OpenException thrownException = r.getException();
		if (thrownException != null)
			throw thrownException;
		return r.getDataflow();
	}

	public WorkflowBundle performOpenDataflow(FileType fileType, Object source)
			throws OpenException {
		DataflowInfo dataflowInfo;
		WorkflowBundle workflowBundle;
		dataflowInfo = openDataflowSilently(fileType, source);
		workflowBundle = dataflowInfo.getDataflow();
		openDataflowInternal(workflowBundle);
		getOpenDataflowInfo(workflowBundle).setOpenedFrom(dataflowInfo);
		observers.notify(new OpenedDataflowEvent(workflowBundle));
		return workflowBundle;
	}

	@Override
	public DataflowInfo openDataflowSilently(FileType fileType, Object source)
			throws OpenException {
		Set<DataflowPersistenceHandler> handlers;
		Class<? extends Object> sourceClass = source.getClass();

		boolean unknownFileType = (fileType == null);
		if (unknownFileType)
			handlers = getPersistanceHandlerRegistry().getOpenHandlersFor(
					sourceClass);
		else
			handlers = getPersistanceHandlerRegistry().getOpenHandlersFor(
					fileType, sourceClass);
		if (handlers.isEmpty())
			throw new OpenException("Unsupported file type or class "
					+ fileType + " " + sourceClass);

		Throwable lastException = null;
		for (DataflowPersistenceHandler handler : handlers) {
			Collection<FileType> fileTypes;
			if (unknownFileType)
				fileTypes = handler.getOpenFileTypes();
			else
				fileTypes = singleton(fileType);
			for (FileType candidateFileType : fileTypes) {
				if (unknownFileType && (source instanceof File))
					/*
					 * If source is file but fileType was not explicitly set
					 * from the open workflow dialog - check the file extension
					 * and decide which handler to use based on that (so that we
					 * do not loop though all handlers)
					 */
					if (!((File) source).getPath().endsWith(
							candidateFileType.getExtension()))
						continue;

				try {
					DataflowInfo openDataflow = handler.openDataflow(
							candidateFileType, source);
					WorkflowBundle workflowBundle = openDataflow.getDataflow();
					logger.info("Loaded workflow: " + workflowBundle.getName()
							+ " " + workflowBundle.getGlobalBaseURI()
							+ " from " + source + " using " + handler);
					return openDataflow;
				} catch (OpenException ex) {
					logger.warn("Could not open workflow " + source + " using "
							+ handler + " of type " + candidateFileType);
					lastException = ex;
				}
			}
		}
		throw new OpenException("Could not open workflow " + source + "\n",
				lastException);
	}

	/**
	 * Mark the WorkflowBundle as opened, and close the blank WorkflowBundle if
	 * needed.
	 * 
	 * @param workflowBundle
	 *            WorkflowBundle that has been opened
	 */
	protected void openDataflowInternal(WorkflowBundle workflowBundle) {
		if (workflowBundle == null)
			throw new NullPointerException("Dataflow can't be null");
		if (isDataflowOpen(workflowBundle))
			throw new IllegalArgumentException("Workflow is already open: "
					+ workflowBundle);

		openDataflowInfos.put(workflowBundle, new OpenDataflowInfo());
		setCurrentDataflow(workflowBundle);
		if (openDataflowInfos.size() == 2 && blankWorkflowBundle != null)
			/*
			 * Behave like a word processor and close the blank WorkflowBundle
			 * when another workflow has been opened
			 */
			try {
				closeDataflow(blankWorkflowBundle, true);
			} catch (UnsavedException e) {
				logger.error("Blank workflow was modified "
						+ "and could not be closed");
			}
	}

	@Override
	public void removeObserver(Observer<FileManagerEvent> observer) {
		observers.removeObserver(observer);
	}

	@Override
	public void saveDataflow(WorkflowBundle workflowBundle,
			boolean failOnOverwrite) throws SaveException {
		if (workflowBundle == null)
			throw new NullPointerException("Dataflow can't be null");
		OpenDataflowInfo lastSave = getOpenDataflowInfo(workflowBundle);
		if (lastSave.getSource() == null)
			throw new SaveException("Can't save without source "
					+ workflowBundle);
		saveDataflow(workflowBundle, lastSave.getFileType(),
				lastSave.getSource(), failOnOverwrite);
	}

	@Override
	public void saveDataflow(WorkflowBundle workflowBundle, FileType fileType,
			Object destination, boolean failOnOverwrite) throws SaveException {
		DataflowInfo savedDataflow = saveDataflowSilently(workflowBundle,
				fileType, destination, failOnOverwrite);
		getOpenDataflowInfo(workflowBundle).setSavedTo(savedDataflow);
		observers.notify(new SavedDataflowEvent(workflowBundle));
	}

	@Override
	public DataflowInfo saveDataflowSilently(WorkflowBundle workflowBundle,
			FileType fileType, Object destination, boolean failOnOverwrite)
			throws SaveException, OverwriteException {
		Set<DataflowPersistenceHandler> handlers;

		Class<? extends Object> destinationClass = destination.getClass();
		if (fileType != null)
			handlers = getPersistanceHandlerRegistry().getSaveHandlersForType(
					fileType, destinationClass);
		else
			handlers = getPersistanceHandlerRegistry().getSaveHandlersFor(
					destinationClass);

		SaveException lastException = null;
		for (DataflowPersistenceHandler handler : handlers) {
			if (failOnOverwrite) {
				OpenDataflowInfo openDataflowInfo = getOpenDataflowInfo(workflowBundle);
				if (handler.wouldOverwriteDataflow(workflowBundle, fileType,
						destination, openDataflowInfo.getDataflowInfo()))
					throw new OverwriteException(destination);
			}
			try {
				DataflowInfo savedDataflow = handler.saveDataflow(
						workflowBundle, fileType, destination);
				savedDataflow.getDataflow();
				logger.info("Saved workflow: " + workflowBundle.getName() + " "
						+ workflowBundle.getGlobalBaseURI() + " to "
						+ savedDataflow.getCanonicalSource() + " using "
						+ handler);
				return savedDataflow;
			} catch (SaveException ex) {
				logger.warn("Could not save to " + destination + " using "
						+ handler);
				lastException = ex;
			}
		}

		if (lastException == null)
			throw new SaveException("Unsupported file type or class "
					+ fileType + " " + destinationClass);
		throw new SaveException("Could not save to " + destination + ":\n"
				+ lastException.getLocalizedMessage(), lastException);
	}

	@Override
	public void setCurrentDataflow(WorkflowBundle workflowBundle) {
		setCurrentDataflow(workflowBundle, false);
	}

	@Override
	public void setCurrentDataflow(WorkflowBundle workflowBundle,
			boolean openIfNeeded) {
		currentWorkflowBundle = workflowBundle;
		if (!isDataflowOpen(workflowBundle)) {
			if (!openIfNeeded)
				throw new IllegalArgumentException("Workflow is not open: "
						+ workflowBundle);
			openDataflow(workflowBundle);
			return;
		}
		observers.notify(new SetCurrentDataflowEvent(workflowBundle));
	}

	@Override
	public void setDataflowChanged(WorkflowBundle workflowBundle,
			boolean isChanged) {
		getOpenDataflowInfo(workflowBundle).setIsChanged(isChanged);
		if (blankWorkflowBundle == workflowBundle)
			blankWorkflowBundle = null;
	}

	@Override
	public Object getCanonical(Object source) throws IllegalArgumentException,
			URISyntaxException, IOException {
		Object canonicalSource = source;

		if (source instanceof URL) {
			URL url = ((URL) source);
			if (url.getProtocol().equalsIgnoreCase("file"))
				canonicalSource = new File(url.toURI());
		}

		if (canonicalSource instanceof File)
			canonicalSource = ((File) canonicalSource).getCanonicalFile();
		return canonicalSource;
	}

	public void setDataflowPersistenceHandlerRegistry(
			DataflowPersistenceHandlerRegistry dataflowPersistenceHandlerRegistry) {
		this.dataflowPersistenceHandlerRegistry = dataflowPersistenceHandlerRegistry;
	}

	/**
	 * Observe the {@link EditManager} for changes to open workflowBundles. A
	 * change of an open workflow would set it as changed using
	 * {@link FileManagerImpl#setDataflowChanged(Dataflow, boolean)}.
	 * 
	 * @author Stian Soiland-Reyes
	 * 
	 */
	private final class EditManagerObserver implements
			Observer<EditManagerEvent> {
		@Override
		public void notify(Observable<EditManagerEvent> sender,
				EditManagerEvent message) throws Exception {
			if (message instanceof AbstractDataflowEditEvent) {
				AbstractDataflowEditEvent dataflowEdit = (AbstractDataflowEditEvent) message;
				WorkflowBundle workflowBundle = dataflowEdit.getDataFlow();
				/**
				 * TODO: on undo/redo - keep last event or similar to determine
				 * if workflow was saved before. See
				 * FileManagerTest#isChangedWithUndo().
				 */
				setDataflowChanged(workflowBundle, true);
			}
		}
	}
}
