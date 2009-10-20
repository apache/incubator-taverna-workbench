/*******************************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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
package net.sf.taverna.t2.activities.stringconstant.menu;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.activities.stringconstant.servicedescriptions.StringConstantTemplateService;
import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.utils.Tools;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;

/**
 * An action to add a string constant activity + a wrapping processor to the workflow.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class AddStringConstantTemplateAction extends AbstractContextualMenuAction {

	private static final URI serviceTemplatesSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/serviceTemplates");
	
	private static Logger logger = Logger.getLogger(AddStringConstantTemplateAction.class);

	public AddStringConstantTemplateAction(){
		super(serviceTemplatesSection, 10);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof Dataflow;
	}
	
	@Override
	protected Action createAction() {
		AbstractAction action = new AbstractAction("Add string constant", ActivityIconManager.getInstance()
				.iconForActivity(new StringConstantActivity())){

					public void actionPerformed(ActionEvent e) {
						
						Dataflow workflow = FileManager.getInstance().getCurrentDataflow();
						
						WorkflowView.importServiceDescription(StringConstantTemplateService.getServiceDescription(),
								false);
					
					}
			
		};
		return action;
	}

}

