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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
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
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OverwriteException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.file.impl.FileTypeFileFilter;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

public class SaveWorkflowAsAction extends AbstractAction {

	private final class ModelMapObserver implements Observer<ModelMapEvent> {
		public void notify(Observable<ModelMapEvent> sender,
				ModelMapEvent message) throws Exception {
			if (message.getModelName().equals(
					ModelMapConstants.CURRENT_DATAFLOW)) {
				Dataflow dataflow = (Dataflow) message.getNewModel();
				updateEnabledStatus(dataflow);
			}
		}
	}

	private static final String SAVE_WORKFLOW_AS = "Save workflow as...";

	private static Logger logger = Logger.getLogger(SaveWorkflowAsAction.class);

	private FileManager fileManager = FileManager.getInstance();

	private ModelMap modelMap = ModelMap.getInstance();

	public SaveWorkflowAsAction() {
		super(SAVE_WORKFLOW_AS, WorkbenchIcons.saveAsIcon);
		putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
		modelMap.addObserver(new ModelMapObserver());
		updateEnabledStatus((Dataflow) modelMap
				.getModel(ModelMapConstants.CURRENT_DATAFLOW));
	}

	public void actionPerformed(ActionEvent e) {
		Component parentComponent = null;
		if (e.getSource() instanceof Component) {
			parentComponent = (Component) e.getSource();
		}
		if (fileManager.getCurrentDataflow() == null) {
			JOptionPane.showMessageDialog(parentComponent,
					"No workflow open yet", "No workflow to save",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		saveCurrentDataflow(parentComponent);
	}

	public boolean saveCurrentDataflow(Component parentComponent) {
		Dataflow dataflow = fileManager.getCurrentDataflow();
		return saveDataflow(parentComponent, dataflow);
	}

	public boolean saveDataflow(Component parentComponent, Dataflow dataflow) {
		fileManager.setCurrentDataflow(dataflow);
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
			logger.warn("No file types found for saving workflow " + dataflow);
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

		boolean tryAgain = true;
		while (tryAgain) {
			tryAgain = false;
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
								.saveDataflow(dataflow, fileType, file, true);
						logger.info("Saved dataflow " + dataflow + " to "
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
							fileManager.saveDataflow(dataflow, fileType, file,
									false);
							logger.info("Saved dataflow " + dataflow
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

	protected void updateEnabledStatus(Dataflow dataflow) {
		if (dataflow == null) {
			setEnabled(false);
		} else {
			setEnabled(true);
		}
	}

}
