/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.taverna.workbench.ui.workflowview;

import static java.awt.GraphicsEnvironment.isHeadless;
import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.datatransfer.DataFlavor.javaJVMLocalObjectMimeType;
import static javax.swing.SwingUtilities.invokeLater;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.taverna.servicedescriptions.ServiceDescription;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.design.actions.RemoveProcessorAction;
import org.apache.taverna.workbench.design.actions.RenameProcessorAction;
import org.apache.taverna.workbench.edits.CompoundEdit;
import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.DataflowSelectionModel;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.actions.activity.ActivityConfigurationAction;
import org.apache.taverna.workflow.edits.AddChildEdit;
import org.apache.taverna.workflow.edits.AddProcessorEdit;

import org.apache.log4j.Logger;
//import org.jdom.Element;



import org.apache.taverna.services.ActivityTypeNotFoundException;
import org.apache.taverna.services.InvalidConfigurationException;
import org.apache.taverna.services.ServiceRegistry;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.iterationstrategy.CrossProduct;
import org.apache.taverna.scufl2.api.port.InputActivityPort;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;
import org.apache.taverna.scufl2.api.port.OutputActivityPort;
import org.apache.taverna.scufl2.api.port.OutputProcessorPort;
import org.apache.taverna.scufl2.api.profiles.ProcessorBinding;
import org.apache.taverna.scufl2.api.profiles.ProcessorInputPortBinding;
import org.apache.taverna.scufl2.api.profiles.ProcessorOutputPortBinding;
import org.apache.taverna.scufl2.api.profiles.Profile;

/**
 * Super class for all UIComponentSPIs that display a Workflow
 * 
 * @author alanrw
 */
public class WorkflowView {
	//public static Element copyRepresentation = null;

	private static Logger logger = Logger.getLogger(WorkflowView.class);

	private static DataFlavor processorFlavor = null;
	private static DataFlavor serviceDescriptionDataFlavor = null;
	//private static HashMap<String, Element> requiredSubworkflows = new HashMap<>();

	private static String UNABLE_TO_ADD_SERVICE = "Unable to add service";
	@SuppressWarnings("unused")
	private static String UNABLE_TO_COPY_SERVICE = "Unable to copy service";

	@SuppressWarnings("unused")
	private static Scufl2Tools scufl2Tools = new Scufl2Tools();

	static {
		try {
			if (serviceDescriptionDataFlavor == null) {
				serviceDescriptionDataFlavor = new DataFlavor(
						javaJVMLocalObjectMimeType + ";class="
								+ ServiceDescription.class.getCanonicalName(),
						"ServiceDescription",
						ServiceDescription.class.getClassLoader());
				if (processorFlavor == null)
					processorFlavor = new DataFlavor(javaJVMLocalObjectMimeType
							+ ";class=" + Processor.class.getCanonicalName(),
							"Processor", Processor.class.getClassLoader());
			}
		} catch (ClassNotFoundException e) {
			logger.error(e);
		}
	}

	public final static Processor importServiceDescription(
			ServiceDescription sd, boolean rename, EditManager editManager,
			MenuManager menuManager, SelectionManager selectionManager,
			ServiceRegistry serviceRegistry) {
		Workflow workflow = selectionManager.getSelectedWorkflow();
		Profile profile = selectionManager.getSelectedProfile();

		Processor processor = new Processor();
		processor.setName(sd.getName());

		CrossProduct crossProduct = new CrossProduct();
		crossProduct.setParent(processor.getIterationStrategyStack());

		URI activityType = sd.getActivityType();

		Activity activity = new Activity();
		activity.setType(activityType);
		Configuration configuration = sd.getActivityConfiguration();
		configuration.setConfigures(activity);

		ProcessorBinding processorBinding = new ProcessorBinding();
		processorBinding.setBoundProcessor(processor);
		processorBinding.setBoundActivity(activity);

		try {
			for (InputActivityPort activityPort : serviceRegistry
					.getActivityInputPorts(activityType,
							configuration.getJson())) {
				// add port to activity
				activityPort.setParent(activity);
				// create processor port
				InputProcessorPort processorPort = new InputProcessorPort(
						processor, activityPort.getName());
				processorPort.setDepth(activityPort.getDepth());
				// add a new port binding
				new ProcessorInputPortBinding(processorBinding, processorPort,
						activityPort);
			}
			for (OutputActivityPort activityPort : serviceRegistry
					.getActivityOutputPorts(activityType,
							configuration.getJson())) {
				// add port to activity
				activityPort.setParent(activity);
				// create processor port
				OutputProcessorPort processorPort = new OutputProcessorPort(
						processor, activityPort.getName());
				processorPort.setDepth(activityPort.getDepth());
				processorPort.setGranularDepth(activityPort.getGranularDepth());
				// add a new port binding
				new ProcessorOutputPortBinding(processorBinding, activityPort,
						processorPort);
			}
		} catch (InvalidConfigurationException | ActivityTypeNotFoundException e) {
			logger.warn("Unable to get activity ports for configuration", e);
		}

		List<Edit<?>> editList = new ArrayList<>();
		editList.add(new AddChildEdit<>(profile, activity));
		editList.add(new AddChildEdit<>(profile, configuration));
		editList.add(new AddChildEdit<>(profile, processorBinding));
		editList.add(new AddProcessorEdit(workflow, processor));
		Edit<?> insertionEdit = sd.getInsertionEdit(workflow, processor, activity);
		if (insertionEdit != null)
			editList.add(insertionEdit);
		try {
			editManager.doDataflowEdit(workflow.getParent(), new CompoundEdit(
					editList));
		} catch (EditException e) {
			showException(UNABLE_TO_ADD_SERVICE, e);
			logger.warn("Could not add processor : edit error", e);
			processor = null;
		}

		if (processor != null && rename) {
			RenameProcessorAction rpa = new RenameProcessorAction(workflow,
					processor, null, editManager, selectionManager);
			rpa.actionPerformed(new ActionEvent(sd, 0, ""));
		}

		if (processor != null && sd.isTemplateService()) {
			Action action = getConfigureAction(processor, menuManager);
			if (action != null)
				action.actionPerformed(new ActionEvent(sd, 0, ""));
		}
		return processor;
	}

	public static Action getConfigureAction(Processor p, MenuManager menuManager) {
		Action result = null;
		JPopupMenu dummyMenu = menuManager.createContextMenu(null, p, null);
		for (Component c : dummyMenu.getComponents()) {
			logger.debug(c.getClass().getCanonicalName());
			if (c instanceof JMenuItem) {
				JMenuItem menuItem = (JMenuItem) c;
				Action action = menuItem.getAction();
				if (action != null
						&& action instanceof ActivityConfigurationAction
						&& action.isEnabled()) {
					if (result != null) {
						// do not return anything if there are two matches
						// logger.info("Multiple actions " +
						// action.getClass().getCanonicalName() + " " +
						// result.getClass().getCanonicalName());
						return null;
					}
					result = action;
				}
			}
		}
		return result;
	}

	public static void pasteTransferable(Transferable t,
			EditManager editManager, MenuManager menuManager,
			SelectionManager selectionManager, ServiceRegistry serviceRegistry) {
		if (t.isDataFlavorSupported(processorFlavor))
			pasteProcessor(t, editManager);
		else if (t.isDataFlavorSupported(serviceDescriptionDataFlavor))
			try {
				ServiceDescription data = (ServiceDescription) t
						.getTransferData(serviceDescriptionDataFlavor);
				importServiceDescription(data, false, editManager, menuManager,
						selectionManager, serviceRegistry);
			} catch (UnsupportedFlavorException | IOException e) {
				showException(UNABLE_TO_ADD_SERVICE, e);
				logger.error(e);
			}
	}

	public static void pasteTransferable(EditManager editManager,
			MenuManager menuManager, SelectionManager selectionManager,
			ServiceRegistry serviceRegistry) {
		pasteTransferable(
				getDefaultToolkit().getSystemClipboard().getContents(null),
				editManager, menuManager, selectionManager, serviceRegistry);
	}

	public static void pasteProcessor(Transferable t, EditManager editManager) {
		//FIXME
//		try {
//			Element e = (Element) t.getTransferData(processorFlavor);
//			WorkflowBundle currentDataflow = (WorkflowBundle) ModelMap.getInstance().getModel(
//					ModelMapConstants.CURRENT_DATAFLOW);
//			Processor p = ProcessorXMLDeserializer.getInstance().deserializeProcessor(e,
//					requiredSubworkflows);
//			if (p == null) {
//				return;
//			}
//			String newName = Tools.uniqueProcessorName(p.getName(), currentDataflow);
//			List<Edit<?>> editList = new ArrayList<Edit<?>>();
//
//			Edits edits = editManager.getEdits();
//			if (!newName.equals(p.getName())) {
//				Edit renameEdit = edits.getRenameProcessorEdit(p, newName);
//				editList.add(renameEdit);
//			}
//
//			Activity activity = null;
//			if (p.getActivityList().size() > 0) {
//				activity = p.getActivityList().get(0);
//			}
//
//			List<InputProcessorPort> processorInputPorts = new ArrayList<InputProcessorPort>();
//			processorInputPorts.addAll(p.getInputPorts());
//			for (InputProcessorPort port : processorInputPorts) {
//				Edit removePortEdit = edits.getRemoveProcessorInputPortEdit(p, port);
//				editList.add(removePortEdit);
//				if (activity != null) {
//					Edit removePortMapEdit = edits.getRemoveActivityInputPortMappingEdit(activity,
//							port);
//					editList.add(removePortMapEdit);
//				}
//			}
//			List<ProcessorOutputPort> processorOutputPorts = new ArrayList<ProcessorOutputPort>();
//			processorOutputPorts.addAll(p.getOutputPorts());
//			for (ProcessorOutputPort port : processorOutputPorts) {
//				Edit removePortEdit = edits.getRemoveProcessorOutputPortEdit(p, port);
//				editList.add(removePortEdit);
//				if (activity != null) {
//					Edit removePortMapEdit = edits.getRemoveActivityOutputPortMappingEdit(activity,
//							port.getName());
//					editList.add(removePortMapEdit);
//				}
//			}
//			Edit edit = edits.getAddProcessorEdit(currentDataflow, p);
//			editList.add(edit);
//			editManager.doDataflowEdit(currentDataflow, new CompoundEdit(editList));
//		} catch (ActivityConfigurationException e) {
//			showException(UNABLE_TO_ADD_SERVICE, e);
//			logger.error(e);
//		} catch (EditException e) {
//			showException(UNABLE_TO_ADD_SERVICE, e);
//			logger.error(e);
//		} catch (ClassNotFoundException e) {
//			showException(UNABLE_TO_ADD_SERVICE, e);
//			logger.error(e);
//		} catch (InstantiationException e) {
//			showException(UNABLE_TO_ADD_SERVICE, e);
//			logger.error(e);
//		} catch (IllegalAccessException e) {
//			showException(UNABLE_TO_ADD_SERVICE, e);
//			logger.error(e);
//		} catch (DeserializationException e) {
//			showException(UNABLE_TO_ADD_SERVICE, e);
//			logger.error(e);
//		} catch (UnsupportedFlavorException e) {
//			showException(UNABLE_TO_ADD_SERVICE, e);
//			logger.error(e);
//		} catch (IOException e) {
//			showException(UNABLE_TO_ADD_SERVICE, e);
//			logger.error(e);
//		}
	}

	public static void copyProcessor(SelectionManager selectionManager) {
		WorkflowBundle currentDataflow = selectionManager.getSelectedWorkflowBundle();
		DataflowSelectionModel dataFlowSelectionModel = selectionManager
				.getDataflowSelectionModel(currentDataflow);
		// Get all selected components
		Set<Object> selectedWFComponents = dataFlowSelectionModel.getSelection();
		Processor p = null;
		for (Object selectedWFComponent : selectedWFComponents)
			if (selectedWFComponent instanceof Processor) {
				p = (Processor) selectedWFComponent;
				break;
			}
		if (p != null)
			copyProcessor(p);
	}

	public static void copyProcessor(Processor p) {
//		try {
//			final Element e = ProcessorXMLSerializer.getInstance().processorToXML(p);
//			requiredSubworkflows = new HashMap<String, Element>();
//			rememberSubworkflows(p, profile);
//			Transferable t = new Transferable() {
//
//				public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,
//						IOException {
//					return e;
//				}
//
//				public DataFlavor[] getTransferDataFlavors() {
//					DataFlavor[] result = new DataFlavor[1];
//					result[0] = processorFlavor;
//					return result;
//				}
//
//				public boolean isDataFlavorSupported(DataFlavor flavor) {
//					return flavor.equals(processorFlavor);
//				}
//
//			};
//			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(t, null);
//			PasteGraphComponentAction.setEnabledStatic(true);
//		} catch (IOException e1) {
//			showException(UNABLE_TO_COPY_SERVICE, e1);
//			logger.error(e1);
//		} catch (JDOMException e1) {
//			logger.error(UNABLE_TO_COPY_SERVICE, e1);
//		} catch (SerializationException e) {
//			logger.error(UNABLE_TO_COPY_SERVICE, e);
//		}
	}

//	private static void rememberSubworkflows(Processor p, Profile profile) throws SerializationException {
//
//		for (ProcessorBinding processorBinding : scufl2Tools.processorBindingsForProcessor(p, profile)) {
//			Activity a = processorBinding.getBoundActivity();
//			if (a.getConfigurableType().equals(URI.create("http://ns.taverna.org.uk/2010/activity/nested-workflow"))) {
//				NestedDataflow da = (NestedDataflow) a;
//				Dataflow df = da.getNestedDataflow();
//				if (!requiredSubworkflows.containsKey(df.getIdentifier())) {
//					requiredSubworkflows.put(df.getIdentifier(), DataflowXMLSerializer
//							.getInstance().serializeDataflow(df));
//					for (Processor sp : df.getProcessors()) {
//						rememberSubworkflows(sp);
//					}
//				}
//			}
//		}
//	}

	public static void cutProcessor(EditManager editManager, SelectionManager selectionManager) {
//		WorkflowBundle currentDataflow = (WorkflowBundle) ModelMap.getInstance().getModel(
//				ModelMapConstants.CURRENT_DATAFLOW);
//		DataflowSelectionModel dataFlowSelectionModel = selectionManager
//				.getDataflowSelectionModel(currentDataflow);
//		// Get all selected components
//		Set<Object> selectedWFComponents = dataFlowSelectionModel.getSelection();
//		Processor p = null;
//		for (Object selectedWFComponent : selectedWFComponents) {
//			if (selectedWFComponent instanceof Processor) {
//				p = (Processor) selectedWFComponent;
//				break;
//			}
//		}
//		if (p != null) {
//			cutProcessor(p.getParent(), p, null, editManager, selectionManager);
//		}
	}

	public static void cutProcessor(Workflow dataflow, Processor processor,
			Component component, EditManager editManager,
			SelectionManager selectionManager) {
		copyProcessor(processor);
		new RemoveProcessorAction(dataflow, processor, component, editManager,
				selectionManager).actionPerformed(null);
	}

	private static void showException(String message, Exception e) {
		if (!isHeadless())
			invokeLater(new ShowExceptionRunnable(message, e));
	}
}
