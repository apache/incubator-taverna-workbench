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
import java.net.URL;
import java.util.Collection;

import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * An {@link SPIRegistry SPI} of a handler for opening or saving dataflows from
 * the {@link FileManager}.
 * <p>
 * </p>
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public interface DataflowPersistenceHandler {

	/**
	 * A collection of supported file types for
	 * {@link #openDataflow(FileType, Object)}, or an empty collection if
	 * opening is not supported by this handler.
	 * 
	 * @return A collection of supported {@link FileType}s for opening.
	 */
	public Collection<FileType> getOpenFileTypes();

	/**
	 * A collection of supported source classes for
	 * {@link #openDataflow(FileType, Object)}, or an empty collection if
	 * opening is not supported by this handler.
	 * <p>
	 * For example, a handler that supports sources opened from a {@link File}
	 * and {@link URL} could return
	 * <code>Arrays.asList(File.class, URL.class)</code>
	 * 
	 * @return A collection of supported {@link Class}es of the open source
	 *         types.
	 */
	public Collection<Class<?>> getOpenSourceTypes();

	/**
	 * A collection of supported destination classes for
	 * {@link #saveDataflow(Dataflow, FileType, Object)}, or an empty collection
	 * if saving is not supported by this handler.
	 * <p>
	 * For example, a handler that supports saving to destinations that are
	 * instances of a {@link File} could return
	 * <code>Arrays.asList(File.class)</code>
	 * 
	 * @return A collection of supported {{@link Class}es of the save
	 *         destination types.
	 */
	public Collection<Class<?>> getSaveDestinationTypes();

	/**
	 * A collection of supported file types for
	 * {@link #saveDataflow(Dataflow, FileType, Object)}, or an empty collection
	 * if saving is not supported by this handler.
	 * 
	 * @return A collection of supported {@link FileType}s for saving.
	 */
	public Collection<FileType> getSaveFileTypes();

	/**
	 * Open a dataflow from a source containing a dataflow of the given
	 * {@link FileType}.
	 * <p>
	 * The {@link FileType} will be equal to one of the types from
	 * {@link #getOpenFileTypes()}, and the source class will be one that is
	 * assignable to one of the classes from {@link #getOpenSourceTypes()}.
	 * 
	 * @param fileType
	 *            {@link FileType} determining which serialisation method has
	 *            been used
	 * @param source
	 *            Source for reading the Dataflow
	 * @return {@link DataflowInfo} describing the opened dataflow, including
	 *         the dataflow itself
	 * @throws OpenException
	 *             If the dataflow could not be read, parsed or opened for any
	 *             reason.
	 */
	public DataflowInfo openDataflow(FileType fileType, Object source)
			throws OpenException;

	/**
	 * Save a dataflow to a destination of the given {@link FileType}.
	 * <p>
	 * The {@link FileType} will be equal to one of the types from
	 * {@link #getSaveFileTypes()}, and the destination class will be one that
	 * is assignable to one of the classes from
	 * {@link #getSaveDestinationTypes()}.
	 * 
	 * @param dataflow
	 *            {@link Dataflow} to be saved
	 * @param fileType
	 *            {@link FileType} determining which serialisation method to use
	 * @param destination
	 *            Destination for writing the Dataflow
	 * @return {@link DataflowInfo} describing the saved dataflow, including the
	 *         dataflow itself
	 * @throws OpenException
	 *             If the dataflow could not be read, parsed or opened for any
	 *             reason.
	 */
	public DataflowInfo saveDataflow(Dataflow dataflow, FileType fileType,
			Object destination) throws SaveException;

	/**
	 * Return <code>true</code> if a call to
	 * {@link #saveDataflow(Dataflow, FileType, Object)} would overwrite the
	 * destination, and the destination is different from last
	 * {@link #openDataflow(FileType, Object)} or
	 * {@link #saveDataflow(Dataflow, FileType, Object)} of the given dataflow.
	 * 
	 * @param dataflow
	 *            {@link Dataflow} that is to be saved
	 * @param fileType
	 *            {@link FileType} for saving dataflow
	 * @param destination
	 *            destination for writing dataflow
	 * @param lastDataflowInfo
	 *            last provided {@link DataflowInfo} returned by
	 *            {@link #openDataflow(FileType, Object)} or
	 *            {@link #saveDataflow(Dataflow, FileType, Object)}. (but not
	 *            necessarily from this handler)
	 * @return <code>true</code> if the save would overwrite
	 */
	public boolean wouldOverwriteDataflow(Dataflow dataflow, FileType fileType,
			Object destination, DataflowInfo lastDataflowInfo);
}
