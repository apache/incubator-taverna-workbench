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
package net.sf.taverna.t2.workbench.ui.dndhandler;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;

/**
 * TransferHandler for accepting ActivityAndBeanWrapper object dropped on the
 * GraphView. On a successful drop a Processor is created from the Activity and
 * configured with the ConfigurationBean.
 * 
 * @author David Withers
 * @author Alan R Williams
 */
public class ServiceTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger
			.getLogger(ServiceTransferHandler.class);

	private Edits edits = EditsRegistry.getEdits();

	private EditManager editManager = EditManager.getInstance();
	
	private Dataflow currentDataflow;

	private DataFlavor activityDataFlavor;

	public ServiceTransferHandler() {
		
		ModelMap.getInstance().addObserver(new Observer<ModelMap.ModelMapEvent>() {
			public void notify(Observable<ModelMapEvent> sender, ModelMapEvent message) {
				if (message.getModelName().equals(ModelMapConstants.CURRENT_DATAFLOW)) {
					if (message.getNewModel() instanceof Dataflow) {
						currentDataflow = ((Dataflow) message.getNewModel());
					}
				}
			}
		});
		
		currentDataflow = (Dataflow) ModelMap.getInstance().getModel(ModelMapConstants.CURRENT_DATAFLOW);
		
		try {
			activityDataFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType + ";class="
							+ ActivityAndBeanWrapper.class.getCanonicalName(),
					"Activity", getClass().getClassLoader());
		} catch (ClassNotFoundException e) {
			logger.warn("Could not find the class "
					+ ActivityAndBeanWrapper.class);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent,
	 *      java.awt.datatransfer.DataFlavor[])
	 */
	@Override
	public boolean canImport(JComponent component, DataFlavor[] dataFlavors) {
		logger.info("Trying to import something");
		for (DataFlavor dataFlavor : dataFlavors) {
			if (dataFlavor.equals(activityDataFlavor)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.TransferHandler#importData(javax.swing.JComponent,
	 *      java.awt.datatransfer.Transferable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(JComponent component, Transferable transferable) {
		boolean result = false;
		logger.info("Importing a transferable");
		try {
			Object data = transferable.getTransferData(activityDataFlavor);
			if (data instanceof ActivityAndBeanWrapper) {
				ActivityAndBeanWrapper activityAndBeanWrapper = (ActivityAndBeanWrapper) data;
				
				Activity activity = activityAndBeanWrapper.getActivity();
				Object bean = activityAndBeanWrapper.getBean();
				
				
				String name = activityAndBeanWrapper.getName()
						.replace(' ', '_');
				name = Tools.uniqueProcessorName(name, currentDataflow);
				
				
				List<Edit<?>> editList = new ArrayList<Edit<?>>();
				editList.add(edits.getConfigureActivityEdit(activity, bean));
				Processor p=edits.createProcessor(name);
				editList.add(edits.getDefaultDispatchStackEdit(p));
				editList.add(edits.getAddActivityEdit(p, activity));
//				editList.add(edits.getMapProcessorPortsForActivityEdit(p));
//				editList.add(edits.getRenameProcessorEdit(p, name));
				editList.add(edits.getAddProcessorEdit(currentDataflow, p));
				editManager
						.doDataflowEdit(currentDataflow, new CompoundEdit(editList));
				result = true;
			}
		} catch (UnsupportedFlavorException e) {
			logger.warn("Could not import data : unsupported flavor", e);
		} catch (IOException e) {
			logger.warn("Could not import data : I/O error", e);
		} catch (EditException e) {
			logger.warn("Could not add processor : edit error", e);
		} 
		return result;
	}
}
