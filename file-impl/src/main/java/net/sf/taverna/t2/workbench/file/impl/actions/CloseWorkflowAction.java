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
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.UnsavedException;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;

public class CloseWorkflowAction extends AbstractAction {

	private static Logger logger = Logger.getLogger(CloseWorkflowAction.class);
	private static final String CLOSE_WORKFLOW = "Close workflow";
	private final SaveWorkflowAction saveWorkflowAction;
	private FileManager fileManager;

	public CloseWorkflowAction(EditManager editManager, FileManager fileManager) {
		super(CLOSE_WORKFLOW, WorkbenchIcons.closeIcon);
		this.fileManager = fileManager;
		saveWorkflowAction = new SaveWorkflowAction(editManager, fileManager);
		putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_W,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);

	}

	public void actionPerformed(ActionEvent e) {
		Component parentComponent = null;
		if (e.getSource() instanceof Component) {
			parentComponent = (Component) e.getSource();
		}
		closeWorkflow(parentComponent, fileManager.getCurrentDataflow());
	}

	public boolean closeWorkflow(Component parentComponent, WorkflowBundle workflowBundle) {
		if (workflowBundle == null) {
			logger.warn("Attempted to close a null workflow");
			return false;
		}

		try {
			return fileManager.closeDataflow(workflowBundle, true);
		} catch (UnsavedException e1) {
			fileManager.setCurrentDataflow(workflowBundle);
			String msg = "Do you want to save changes before closing the workflow "
					+ fileManager.getDataflowName(workflowBundle) + "?";
			int ret = JOptionPane.showConfirmDialog(parentComponent, msg,
					"Save workflow?", JOptionPane.YES_NO_CANCEL_OPTION);
			if (ret == JOptionPane.CANCEL_OPTION || ret == JOptionPane.CLOSED_OPTION) {
				return false;
			} else if (ret == JOptionPane.NO_OPTION) {
				try {
					fileManager.closeDataflow(workflowBundle, false);
					return true;
				} catch (UnsavedException e2) {
					logger.error("Unexpected UnsavedException while "
							+ "closing workflow", e2);
					return false;
				}
			} else if (ret == JOptionPane.YES_OPTION) {
				boolean saved = saveWorkflowAction.saveDataflow(parentComponent, workflowBundle);
				if (! saved) {
					return false;
				}
				return closeWorkflow(parentComponent, workflowBundle);
			} else {
				logger.error("Unknown return from JOptionPane: " + ret);
				return false;
			}
		}
	}
}
