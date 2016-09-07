package org.apache.taverna.ui.menu.items.activityport;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.services.ServiceRegistry;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;

public class SetConstantInputPortValueMenuAction extends AbstractContextualMenuAction {

	private EditManager editManager;
	private SelectionManager selectionManager;
	private ServiceRegistry serviceRegistry;

	public SetConstantInputPortValueMenuAction() {
		super(ActivityInputPortSection.activityInputPortSection, 10);
	}

	@Override
	public synchronized Action getAction() {
		SetDefaultInputPortValueAction action = (SetDefaultInputPortValueAction) super.getAction();
		action.updateStatus();
		return action;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof InputProcessorPort
				&& getContextualSelection().getParent() instanceof Workflow;
	}

	@Override
	protected Action createAction() {
		return new SetDefaultInputPortValueAction(editManager, selectionManager, serviceRegistry);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

}
