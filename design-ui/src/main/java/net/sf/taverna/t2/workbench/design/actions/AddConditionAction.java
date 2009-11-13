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
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.apache.log4j.Logger;

/**
 * Action for adding a condition to the dataflow.
 *
 * @author David Withers
 */
public class AddConditionAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(AddConditionAction.class);
	
	private Processor control;
	private Processor target;

	public AddConditionAction(Dataflow dataflow, Processor control, Processor target, Component component) {
		super(dataflow, component);
		this.control = control;
		this.target = target;
		Activity<?> activity = control.getActivityList().get(0);
		Icon activityIcon = ActivityIconManager.getInstance().iconForActivity(activity);
		putValue(SMALL_ICON, activityIcon);
		putValue(NAME, control.getLocalName());		
	}

	public void actionPerformed(ActionEvent event) {
		try {
			editManager.doDataflowEdit(dataflow, edits.getCreateConditionEdit(control, target));
		} catch (EditException e) {
			logger.debug("Create control link between '" + control.getLocalName() + "' and '" + target.getLocalName() + "' failed");
		}

	}

}
