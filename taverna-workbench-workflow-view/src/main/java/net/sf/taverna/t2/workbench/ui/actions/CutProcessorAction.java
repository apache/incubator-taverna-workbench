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
package net.sf.taverna.t2.workbench.ui.actions;

import static java.awt.event.KeyEvent.VK_C;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.cutIcon;
import static net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView.cutProcessor;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;

/**
 * Action for copying a processor.
 * 
 * @author Alan R Williams
 */
@SuppressWarnings("serial")
public class CutProcessorAction extends AbstractAction {
	private Processor processor;
	private Workflow dataflow;
	private Component component;

	private final EditManager editManager;
	private final SelectionManager selectionManager;

	public CutProcessorAction(Workflow dataflow, Processor processor,
			Component component, EditManager editManager,
			SelectionManager selectionManager) {
		this.dataflow = dataflow;
		this.processor = processor;
		this.component = component;
		this.editManager = editManager;
		this.selectionManager = selectionManager;
		putValue(SMALL_ICON, cutIcon);
		putValue(NAME, "Cut");
		putValue(MNEMONIC_KEY, VK_C);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		cutProcessor(dataflow, processor, component, editManager,
				selectionManager);
	}
}
