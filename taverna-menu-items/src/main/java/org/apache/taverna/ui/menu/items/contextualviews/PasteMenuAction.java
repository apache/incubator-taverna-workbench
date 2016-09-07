/**********************************************************************
 **********************************************************************/
package org.apache.taverna.ui.menu.items.contextualviews;

import java.net.URI;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.actions.PasteGraphComponentAction;
import org.apache.taverna.services.ServiceRegistry;
import org.apache.taverna.scufl2.api.core.Workflow;

public class PasteMenuAction extends AbstractContextualMenuAction {

	private static final URI PASTE_SERVICE_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/paste#pasteServiceComponent");

	private EditManager editManager;
	private MenuManager menuManager;
	private SelectionManager selectionManager;
	private ServiceRegistry serviceRegistry;

	public PasteMenuAction() {
		super(EditSection.editSection, 20, PASTE_SERVICE_URI);
	}

	@Override
	protected Action createAction() {
		return PasteGraphComponentAction.getInstance(editManager, menuManager, selectionManager, serviceRegistry);
	}

	public boolean isEnabled() {
		return super.isEnabled() && (getContextualSelection().getSelection() instanceof Workflow);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

}
