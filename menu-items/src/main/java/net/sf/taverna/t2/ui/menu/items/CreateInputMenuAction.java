package net.sf.taverna.t2.ui.menu.items;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.ui.menu.ContextualMenuComponent;
import net.sf.taverna.t2.ui.menu.ContextualSelection;
import net.sf.taverna.t2.workbench.design.actions.AddDataflowInputAction;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public class CreateInputMenuAction extends AbstractMenuAction implements ContextualMenuComponent {

	private ContextualSelection contextualSelection;

	public CreateInputMenuAction() {
		super(WorkflowInputsSection.inputsSection, 10);
	}

	@Override
	protected Action createAction() {
		return new AddDataflowInputAction((Dataflow)contextualSelection.getSelection(), 
				contextualSelection.getRelativeToComponent());
	}
	
	@Override
	public boolean isEnabled() {
		return contextualSelection != null;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
		// Force new createAction() call
		action = null;	
	}

}
