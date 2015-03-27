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

package org.apache.taverna.workbench.file.impl.actions;

import static java.awt.event.KeyEvent.VK_F6;
import static java.awt.event.KeyEvent.VK_S;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.NO_OPTION;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_CANCEL_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.saveAsIcon;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.FileType;
import org.apache.taverna.workbench.file.events.FileManagerEvent;
import org.apache.taverna.workbench.file.events.SetCurrentDataflowEvent;
import org.apache.taverna.workbench.file.exceptions.OverwriteException;
import org.apache.taverna.workbench.file.exceptions.SaveException;
import org.apache.taverna.workbench.file.impl.FileTypeFileFilter;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Workflow;

@SuppressWarnings("serial")
public class SaveWorkflowAsAction extends AbstractAction {
	private static final String SAVE_WORKFLOW_AS = "Save workflow as...";
	private static final String PREF_CURRENT_DIR = "currentDir";
	private static Logger logger = Logger.getLogger(SaveWorkflowAsAction.class);
	private FileManager fileManager;

	public SaveWorkflowAsAction(FileManager fileManager) {
		super(SAVE_WORKFLOW_AS, saveAsIcon);
		this.fileManager = fileManager;
		fileManager.addObserver(new FileManagerObserver());
		putValue(ACCELERATOR_KEY, getKeyStroke(VK_F6, 0));
		putValue(MNEMONIC_KEY, VK_S);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Component parentComponent = null;
		if (e.getSource() instanceof Component)
			parentComponent = (Component) e.getSource();
		WorkflowBundle workflowBundle = fileManager.getCurrentDataflow();
		if (workflowBundle == null) {
			showMessageDialog(parentComponent, "No workflow open yet",
					"No workflow to save", ERROR_MESSAGE);
			return;
		}
		saveCurrentDataflow(parentComponent);
	}

	public boolean saveCurrentDataflow(Component parentComponent) {
		WorkflowBundle workflowBundle = fileManager.getCurrentDataflow();
		return saveDataflow(parentComponent, workflowBundle);
	}

	private String determineFileName(final WorkflowBundle workflowBundle) {
		String result;
		Object source = fileManager.getDataflowSource(workflowBundle);
		String fileName = null;
		if (source instanceof File)
			fileName = ((File) source).getName();
		else if (source instanceof URL)
			fileName = ((URL) source).getPath();

		if (fileName != null) {
			int lastIndex = fileName.lastIndexOf(".");
			if (lastIndex > 0)
				fileName = fileName.substring(0, fileName.lastIndexOf("."));
			result = fileName;
		} else {
			Workflow mainWorkflow = workflowBundle.getMainWorkflow();
			if (mainWorkflow != null)
				result = mainWorkflow.getName();
			else
				result = workflowBundle.getName();
		}
		return result;
	}

	public boolean saveDataflow(Component parentComponent, WorkflowBundle workflowBundle) {
		fileManager.setCurrentDataflow(workflowBundle);
		JFileChooser fileChooser = new JFileChooser();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs
				.get(PREF_CURRENT_DIR, System.getProperty("user.home"));
		fileChooser.setDialogTitle(SAVE_WORKFLOW_AS);

		fileChooser.resetChoosableFileFilters();
		fileChooser.setAcceptAllFileFilterUsed(false);

		List<FileFilter> fileFilters = fileManager
				.getSaveFileFilters(File.class);
		if (fileFilters.isEmpty()) {
			logger.warn("No file types found for saving workflow "
					+ workflowBundle);
			showMessageDialog(parentComponent,
					"No file types found for saving workflow.", "Error",
					ERROR_MESSAGE);
			return false;
		}
		for (FileFilter fileFilter : fileFilters)
			fileChooser.addChoosableFileFilter(fileFilter);
		fileChooser.setFileFilter(fileFilters.get(0));
		fileChooser.setCurrentDirectory(new File(curDir));

		File possibleName = new File(determineFileName(workflowBundle));
		boolean tryAgain = true;
		while (tryAgain) {
			tryAgain = false;
			fileChooser.setSelectedFile(possibleName);
			int returnVal = fileChooser.showSaveDialog(parentComponent);
			if (returnVal == APPROVE_OPTION) {
				prefs.put(PREF_CURRENT_DIR, fileChooser.getCurrentDirectory()
						.toString());
				File file = fileChooser.getSelectedFile();
				FileTypeFileFilter fileFilter = (FileTypeFileFilter) fileChooser
						.getFileFilter();
				FileType fileType = fileFilter.getFileType();
				String extension = "." + fileType.getExtension();
				if (!file.getName().toLowerCase().endsWith(extension)) {
					String newName = file.getName() + extension;
					file = new File(file.getParentFile(), newName);
				}

				// TODO: Open in separate thread to avoid hanging UI
				try {
					try {
						fileManager.saveDataflow(workflowBundle, fileType,
								file, true);
						logger.info("Saved workflow " + workflowBundle + " to "
								+ file);
						return true;
					} catch (OverwriteException ex) {
						logger.info("File already exists: " + file);
						String msg = "Are you sure you want to overwrite existing file "
								+ file + "?";
						int ret = showConfirmDialog(parentComponent, msg,
								"File already exists", YES_NO_CANCEL_OPTION);
						if (ret == YES_OPTION) {
							fileManager.saveDataflow(workflowBundle, fileType,
									file, false);
							logger.info("Saved workflow " + workflowBundle
									+ " by overwriting " + file);
							return true;
						} else if (ret == NO_OPTION) {
							tryAgain = true;
							continue;
						} else {
							logger.info("Aborted overwrite of " + file);
							return false;
						}
					}
				} catch (SaveException ex) {
					logger.warn("Could not save workflow to " + file, ex);
					showMessageDialog(parentComponent,
							"Could not save workflow to " + file + ": \n\n"
									+ ex.getMessage(), "Warning",
							WARNING_MESSAGE);
					return false;
				}
			}
		}
		return false;
	}

	protected void updateEnabledStatus(WorkflowBundle workflowBundle) {
		setEnabled(workflowBundle != null);
	}

	private final class FileManagerObserver implements Observer<FileManagerEvent> {
		@Override
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			if (message instanceof SetCurrentDataflowEvent)
				updateEnabledStatus(((SetCurrentDataflowEvent) message)
						.getDataflow());
		}
	}
}
