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

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map.Entry;

import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
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
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * Implementation of {@link FileManager}
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class FileManagerImpl extends FileManager {
	private static Logger logger = Logger.getLogger(FileManagerImpl.class);

	/**
	 * The last blank dataflow created using #newDataflow() until it has been
	 * changed - when this variable will be set to null again. Used to
	 * automatically close unmodified blank dataflows on open.
	 */
	private Dataflow blankDataflow = null;

	private EditManager editManager = EditManager.getInstance();

	private EditManagerObserver editManagerObserver = new EditManagerObserver();

	private ModelMap modelMap = ModelMap.getInstance();

	private ModelMapObserver modelMapObserver = new ModelMapObserver();

	protected MultiCaster<FileManagerEvent> observers = new MultiCaster<FileManagerEvent>(
			this);

	/**
	 * Ordered list of open dataflows
	 */
	private LinkedHashMap<Dataflow, OpenDataflowInfo> openDataflowInfos = new LinkedHashMap<Dataflow, OpenDataflowInfo>();

	public DataflowPersistenceHandlerRegistry getPersistanceHandlerRegistry() {
		// Delay initialization of handlers
		return DataflowPersistenceHandlerRegistry.getInstance();
	}

	public FileManagerImpl() {
		editManager.addObserver(editManagerObserver);
		modelMap.addObserver(modelMapObserver);
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
	public boolean canSaveWithoutDestination(Dataflow dataflow) {
		OpenDataflowInfo dataflowInfo = getOpenDataflowInfo(dataflow);
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
	public boolean closeDataflow(Dataflow dataflow, boolean failOnUnsaved)
			throws UnsavedException {
		if (dataflow == null) {
			throw new NullPointerException("Dataflow can't be null");
		}
		ClosingDataflowEvent message = new ClosingDataflowEvent(dataflow);
		observers.notify(message);
		if (message.isAbortClose()) {
			return false;
		}
		if ((failOnUnsaved && getOpenDataflowInfo(dataflow).isChanged())) {
			throw new UnsavedException(dataflow);
		}
		if (dataflow.equals(getCurrentDataflow())) {
			// We'll need to change current dataflow
			// Find best candidate to the left or right
			List<Dataflow> dataflows = getOpenDataflows();
			int openIndex = dataflows.indexOf(dataflow);
			if (openIndex == -1) {
				throw new IllegalArgumentException("Workflow was not opened "
						+ dataflow);
			} else if (openIndex > 0) {
				setCurrentDataflow(dataflows.get(openIndex - 1));
			} else if (openIndex == 0 && dataflows.size() > 1) {
				setCurrentDataflow(dataflows.get(1));
			} else {
				// If it was the last one, start a new, empty dataflow
				newDataflow();
			}
		}
		if (dataflow == blankDataflow) {
			blankDataflow = null;
		}
		openDataflowInfos.remove(dataflow);
		observers.notify(new ClosedDataflowEvent(dataflow));
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dataflow getCurrentDataflow() {
		return (Dataflow) modelMap.getModel(ModelMapConstants.CURRENT_DATAFLOW);
	}

	@Override
	public Dataflow getDataflowBySource(Object source) {
		for (Entry<Dataflow,OpenDataflowInfo> infoEntry : openDataflowInfos.entrySet()) {
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
	public String getDataflowName(Dataflow dataflow) {
		Object source = getDataflowSource(dataflow);
		String name = dataflow.getLocalName(); 	// Fallback
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
	public Object getDataflowSource(Dataflow dataflow) {
		return getOpenDataflowInfo(dataflow).getSource();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileType getDataflowType(Dataflow dataflow) {
		return getOpenDataflowInfo(dataflow).getFileType();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Observer<FileManagerEvent>> getObservers() {
		return observers.getObservers();
	}

	/**
	 * Get the {@link OpenDataflowInfo} for the given dataflow
	 * 
	 * @throws NullPointerException
	 *             if the dataflow was <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the dataflow was not open.
	 * @param dataflow
	 *            Dataflow which information is to be found
	 * @return The {@link OpenDataflowInfo} describing the dataflow
	 */
	protected synchronized OpenDataflowInfo getOpenDataflowInfo(
			Dataflow dataflow) {
		if (dataflow == null) {
			throw new NullPointerException("Dataflow can't be null");
		}
		OpenDataflowInfo info = openDataflowInfos.get(dataflow);
		if (info != null) {
			return info;
		} else {
			throw new IllegalArgumentException("Workflow was not opened"
					+ dataflow);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Dataflow> getOpenDataflows() {
		return new ArrayList<Dataflow>(openDataflowInfos.keySet());
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
					"All supported workflow types"));
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
	public boolean isDataflowChanged(Dataflow dataflow) {
		return getOpenDataflowInfo(dataflow).isChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDataflowOpen(Dataflow dataflow) {
		return openDataflowInfos.containsKey(dataflow);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dataflow newDataflow() {
		Dataflow dataflow = editManager.getEdits().createDataflow();
		blankDataflow = null;
		openDataflowInternal(dataflow);
		blankDataflow = dataflow;
		observers.notify(new OpenedDataflowEvent(dataflow));
		return dataflow;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void openDataflow(Dataflow dataflow) {
		
		openDataflowInternal(dataflow);
		observers.notify(new OpenedDataflowEvent(dataflow));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dataflow openDataflow(FileType fileType, Object source)
			throws OpenException {		
		DataflowInfo openDataflow = openDataflowSilently(fileType, source);
		Dataflow dataflow = openDataflow.getDataflow();
		openDataflowInternal(dataflow);
		getOpenDataflowInfo(dataflow).setOpenedFrom(openDataflow);
		observers.notify(new OpenedDataflowEvent(dataflow));
		return dataflow;
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
					Dataflow dataflow = openDataflow.getDataflow();					
					logger.info("Loaded workflow: " + dataflow.getLocalName()
							+ " " + dataflow.getInternalIdentier() + " from "
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
	 * Mark the dataflow as opened, and close the blank dataflow if needed.
	 * 
	 * @param dataflow
	 *            Dataflow that has been opened
	 */
	protected void openDataflowInternal(Dataflow dataflow) {
		if (dataflow == null) {
			throw new NullPointerException("Dataflow can't be null");
		}

		if (isDataflowOpen(dataflow)) {
			throw new IllegalArgumentException("Workflow is already open: "
					+ dataflow);
		}
		openDataflowInfos.put(dataflow, new OpenDataflowInfo());
		setCurrentDataflow(dataflow);
		if (openDataflowInfos.size() == 2 && blankDataflow != null) {
			// Behave like a word processor and close the blank workflow
			// when another workflow has been opened
			try {
				closeDataflow(blankDataflow, true);
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
	public void saveDataflow(Dataflow dataflow, boolean failOnOverwrite)
			throws SaveException {
		if (dataflow == null) {
			throw new NullPointerException("Dataflow can't be null");
		}
		OpenDataflowInfo lastSave = getOpenDataflowInfo(dataflow);
		if (lastSave.getSource() == null) {
			throw new SaveException("Can't save without source " + dataflow);
		}
		saveDataflow(dataflow, lastSave.getFileType(), lastSave.getSource(),
				failOnOverwrite);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveDataflow(Dataflow dataflow, FileType fileType,
			Object destination, boolean failOnOverwrite) throws SaveException {
		DataflowInfo savedDataflow = saveDataflowSilently(dataflow, fileType, destination, failOnOverwrite);
		getOpenDataflowInfo(dataflow).setSavedTo(savedDataflow);
		observers.notify(new SavedDataflowEvent(dataflow));
	}
	

	@Override
	public DataflowInfo saveDataflowSilently(Dataflow dataflow, FileType fileType,
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
				OpenDataflowInfo openDataflowInfo = getOpenDataflowInfo(dataflow);
				if (handler.wouldOverwriteDataflow(dataflow, fileType,
						destination, openDataflowInfo.getDataflowInfo())) {
					throw new OverwriteException(destination);
				}
			}
			try {
				DataflowInfo savedDataflow = handler.saveDataflow(dataflow,
						fileType, destination);
				savedDataflow.getDataflow();
				logger.info("Saved workflow: " + dataflow.getLocalName() + " "
						+ dataflow.getInternalIdentier() + " to "
						+ savedDataflow.getCanonicalSource() + " using "
						+ handler);				
				return savedDataflow;
			} catch (SaveException ex) {
				logger.warn("Could not save to " + destination + " using "
						+ handler);
				lastException = ex;
			}
		}
		throw new SaveException("Could not save to " + destination,
				lastException);
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCurrentDataflow(Dataflow dataflow) {
		setCurrentDataflow(dataflow, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCurrentDataflow(Dataflow dataflow, boolean openIfNeeded) {
		if (!isDataflowOpen(dataflow)) {
			if (openIfNeeded) {
				openDataflow(dataflow);
				return;
			} else {
				throw new IllegalArgumentException("Workflow is not open: "
						+ dataflow);
			}
		}
		modelMap.setModel(ModelMapConstants.CURRENT_DATAFLOW, dataflow);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataflowChanged(Dataflow dataflow, boolean isChanged) {
		getOpenDataflowInfo(dataflow).setIsChanged(isChanged);
		if (blankDataflow == dataflow) {
			blankDataflow = null;
		}
	}

	/**
	 * Observe the {@link EditManager} for changes to open dataflows. A change
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
				Dataflow dataflow = dataflowEdit.getDataFlow();
				/**
				 * TODO: on undo/redo - keep last event or similar to determine
				 * if workflow was saved before. See
				 * FileManagerTest#isChangedWithUndo().
				 */
				setDataflowChanged(dataflow, true);
			}
		}
	}

	/**
	 * Observes the {@link ModelMap} for the ModelMapConstants.CURRENT_DATAFLOW.
	 * Make sure that the dataflow is opened and notifies observers with a
	 * SetCurrentDataflowEvent.
	 * 
	 * @author Stian Soiland-Reyes
	 * 
	 */
	private final class ModelMapObserver implements Observer<ModelMapEvent> {
		public void notify(Observable<ModelMapEvent> sender,
				ModelMapEvent message) throws Exception {
			if (message.getModelName().equals(
					ModelMapConstants.CURRENT_DATAFLOW)) {
				Dataflow newModel = (Dataflow) message.getNewModel();
				if (newModel != null) {
					if (!isDataflowOpen(newModel)) {
						openDataflowInternal(newModel);
					}
				}
				observers.notify(new SetCurrentDataflowEvent(newModel));
			}
		}
	}

	

	
}
