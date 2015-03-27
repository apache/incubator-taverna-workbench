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

/**
 * A filetype to identify a way to (de)serialise a {@link WorkflowBundle} with
 * the {@link FileManager}.
 * <p>
 * Two filetypes are considered equal if they share an extension or mime type or
 * are the same instance.
 * 
 * @see net.sf.taverna.t2.workbench.file.impl.WorkflowBundleFileType
 * @author Stian Soiland-Reyes
 */
public abstract class FileType {
	public abstract String getExtension();

	public abstract String getMimeType();

	public abstract String getDescription();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof FileType))
			return false;
		FileType other = (FileType) obj;
		if (getMimeType() != null && other.getMimeType() != null)
			return getMimeType().equalsIgnoreCase(other.getMimeType());
		if (getExtension() != null && other.getExtension() != null)
			return getExtension().equalsIgnoreCase(other.getExtension());
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + getExtension().hashCode();
		hash = 31 * hash + getMimeType().hashCode();
		return hash;
	}
}
