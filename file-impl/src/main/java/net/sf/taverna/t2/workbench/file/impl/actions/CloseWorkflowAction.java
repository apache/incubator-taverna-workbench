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

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.UnsavedException;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

public class CloseWorkflowAction extends AbstractAction {

	private static final SaveWorkflowAction saveWorkflowAction = new SaveWorkflowAction();
	private static Logger logger = Logger.getLogger(CloseWorkflowAction.class);
	private static final String CLOSE_WORKFLOW = "Close workflow";
	private FileManager fileManager = FileManager.getInstance();

	public CloseWorkflowAction() {
		super(CLOSE_WORKFLOW, WorkbenchIcons.closeIcon);
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

	public boolean closeWorkflow(Component parentComponent, Dataflow dataflow) {
		if (dataflow == null) {
			logger.warn("Attempted to close a null dataflow");
			return false;
		}

		try {
			return fileManager.closeDataflow(dataflow, true);
		} catch (UnsavedException e1) {
			fileManager.setCurrentDataflow(dataflow);
			String msg = "Do you want to save changes before closing the workflow "
					+ fileManager.getDataflowName(dataflow) + "?";
			int ret = JOptionPane.showConfirmDialog(parentComponent, msg,
					"Save workflow?", JOptionPane.YES_NO_CANCEL_OPTION);
			if (ret == JOptionPane.CANCEL_OPTION) {
				return false;
			} else if (ret == JOptionPane.NO_OPTION) {
				try {
					fileManager.closeDataflow(dataflow, false);
					return true;
				} catch (UnsavedException e2) {
					logger.error("Unexpected UnsavedException while "
							+ "closing workflow", e2);
					return false;
				}
			} else if (ret == JOptionPane.YES_OPTION) {
				boolean saved = saveWorkflowAction.saveDataflow(parentComponent, dataflow);
				if (! saved) {
					return false;
				}
				return closeWorkflow(parentComponent, dataflow);			
			} else { 
				logger.error("Unknown return from JOptionPane: " + ret);
				return false;
			}
		}
	}
}
