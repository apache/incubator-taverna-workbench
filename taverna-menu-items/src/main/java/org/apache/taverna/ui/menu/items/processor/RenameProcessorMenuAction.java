package org.apache.taverna.ui.menu.items.processor;

import java.awt.Component;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.ui.menu.items.contextualviews.ConfigureSection;
import org.apache.taverna.workbench.design.actions.RenameProcessorAction;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;

public class RenameProcessorMenuAction extends AbstractContextualMenuAction {

	private EditManager editManager;
	private SelectionManager selectionManager;

	public RenameProcessorMenuAction() {
		super(ConfigureSection.configureSection, 60);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof Processor
				&& getContextualSelection().getParent() instanceof Workflow;
	}

	@Override
	protected Action createAction() {
		Workflow workflow = (Workflow) getContextualSelection().getParent();
		Processor processor = (Processor) getContextualSelection()
				.getSelection();
		Component component = getContextualSelection().getRelativeToComponent();
		return new RenameProcessorAction(workflow, processor, component, editManager, selectionManager);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

}
