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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;
import net.sf.taverna.t2.workbench.design.ui.ProcessorPanel;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.apache.log4j.Logger;

/**
 * Action for renaming a processor.
 * 
 * @author David Withers
 */
public class RenameProcessorAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RenameProcessorAction.class);

	private Processor processor;

	public RenameProcessorAction(Dataflow dataflow, Processor processor, Component component) {
		super(dataflow, component);
		this.processor = processor;
		putValue(SMALL_ICON, WorkbenchIcons.renameIcon);
		putValue(NAME, "Rename service...");		
	}

	public void actionPerformed(ActionEvent e) {
		try {
			Set<String> usedProcessors = new HashSet<String>();
			for (Processor usedProcessor : dataflow.getProcessors()) {
				if (!usedProcessor.getLocalName().equals(processor.getLocalName())) {
					usedProcessors.add(usedProcessor.getLocalName());
				}
			}

			ProcessorPanel inputPanel = new ProcessorPanel();
			
			ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
					"Rename service", inputPanel);
			vuid.addTextComponentValidation(inputPanel.getProcessorNameField(),
					"Set the service name.", usedProcessors,
					"Duplicate service.", "[\\p{L}\\p{Digit}_.]+",
					"Invalid service name.");
			vuid.setSize(new Dimension(400, 200));

			inputPanel.setProcessorName(processor.getLocalName());

			if (vuid.show(component)) {
				String processorName = inputPanel.getProcessorName();
				editManager.doDataflowEdit(dataflow, edits.getRenameProcessorEdit(processor, processorName));
			}
		
		} catch (EditException e1) {
			logger.debug("Rename service (processor) failed", e1);
		}
	}

}
