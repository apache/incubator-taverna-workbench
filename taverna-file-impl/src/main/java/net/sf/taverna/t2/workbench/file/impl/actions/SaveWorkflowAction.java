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

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.VK_S;
import static javax.swing.JOptionPane.NO_OPTION;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_CANCEL_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.saveIcon;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.edits.EditManager.AbstractDataflowEditEvent;
import org.apache.taverna.workbench.edits.EditManager.EditManagerEvent;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.events.FileManagerEvent;
import org.apache.taverna.workbench.file.events.SavedDataflowEvent;
import org.apache.taverna.workbench.file.events.SetCurrentDataflowEvent;
import org.apache.taverna.workbench.file.exceptions.OverwriteException;
import org.apache.taverna.workbench.file.exceptions.SaveException;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

@SuppressWarnings("serial")
public class SaveWorkflowAction extends AbstractAction {
	private static Logger logger = Logger.getLogger(SaveWorkflowAction.class);
	private static final String SAVE_WORKFLOW = "Save workflow";

	private final SaveWorkflowAsAction saveWorkflowAsAction;
	private EditManagerObserver editManagerObserver = new EditManagerObserver();
	private FileManager fileManager;
	private FileManagerObserver fileManagerObserver = new FileManagerObserver();

	public SaveWorkflowAction(EditManager editManager, FileManager fileManager) {
		super(SAVE_WORKFLOW, saveIcon);
		this.fileManager = fileManager;
		saveWorkflowAsAction = new SaveWorkflowAsAction(fileManager);
		putValue(
				ACCELERATOR_KEY,
				getKeyStroke(VK_S, getDefaultToolkit().getMenuShortcutKeyMask()));
		putValue(MNEMONIC_KEY, VK_S);
		editManager.addObserver(editManagerObserver);
		fileManager.addObserver(fileManagerObserver);
		updateEnabledStatus(fileManager.getCurrentDataflow());
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		Component parentComponent = null;
		if (ev.getSource() instanceof Component)
			parentComponent = (Component) ev.getSource();
		saveCurrentDataflow(parentComponent);
	}

	public boolean saveCurrentDataflow(Component parentComponent) {
		WorkflowBundle workflowBundle = fileManager.getCurrentDataflow();
		return saveDataflow(parentComponent, workflowBundle);
	}

	public boolean saveDataflow(Component parentComponent,
			WorkflowBundle workflowBundle) {
		if (!fileManager.canSaveWithoutDestination(workflowBundle))
			return saveWorkflowAsAction.saveDataflow(parentComponent,
					workflowBundle);

		try {
			try {
				fileManager.saveDataflow(workflowBundle, true);
				Object workflowBundleSource = fileManager
						.getDataflowSource(workflowBundle);
				logger.info("Saved workflow " + workflowBundle + " to "
						+ workflowBundleSource);
				return true;
			} catch (OverwriteException ex) {
				Object workflowBundleSource = fileManager
						.getDataflowSource(workflowBundle);
				logger.info("Workflow was changed on source: "
						+ workflowBundleSource);
				fileManager.setCurrentDataflow(workflowBundle);
				String msg = "Workflow destination " + workflowBundleSource
						+ " has been changed from elsewhere, "
						+ "are you sure you want to overwrite?";
				int ret = showConfirmDialog(parentComponent, msg,
						"Workflow changed", YES_NO_CANCEL_OPTION);
				if (ret == YES_OPTION) {
					fileManager.saveDataflow(workflowBundle, false);
					logger.info("Saved workflow " + workflowBundle
							+ " by overwriting " + workflowBundleSource);
					return true;
				} else if (ret == NO_OPTION) {
					// Pop up Save As instead to choose another name
					return saveWorkflowAsAction.saveDataflow(parentComponent,
							workflowBundle);
				} else {
					logger.info("Aborted overwrite of " + workflowBundleSource);
					return false;
				}
			}
		} catch (SaveException ex) {
			logger.warn("Could not save workflow " + workflowBundle, ex);
			showMessageDialog(parentComponent, "Could not save workflow: \n\n"
					+ ex.getMessage(), "Warning", WARNING_MESSAGE);
			return false;
		} catch (RuntimeException ex) {
			logger.warn("Could not save workflow " + workflowBundle, ex);
			showMessageDialog(parentComponent, "Could not save workflow: \n\n"
					+ ex.getMessage(), "Warning", WARNING_MESSAGE);
			return false;
		}
	}

	protected void updateEnabledStatus(WorkflowBundle workflowBundle) {
		setEnabled(workflowBundle != null
				&& fileManager.isDataflowChanged(workflowBundle));
	}

	private final class EditManagerObserver implements
			Observer<EditManagerEvent> {
		@Override
		public void notify(Observable<EditManagerEvent> sender,
				EditManagerEvent message) throws Exception {
			if (message instanceof AbstractDataflowEditEvent) {
				WorkflowBundle workflowBundle = ((AbstractDataflowEditEvent) message)
						.getDataFlow();
				if (workflowBundle == fileManager.getCurrentDataflow())
					updateEnabledStatus(workflowBundle);
			}
		}
	}

	private final class FileManagerObserver implements
			Observer<FileManagerEvent> {
		@Override
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			if (message instanceof SavedDataflowEvent)
				updateEnabledStatus(((SavedDataflowEvent) message)
						.getDataflow());
			else if (message instanceof SetCurrentDataflowEvent)
				updateEnabledStatus(((SetCurrentDataflowEvent) message)
						.getDataflow());
		}
	}
}
