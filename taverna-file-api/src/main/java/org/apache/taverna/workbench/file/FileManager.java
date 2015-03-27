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

package org.apache.taverna.workbench.file;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.workbench.file.events.ClosedDataflowEvent;
import org.apache.taverna.workbench.file.events.FileManagerEvent;
import org.apache.taverna.workbench.file.events.OpenedDataflowEvent;
import org.apache.taverna.workbench.file.events.SavedDataflowEvent;
import org.apache.taverna.workbench.file.exceptions.OpenException;
import org.apache.taverna.workbench.file.exceptions.OverwriteException;
import org.apache.taverna.workbench.file.exceptions.SaveException;
import org.apache.taverna.workbench.file.exceptions.UnsavedException;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * Manager of open files (WorkflowBundleBundles) in the workbench.
 * <p>
 * A {@link WorkflowBundle} can be opened for the workbench using
 * {@link #openDataflow(FileType, Object)} or {@link #openDataflow(WorkflowBundle)}.
 * {@link Observer}s of the FileManager gets notified with an
 * {@link OpenedDataflowEvent}. The opened workflow is also
 * {@link #setCurrentDataflow(WorkflowBundle) made the current dataflow}, available
 * through {@link #getCurrentDataflow()} or by observing the
 * {@link net.sf.taverna.t2.lang.ui.ModelMap} for the model name
 * {@link net.sf.taverna.t2.workbench.ModelMapConstants#CURRENT_DATAFLOW}.
 * <p>
 * A dataflow can be saved using
 * {@link #saveDataflow(WorkflowBundle, FileType, Object, boolean)}. Observers will be
 * presented a {@link SavedDataflowEvent}.
 * <p>
 * If a dataflow was previously opened from a saveable destination or previously
 * saved using {@link #saveDataflow(WorkflowBundle, FileType, Object, boolean)},
 * {@link #saveDataflow(WorkflowBundle, boolean)} can be used to resave to that
 * destination.
 * <p>
 * You can get the last opened/saved source and type using
 * {@link #getDataflowSource(WorkflowBundle)} and {@link #getDataflowType(WorkflowBundle)}.
 * <p>
 * If the save methods are used with failOnOverwrite=true, an
 * {@link OverwriteException} will be thrown if the destination file already
 * exists and was not last written by a previous save on that dataflow. (This is
 * typically checked using timestamps on the file).
 * <p>
 * A dataflow can be closed using {@link #closeDataflow(WorkflowBundle, boolean)}. A
 * closed dataflow is no longer monitored for changes and can no longer be used
 * with the other operations, except {@link #openDataflow(WorkflowBundle)}.
 * <p>
 * If a dataflow has been changed using the {@link EditManager},
 * {@link #isDataflowChanged(WorkflowBundle)} will return true until the next save. If
 * the close methods are used with failOnUnsaved=true, an
 * {@link UnsavedException} will be thrown if the dataflow has been changed.
 * <p>
 * The implementation of this interface is an OSGi Service.
 *
 * @author Stian Soiland-Reyes
 */
public interface FileManager extends Observable<FileManagerEvent> {
	/**
	 * True if {@link #saveDataflow(WorkflowBundle, boolean)} can save the
	 * workflow, i.e., that there exists an SPI implementation of
	 * {@link DataflowPersistenceHandler} that can save to
	 * {@link #getDataflowSource(WorkflowBundle)} using
	 * {@link #getDataflowType(WorkflowBundle)}.
	 * 
	 * @see #saveDataflow(WorkflowBundle, boolean)
	 * @param dataflow
	 *            The dataflow to check
	 * @return <code>true</code> if the given dataflow can be saved without
	 *         providing a destination and filetype
	 */
	boolean canSaveWithoutDestination(WorkflowBundle dataflow);

	/**
	 * Close the specified dataflow.
	 * <p>
	 * A closed dataflow can no longer be used with the save methods, and will
	 * disappear from the UI's list of open dataflows.
	 * <p>
	 * If no more dataflows would be open after the close, a new empty dataflow
	 * is opened as through {@link #newDataflow()}.
	 * <p>
	 * If the failOnUnsaved parameters is <code>true</code>, and
	 * {@link #isDataflowChanged(WorkflowBundle)} is <code>true</code>, an
	 * {@link UnsavedException} will be thrown, typically because the workflow
	 * has been changed using the {@link EditManager} since the last change.
	 * <p>
	 * Listeners registered using {@link Observable#addObserver(Observer)} will
	 * be notified with an {@link ClosedDataflowEvent}.
	 *
	 * @param dataflow
	 *            {@link WorkflowBundle} to close
	 * @param failOnUnsaved
	 *            If <code>true</code>, fail on unsaved changes
	 * @throws UnsavedException
	 *             If failOnUnsaved was <code>true</code> and there has been
	 *             changes to the dataflow since the last save
	 */
	boolean closeDataflow(WorkflowBundle dataflow, boolean failOnUnsaved)
			throws UnsavedException;

	/**
	 * Get the current dataflow.
	 * <p>
	 * The current workflow is typically the one currently showed on the screen,
	 * and is also in {@link #getOpenDataflows()}.
	 * <p>
	 * The current dataflow is set through {@link #setCurrentDataflow(WorkflowBundle)}
	 * or the {@link net.sf.taverna.t2.lang.ui.ModelMap} using the key
	 * {@link net.sf.taverna.t2.workbench.ModelMapConstants#CURRENT_DATAFLOW}.
	 *
	 * @return The current dataflow, or <code>null</code> if no dataflow is
	 *         current
	 */
	WorkflowBundle getCurrentDataflow();

	/**
	 * Get the dataflow that was opened from or last saved to the given source.
	 *
	 * @param source
	 *            The source as opened with or saved to
	 *            {@link #openDataflow(FileType, Object)}
	 * @return The opened {@link WorkflowBundle} or <code>null</code> if no matching
	 *         dataflow found.
	 */
	WorkflowBundle getDataflowBySource(Object source);

	/**
	 * Get a name to represent this dataflow.
	 * <p>
	 * The name will primarily be deduced from the source of where the workflow
	 * is opened from, unless {@link Object#toString()} is not overridden (for
	 * instance opened from an InputStream) or if the source is unknown, in
	 * which case the dataflow's internal name {@link WorkflowBundle#getName()}
	 * is returned.
	 * <p>
	 * The returned name can be used in listings like the WorkflowBundles menu, but is
	 * not guaranteed to be unique. (For instance a workflow could be opened
	 * twice from the same source).
	 *
	 * @param dataflow
	 *            WorkflowBundle to get the name for
	 * @return The deduced workflow name
	 */
	String getDataflowName(WorkflowBundle dataflow);

	/**
	 * Returns the default name to use when creating new workflows.
	 *
	 * @return the default name to use when creating new workflows
	 */
	String getDefaultWorkflowName();

	/**
	 * Get the last opened/saved source/destination for the given dataflow.
	 * <p>
	 * The source is the last source used with
	 * {@link #saveDataflow(WorkflowBundle, FileType, Object, boolean)} for the given
	 * dataflow, or {@link #openDataflow(FileType, Object)} if it has not yet
	 * been saved.
	 * <p>
	 * If the given dataflow's last opened/saved location was unknown (opened
	 * with {@link #newDataflow()} or {@link #openDataflow(WorkflowBundle)}), return
	 * <code>null</code>.
	 *
	 * @param dataflow
	 *            {@link WorkflowBundle} which file is to be returned
	 * @return The last opened/saved source for the given dataflow, or
	 *         <code>null</code> if unknown.
	 */
	Object getDataflowSource(WorkflowBundle dataflow);

	/**
	 * Get the last opened/saved source/destination FileType for the given
	 * dataflow.
	 * <p>
	 * The type is the last {@link FileType} used with
	 * {@link #saveDataflow(WorkflowBundle, FileType, Object, boolean)} for the given
	 * dataflow, or {@link #openDataflow(FileType, Object)} if it has not yet
	 * been saved.
	 * <p>
	 * If the given dataflow's last opened/saved file type was unknown (opened
	 * with {@link #newDataflow()} or {@link #openDataflow(WorkflowBundle)}), return
	 * <code>null</code>.
	 *
	 * @param dataflow
	 *            {@link WorkflowBundle} which file is to be returned
	 * @return The last opened/saved {@link FileType} for the given dataflow, or
	 *         <code>null</code> if unknown.
	 */
	FileType getDataflowType(WorkflowBundle dataflow);

	/**
	 * Get the list of currently open dataflows. This list of dataflows are
	 * typically displayed in the UI in the "WorkflowBundles" menu to allow switching
	 * the {@link #getCurrentDataflow() current dataflow}.
	 *
	 * @return A copy of the {@link List} of open {@link WorkflowBundle}s
	 */
	List<WorkflowBundle> getOpenDataflows();

	/**
	 * Get a list of {@link FileFilter}s for supported {@link FileType}s that
	 * can be opened with any source class.
	 *
	 * @return A {@link List} of {@link FileFilter}s supported by
	 *         {@link #openDataflow(FileType, Object)}
	 */
	List<FileFilter> getOpenFileFilters();

	/**
	 * Get a list of {@link FileFilter}s for supported {@link FileType}s that
	 * can be opened with given source class.
	 *
	 * @param sourceClass
	 *            Source class that can be opened from
	 * @return A {@link List} of {@link FileFilter}s supported by
	 *         {@link #openDataflow(FileType, Object)}
	 */
	List<FileFilter> getOpenFileFilters(Class<?> sourceClass);

	/**
	 * Get a list of {@link FileFilter}s for supported {@link FileType}s that
	 * can be saved to any destination class.
	 *
	 * @return A {@link List} of {@link FileFilter}s supported by
	 *         {@link #saveDataflow(WorkflowBundle, FileType, Object, boolean)}
	 */
	List<FileFilter> getSaveFileFilters();

	/**
	 * Get a list of {@link FileFilter}s for supported {@link FileType}s that
	 * can be saved to the given destination class.
	 *
	 * @param destinationClass
	 *            Destination class that can be saved to
	 * @return A {@link List} of {@link FileFilter}s supported by
	 *         {@link #saveDataflow(WorkflowBundle, FileType, Object, boolean)}
	 */
	List<FileFilter> getSaveFileFilters(Class<?> destinationClass);

	/**
	 * Return <code>true</code> if the dataflow has been changed (through the
	 * {@link EditManager} or {@link #setDataflowChanged(WorkflowBundle, boolean)})
	 * since last save.
	 *
	 * @param dataflow
	 *            WorkflowBundle which changed status is to be checked
	 * @return <code>true</code> if the dataflow has been changed since last
	 *         save.
	 */
	boolean isDataflowChanged(WorkflowBundle dataflow);

	/**
	 * True if the given dataflow has been opened and is in
	 * {@link #getOpenDataflows()}.
	 *
	 * @param dataflow
	 *            Dataflow to check
	 * @return <code>true</code> if dataflow is open
	 */
	boolean isDataflowOpen(WorkflowBundle dataflow);

	/**
	 * Create and open a new, blank dataflow. The dataflow will not initially be
	 * marked as changed.
	 * <p>
	 * Listeners registered using {@link Observable#addObserver(Observer)} will
	 * be notified with an {@link OpenedDataflowEvent}.
	 * <p>
	 * Note, if the dataflow is later changed, it will not be possible to save
	 * it to any original location using
	 * {@link #saveDataflow(WorkflowBundle, boolean)}, only
	 * {@link #saveDataflow(WorkflowBundle, FileType, Object, boolean)}.
	 *
	 * @return The newly opened blank {@link WorkflowBundle}
	 */
	WorkflowBundle newDataflow();

	/**
	 * Open a {@link WorkflowBundle} instance that has been created outside the
	 * {@link FileManager}. The dataflow will not initially be marked as
	 * changed.
	 * <p>
	 * Listeners registered using {@link Observable#addObserver(Observer)} will
	 * be notified with an {@link OpenedDataflowEvent}.
	 * <p>
	 * Note, if the dataflow is later changed, it will not be possible to save
	 * it to its original location using
	 * {@link #saveDataflow(WorkflowBundle, boolean)}, only
	 * {@link #saveDataflow(WorkflowBundle, FileType, Object, boolean)}.
	 * <p>
	 * Instead of using this option it is recommended to create your own
	 * {@link FileType} and/or source type and a
	 * {@link DataflowPersistenceHandler} to implement save and/or reopen
	 * (revert).
	 * <p>
	 * If there is only one workflow open before opening this workflow, and it
	 * is an unchanged blank workflow, the blank workflow will be closed.
	 *
	 * @param dataflow
	 *            {@link WorkflowBundle} instance that is to be added as an open
	 *            dataflow
	 */
	void openDataflow(WorkflowBundle dataflow);

	/**
	 * Open a dataflow from a source. The dataflow will not initially be marked
	 * as changed, and will be set as the new current workflow.
	 * <p>
	 * The file manager will find implementations of the SPI
	 * {@link DataflowPersistenceHandler} to perform the opening for the given file
	 * type and destination class.
	 * <p>
	 * Listeners registered using {@link Observable#addObserver(Observer)} will
	 * be notified with an {@link OpenedDataflowEvent}.
	 * <p>
	 * If there is only one workflow open before opening this workflow, and it
	 * is an unchanged blank workflow, the blank workflow will be closed.
	 *
	 * @param fileType
	 *            The filetype, for instance
	 *            {@link net.sf.taverna.t2.workbench.file.impl.T2FlowFileType}.
	 *            The file type must be supported by an implementation of the
	 *            SPI DataflowPersistenceHandler.
	 * @param source
	 *            The source, for instance a {@link File} or {@link URL}. The
	 *            source type must be supported by an implementation of
	 *            DataflowPersistenceHandler.
	 * @return The opened {@link WorkflowBundle}.
	 * @throws OpenException
	 *             If there was no matching DataflowPersistenceHandler found or
	 *             the source could not be opened for any other reason, such as
	 *             IO errors or syntax errors.
	 */
	WorkflowBundle openDataflow(FileType fileType, Object source)
			throws OpenException;

	/**
	 * Open a dataflow from a source silently. The dataflow will not be listed
	 * as open, and will not be made the current workflow.
	 * <p>
	 * The file manager will find implementations of the SPI
	 * {@link DataflowPersistenceHandler} to perform the opening for the given file
	 * type and destination class.
	 * <p>
	 * Listeners will <strong>not</strong> be notified.
	 *
	 * @param fileType
	 *            The filetype, for instance
	 *            {@link net.sf.taverna.t2.workbench.file.impl.T2FlowFileType}.
	 *            The file type must be supported by an implementation of the
	 *            SPI DataflowPersistenceHandler.
	 * @param source
	 *            The source, for instance a {@link File} or {@link URL}. The
	 *            source type must be supported by an implementation of
	 *            DataflowPersistenceHandler.
	 * @return The {@link DataflowInfo} describing the opened dataflow.
	 * @throws OpenException
	 *             If there was no matching DataflowPersistenceHandler found or
	 *             the source could not be opened for any other reason, such as
	 *             IO errors or syntax errors.
	 */
	DataflowInfo openDataflowSilently(FileType fileType, Object source)
			throws OpenException;

	/**
	 * Save the dataflow to the last saved destination and FileType from
	 * {@link #saveDataflow(WorkflowBundle, FileType, Object, boolean)} or the last
	 * opened source and FileType from {@link #openDataflow(FileType, Object)}.
	 * <p>
	 * Listeners registered using {@link Observable#addObserver(Observer)} will
	 * be notified with an {@link SavedDataflowEvent}.
	 *
	 * @param dataflow
	 *            Dataflow to save. Dataflow must have been opened with
	 *            {@link #openDataflow(FileType, Object)} or saved using
	 *            {@link #saveDataflow(WorkflowBundle, FileType, Object, boolean)}.
	 * @param failOnOverwrite
	 *            If <code>true</code>, an {@link OverwriteException} is thrown
	 *            if a save would overwrite the destination because it has been
	 *            changed since last open/save.
	 * @throws OverwriteException
	 *             if failOnOverwrite was true, and a save would overwrite the
	 *             destination because it has been changed since last open/save.
	 *             The save was not performed.
	 * @throws SaveException
	 *             If any other error occurs during saving, including the case
	 *             that a dataflow is not connected to a source or destination,
	 *             that there are no handlers (some source types can't be saved
	 *             to, such as HTTP URLs), or any other IO error occurring while
	 *             saving.
	 */
	void saveDataflow(WorkflowBundle dataflow, boolean failOnOverwrite)
			throws SaveException, OverwriteException;

	/**
	 * Save the dataflow to the given destination using the given filetype.
	 * <p>
	 * The file manager will find implementations of the SPI
	 * {@link DataflowPersistenceHandler} to perform the save for the given file
	 * type and destination class.
	 * <p>
	 * Listeners registered using {@link Observable#addObserver(Observer)} will
	 * be notified with an {@link SavedDataflowEvent}.
	 *
	 * @param dataflow
	 *            {@link Dataflow} to be saved
	 * @param fileType
	 *            {@link FileType} to save dataflow as, for instance
	 *            {@link net.sf.taverna.t2.workbench.file.impl.T2FlowFileType}.
	 *            The file type must be supported by an SPI implementation of
	 *            {@link DataflowPersistenceHandler}.
	 * @param destination
	 *            Destination to save dataflow to, for instance a {@link File}
	 * @param failOnOverwrite
	 *            If <code>true</code>, an {@link OverwriteException} is thrown
	 *            if a save would overwrite the destination because it already
	 *            exists, but was not opened or save to using the file manager
	 *            for the given dataflow. (ie. a repeated call to this function
	 *            should not throw an OverwriteException unless someone outside
	 *            has modified the file)
	 * @throws OverwriteException
	 *             if failOnOverwrite was true, and a save would overwrite the
	 *             destination because it already existed, and was not last
	 *             written to by a previous save. The save was not performed.
	 * @throws SaveException
	 *             If any other error occurs during saving, including the case
	 *             that a dataflow is not connected to a source or destination,
	 *             that there are no handlers (some source types can't be saved
	 *             to, such as HTTP URLs), or any other IO error occurring while
	 *             saving.
	 */
	void saveDataflow(WorkflowBundle dataflow, FileType fileType,
			Object destination, boolean failOnOverwrite) throws SaveException,
			OverwriteException;

	/**
	 * Silently save the dataflow to the given destination using the given
	 * filetype.
	 * <p>
	 * The file manager will find implementations of the SPI
	 * {@link DataflowPersistenceHandler} to perform the save for the given file
	 * type and destination class.
	 * <p>
	 * Listeners will <strong>not</strong> be notified, and the dataflow does
	 * not previously have to be opened. getDataflowSource(),
	 * isDataflowChanged() etc will not be affected - as if the silent save
	 * never happened.
	 * 
	 * @param dataflow
	 *            {@link WorkflowBundle} to be saved
	 * @param fileType
	 *            {@link FileType} to save dataflow as, for instance
	 *            {@link net.sf.taverna.t2.workbench.file.impl.T2FlowFileType}.
	 *            The file type must be supported by an SPI implementation of
	 *            {@link DataflowPersistenceHandler}.
	 * @param destination
	 *            Destination to save dataflow to, for instance a {@link File}
	 * @param failOnOverwrite
	 *            If <code>true</code>, an {@link OverwriteException} is thrown
	 *            if a save would overwrite the destination because it already
	 *            exists, but was not opened or save to using the file manager
	 *            for the given dataflow. (ie. a repeated call to this function
	 *            should not throw an OverwriteException unless someone outside
	 *            has modified the file)
	 * @return The {@link DataflowInfo} describing where the workflow was saved
	 * @throws OverwriteException
	 *             if failOnOverwrite was true, and a save would overwrite the
	 *             destination because it already existed, and was not last
	 *             written to by a previous save. The save was not performed.
	 * @throws SaveException
	 *             If any other error occurs during saving, including the case
	 *             that a dataflow is not connected to a source or destination,
	 *             that there are no handlers (some source types can't be saved
	 *             to, such as HTTP URLs), or any other IO error occurring while
	 *             saving.
	 */
	DataflowInfo saveDataflowSilently(WorkflowBundle dataflow, FileType fileType,
			Object destination, boolean failOnOverwrite) throws SaveException,
			OverwriteException;

	/**
	 * Set the current dataflow to the one provided.
	 * <p>
	 * The current dataflow can be retrieved using {@link #getCurrentDataflow()}
	 * . Note that opening a dataflow will normally also set it as the current
	 * dataflow.
	 * <p>
	 * Listeners registered using {@link Observable#addObserver(Observer)} will
	 * be notified with an {@link SetCurrentDataflowEvent}.
	 * <p>
	 * Note, the dataflow must already be open. If this is not the case, use one
	 * of the openDataflow() methods or
	 * {@link #setCurrentDataflow(WorkflowBundle, boolean)}.
	 *
	 * @see #setCurrentDataflow(WorkflowBundle, boolean)
	 * @param dataflow
	 *            {@link WorkflowBundle} to be made current
	 */
	void setCurrentDataflow(WorkflowBundle dataflow);

	/**
	 * Set the current dataflow to the one provided.
	 * <p>
	 * The current dataflow can be retrieved using {@link #getCurrentDataflow()}
	 * . Note that opening a dataflow will normally also set it as the current
	 * dataflow.
	 * <p>
	 * Listeners registered using {@link Observable#addObserver(Observer)} will
	 * be notified with an {@link SetCurrentDataflowEvent}.
	 * <p>
	 * Unless <code>openIfNeeded</code> is <code>true</code>, the dataflow must
	 * already be open.
	 *
	 * @see #setCurrentDataflow(WorkflowBundle, boolean)
	 * @param dataflow
	 *            {@link WorkflowBundle} to be made current
	 * @param openIfNeeded
	 *            If <code>true</code>, open the dataflow if needed
	 */
	void setCurrentDataflow(WorkflowBundle dataflow, boolean openIfNeeded);

	/**
	 * Set a dataflow as changed or not. This changes the value returned by
	 * {@link #isDataflowChanged(WorkflowBundle)}.
	 * <p>
	 * This method can be used if the dataflow has been changed outside the
	 * {@link EditManager}.
	 *
	 * @param dataflow
	 *            Dataflow which is to be marked
	 * @param isChanged
	 *            <code>true</code> if the dataflow is to be marked as changed,
	 *            <code>false</code> if it is to be marked as not changed.
	 */
	void setDataflowChanged(WorkflowBundle dataflow, boolean isChanged);

	/**
	 * Returns the canonical form of the source where the dataflow was opened
	 * from or saved to. The code for this method was devised based on
	 * {@link net.sf.taverna.t2.workbench.file.impl.T2DataflowOpener#openDataflow(FileType fileType, Object source)}.
	 */
	Object getCanonical(Object source) throws IllegalArgumentException,
			URISyntaxException, IOException;
}
