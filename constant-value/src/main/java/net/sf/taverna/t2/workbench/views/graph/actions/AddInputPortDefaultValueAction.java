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
package net.sf.taverna.t2.workbench.views.graph.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.workbench.design.actions.DataflowEditAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

/**
 * Action for adding a default value to an input port of a processor.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class AddInputPortDefaultValueAction extends DataflowEditAction {

	private static Logger logger = Logger.getLogger(AddInputPortDefaultValueAction.class);

	private ActivityInputPort inputPort;
	
	public AddInputPortDefaultValueAction(Dataflow dataflow, ActivityInputPort inputPort, Component component) {
		super(dataflow, component);
		this.inputPort = inputPort;
		putValue(SMALL_ICON, WorkbenchIcons.inputValueIcon);
		putValue(NAME, "Set constant value");		
	}
	
	public void actionPerformed(ActionEvent e) {
		try {
			String defaultValue = JOptionPane.showInputDialog(component,"Enter string value",null);
			if (defaultValue != null) {

				// List of all edits to be done
				List<Edit<?>> editList = new ArrayList<Edit<?>>();
				
				// Create new string constant activity with the given default value
				StringConstantActivity strConstActivity = new StringConstantActivity();
				StringConstantConfigurationBean strConstConfBean = new StringConstantConfigurationBean();
				strConstConfBean.setValue(defaultValue);
				editList.add(edits.getConfigureActivityEdit(strConstActivity, strConstConfBean));
				
				// Create new string constant processor - also check for duplicate processor names
				String suggestedProcessorName = inputPort.getName() +"_value";
				HashSet<String> procesorNames = new HashSet<String>();
				for (Processor processor : dataflow.getProcessors()) {
					procesorNames.add(processor.getLocalName());
				}
				int counter = 1;
				String base = suggestedProcessorName;
				while (procesorNames.contains(suggestedProcessorName)){
						suggestedProcessorName = base + "_" + counter++;
				}
				Processor strConstProcessor = edits.createProcessor(suggestedProcessorName);				
				
				// Set the processor's activity
				Edit<Processor> processorEdit = edits.getAddActivityEdit(strConstProcessor, strConstActivity);
				editList.add(processorEdit);				

				// Create the output port for string constant processor to correspond to the
				// string constant activity's output port called "value"
				ProcessorOutputPort strConstProcessorOutputPort =
						edits.createProcessorOutputPort(strConstProcessor, "value", 0, 0); // this port is the source of the datalink
				editList.add(edits.getAddProcessorOutputPortEdit(strConstProcessor, strConstProcessorOutputPort));
				editList.add(edits.getAddActivityOutputPortMappingEdit(strConstActivity, "value", "value"));
				
				editList.add(edits.getDefaultDispatchStackEdit(strConstProcessor));
				editList.add(edits.getAddProcessorEdit(dataflow, strConstProcessor));
				
				// Create the input port for the sink (i.e. target) processor to correspond to the
				// passed activity input port
				HashSet<Processor> processorsWithInputPort = (HashSet<Processor>) Tools.getProcessorsWithActivityInputPort(dataflow, inputPort);
				Processor sinkProcessor = (Processor) (processorsWithInputPort.toArray()[0]);
				ProcessorInputPort sinkProcessorInputPort =
					edits.createProcessorInputPort(sinkProcessor, inputPort.getName(), inputPort.getDepth()); // this port is the sink of the datalink
				Activity<?> sinkActivity = null;
				for (Activity<?> procActivity : sinkProcessor.getActivityList()){
					if (procActivity.getInputPorts().contains(inputPort)) { // found it
						sinkActivity = procActivity;
						break;
					}
				}
				editList.add(edits.getAddProcessorInputPortEdit(sinkProcessor, sinkProcessorInputPort));
				editList.add(edits.getAddActivityInputPortMappingEdit(sinkActivity, inputPort.getName(), inputPort.getName()));

				// Add a data link between the string constant processor's output port 
				// and the processor containing the passed inputPort.
				Datalink datalink = edits.createDatalink(strConstProcessorOutputPort,sinkProcessorInputPort);
				editList.add(edits.getConnectDatalinkEdit(datalink));

				EditManager.getInstance().doDataflowEdit(dataflow,  new CompoundEdit(editList));

			}
		} catch (EditException ex) {
			logger.error("Adding default value for input port failed", ex);
		}
	}

}

