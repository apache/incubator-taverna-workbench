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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;

/**
 * Action for removing an input port from the dataflow.
 *
 * @author David Withers
 */
public class RemoveDataflowInputPortAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RemoveDataflowInputPortAction.class);

	private DataflowInputPort port;

	public RemoveDataflowInputPortAction(Dataflow dataflow, DataflowInputPort port, Component component) {
		super(dataflow, component);
		this.port = port;
		putValue(SMALL_ICON, WorkbenchIcons.deleteIcon);
		putValue(NAME, "Delete workflow input");		
	}

	public void actionPerformed(ActionEvent e) {
		try {
			Set<? extends Datalink> datalinks = port.getInternalOutputPort().getOutgoingLinks();
			if (datalinks.isEmpty()) {
				editManager.doDataflowEdit(dataflow, edits.getRemoveDataflowInputPortEdit(dataflow, port));
			} else {
				List<Edit<?>> editList = new ArrayList<Edit<?>>();
				for (Datalink datalink : datalinks) {
					editList.add(Tools.getDisconnectDatalinkAndRemovePortsEdit(datalink));
				}
				editList.add(edits.getRemoveDataflowInputPortEdit(dataflow, port));
				editManager.doDataflowEdit(dataflow, new CompoundEdit(editList));
			}			
			dataflowSelectionModel.removeSelection(port);
		} catch (EditException e1) {
			logger.debug("Delete dataflow input port failed", e1);
		}
	}

}
