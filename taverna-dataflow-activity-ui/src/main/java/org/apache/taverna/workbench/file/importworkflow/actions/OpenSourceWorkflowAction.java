package org.apache.taverna.workbench.file.importworkflow.actions;
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

/**
 * @author David Withers
 */
@SuppressWarnings("serial")
public abstract class OpenSourceWorkflowAction extends AbstractAction {

	private static Logger logger = Logger.getLogger(OpenSourceWorkflowAction.class);

	private static final String OPEN_WORKFLOW = "Open workflow...";

	protected FileManager fileManager;

	public OpenSourceWorkflowAction(FileManager fileManager) {
		super(OPEN_WORKFLOW, WorkbenchIcons.openIcon);
		this.fileManager = fileManager;
	}

	public void actionPerformed(ActionEvent e) {
		final Component parentComponent;
		if (e.getSource() instanceof Component) {
			parentComponent = (Component) e.getSource();
		} else {
			parentComponent = null;
		}
		openWorkflows(parentComponent);
	}

	public abstract void openWorkflows(Component parentComponent, File[] files);

	/**
	 * Pop up an Open-dialogue to select one or more workflow files to open.
	 *
	 * @param parentComponent
	 *            The UI parent component to use for pop up dialogues
	 * @param openCallback
	 *            An {@link OpenCallback} to be called during the file opening.
	 *            The callback will be invoked for each file that has been
	 *            opened, as file opening happens in a separate thread that
	 *            might execute after the return of this method.
	 * @return <code>false</code> if no files were selected or the dialogue was
	 *         cancelled, or <code>true</code> if the process of opening one or
	 *         more files has been started.
	 */
	public boolean openWorkflows(final Component parentComponent) {
		JFileChooser fileChooser = new JFileChooser();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs.get("currentDir", System.getProperty("user.home"));
		fileChooser.setDialogTitle(OPEN_WORKFLOW);

		fileChooser.resetChoosableFileFilters();
		fileChooser.setAcceptAllFileFilterUsed(false);
		List<FileFilter> fileFilters = fileManager.getOpenFileFilters();
		if (fileFilters.isEmpty()) {
			logger.warn("No file types found for opening workflow");
			JOptionPane
					.showMessageDialog(parentComponent,
							"No file types found for opening workflow.", "Error",
							JOptionPane.ERROR_MESSAGE);
			return false;
		}
		for (FileFilter fileFilter : fileFilters) {
			fileChooser.addChoosableFileFilter(fileFilter);
		}

		fileChooser.setFileFilter(fileFilters.get(0));

		fileChooser.setCurrentDirectory(new File(curDir));
		fileChooser.setMultiSelectionEnabled(true);

		int returnVal = fileChooser.showOpenDialog(parentComponent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			prefs.put("currentDir", fileChooser.getCurrentDirectory().toString());
			final File[] selectedFiles = fileChooser.getSelectedFiles();
			if (selectedFiles.length == 0) {
				logger.warn("No files selected");
				return false;
			}
			new FileOpenerThread(parentComponent, selectedFiles).start();
			return true;
		}
		return false;
	}

	private final class FileOpenerThread extends Thread {
		private final File[] files;
		private final Component parentComponent;

		private FileOpenerThread(Component parentComponent, File[] selectedFiles) {
			super("Opening workflows(s) " + Arrays.asList(selectedFiles));
			this.parentComponent = parentComponent;
			this.files = selectedFiles;
		}

		@Override
		public void run() {
			openWorkflows(parentComponent, files);
		}
	}

}
