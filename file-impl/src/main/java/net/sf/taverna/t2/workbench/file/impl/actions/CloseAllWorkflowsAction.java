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
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_L;
import static java.awt.event.KeyEvent.VK_W;
import static javax.swing.KeyStroke.getKeyStroke;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.closeAllIcon;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;

@SuppressWarnings("serial")
public class CloseAllWorkflowsAction extends AbstractAction {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CloseWorkflowAction.class);
	private static final String CLOSE_ALL_WORKFLOWS = "Close all workflows";
	private FileManager fileManager;
	private CloseWorkflowAction closeWorkflowAction;

	public CloseAllWorkflowsAction(EditManager editManager, FileManager fileManager) {
		super(CLOSE_ALL_WORKFLOWS, closeAllIcon);
		this.fileManager = fileManager;
		closeWorkflowAction = new CloseWorkflowAction(editManager, fileManager);
		putValue(
				ACCELERATOR_KEY,
				getKeyStroke(VK_W, getDefaultToolkit().getMenuShortcutKeyMask()
						| SHIFT_DOWN_MASK));
		putValue(MNEMONIC_KEY, VK_L);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Component parentComponent = null;
		if (event.getSource() instanceof Component)
			parentComponent = (Component) event.getSource();
		closeAllWorkflows(parentComponent);
	}

	public boolean closeAllWorkflows(Component parentComponent) {
		// Close in reverse so we can save nested workflows first
		List<WorkflowBundle> workflowBundles = fileManager.getOpenDataflows();

		Collections.reverse(workflowBundles);

		for (WorkflowBundle workflowBundle : workflowBundles) {
			boolean success = closeWorkflowAction.closeWorkflow(
					parentComponent, workflowBundle);
			if (!success)
				return false;
		}
		return true;
	}
}
