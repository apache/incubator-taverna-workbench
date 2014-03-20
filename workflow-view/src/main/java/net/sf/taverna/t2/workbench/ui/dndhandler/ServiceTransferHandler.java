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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.serialization.xml.ProcessorXMLSerializer;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;

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

	private Dataflow currentDataflow;

	private DataFlavor serviceDescriptionDataFlavor;
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
			serviceDescriptionDataFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType + ";class="
							+ ServiceDescription.class.getCanonicalName(),
					"ServiceDescription", getClass().getClassLoader());
		} catch (ClassNotFoundException e) {
			logger.warn("Could not find the class "
					+ ServiceDescription.class);
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
		logger.debug("Trying to import something");
		for (DataFlavor dataFlavor : dataFlavors) {
			if (dataFlavor.equals(serviceDescriptionDataFlavor)) {
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
		logger.info("Importing a transferable");
		logger.debug(component.getClass().getCanonicalName());
		WorkflowView.pasteTransferable(transferable);
		return true;
	}
	
	protected Transferable createTransferable(JComponent c) {
		return null;
	}

	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}
	
	@Override
	public void exportToClipboard(JComponent comp, Clipboard clip, int action)
			throws IllegalStateException {
		super.exportToClipboard(comp, clip, action);
		if (action == COPY) {
			WorkflowView.copyProcessor();
		} else if (action == MOVE) {
			WorkflowView.cutProcessor();
		}
	}
}
