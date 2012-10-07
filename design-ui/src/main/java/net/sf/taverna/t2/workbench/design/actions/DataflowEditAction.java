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

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.core.Workflow;

/**
 * Abstract superclass of dataflow edit actions.
 *
 * @author David Withers
 */
public abstract class DataflowEditAction extends AbstractAction {

	private static final long serialVersionUID = -1155192575675025091L;
	protected final DataflowSelectionManager dataflowSelectionManager;
	protected EditManager editManager;
	protected DataflowSelectionModel dataflowSelectionModel;
	protected Workflow dataflow;
	protected Component component;
	protected Scufl2Tools scufl2Tools = new Scufl2Tools();

	public DataflowEditAction(Workflow dataflow, Component component, EditManager editManager,
			DataflowSelectionManager dataflowSelectionManager) {
		this.dataflow = dataflow;
		this.component = component;
		this.editManager = editManager;
		this.dataflowSelectionManager = dataflowSelectionManager;
		dataflowSelectionModel = dataflowSelectionManager.getDataflowSelectionModel(dataflow.getParent());
	}

}
