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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.merge;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;

import org.apache.log4j.Logger;

/**
 * Configuration action for a {@link Merge}. This action changes the order of
 * merge's incoming ports.
 * 
 * @author Alex Nenadic
 * 
 */
@SuppressWarnings("serial")
public class MergeConfigurationAction extends AbstractAction {

	private static Logger logger = Logger
	.getLogger(MergeConfigurationAction.class);

	private Merge merge;
	private List<MergeInputPort> reorderedInputPortsList;

	MergeConfigurationAction(Merge merge,
			List<MergeInputPort> reorderedInputPortsList) {
		super();
		this.merge = merge;
		this.reorderedInputPortsList = reorderedInputPortsList;
	}

	public void actionPerformed(ActionEvent e) {

		Edits edits = EditsRegistry.getEdits();
		Edit<?> reorderMergeInputPortsEdit = edits
				.getReorderMergeInputPortsEdit(merge, reorderedInputPortsList);

		Dataflow currentDataflow = FileManager.getInstance()
				.getCurrentDataflow();

		try {
			EditManager.getInstance().doDataflowEdit(currentDataflow,
					reorderMergeInputPortsEdit);
		} catch (IllegalStateException ex1) {
			logger.error("Could not configure merge", ex1);
		} catch (EditException ex2) {
			logger.error("Could not configure merge", ex2);
		}

	}

}
