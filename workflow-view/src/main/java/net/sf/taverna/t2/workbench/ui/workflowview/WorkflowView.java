package net.sf.taverna.t2.workbench.ui.workflowview;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.design.actions.RemoveProcessorAction;
import net.sf.taverna.t2.workbench.design.actions.RenameProcessorAction;
import net.sf.taverna.t2.workbench.edits.CompoundEdit;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.DataFlowUndoEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflow.edits.AddActivityEdit;
import net.sf.taverna.t2.workflow.edits.AddActivityToProfileEdit;
import net.sf.taverna.t2.workflow.edits.AddConfigurationToProfileEdit;
import net.sf.taverna.t2.workflow.edits.AddDataflowInputPortEdit;
import net.sf.taverna.t2.workflow.edits.AddDataflowOutputPortEdit;
import net.sf.taverna.t2.workflow.edits.AddProcessorEdit;
import net.sf.taverna.t2.workflow.edits.ConfigureEdit;

import org.apache.log4j.Logger;
import org.jdom.Element;

import uk.org.taverna.platform.capability.api.ActivityService;
import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.profiles.Profile;

/**
 *
 * Super class for all UIComponentSPIs that display a Workflow
 *
 * @author alanrw
 *
 */
@SuppressWarnings("serial")
public abstract class WorkflowView extends JPanel implements UIComponentSPI {

	public static Element copyRepresentation = null;

	private static Logger logger = Logger.getLogger(WorkflowView.class);

	private static DataFlavor processorFlavor = null;
	private static DataFlavor serviceDescriptionDataFlavor = null;
	private static HashMap<String, Element> requiredSubworkflows = new HashMap<String, Element>();

	private static ChangeObserver observer = null;

	private static String UNABLE_TO_ADD_SERVICE = "Unable to add service";
	private static String UNABLE_TO_COPY_SERVICE = "Unable to copy service";

	private static ActivityService activityService = null;

	private static Scufl2Tools scufl2Tools = new Scufl2Tools();

	/**
	 * Create a WorkflowView and set it up to receive services.
	 */
	public WorkflowView(EditManager editManager, DataflowSelectionManager dataflowSelectionManager, ActivityService activityService) {
		super();
		if (observer == null) {
			observer = new ChangeObserver(editManager, dataflowSelectionManager);
		}
		if (WorkflowView.activityService == null) {
			WorkflowView.activityService = activityService;
		}
		setFocusable(true);
		if (serviceDescriptionDataFlavor == null) {
			try {
				serviceDescriptionDataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
						+ ";class=" + ServiceDescription.class.getCanonicalName(),
						"ServiceDescription", ServiceDescription.class.getClassLoader());
				if (processorFlavor == null) {
					processorFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
							+ ";class=" + Processor.class.getCanonicalName(), "Processor",
							Processor.class.getClassLoader());
				}
			} catch (ClassNotFoundException e) {
				logger.error(e);
			}
		}
	}

	public final static Processor importServiceDescription(ServiceDescription sd, boolean rename, EditManager editManager, MenuManager menuManager, DataflowSelectionManager dataflowSelectionManager) {
		WorkflowBundle currentDataflow = (WorkflowBundle) ModelMap.getInstance().getModel(ModelMapConstants.CURRENT_DATAFLOW);
		Workflow workflow = currentDataflow.getMainWorkflow();
		Profile profile = currentDataflow.getMainProfile();

		Processor processor = new Processor();
		processor.setName(sd.getName());

		Activity activity = new Activity();
		activity.setConfigurableType(sd.getActivityURI());
		Configuration configuration = sd.getActivityConfiguration();
		configuration.setConfigures(activity);

		List<Edit<?>> editList = new ArrayList<Edit<?>>();
		editList.add(new AddActivityToProfileEdit(profile, activity));
		editList.add(new AddConfigurationToProfileEdit(profile, configuration));
		// editList.add(edits.getDefaultDispatchStackEdit(p));
		editList.add(new AddActivityEdit(processor, activity));
		// editList.add(edits.getMapProcessorPortsForActivityEdit(p));
		editList.add(new AddProcessorEdit(workflow, processor));
		Edit<?> insertionEdit = sd.getInsertionEdit(workflow, processor, activity);
		if (insertionEdit != null) {
			editList.add(insertionEdit);
		}
		try {
			editManager.doDataflowEdit(currentDataflow, new CompoundEdit(editList));
		} catch (EditException e) {
			showException(UNABLE_TO_ADD_SERVICE, e);
			logger.warn("Could not add processor : edit error", e);
			processor = null;
		}

		if ((processor != null) && rename) {
			RenameProcessorAction rpa = new RenameProcessorAction(workflow, processor, null, editManager, dataflowSelectionManager);
			rpa.actionPerformed(new ActionEvent(sd, 0, ""));
		}

		if ((processor != null) && sd.isTemplateService()) {
			Action action = getConfigureAction(processor, menuManager);
			if (action != null) {
				action.actionPerformed(new ActionEvent(sd, 0, ""));
			}

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
				if ((action != null) && (action instanceof ActivityConfigurationAction)
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

	public static void pasteTransferable(Transferable t, EditManager editManager, MenuManager menuManager, DataflowSelectionManager dataflowSelectionManager) {
		if (t.isDataFlavorSupported(processorFlavor)) {
			pasteProcessor(t, editManager);
		} else if (t.isDataFlavorSupported(serviceDescriptionDataFlavor)) {
			try {
				ServiceDescription data = (ServiceDescription) t
						.getTransferData(serviceDescriptionDataFlavor);
				importServiceDescription(data, false, editManager, menuManager, dataflowSelectionManager);
			} catch (UnsupportedFlavorException e) {
				showException(UNABLE_TO_ADD_SERVICE, e);
				logger.error(e);
			} catch (IOException e) {
				showException(UNABLE_TO_ADD_SERVICE, e);
				logger.error(e);
			}

		}
	}

	public static void pasteTransferable(EditManager editManager, MenuManager menuManager, DataflowSelectionManager dataflowSelectionManager) {
		pasteTransferable(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null), editManager, menuManager, dataflowSelectionManager);

	}

	public static void pasteProcessor(Transferable t, EditManager editManager) {
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

	public static void copyProcessor(DataflowSelectionManager dataflowSelectionManager) {
		WorkflowBundle currentDataflow = (WorkflowBundle) ModelMap.getInstance().getModel(
				ModelMapConstants.CURRENT_DATAFLOW);
		DataflowSelectionModel dataFlowSelectionModel = dataflowSelectionManager
				.getDataflowSelectionModel(currentDataflow);
		// Get all selected components
		Set<Object> selectedWFComponents = dataFlowSelectionModel.getSelection();
		Processor p = null;
		for (Object selectedWFComponent : selectedWFComponents) {
			if (selectedWFComponent instanceof Processor) {
				p = (Processor) selectedWFComponent;
				break;
			}
		}
		if (p != null) {
			copyProcessor(p);
		}
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

	public static void cutProcessor(EditManager editManager, DataflowSelectionManager dataflowSelectionManager) {
//		WorkflowBundle currentDataflow = (WorkflowBundle) ModelMap.getInstance().getModel(
//				ModelMapConstants.CURRENT_DATAFLOW);
//		DataflowSelectionModel dataFlowSelectionModel = dataflowSelectionManager
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
//			cutProcessor(p.getParent(), p, null, editManager, dataflowSelectionManager);
//		}
	}

	public static void cutProcessor(Workflow dataflow, Processor processor, Component component, EditManager editManager, DataflowSelectionManager dataflowSelectionManager) {
		copyProcessor(processor);
		new RemoveProcessorAction(dataflow, processor, component, editManager, dataflowSelectionManager).actionPerformed(null);

	}

	private class ChangeObserver implements Observer<EditManagerEvent> {

		private final DataflowSelectionManager dataflowSelectionManager;

		public ChangeObserver(EditManager editManager, DataflowSelectionManager dataflowSelectionManager) {
			super();
			this.dataflowSelectionManager = dataflowSelectionManager;
			editManager.addObserver(this);
		}

		public void notify(Observable<EditManagerEvent> sender, EditManagerEvent message)
				throws Exception {
			Edit<?> edit = message.getEdit();
			considerEdit(edit, message instanceof DataFlowUndoEvent);
		}

		private void considerEdit(Edit<?> edit, boolean undoing) {
			if (edit instanceof CompoundEdit) {
				CompoundEdit compound = (CompoundEdit) edit;
				for (Edit e : compound.getChildEdits()) {
					considerEdit(e, undoing);
				}
			} else {
				Object subject = edit.getSubject();
				if (subject instanceof Workflow) {
					DataflowSelectionModel selectionModel = dataflowSelectionManager
							.getDataflowSelectionModel(((Workflow) edit.getSubject()).getParent());
					Object objectOfEdit = null;
					if (edit instanceof AddProcessorEdit) {
						objectOfEdit = ((AddProcessorEdit) edit).getProcessor();
					} else if (edit instanceof AddDataflowInputPortEdit) {
						objectOfEdit = ((AddDataflowInputPortEdit) edit).getDataflowInputPort();
					} else if (edit instanceof AddDataflowOutputPortEdit) {
						objectOfEdit = ((AddDataflowOutputPortEdit) edit).getDataflowOutputPort();
					}
					if (objectOfEdit != null) {
						if (undoing && selectionModel.getSelection().contains(objectOfEdit)) {
							selectionModel.clearSelection();
						} else {
							HashSet selection = new HashSet();
							selection.add(objectOfEdit);
							selectionModel.setSelection(selection);
						}
					}
				}
			}
		}

	}

	private static void showException(String message, Exception e) {
		if (!GraphicsEnvironment.isHeadless()) {
			SwingUtilities.invokeLater(new ShowExceptionRunnable(message, e));
		}
	}
}
