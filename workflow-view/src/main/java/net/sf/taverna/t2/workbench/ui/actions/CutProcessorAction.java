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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.workbench.design.actions.RemoveProcessorAction;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.utils.Tools;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;

import org.apache.log4j.Logger;

/**
 * Action for copying a processor.
 *
 * @author Alan R Williams
 */
public class CutProcessorAction extends AbstractAction {

	private static Logger logger = Logger.getLogger(CutProcessorAction.class);

	private Processor processor;

	private Dataflow dataflow;

	private Component component;

	public CutProcessorAction(Dataflow dataflow, Processor processor, Component component) {
		this.dataflow = dataflow;
		this.processor = processor;
		this.component = component;
		putValue(SMALL_ICON, WorkbenchIcons.cutIcon);
		putValue(NAME, "Cut");		
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
	}

	public void actionPerformed(ActionEvent e) {
			WorkflowView.cutProcessor(dataflow, processor, component);
	}

}
