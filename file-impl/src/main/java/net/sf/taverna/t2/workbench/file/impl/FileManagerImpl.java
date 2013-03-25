/*******************************************************************************
 * Copyright (C) 2007-2010 The University of Manchester
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.DataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.ClosingDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.OpenedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.SavedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.SetCurrentDataflowEvent;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.exceptions.OverwriteException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.file.exceptions.UnsavedException;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.profiles.Profile;

/**
 * Implementation of {@link FileManager}
 *
 * @author Stian Soiland-Reyes
 *
 */
public class FileManagerImpl implements FileManager {
	private static Logger logger = Logger.getLogger(FileManagerImpl.class);

	/**
	 * The last blank workflowBundle created using #newDataflow() until it has been
	 * changed - when this variable will be set to null again. Used to
	 * automatically close unmodified blank workflowBundles on open.
	 */
	private WorkflowBundle blankWorkflowBundle = null;

	private EditManager editManager;

	private EditManagerObserver editManagerObserver = new EditManagerObserver();

	protected MultiCaster<FileManagerEvent> observers = new MultiCaster<FileManagerEvent>(
			this);

	/**
	 * Ordered list of open WorkflowBundle
	 */
	private LinkedHashMap<WorkflowBundle, OpenDataflowInfo> openDataflowInfos = new LinkedHashMap<WorkflowBundle, OpenDataflowInfo>();

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
	public void addObserver(Observer<FileManagerEvent> observer) {
		observers.addObserver(observer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canSaveWithoutDestination(WorkflowBundle workflowBundle) {
		OpenDataflowInfo dataflowInfo = getOpenDataflowInfo(workflowBundle);
		if (dataflowInfo.getSource() == null) {
			return false;
		}
		Set<DataflowPersistenceHandler> handlers = getPersistanceHandlerRegistry()
				.getSaveHandlersForType(dataflowInfo.getFileType(),
						dataflowInfo.getDataflowInfo().getCanonicalSource()
								.getClass());
		return !handlers.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean closeDataflow(WorkflowBundle workflowBundle, boolean failOnUnsaved)
			throws UnsavedException {
		if (workflowBundle == null) {
			throw new NullPointerException("Dataflow can't be null");
		}
		ClosingDataflowEvent message = new ClosingDataflowEvent(workflowBundle);
		observers.notify(message);
		if (message.isAbortClose()) {
			return false;
		}
		if ((failOnUnsaved && getOpenDataflowInfo(workflowBundle).isChanged())) {
			throw new UnsavedException(workflowBundle);
		}
		if (workflowBundle.equals(getCurrentDataflow())) {
			// We'll need to change current workflowBundle
			// Find best candidate to the left or right
			List<WorkflowBundle> workflowBundles = getOpenDataflows();
			int openIndex = workflowBundles.indexOf(workflowBundle);
			if (openIndex == -1) {
				throw new IllegalArgumentException("Workflow was not opened "
						+ workflowBundle);
			} else if (openIndex > 0) {
				setCurrentDataflow(workflowBundles.get(openIndex - 1));
			} else if (openIndex == 0 && workflowBundles.size() > 1) {
				setCurrentDataflow(workflowBundles.get(1));
			} else {
				// If it was the last one, start a new, empty workflowBundle
				newDataflow();
			}
		}
		if (workflowBundle == blankWorkflowBundle) {
			blankWorkflowBundle = null;
		}
		openDataflowInfos.remove(workflowBundle);
		observers.notify(new ClosedDataflowEvent(workflowBundle));
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public WorkflowBundle getCurrentDataflow() {
		return currentWorkflowBundle;
	}

	@Override
	public WorkflowBundle getDataflowBySource(Object source) {
		for (Entry<WorkflowBundle,OpenDataflowInfo> infoEntry : openDataflowInfos.entrySet()) {
			OpenDataflowInfo info = infoEntry.getValue();
			if (source.equals(info.getSource())) {
				return infoEntry.getKey();
			}
		}
		// Not found
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDataflowName(WorkflowBundle workflowBundle) {
		Object source = null;
		if (isDataflowOpen(workflowBundle)) {
			source = getDataflowSource(workflowBundle);
		}
		String name = workflowBundle.getName(); 	// Fallback
		if (source == null) {
			return name;
		}
		if (source instanceof File){
			return ((File)source).getAbsolutePath();
		} else if (source instanceof URL){
			return source.toString();
		} else {
			// Check if it has implemented a toString() method
			Method toStringMethod = null;
			Method toStringMethodFromObject = null;
			try {
				toStringMethod = source.getClass().getMethod("toString");
				toStringMethodFromObject = Object.class.getMethod("toString");
			} catch (Exception e) {
				throw new IllegalStateException("Source did not implement Object.toString() " + source);
			}
			if (! toStringMethod.equals(toStringMethodFromObject)) {
				return source.toString();
			}
		}
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getDataflowSource(WorkflowBundle workflowBundle) {
		return getOpenDataflowInfo(workflowBundle).getSource();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileType getDataflowType(WorkflowBundle workflowBundle) {
		return getOpenDataflowInfo(workflowBundle).getFileType();
	}

	/**
	 * {@inheritDoc}
	 */
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
	protected synchronized OpenDataflowInfo getOpenDataflowInfo(WorkflowBundle workflowBundle) {
		if (workflowBundle == null) {
			throw new NullPointerException("Dataflow can't be null");
		}
		OpenDataflowInfo info = openDataflowInfos.get(workflowBundle);
		if (info != null) {
			return info;
		} else {
			throw new IllegalArgumentException("Workflow was not opened"
					+ workflowBundle);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<WorkflowBundle> getOpenDataflows() {
		return new ArrayList<WorkflowBundle>(openDataflowInfos.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<FileFilter> getOpenFileFilters() {
		List<FileFilter> fileFilters = new ArrayList<FileFilter>();

		Set<FileType> fileTypes = getPersistanceHandlerRegistry().getOpenFileTypes();
		if (!fileTypes.isEmpty()) {
			fileFilters.add(new MultipleFileTypes(fileTypes,
					"All supported workflows"));
		}
		for (FileType fileType : fileTypes) {
			fileFilters.add(new FileTypeFileFilter(fileType));
		}
		return fileFilters;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<FileFilter> getOpenFileFilters(Class<?> sourceClass) {
		List<FileFilter> fileFilters = new ArrayList<FileFilter>();
		for (FileType fileType : getPersistanceHandlerRegistry()
				.getOpenFileTypesFor(sourceClass)) {
			fileFilters.add(new FileTypeFileFilter(fileType));
		}
		return fileFilters;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<FileFilter> getSaveFileFilters() {
		List<FileFilter> fileFilters = new ArrayList<FileFilter>();
		for (FileType fileType : getPersistanceHandlerRegistry().getSaveFileTypes()) {
			fileFilters.add(new FileTypeFileFilter(fileType));
		}
		return fileFilters;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<FileFilter> getSaveFileFilters(Class<?> destinationClass) {
		List<FileFilter> fileFilters = new ArrayList<FileFilter>();
		for (FileType fileType : getPersistanceHandlerRegistry()
				.getSaveFileTypesFor(destinationClass)) {
			fileFilters.add(new FileTypeFileFilter(fileType));
		}
		return fileFilters;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDataflowChanged(WorkflowBundle workflowBundle) {
		return getOpenDataflowInfo(workflowBundle).isChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDataflowOpen(WorkflowBundle workflowBundle) {
		return openDataflowInfos.containsKey(workflowBundle);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorkflowBundle newDataflow() {
		WorkflowBundle workflowBundle = new WorkflowBundle();
		workflowBundle.setMainWorkflow(new Workflow());
		workflowBundle.setMainProfile(new Profile());
		scufl2Tools.setParents(workflowBundle);
		blankWorkflowBundle = null;
		openDataflowInternal(workflowBundle);
		blankWorkflowBundle = workflowBundle;
		observers.notify(new OpenedDataflowEvent(workflowBundle));
		return workflowBundle;
	}

	/**
	 * {@inheritDoc}
	 */
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
		if (!java.awt.GraphicsEnvironment.isHeadless()) {
			OpenDataflowRunnable r = new OpenDataflowRunnable(this, fileType, source);
			if (SwingUtilities.isEventDispatchThread()) {
				r.run();
			} else {
				try {
					SwingUtilities.invokeAndWait(r);
				} catch (InterruptedException e) {
					throw new OpenException("Opening was interrupted", e);
				}catch (InvocationTargetException e) {
					throw new OpenException("Opening was interrupted", e);
				}
			}
			OpenException thrownException = r.getException();
			if (thrownException != null) {
			    throw (thrownException);
			}
			return r.getDataflow();
		}
		else {
			return performOpenDataflow(fileType, source);
		}
	}

	public WorkflowBundle performOpenDataflow(FileType fileType, Object source) throws OpenException {
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
		if (unknownFileType) {
			handlers = getPersistanceHandlerRegistry()
					.getOpenHandlersFor(sourceClass);
		} else {
			handlers = getPersistanceHandlerRegistry().getOpenHandlersFor(fileType,
					sourceClass);
		}

		if (handlers.isEmpty()) {
			throw new OpenException("Unsupported file type or class "
					+ fileType + " " + sourceClass);
		}
		Throwable lastException = null;
		for (DataflowPersistenceHandler handler : handlers) {
			Collection<FileType> fileTypes;
			if (unknownFileType) {
				fileTypes = handler.getOpenFileTypes();
			} else {
				fileTypes = Collections.singleton(fileType);
			}
			for (FileType candidateFileType : fileTypes) {
				if (unknownFileType && (source instanceof File)) {
					// If source is file but fileType was not explicitly set from the
					// open workflow dialog - check the file extension and decide which
					// handler to use based on that (so that we do not loop though all handlers)
					File file = (File) source;
					if (! file.getPath().endsWith(candidateFileType.getExtension())) {
						continue;
					}
				}

				try {
					DataflowInfo openDataflow = handler.openDataflow(
							candidateFileType, source);
					WorkflowBundle workflowBundle = openDataflow.getDataflow();
					logger.info("Loaded workflow: " + workflowBundle.getName()
							+ " " + workflowBundle.getGlobalBaseURI() + " from "
							+ source + " using " + handler);
					return openDataflow;
				} catch (OpenException ex) {
					logger.warn("Could not open workflow " + source + " using "
							+ handler + " of type " + candidateFileType);
					lastException = ex;
				}
			}
		}
		throw new OpenException("Could not open workflow " + source + "\n", lastException);
	}

	/**
	 * Mark the WorkflowBundle as opened, and close the blank WorkflowBundle if needed.
	 *
	 * @param workflowBundle
	 *            WorkflowBundle that has been opened
	 */
	protected void openDataflowInternal(WorkflowBundle workflowBundle) {
		if (workflowBundle == null) {
			throw new NullPointerException("Dataflow can't be null");
		}

		if (isDataflowOpen(workflowBundle)) {
			throw new IllegalArgumentException("Workflow is already open: "
					+ workflowBundle);
		}
		openDataflowInfos.put(workflowBundle, new OpenDataflowInfo());
		setCurrentDataflow(workflowBundle);
		if (openDataflowInfos.size() == 2 && blankWorkflowBundle != null) {
			// Behave like a word processor and close the blank WorkflowBundle
			// when another workflow has been opened
			try {
				closeDataflow(blankWorkflowBundle, true);
			} catch (UnsavedException e) {
				logger.error("Blank workflow was modified "
						+ "and could not be closed");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeObserver(Observer<FileManagerEvent> observer) {
		observers.removeObserver(observer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveDataflow(WorkflowBundle workflowBundle, boolean failOnOverwrite)
			throws SaveException {
		if (workflowBundle == null) {
			throw new NullPointerException("Dataflow can't be null");
		}
		OpenDataflowInfo lastSave = getOpenDataflowInfo(workflowBundle);
		if (lastSave.getSource() == null) {
			throw new SaveException("Can't save without source " + workflowBundle);
		}
		saveDataflow(workflowBundle, lastSave.getFileType(), lastSave.getSource(),
				failOnOverwrite);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveDataflow(WorkflowBundle workflowBundle, FileType fileType,
			Object destination, boolean failOnOverwrite) throws SaveException {
		DataflowInfo savedDataflow = saveDataflowSilently(workflowBundle, fileType, destination, failOnOverwrite);
		getOpenDataflowInfo(workflowBundle).setSavedTo(savedDataflow);
		observers.notify(new SavedDataflowEvent(workflowBundle));
	}


	@Override
	public DataflowInfo saveDataflowSilently(WorkflowBundle workflowBundle, FileType fileType,
			Object destination, boolean failOnOverwrite) throws SaveException,
			OverwriteException {
		Set<DataflowPersistenceHandler> handlers;

		Class<? extends Object> destinationClass = destination.getClass();
		if (fileType != null) {
			handlers = getPersistanceHandlerRegistry().getSaveHandlersForType(
					fileType, destinationClass);
		} else {
			handlers = getPersistanceHandlerRegistry()
					.getSaveHandlersFor(destinationClass);
		}

		if (handlers.isEmpty()) {
			throw new SaveException("Unsupported file type or class "
					+ fileType + " " + destinationClass);
		}
		SaveException lastException = null;

		for (DataflowPersistenceHandler handler : handlers) {

			if (failOnOverwrite) {
				OpenDataflowInfo openDataflowInfo = getOpenDataflowInfo(workflowBundle);
				if (handler.wouldOverwriteDataflow(workflowBundle, fileType,
						destination, openDataflowInfo.getDataflowInfo())) {
					throw new OverwriteException(destination);
				}
			}
			try {
				DataflowInfo savedDataflow = handler.saveDataflow(workflowBundle,
						fileType, destination);
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
		throw new SaveException("Could not save to " + destination + ":\n" + lastException.getLocalizedMessage(),
				lastException);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCurrentDataflow(WorkflowBundle workflowBundle) {
		setCurrentDataflow(workflowBundle, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCurrentDataflow(WorkflowBundle workflowBundle, boolean openIfNeeded) {
		currentWorkflowBundle = workflowBundle;
		if (!isDataflowOpen(workflowBundle)) {
			if (openIfNeeded) {
				openDataflow(workflowBundle);
				return;
			} else {
				throw new IllegalArgumentException("Workflow is not open: "
						+ workflowBundle);
			}
		}
		observers.notify(new SetCurrentDataflowEvent(workflowBundle));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataflowChanged(WorkflowBundle workflowBundle, boolean isChanged) {
		getOpenDataflowInfo(workflowBundle).setIsChanged(isChanged);
		if (blankWorkflowBundle == workflowBundle) {
			blankWorkflowBundle = null;
		}
	}

	public Object getCanonical(Object source)
			throws IllegalArgumentException, URISyntaxException, IOException {

		Object canonicalSource = source;

		if (source instanceof URL){
			URL url = ((URL) source);
			if (url.getProtocol().equalsIgnoreCase("file")) {
				canonicalSource = new File(url.toURI());
			}
		}

		if (canonicalSource instanceof File) {
			canonicalSource = ((File)canonicalSource).getCanonicalFile();
		}
		return canonicalSource;
	}

	public void setDataflowPersistenceHandlerRegistry(DataflowPersistenceHandlerRegistry dataflowPersistenceHandlerRegistry) {
		this.dataflowPersistenceHandlerRegistry = dataflowPersistenceHandlerRegistry;
	}

	/**
	 * Observe the {@link EditManager} for changes to open workflowBundles. A change
	 * of an open workflow would set it as changed using
	 * {@link FileManagerImpl#setDataflowChanged(Dataflow, boolean)}.
	 *
	 * @author Stian Soiland-Reyes
	 *
	 */
	private final class EditManagerObserver implements
			Observer<EditManagerEvent> {

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
