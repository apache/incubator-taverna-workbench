package org.apache.taverna.workbench.views.results.saveactions;
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

import static java.nio.file.Files.copy;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.AbstractAction;

import org.apache.taverna.workbench.icons.WorkbenchIcons;
import org.apache.taverna.robundle.Bundles;

/**
 * Stores results to the file system.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class SaveAllResultsToFileSystem extends SaveAllResultsSPI {
	public SaveAllResultsToFileSystem(){
		super();
		putValue(NAME, "Save as directory");
		putValue(SMALL_ICON, WorkbenchIcons.saveAllIcon);
	}

	@Override
	public AbstractAction getAction() {
		return new SaveAllResultsToFileSystem();
	}

	/**
	 * Saves the result data as a file structure
	 * 
	 * @throws IOException
	 */
	@Override
	protected void saveData(File directory) throws IOException {
		if (directory.exists() && !directory.isDirectory())
			throw new IOException(directory.getName() + " is not a directory.");
		for (String portName : chosenReferences.keySet())
			writeToFileSystem(chosenReferences.get(portName), new File(
					directory, portName));
	}

	/**
	 * Write a specific object to the filesystem this has no access to metadata
	 * about the object and so is not particularly clever. A File object
	 * representing the file or directory that has been written is returned.
	 */
	public File writeToFileSystem(Path source, File destination)
			throws IOException {
		destination.mkdirs();
		if (isDirectory(source))
			Bundles.copyRecursively(source, destination.toPath(), REPLACE_EXISTING);
		else if (exists(source))
			copy(source, destination.toPath(), REPLACE_EXISTING);
		return destination;
	}

	@Override
	protected int getFileSelectionMode() {
		return DIRECTORIES_ONLY;
	}

	@Override
	protected String getFilter() {
		return null;
	}
}
