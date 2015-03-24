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
package net.sf.taverna.t2.workbench.design.actions;

import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.deleteIcon;

import java.awt.Component;
import java.awt.event.ActionEvent;

import net.sf.taverna.t2.workbench.edits.EditException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workflow.edits.RemoveDataLinkEdit;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.core.DataLink;
import org.apache.taverna.scufl2.api.core.Workflow;

/**
 * Action for removing a datalink from the dataflow.
 * 
 * @author David Withers
 */
@SuppressWarnings("serial")
public class RemoveDatalinkAction extends DataflowEditAction {
	private static final Logger logger = Logger.getLogger(RemoveDatalinkAction.class);

	private DataLink datalink;

	public RemoveDatalinkAction(Workflow dataflow, DataLink datalink,
			Component component, EditManager editManager,
			SelectionManager selectionManager) {
		super(dataflow, component, editManager, selectionManager);
		this.datalink = datalink;
		putValue(SMALL_ICON, deleteIcon);
		putValue(NAME, "Delete data link");
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		try {
			dataflowSelectionModel.removeSelection(datalink);
			editManager.doDataflowEdit(dataflow.getParent(),
					new RemoveDataLinkEdit(dataflow, datalink));
		} catch (EditException ex) {
			logger.debug("Delete data link failed", ex);
		}
	}
}
