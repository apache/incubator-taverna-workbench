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
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

public class CloseAllWorkflowsAction extends AbstractAction {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CloseWorkflowAction.class);
	private static final String CLOSE_ALL_WORKFLOWS = "Close all workflows";
	private FileManager fileManager = FileManager.getInstance();
	private CloseWorkflowAction closeWorkflowAction = new CloseWorkflowAction();

	public CloseAllWorkflowsAction() {
		super(CLOSE_ALL_WORKFLOWS, WorkbenchIcons.closeAllIcon);
		putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_W,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
						| InputEvent.SHIFT_DOWN_MASK));
	}

	public void actionPerformed(ActionEvent event) {
		Component parentComponent = null;
		if (event.getSource() instanceof Component) {
			parentComponent = (Component) event.getSource();
		}
		closeAllWorkflows(parentComponent);
	}

	public boolean closeAllWorkflows(Component parentComponent) {
		// Close in reverse so we can save nested workflows first
		List<Dataflow> dataflows = fileManager.getOpenDataflows();
		
		Collections.reverse(dataflows);

		for (Dataflow dataflow : dataflows) {
			boolean success = closeWorkflowAction.closeWorkflow(
					parentComponent, dataflow);
			if (!success) {
				return false;
			}
		}
		return true;
	}

}
