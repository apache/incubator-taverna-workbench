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
package net.sf.taverna.t2.workbench.file.impl.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.SetCurrentDataflowEvent;
import net.sf.taverna.t2.workbench.file.exceptions.OverwriteException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.file.impl.FileTypeFileFilter;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Workflow;

public class SaveWorkflowAsAction extends AbstractAction {

	private static final String SAVE_WORKFLOW_AS = "Save workflow as...";

	private static Logger logger = Logger.getLogger(SaveWorkflowAsAction.class);

	private FileManager fileManager;

	public SaveWorkflowAsAction(FileManager fileManager) {
		super(SAVE_WORKFLOW_AS, WorkbenchIcons.saveAsIcon);
		this.fileManager = fileManager;
		fileManager.addObserver(new FileManagerObserver());
		putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
	}

	public void actionPerformed(ActionEvent e) {
		Component parentComponent = null;
		if (e.getSource() instanceof Component) {
			parentComponent = (Component) e.getSource();
		}
		WorkflowBundle workflowBundle = fileManager.getCurrentDataflow();
		if (workflowBundle == null) {
			JOptionPane.showMessageDialog(parentComponent,
					"No workflow open yet", "No workflow to save",
					JOptionPane.ERROR_MESSAGE);
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
		if (source instanceof File) {
			fileName = ((File) source).getName();

		} else if (source instanceof URL) {
			fileName = ((URL) source).getPath();
		}
		if (fileName != null) {
			int lastIndex = fileName.lastIndexOf(".");
				if (lastIndex > 0) {
					fileName = fileName.substring(0, fileName.lastIndexOf("."));
				}
			result = fileName;
		}
		else {
			Workflow mainWorkflow = workflowBundle.getMainWorkflow();
			if (mainWorkflow != null) {
				result = mainWorkflow.getName();
			} else {
				result = workflowBundle.getName();
			}
		}
		return result;
	}

	public boolean saveDataflow(Component parentComponent, WorkflowBundle workflowBundle) {
		fileManager.setCurrentDataflow(workflowBundle);
		JFileChooser fileChooser = new JFileChooser();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs
				.get("currentDir", System.getProperty("user.home"));
		fileChooser.setDialogTitle(SAVE_WORKFLOW_AS);

		fileChooser.resetChoosableFileFilters();
		fileChooser.setAcceptAllFileFilterUsed(false);

		List<FileFilter> fileFilters = fileManager
				.getSaveFileFilters(File.class);
		if (fileFilters.isEmpty()) {
			logger.warn("No file types found for saving workflow " + workflowBundle);
			JOptionPane.showMessageDialog(parentComponent,
					"No file types found for saving workflow.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		for (FileFilter fileFilter : fileFilters) {
			fileChooser.addChoosableFileFilter(fileFilter);
		}
		fileChooser.setFileFilter(fileFilters.get(0));

		fileChooser.setCurrentDirectory(new File(curDir));

		File possibleName = new File(determineFileName(workflowBundle));

		boolean tryAgain = true;
		while (tryAgain) {
			tryAgain = false;
			fileChooser.setSelectedFile(possibleName);
			int returnVal = fileChooser.showSaveDialog(parentComponent);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				prefs.put("currentDir", fileChooser.getCurrentDirectory()
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
						fileManager
								.saveDataflow(workflowBundle, fileType, file, true);
						logger.info("Saved workflow " + workflowBundle + " to "
								+ file);
						return true;
					} catch (OverwriteException ex) {
						logger.info("File already exists: " + file);
						String msg = "Are you sure you want to overwrite existing file "
								+ file + "?";
						int ret = JOptionPane.showConfirmDialog(
								parentComponent, msg, "File already exists",
								JOptionPane.YES_NO_CANCEL_OPTION);
						if (ret == JOptionPane.YES_OPTION) {
							fileManager.saveDataflow(workflowBundle, fileType, file,
									false);
							logger.info("Saved workflow " + workflowBundle
									+ " by overwriting " + file);
							return true;
						} else if (ret == JOptionPane.NO_OPTION) {
							tryAgain = true;
							continue;
						} else {
							logger.info("Aborted overwrite of " + file);
							return false;
						}
					}
				} catch (SaveException ex) {
					logger.warn("Could not save workflow to " + file, ex);
					JOptionPane.showMessageDialog(parentComponent,
							"Could not save workflow to " + file + ": \n\n"
									+ ex.getMessage(), "Warning",
							JOptionPane.WARNING_MESSAGE);
					return false;
				}
			}
		}
		return false;
	}

	protected void updateEnabledStatus(WorkflowBundle workflowBundle) {
		if (workflowBundle == null) {
			setEnabled(false);
		} else {
			setEnabled(true);
		}
	}

	private final class FileManagerObserver implements Observer<FileManagerEvent> {
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			if (message instanceof SetCurrentDataflowEvent){
				updateEnabledStatus(((SetCurrentDataflowEvent) message).getDataflow());
			}
		}
	}

}
