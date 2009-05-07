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
package net.sf.taverna.t2.workbench.views.results;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPIRegistry;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * This component contains a tabbed pane, where each tab displays results for one of
 * the output ports of a workflow, and a set of 'save results' buttons that save results 
 * from all ports in a certain format.
 * 
 * @author David Withers
 * @author Alex Nenadic
 *
 */
public class ResultViewComponent extends JPanel implements UIComponentSPI, ResultListener {

	private static final long serialVersionUID = 988812623494396366L;
	
	// Invocation context
	private InvocationContext context = null;
	
	// The map contains a mapping for each port to a T2Reference pointing to the port's result(s)
	private HashMap<String, T2Reference> resultReferencesMap = new HashMap<String, T2Reference>();
	
	// Per-port boolean values indicating if all results have been received per port
	private HashMap<String, Boolean> receivedAllResultsForPort = new HashMap<String, Boolean>();
	
	// Tabbed pane - each tab contains a results tree and a RenderedResultComponent, 
	// which in turn contains the currently selected result node rendered according 
	// to its mime type and a button for saving the selected inividual result
	private JTabbedPane tabbedPane;
	
	// Panel containing the save buttons
	private JPanel saveButtonsPanel;
		
	// Registry of all existing 'save results' actions, each one can save results
	// in a different format
	private static SaveAllResultsSPIRegistry saveAllResultsRegistry = SaveAllResultsSPIRegistry.getInstance();	
	
	
	public ResultViewComponent() {
		super(new BorderLayout());
		setBorder(new EtchedBorder());
		tabbedPane = new JTabbedPane();
		saveButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//		add(saveButtonsPanel, BorderLayout.NORTH);
		add(tabbedPane, BorderLayout.CENTER);
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Results View Component";
	}

	public void onDisplay() {
	}

	public void onDispose() {
	}

	public void register(WorkflowInstanceFacade facade)
			throws EditException {
		
		clear();
		
		// Get all existing 'Save result' actions
		List<SaveAllResultsSPI> saveActions = saveAllResultsRegistry.getSaveResultActions();
		for (SaveAllResultsSPI action : saveActions){
			JButton saveButton = new JButton(action.getAction());
			action.setResultReferencesMap(null);
			action.setInvocationContext(null);
			saveButton.setEnabled(false);
			saveButtonsPanel.add(saveButton);
		}

		final List<? extends DataflowOutputPort> dataflowOutputPorts = facade
				.getDataflow().getOutputPorts();
		
		for (DataflowOutputPort dataflowOutputPort : dataflowOutputPorts) {
			String portName = dataflowOutputPort.getName();
						
			// Initially we have no results for a port
			receivedAllResultsForPort.put(portName, new Boolean(Boolean.FALSE));
			
			// Create a tab containing a tree view of per-port results and a rendering
			// component for displaying individual results
			PortResultsViewTab resultTab = new PortResultsViewTab(dataflowOutputPort);

			// Per-port tree model listens for results coming out of the data facade
			facade.addResultListener(resultTab.getResultModel());
			// This component also listens to the results coming out in order to know
			// when receiving of results has finished
			facade.addResultListener(this);
			
			tabbedPane.add(portName, resultTab);
		}
		revalidate();
	}

	public void clear() {
		saveButtonsPanel.removeAll();
		tabbedPane.removeAll();
	}

	public void resultTokenProduced(WorkflowDataToken token, String portName) {
		
		// Set the invocation context the first time you get the chance
		if (context == null)
			context = token.getContext();
		
		// If we have finished receiving results - token.getIndex().length is 0
		if (token.getIndex().length == 0){
			receivedAllResultsForPort.put(portName, new Boolean(Boolean.TRUE));
			// We know that at this point the token.getData() contains a T2Reference to 
			// all result(s)
			T2Reference resultsRef = token.getData();
			// Put the resultsRef in the resultReferencesMap
			resultReferencesMap.put(portName, resultsRef);
		}
		
		// If this is the last token for all ports - update the save buttons' state
		 boolean receivedAll = true;
		 for (String pName : receivedAllResultsForPort.keySet()){
		 	if (!receivedAllResultsForPort.get(pName).booleanValue()){
		 		receivedAll = false;
		 		break;
		 	}
		 }
		 if (receivedAll){
			 for (int i=0; i< saveButtonsPanel.getComponents().length; i++){
					JButton saveButton = (JButton)saveButtonsPanel.getComponent(i);
					SaveAllResultsSPI action = (SaveAllResultsSPI)(saveButton.getAction());
					// Update the action
					action.setResultReferencesMap(resultReferencesMap);
					action.setInvocationContext(context);
					saveButton.setEnabled(true);
			}
		 }
	}

}
