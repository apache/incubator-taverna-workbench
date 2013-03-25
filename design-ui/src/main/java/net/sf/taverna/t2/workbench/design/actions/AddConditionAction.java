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

import javax.swing.Icon;

import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.edits.EditException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workflow.edits.AddChildEdit;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.core.BlockingControlLink;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.profiles.ProcessorBinding;

/**
 * Action for adding a condition to the dataflow.
 *
 * @author David Withers
 */
public class AddConditionAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(AddConditionAction.class);

	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	private Processor control;
	private Processor target;

	public AddConditionAction(Workflow dataflow, Processor control, Processor target,
			Component component, EditManager editManager,
			SelectionManager selectionManager, ActivityIconManager activityIconManager) {
		super(dataflow, component, editManager, selectionManager);
		this.control = control;
		this.target = target;
		ProcessorBinding processorBinding = scufl2Tools.processorBindingForProcessor(control, dataflow.getParent().getMainProfile());
		Icon activityIcon = activityIconManager.iconForActivity(processorBinding.getBoundActivity().getConfigurableType());
		putValue(SMALL_ICON, activityIcon);
		putValue(NAME, control.getName());
	}

	public void actionPerformed(ActionEvent event) {
		try {
			BlockingControlLink controlLink = new BlockingControlLink();
			controlLink.setUntilFinished(control);
			controlLink.setBlock(target);
			editManager.doDataflowEdit(dataflow.getParent(), new AddChildEdit<Workflow>(dataflow, controlLink));
		} catch (EditException e) {
			logger.debug("Create control link between '" + control.getName() + "' and '"
					+ target.getName() + "' failed");
		}

	}

}
