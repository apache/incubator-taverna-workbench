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

import java.util.Date;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * Information about a WorkflowBundle that has been opened by the
 * {@link FileManager}.
 * <p>
 * This class, or a subclass of it, is used by
 * {@link DataflowPersistenceHandler}s to keep information about where a
 * {@link WorkflowBundle} came from or where it was saved to.
 * 
 * @author Stian Soiland-Reyes
 */
public class DataflowInfo {
	private final FileType fileType;
	private final WorkflowBundle worflowBundle;
	private final Date lastModified;
	private final Object canonicalSource;

	public DataflowInfo(FileType fileType, Object canonicalSource,
			WorkflowBundle worflowBundle, Date lastModified) {
		this.fileType = fileType;
		this.canonicalSource = canonicalSource;
		this.worflowBundle = worflowBundle;
		this.lastModified = lastModified;
	}

	public DataflowInfo(FileType fileType, Object canonicalSource,
			WorkflowBundle worflowBundle) {
		this(fileType, canonicalSource, worflowBundle, null);
	}

	/**
	 * Return the canonical source of where the WorkflowBundle was opened from
	 * or saved to.
	 * <p>
	 * This is not necessarily the source provided to
	 * {@link FileManager#openDataflow(FileType, Object)} or
	 * {@link FileManager#saveDataflow(WorkflowBundle, FileType, Object, boolean)}
	 * , but it's canonical version.
	 * <p>
	 * For instance, if a WorkflowBundle was opened from a
	 * File("relative/something.wfbundle) this canonical source would resolve
	 * the relative path.
	 * 
	 * @return
	 */
	public Object getCanonicalSource() {
		return canonicalSource;
	}

	/**
	 * Return the WorkflowBundle that is open.
	 * 
	 * @return The open WorkflowBundle
	 */
	public WorkflowBundle getDataflow() {
		return worflowBundle;
	}

	/**
	 * Get the last modified {@link Date} of the source at the time when it was
	 * opened/saved.
	 * <p>
	 * It is important that this value is checked on creation time, and not on
	 * demand.
	 * 
	 * @return The {@link Date} of the source/destination's last modified
	 *         timestamp, or <code>null</code> if unknown.
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * The {@link FileType} of this {@link WorkflowBundle} serialisation used
	 * for opening/saving.
	 * 
	 * @return The {@link FileType}, for instance
	 *         {@link net.sf.taverna.t2.workbench.file.impl.WorkflowBundleFileType}
	 */
	public FileType getFileType() {
		return fileType;
	}
}
