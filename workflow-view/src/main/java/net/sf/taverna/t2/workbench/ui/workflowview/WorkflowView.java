package net.sf.taverna.t2.workbench.ui.workflowview;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.ui.dndhandler.ServiceTransferHandler;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.utils.Tools;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.design.actions.RenameProcessorAction;

/**
 * 
 * Super class for all UIComponentSPIs that display a Workflow
 * @author alanrw
 *
 */
@SuppressWarnings("serial")
public abstract class WorkflowView extends JPanel implements UIComponentSPI{
	
	private static Edits edits = EditsRegistry.getEdits();

	private static EditManager editManager = EditManager.getInstance();
	
	private static Logger logger = Logger
	.getLogger(WorkflowView.class);

	/**
	 * Create a WorkflowView and set it up to receive services.
	 */
	public WorkflowView() {
		super();
		this.setTransferHandler(new ServiceTransferHandler());
	}
	
	@SuppressWarnings("unchecked")
	public final static Processor importServiceDescription(Dataflow currentDataflow, ServiceDescription sd, JComponent component, boolean rename) throws InstantiationException, IllegalAccessException {
		Processor p = null;
		Object bean = sd.getActivityConfiguration();
		Activity activity;
			activity = (Activity) sd.getActivityClass().newInstance();
			String name = sd.getName()
			.replace(' ', '_');
	name = Tools.uniqueProcessorName(name, currentDataflow);
	try {
		List<Edit<?>> editList = new ArrayList<Edit<?>>();
		editList.add(edits.getConfigureActivityEdit(activity, bean));
		p=edits.createProcessor(name);
		editList.add(edits.getDefaultDispatchStackEdit(p));
		editList.add(edits.getAddActivityEdit(p, activity));
//		editList.add(edits.getMapProcessorPortsForActivityEdit(p));
//		editList.add(edits.getRenameProcessorEdit(p, name));
		editList.add(edits.getAddProcessorEdit(currentDataflow, p));
			editManager
					.doDataflowEdit(currentDataflow, new CompoundEdit(editList));
	} catch (EditException e) {
		logger.warn("Could not add processor : edit error", e);
		p = null;
	}
	
	if ((p != null) && rename) {
		RenameProcessorAction rpa = new RenameProcessorAction(currentDataflow, p, component);
		rpa.actionPerformed(new ActionEvent(component, 0, ""));
	}
	
	if ((p != null) && sd.isTemplateService()) {
		JPopupMenu dummyMenu = MenuManager.getInstance().createContextMenu(currentDataflow,
				p, null);
		for (Component c : dummyMenu.getComponents()) {
			logger.info(c.getClass().getCanonicalName());
			if (c instanceof JMenuItem) {
				JMenuItem menuItem = (JMenuItem) c;
				Action action = menuItem.getAction();
				if (action != null) {
					logger.info(action.getClass().getCanonicalName());
					if (action instanceof ActivityConfigurationAction) {
						logger.info("Got appropriate action " + action.getClass().getCanonicalName());
					action.actionPerformed(new ActionEvent(component, 0, ""));
					break;
				}
			}
			}
		}
	}
	return p;
}
}
