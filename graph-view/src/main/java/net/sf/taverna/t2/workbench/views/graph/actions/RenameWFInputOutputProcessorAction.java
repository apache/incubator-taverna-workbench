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

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.ui.zaria.WorkflowPerspective;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.design.actions.EditDataflowInputPortAction;
import net.sf.taverna.t2.workbench.design.actions.EditDataflowOutputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RenameProcessorAction;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * An action that allows user to rename workflow input, output or
 * processor, in case one of these is currently seected in the Graph View.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class RenameWFInputOutputProcessorAction extends AbstractAction{

	private static ModelMap modelMap = ModelMap.getInstance();

	/* Perspective switch observer */
	private CurrentPerspectiveObserver perspectiveObserver = new CurrentPerspectiveObserver();
	
	/* Current workflow's selection model event observer.*/
	private Observer<DataflowSelectionMessage> workflowSelectionObserver = new DataflowSelectionObserver();

	public RenameWFInputOutputProcessorAction(){
		super();
		putValue(SMALL_ICON, WorkbenchIcons.renameIcon);
		putValue(NAME, "Rename");	
		putValue(SHORT_DESCRIPTION, "Rename inputs, outputs or processors");
		setEnabled(false);

		modelMap.addObserver(perspectiveObserver);

		modelMap.addObserver(new Observer<ModelMap.ModelMapEvent>() {
			public void notify(Observable<ModelMapEvent> sender, ModelMapEvent message) {
				if (message.getModelName().equals(ModelMapConstants.CURRENT_DATAFLOW)) {
					if (message.getNewModel() instanceof Dataflow) {
						
						// Update the buttons status as current dataflow has changed
						updateStatus((Dataflow) message.getNewModel());
						
						// Remove the workflow selection model listener from the previous (if any) 
						// and add to the new workflow (if any)
						Dataflow oldFlow = (Dataflow) message.getOldModel();
						Dataflow newFlow = (Dataflow) message.getNewModel();
						if (oldFlow != null) {
							DataflowSelectionManager
							.getInstance().getDataflowSelectionModel(oldFlow)
									.removeObserver(workflowSelectionObserver);
						}

						if (newFlow != null) {
							DataflowSelectionManager
							.getInstance().getDataflowSelectionModel(newFlow)
									.addObserver(workflowSelectionObserver);
						}
					}
				}
			}
		});
	}
	
	public void actionPerformed(ActionEvent e) {
		
		Dataflow dataflow = FileManager.getInstance().getCurrentDataflow();
		DataflowSelectionModel dataFlowSelectionModel = DataflowSelectionManager
		.getInstance().getDataflowSelectionModel(dataflow);
		// Get selected port
		Set<Object> selectedWFComponents = dataFlowSelectionModel
				.getSelection();
		if (selectedWFComponents.size() > 1){
			JOptionPane
					.showMessageDialog(
							null,
							"Only one workflow component should be selected for this action.",
							"Warning", JOptionPane.WARNING_MESSAGE);
		}
		else{
			Object selectedWFComponent = selectedWFComponents.toArray()[0];
			if (selectedWFComponent instanceof DataflowInputPort) {
				new EditDataflowInputPortAction(dataflow,
						(DataflowInputPort) selectedWFComponent, null)
						.actionPerformed(e);
			}
			else if (selectedWFComponent instanceof DataflowOutputPort){
				new EditDataflowOutputPortAction(dataflow,
						(DataflowOutputPort) selectedWFComponent, null)
						.actionPerformed(e);
			}
			else if (selectedWFComponent instanceof Processor){
				new RenameProcessorAction(dataflow,
						(Processor) selectedWFComponent, null)
						.actionPerformed(e);
			}
			else{ // should not happen as the button will be disabled otherwise, but ...
				JOptionPane
				.showMessageDialog(
						null,
						"This action does not apply for the selected component.",
						"Warning", JOptionPane.WARNING_MESSAGE);
			}
		}
	}
	
	/**
	 * Check if action should be enabled or disabled and update its status.
	 */
	public void updateStatus(Dataflow dataflow) {

		DataflowSelectionModel selectionModel = DataflowSelectionManager	
		.getInstance().getDataflowSelectionModel(dataflow);
		
		// List of all selected objects in the graph view
		Set<Object> selection = selectionModel.getSelection();
		
		if (selection.isEmpty()){
			setEnabled(false);
		}
		else{
			// Take the first selected item - we only support single selections anyway
			Object selected = selection.toArray()[0];
			if ((selected instanceof Processor) || 
					(selected instanceof DataflowInputPort) || 
					(selected instanceof DataflowOutputPort)){
				setEnabled(true);
			}
			else{
				setEnabled(false);
			}
		}
	}
	
	/**
	 * Observes events on workflow Selection Manager, i.e. when a workflow 
	 * node is selected in the graph view, and enables/disables this action accordingly.
	 */
	private final class DataflowSelectionObserver implements
			Observer<DataflowSelectionMessage> {

		public void notify(Observable<DataflowSelectionMessage> sender,
				DataflowSelectionMessage message) throws Exception {
			updateStatus(FileManager.getInstance().getCurrentDataflow());
		}
	}

	/**
	 * Modify the enabled/disabled state of the action when ModelMapConstants.CURRENT_PERSPECTIVE has been
	 * modified (i.e. when perspective has been switched).
	 */
	public class CurrentPerspectiveObserver implements Observer<ModelMapEvent> {
		public void notify(Observable<ModelMapEvent> sender,
				ModelMapEvent message) throws Exception {
			if (message.getModelName().equals(
					ModelMapConstants.CURRENT_PERSPECTIVE)) {
				if (message.getNewModel() instanceof WorkflowPerspective) {
					updateStatus(FileManager.getInstance().getCurrentDataflow());
				}
				else{
					setEnabled(false);
				}
			}
		}
	}
}
