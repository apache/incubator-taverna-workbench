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
import net.sf.taverna.t2.workbench.edits.EditException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workflow.edits.RenameEdit;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;

/**
 * Action for renaming a processor.
 * 
 * @author David Withers
 */
public class RenameProcessorAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger
			.getLogger(RenameProcessorAction.class);

	private Processor processor;

	public RenameProcessorAction(Workflow dataflow, Processor processor,
			Component component, EditManager editManager,
			SelectionManager selectionManager) {
		super(dataflow, component, editManager, selectionManager);
		this.processor = processor;
		putValue(SMALL_ICON, WorkbenchIcons.renameIcon);
		putValue(NAME, "Rename service...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Set<String> usedProcessors = new HashSet<>();
		for (Processor usedProcessor : dataflow.getProcessors())
			if (!usedProcessor.getName().equals(processor.getName()))
				usedProcessors.add(usedProcessor.getName());

		ProcessorPanel inputPanel = new ProcessorPanel();

		ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
				"Rename service", inputPanel);
		vuid.addTextComponentValidation(inputPanel.getProcessorNameField(),
				"Set the service name.", usedProcessors, "Duplicate service.",
				"[\\p{L}\\p{Digit}_.]+", "Invalid service name.");
		vuid.setSize(new Dimension(400, 200));

		inputPanel.setProcessorName(processor.getName());

		try {
			if (vuid.show(component))
				changeProcessorName(inputPanel);
		} catch (EditException e1) {
			logger.debug("Rename service (processor) failed", e1);
		}
	}

	private void changeProcessorName(ProcessorPanel inputPanel)
			throws EditException {
		String processorName = inputPanel.getProcessorName();
		editManager.doDataflowEdit(dataflow.getParent(), new RenameEdit<>(
				processor, processorName));
	}
}
