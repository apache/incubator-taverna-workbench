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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.OpenedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.SavedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.SetCurrentDataflowEvent;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.exceptions.OverwriteException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.file.exceptions.UnsavedException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * Manager of open files (Dataflows) in the workbench.
 * <p>
 * A {@link Dataflow} can be opened for the workbench using
 * {@link #openDataflow(FileType, Object)} or {@link #openDataflow(Dataflow)}.
 * {@link Observer}s of the FileManager gets notified with an
 * {@link OpenedDataflowEvent}. The opened workflow is also
 * {@link #setCurrentDataflow(Dataflow) made the current dataflow}, available
 * through {@link #getCurrentDataflow()} or by observing the
 * {@link net.sf.taverna.t2.lang.ui.ModelMap} for the model name
 * {@link net.sf.taverna.t2.workbench.ModelMapConstants#CURRENT_DATAFLOW}.
 * </p>
 * <p>
 * A dataflow can be saved using
 * {@link #saveDataflow(Dataflow, FileType, Object, boolean)}. Observers will be
 * presented a {@link SavedDataflowEvent}.
 * </p>
 * <p>
 * If a dataflow was previously opened from a saveable destination or previously
 * saved using {@link #saveDataflow(Dataflow, FileType, Object, boolean)
 * 
 * {@link #saveDataflow(Dataflow, boolean)} can be used to resave to that
 * destination.
 * </p>
 * <p>
 * You can get the last opened/saved source and type using
 * {@link #getDataflowSource(Dataflow)} and {@link #getDataflowType(Dataflow)}.
 * </p>
 * <p>
 * If the save methods are used with failOnOverwrite=true, an
 * {@link OverwriteException} will be thrown if the destination file already
 * exists and was not last written by a previous save on that dataflow. (This is
 * typically checked using timestamps on the file).
 * </p>
 * <p>
 * A dataflow can be closed using {@link #closeDataflow(Dataflow, boolean)}. A
 * closed dataflow is no longer monitored for changes and can no longer be used
 * with the other operations, except {@link #openDataflow(Dataflow)}.
 * </p>
 * <p>
 * If a dataflow has been changed using the {@link EditManager},
 * {@link #isDataflowChanged(Dataflow)} will return true until the next save. If
 * the close methods are used with failOnUnsaved=true, an
 * {@link UnsavedException} will be thrown if the dataflow has been changed.
 * </p>
 * <p>
 * The implementation of this FileManager can be discovered using
 * {@link #getInstance()}.
 * </p>
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class FileManager implements Observable<FileManagerEvent> {

	/**
	 * SPI instance discovered by {@link #getInstance()}
	 */
	private static FileManager instance;

	/**
	 * Get the {@link FileManager} implementation singleton as discovered
	 * through an {@link SPIRegistry}.
	 * 
	 * @throws IllegalStateException
	 *             If no implementation was found
	 * @return Discovered {@link FileManager} implementation singleton
	 */
	public static synchronized FileManager getInstance()
			throws IllegalStateException {
		if (instance == null) {
			SPIRegistry<FileManager> registry = new SPIRegistry<FileManager>(
					FileManager.class);
			try {
				instance = registry.getInstances().get(0);
			} catch (IndexOutOfBoundsException ex) {
				throw new IllegalStateException(
						"Could not find implementation of " + FileManager.class);
			}
		}
		return instance;
	}

	/**
	 * True if {@link #saveDataflow(Dataflow, boolean)} can save the workflow,
	 * ie. that there exists an SPI implementation of
	 * {@link DataflowPersistenceHandler} that can save to
	 * {@link #getDataflowSource(Dataflow)} using
	 * {@link #getDataflowType(Dataflow)}.
	 * 
	 * @see #saveDataflow(Dataflow, boolean)
	 * @param dataflow
	 *            The dataflow to check
	 * @return <code>true</code> if the given dataflow can be saved without
	 *         providing a destination and filetype
	 */
	public abstract boolean canSaveWithoutDestination(Dataflow dataflow);

	/**
	 * Close the specified dataflow.
	 * <p>
	 * A closed dataflow can no longer be used with the save methods, and will
	 * disappear from the UI's list of open dataflows.
	 * </p>
	 * <p>
	 * If no more dataflows would be open after the close, a new empty dataflow
	 * is opened as through {@link #newDataflow()}.
	 * </p>
	 * <p>
	 * If the failOnUnsaved parameters is <code>true</code>, and
	 * {@link #isDataflowChanged(Dataflow)} is <code>true</code>, an
	 * {@link UnsavedException} will be thrown, typically because the workflow
	 * has been changed using the {@link EditManager} since the last change.
	 * </p>
	 * <p>
	 * Listeners registered using {@link Observable#addObserver(Observer)} will
	 * be notified with an {@link ClosedDataflowEvent}.
	 * </p>
	 * 
	 * @param dataflow
	 *            {@link Dataflow} to close
	 * @param failOnUnsaved
	 *            If <code>true</code>, fail on unsaved changes
	 * @throws UnsavedException
	 *             If failOnUnsaved was <code>true</code> and there has been
	 *             changes to the dataflow since the last save
	 */
	public abstract boolean closeDataflow(Dataflow dataflow, boolean failOnUnsaved)
			throws UnsavedException;

	/**
	 * Get the current dataflow.
	 * <p>
	 * The current workflow is typically the one currently showed on the screen,
	 * and is also in {@link #getOpenDataflows()}.
	 * </p>
	 * <p>
	 * The current dataflow is set through {@link #setCurrentDataflow(Dataflow)}
	 * or the {@link net.sf.taverna.t2.lang.ui.ModelMap} using the key
	 * {@link net.sf.taverna.t2.workbench.ModelMapConstants#CURRENT_DATAFLOW}.
	 * </p>
	 * 
	 * @return The current dataflow, or <code>null</code> if no dataflow is
	 *         current
	 */
	public abstract Dataflow getCurrentDataflow();

	/**
	 * Get the dataflow that was opened from or last saved to the given source.
	 * 
	 * @param source
	 *            The source as opened with or saved to
	 *            {@link #openDataflow(FileType, Object)}
	 * @return The opened {@link Dataflow} or <code>null</code> if no matching
	 *         dataflow found.
	 */
	public abstract Dataflow getDataflowBySource(Object source);
	
	/**
	 * Get the last opened/saved source/destination for the given dataflow.
	 * <p>
	 * The source is the last source used with
	 * {@link #saveDataflow(Dataflow, FileType, Object, boolean)} for the given
	 * dataflow, or {@link #openDataflow(FileType, Object)} if it has not yet
	 * been saved.
	 * <p>
	 * If the given dataflow's last opened/saved location was unknown (opened
	 * with {@link #newDataflow()} or {@link #openDataflow(Dataflow)}), return
	 * <code>null</code>.
	 * </p>
	 * 
	 * @param dataflow
	 *            {@link Dataflow} which file is to be returned
	 * @return The last opened/saved source for the given dataflow, or
	 *         <code>null</code> if unknown.
	 */
	public abstract Object getDataflowSource(Dataflow dataflow);

	/**
	 * Get the last opened/saved source/destination FileType for the given
	 * dataflow.
	 * <p>
	 * The type is the last {@link FileType} used with
	 * {@link #saveDataflow(Dataflow, FileType, Object, boolean)} for the given
	 * dataflow, or {@link #openDataflow(FileType, Object)} if it has not yet
	 * been saved.
	 * <p>
	 * If the given dataflow's last opened/saved file type was unknown (opened
	 * with {@link #newDataflow()} or {@link #openDataflow(Dataflow)}), return
	 * <code>null</code>.
	 * </p>
	 * 
	 * @param dataflow
	 *            {@link Dataflow} which file is to be returned
	 * @return The last opened/saved {@link FileType} for the given dataflow, or
	 *         <code>null</code> if unknown.
	 */
	public abstract FileType getDataflowType(Dataflow dataflow);

	/**
	 * Get the list of currently open dataflows. This list of dataflows are
	 * typically displayed in the UI in the "Workflows" menu to allow switching
	 * the {@link #getCurrentDataflow() current dataflow}.
	 * 
	 * @return A copy of the {@link List} of open {@link Dataflow}s
	 */
	public abstract List<Dataflow> getOpenDataflows();

	/**
	 * Get a list of {@link FileFilter}s for supported {@link FileType}s that
	 * can be opened with any source class.
	 * 
	 * @return A {@link List} of {@link FileFilter}s supported by
	 *         {@link #openDataflow(FileType, Object)}
	 */
	public abstract List<FileFilter> getOpenFileFilters();

	/**
	 * Get a list of {@link FileFilter}s for supported {@link FileType}s that
	 * can be opened with given source class.
	 * 
	 * @param sourceClass
	 *            Source class that can be opened from
	 * @return A {@link List} of {@link FileFilter}s supported by
	 *         {@link #openDataflow(FileType, Object)}
	 */
	public abstract List<FileFilter> getOpenFileFilters(Class<?> sourceClass);

	/**
	 * Get a list of {@link FileFilter}s for supported {@link FileType}s that
	 * can be saved to any destination class.
	 * 
	 * @return A {@link List} of {@link FileFilter}s supported by
	 *         {@link #saveDataflow(Dataflow, FileType, Object, boolean)

	 */
	public abstract List<FileFilter> getSaveFileFilters();

	/**
	 * Get a list of {@link FileFilter}s for supported {@link FileType}s that
	 * can be saved to the given destination class.
	 * 
	 * @param destinationClass
	 *            Destination class that can be saved to
	 * @return A {@link List} of {@link FileFilter}s supported by
	 *         {@link #saveDataflow(Dataflow, FileType, Object, boolean)

	 */
	public abstract List<FileFilter> getSaveFileFilters(
			Class<?> destinationClass);
	
	/**
	 * Return <code>true</code> if the dataflow has been changed (through the
	 * {@link EditManager} or {@link #setDataflowChanged(Dataflow, boolean)})
	 * since last save.
	 * 
	 * @param dataflow
	 *            Dataflow which changed status is to be checked
	 * @return <code>true</code> if the dataflow has been changed since last
	 *         save.
	 */
	public abstract boolean isDataflowChanged(Dataflow dataflow);

	/**
	 * True if the given dataflow has been opened and is in
	 * {@link #getOpenDataflows()}.
	 * 
	 * @param dataflow
	 *            Dataflow to check
	 * @return <code>true</code> if dataflow is open
	 */
	public abstract boolean isDataflowOpen(Dataflow dataflow);

	/**
	 * Create and open a new, blank dataflow. The dataflow will not initially be
	 * marked as changed.
	 * <p>
	 * Listeners registered using {@link Observable#addObserver(Observer)} will
	 * be notified with an {@link OpenedDataflowEvent}.
	 * </p>
	 * <p>
	 * Note, if the dataflow is later changed, it will not be possible to save
	 * it to any original location using
	 * {@link #saveDataflow(Dataflow, boolean)}, only
	 * {@link #saveDataflow(Dataflow, FileType, Object, boolean)}.
	 * </p>
	 * 
	 * @return The newly opened blank {@link Dataflow}
	 */
	public abstract Dataflow newDataflow();

	/**
	 * Open a {@link Dataflow} instance that has been created outside the
	 * {@link FileManager}. The dataflow will not initially be marked as
	 * changed.
	 * <p>
	 * Listeners registered using {@link Observable#addObserver(Observer)} will
	 * be notified with an {@link OpenedDataflowEvent}.
	 * </p>
	 * <p>
	 * Note, if the dataflow is later changed, it will not be possible to save
	 * it to its original location using
	 * {@link #saveDataflow(Dataflow, boolean)}, only
	 * {@link #saveDataflow(Dataflow, FileType, Object, boolean)}.
	 * </p>
	 * <p>
	 * Instead of using this option it is recommended to create your own
	 * {@link FileType} and/or source type and a
	 * {@link DataflowPersistenceHandler} to implement save and/or reopen
	 * (revert).
	 * <p>
	 * If there is only one workflow open before opening this workflow, and it
	 * is an unchanged blank workflow, the blank workflow will be closed.
	 * </p>
	 * 
	 * @param dataflow
	 *            {@link Dataflow} instance that is to be added as an open
	 *            dataflow
	 */
	public abstract void openDataflow(Dataflow dataflow);

	/**
	 * Open a dataflow from a source. The dataflow will not initially be marked
	 * as changed.
	 * <p>
	 * The file manager will find implementations of the SPI
	 * {@link DataflowPersistenceHandler} to perform the save for the given file
	 * type and destination class.
	 * </p>
	 * <p>
	 * Listeners registered using {@link Observable#addObserver(Observer)} will
	 * be notified with an {@link OpenedDataflowEvent}.
	 * </p>
	 * <p>
	 * If there is only one workflow open before opening this workflow, and it
	 * is an unchanged blank workflow, the blank workflow will be closed.
	 * </p>
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
	 * 
	 * @return The opened {@link Dataflow}.
	 * @throws OpenException
	 *             If there was no matching DataflowPersistenceHandler found or
	 *             the source could not be opened for any other reason, such as
	 *             IO errors or syntax errors.
	 */
	public abstract Dataflow openDataflow(FileType fileType, Object source)
			throws OpenException;

	/**
	 * Save the dataflow to the last saved destination and FileType from
	 * {@link #saveDataflow(Dataflow, FileType, Object, boolean)} or the last
	 * opened source and FileType from {@link #openDataflow(FileType, Object)}.
	 * </p>
	 * <p>
	 * Listeners registered using {@link Observable#addObserver(Observer)} will
	 * be notified with an {@link SavedDataflowEvent}.
	 * </p>
	 * 
	 * @param dataflow
	 *            Dataflow to save. Dataflow must have been opened with
	 *            {@link #openDataflow(FileType, Object)} or saved using
	 *            {@link #saveDataflow(Dataflow, FileType, Object, boolean)}.
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
	public abstract void saveDataflow(Dataflow dataflow, boolean failOnOverwrite)
			throws SaveException, OverwriteException;

	/**
	 * Save the dataflow to the given destination using the given filetype.
	 * <p>
	 * The file manager will find implementations of the SPI
	 * {@link DataflowPersistenceHandler} to perform the save for the given file
	 * type and destination class.
	 * </p>
	 * <p>
	 * Listeners registered using {@link Observable#addObserver(Observer)} will
	 * be notified with an {@link SavedDataflowEvent}.
	 * </p>
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
	public abstract void saveDataflow(Dataflow dataflow, FileType fileType,
			Object destination, boolean failOnOverwrite) throws SaveException,
			OverwriteException;

	/**
	 * Set the current dataflow to the one provided.
	 * <p>
	 * The current dataflow can be retrieved using {@link #getCurrentDataflow()}
	 * . Note that opening a dataflow will normally also set it as the current
	 * dataflow.
	 * </p>
	 * <p>
	 * Listeners registered using {@link Observable#addObserver(Observer)} will
	 * be notified with an {@link SetCurrentDataflowEvent}.
	 * </p>
	 * <p>
	 * Note, the dataflow must already be open. If this is not the case, use one
	 * of the openDataflow() methods or
	 * {@link #setCurrentDataflow(Dataflow, boolean)}.
	 * 
	 * @see #setCurrentDataflow(Dataflow, boolean)
	 * @param dataflow
	 *            {@link Dataflow} to be made current
	 */
	public abstract void setCurrentDataflow(Dataflow dataflow);

	/**
	 * Set the current dataflow to the one provided.
	 * <p>
	 * The current dataflow can be retrieved using {@link #getCurrentDataflow()}
	 * . Note that opening a dataflow will normally also set it as the current
	 * dataflow.
	 * </p>
	 * <p>
	 * Listeners registered using {@link Observable#addObserver(Observer)} will
	 * be notified with an {@link SetCurrentDataflowEvent}.
	 * </p>
	 * <p>
	 * Unless <code>openIfNeeded</code> is <code>true</code>, the dataflow must
	 * already be open.
	 * 
	 * @see #setCurrentDataflow(Dataflow, boolean)
	 * @param dataflow
	 *            {@link Dataflow} to be made current
	 * @param openIfNeeded
	 *            If <code>true</code>, open the dataflow if needed
	 */
	public abstract void setCurrentDataflow(Dataflow dataflow,
			boolean openIfNeeded);

	/**
	 * Set a dataflow as changed or not. This changes the value returned by
	 * {@link #isDataflowChanged(Dataflow)}.
	 * <p>
	 * This method can be used if the dataflow has been changed outside the
	 * {@link EditManager}.
	 * </p>
	 * 
	 * @param dataflow
	 *            Dataflow which is to be marked
	 * @param isChanged
	 *            <code>true</code> if the dataflow is to be marked as changed,
	 *            <code>false</code> if it is to be marked as not changed.
	 */
	public abstract void setDataflowChanged(Dataflow dataflow, boolean isChanged);

	/**
	 * Returns the canonical form of the source where the dataflow was opened
	 * from or saved to. The code for this method was devised based on
	 * {@link net.sf.taverna.t2.workbench.file.impl.T2DataflowOpener#openDataflow(FileType fileType, Object source)}.
	 */
	public static Object getCanonical(Object source)
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

}
