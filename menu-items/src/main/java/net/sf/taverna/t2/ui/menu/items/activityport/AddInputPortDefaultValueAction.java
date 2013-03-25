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
package net.sf.taverna.t2.ui.menu.items.activityport;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import net.sf.taverna.t2.activities.stringconstant.views.StringConstantConfigView;
import net.sf.taverna.t2.workbench.design.actions.DataflowEditAction;
import net.sf.taverna.t2.workbench.edits.CompoundEdit;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workflow.edits.AddActivityEdit;
import net.sf.taverna.t2.workflow.edits.AddActivityOutputPortMappingEdit;
import net.sf.taverna.t2.workflow.edits.AddDataLinkEdit;
import net.sf.taverna.t2.workflow.edits.AddProcessorEdit;
import net.sf.taverna.t2.workflow.edits.ConfigureEdit;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.port.OutputActivityPort;
import uk.org.taverna.scufl2.api.port.OutputProcessorPort;

/**
 * Action for adding a default value to an input port of a processor.
 *
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class AddInputPortDefaultValueAction extends DataflowEditAction {

	private static Logger logger = Logger.getLogger(AddInputPortDefaultValueAction.class);

	private static final URI STRING_CONSTANT = URI.create("http://ns.taverna.org.uk/2010/activity/constant");

	private InputProcessorPort inputPort;

	public AddInputPortDefaultValueAction(Workflow workflow, InputProcessorPort inputPort, Component component, EditManager editManager, SelectionManager selectionManager) {
		super(workflow, component, editManager, selectionManager);
		this.inputPort = inputPort;
		putValue(SMALL_ICON, WorkbenchIcons.inputValueIcon);
		putValue(NAME, "Set constant value");
	}

	public void actionPerformed(ActionEvent e) {
		try {
			String defaultValue = JOptionPane.showInputDialog(component,"Enter string value",null);
			if (defaultValue != null) {

			Activity strConstActivity = new Activity();
			strConstActivity.setConfigurableType(STRING_CONSTANT);
			Configuration strConstConfBean = new Configuration();
			strConstConfBean.setType(STRING_CONSTANT.resolve("#Config"));
			strConstConfBean.getPropertyResource().addPropertyAsString(STRING_CONSTANT.resolve("#string"), defaultValue);
			StringConstantConfigView configView = new StringConstantConfigView(strConstActivity, null);

			int answer = JOptionPane.showConfirmDialog(component, configView, "Text constant value", JOptionPane.OK_CANCEL_OPTION);
			if (answer != JOptionPane.CANCEL_OPTION) {

				configView.noteConfiguration();
				strConstConfBean = configView.getConfiguration();

				// List of all edits to be done
				List<Edit<?>> editList = new ArrayList<Edit<?>>();

				// Create new string constant activity with the given default value
				editList.add(new ConfigureEdit<Activity>(strConstActivity, null, strConstConfBean));

				OutputActivityPort outputActivityPort = new OutputActivityPort(strConstActivity, "value");
				outputActivityPort.setDepth(0);
				outputActivityPort.setGranularDepth(0);

				// Create new string constant processor
				Processor strConstProcessor = new Processor();
				strConstProcessor.setName(inputPort.getName() +"_value");

				// Set the processor's activity
				Edit<Processor> processorEdit = new AddActivityEdit(strConstProcessor, strConstActivity);
				editList.add(processorEdit);

				// Create the output port for string constant processor to correspond to the
				// string constant activity's output port called "value"
				OutputProcessorPort strConstProcessorOutputPort = new OutputProcessorPort(strConstProcessor, "value");
				strConstProcessorOutputPort.setDepth(0);
				strConstProcessorOutputPort.setGranularDepth(0);
				editList.add(new AddActivityOutputPortMappingEdit(strConstActivity, strConstProcessorOutputPort, outputActivityPort));

				editList.add(new AddProcessorEdit(dataflow, strConstProcessor));

				// Add a data link between the string constant processor's output port
				// and the processor containing the passed inputPort.
				DataLink datalink = new DataLink();
				datalink.setReceivesFrom(strConstProcessorOutputPort);
				datalink.setSendsTo(inputPort);
				editList.add(new AddDataLinkEdit(dataflow, datalink));

				editManager.doDataflowEdit(dataflow.getParent(),  new CompoundEdit(editList));

			}
			}
		} catch (EditException ex) {
			logger.error("Adding default value for input port failed", ex);
		}
	}

}

