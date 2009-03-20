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
package net.sf.taverna.t2.workbench.views.graph;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.utils.Tools;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper;

import org.apache.log4j.Logger;

/**
 * TransferHandler for accepting ActivityAndBeanWrapper object dropped on the
 * GraphView. On a successful drop a Processor is created from the Activity and
 * configured with the ConfigurationBean.
 * 
 * @author David Withers
 */
public class GraphViewTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger
			.getLogger(GraphViewTransferHandler.class);

	private Edits edits = EditsRegistry.getEdits();

	private EditManager editManager = EditManager.getInstance();

	private GraphViewComponent graphViewComponent;

	private DataFlavor activityDataFlavor;

	public GraphViewTransferHandler(GraphViewComponent graphViewComponent) {
		this.graphViewComponent = graphViewComponent;
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
		try {
			Object data = transferable.getTransferData(activityDataFlavor);
			if (data instanceof ActivityAndBeanWrapper) {
				ActivityAndBeanWrapper activityAndBeanWrapper = (ActivityAndBeanWrapper) data;
				
				Activity activity = activityAndBeanWrapper.getActivity();
				Object bean = activityAndBeanWrapper.getBean();
				
				
				
				Dataflow dataflow = graphViewComponent.getDataflow();
				
				String name = activityAndBeanWrapper.getName()
						.replace(' ', '_');
				name = Tools.uniqueProcessorName(name, dataflow);
				
				
				List<Edit<?>> editList = new ArrayList<Edit<?>>();
				editList.add(edits.getConfigureActivityEdit(activity, bean));
				Processor p=edits.createProcessor(name);
				editList.add(edits.getDefaultDispatchStackEdit(p));
				editList.add(edits.getAddActivityEdit(p, activity));
//				editList.add(edits.getMapProcessorPortsForActivityEdit(p));
//				editList.add(edits.getRenameProcessorEdit(p, name));
				editList.add(edits.getAddProcessorEdit(dataflow, p));
				editManager
						.doDataflowEdit(dataflow, new CompoundEdit(editList));
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
