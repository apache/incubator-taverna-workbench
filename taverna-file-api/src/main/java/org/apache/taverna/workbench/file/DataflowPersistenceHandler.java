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
package org.apache.taverna.workbench.file;

import java.io.File;
import java.net.URL;
import java.util.Collection;

import org.apache.taverna.workbench.file.exceptions.OpenException;
import org.apache.taverna.workbench.file.exceptions.SaveException;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * A handler for opening or saving {@link WorkflowBundle} from the
 * {@link FileManager}.
 * 
 * @author Stian Soiland-Reyes
 */
public interface DataflowPersistenceHandler {
	/**
	 * A collection of supported file types for
	 * {@link #openDataflow(FileType, Object)}, or an empty collection if
	 * opening is not supported by this handler.
	 * 
	 * @return A collection of supported {@link FileType}s for opening.
	 */
	Collection<FileType> getOpenFileTypes();

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
	Collection<Class<?>> getOpenSourceTypes();

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
	Collection<Class<?>> getSaveDestinationTypes();

	/**
	 * A collection of supported file types for
	 * {@link #saveDataflow(WorkflowBundle, FileType, Object)}, or an empty
	 * collection if saving is not supported by this handler.
	 * 
	 * @return A collection of supported {@link FileType}s for saving.
	 */
	Collection<FileType> getSaveFileTypes();

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
	 *            Source for reading the WorkflowBundle
	 * @return {@link DataflowInfo} describing the opened WorkflowBundle,
	 *         including the WorkflowBundle itself
	 * @throws OpenException
	 *             If the WorkflowBundle could not be read, parsed or opened for
	 *             any reason.
	 */
	DataflowInfo openDataflow(FileType fileType, Object source)
			throws OpenException;

	/**
	 * Save a WorkflowBundle to a destination of the given {@link FileType}.
	 * <p>
	 * The {@link FileType} will be equal to one of the types from
	 * {@link #getSaveFileTypes()}, and the destination class will be one that
	 * is assignable to one of the classes from
	 * {@link #getSaveDestinationTypes()}.
	 * 
	 * @param dataflow
	 *            {@link WorkflowBundle} to be saved
	 * @param fileType
	 *            {@link FileType} determining which serialisation method to use
	 * @param destination
	 *            Destination for writing the WorkflowBundle
	 * @return {@link DataflowInfo} describing the saved WorkflowBundle,
	 *         including the WorkflowBundle itself
	 * @throws OpenException
	 *             If the WorkflowBundle could not be read, parsed or opened for
	 *             any reason.
	 */
	DataflowInfo saveDataflow(WorkflowBundle dataflow, FileType fileType,
			Object destination) throws SaveException;

	/**
	 * Return <code>true</code> if a call to
	 * {@link #saveDataflow(WorkflowBundle, FileType, Object)} would overwrite
	 * the destination, and the destination is different from last
	 * {@link #openDataflow(FileType, Object)} or
	 * {@link #saveDataflow(WorkflowBundle, FileType, Object)} of the given
	 * dataflow.
	 * 
	 * @param dataflow
	 *            {@link WorkflowBundle} that is to be saved
	 * @param fileType
	 *            {@link FileType} for saving WorkflowBundle
	 * @param destination
	 *            destination for writing WorkflowBundle
	 * @param lastDataflowInfo
	 *            last provided {@link DataflowInfo} returned by
	 *            {@link #openDataflow(FileType, Object)} or
	 *            {@link #saveDataflow(WorkflowBundle, FileType, Object)}. (but
	 *            not necessarily from this handler)
	 * @return <code>true</code> if the save would overwrite
	 */
	boolean wouldOverwriteDataflow(WorkflowBundle dataflow, FileType fileType,
			Object destination, DataflowInfo lastDataflowInfo);
}
