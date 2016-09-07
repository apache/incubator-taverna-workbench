/**********************************************************************
 **********************************************************************/
package org.apache.taverna.ui.menu.items.workflow;

import java.net.URI;

import javax.swing.Action;

import org.apache.taverna.scufl2.api.core.Workflow;

import org.apache.taverna.lang.ui.ShadedLabel;
import org.apache.taverna.ui.menu.AbstractMenuSection;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.ContextualSelection;
import org.apache.taverna.ui.menu.DefaultContextualMenu;

/**
 * Menu section containing the actions to add service templates, i.e. activities
 * than are not readily runnable but need to be configured first. The actual actions that
 * go into this menu can be found in the ui modules for the activities.
 *
 * @author Alex Nenadic
 *
 */
public class WorkflowServiceTemplatesSection extends AbstractMenuSection
		implements ContextualMenuComponent {

	private static final String SERVICE_TEMPLATES = "Service templates";
	public static final URI serviceTemplatesSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/serviceTemplates");
	private ContextualSelection contextualSelection;

	public WorkflowServiceTemplatesSection() {
		super(DefaultContextualMenu.DEFAULT_CONTEXT_MENU, 30, serviceTemplatesSection);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof Workflow;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
	}

	@Override
	protected Action createAction() {
		DummyAction action = new DummyAction(SERVICE_TEMPLATES);
		// Set the colour for the section
		action.putValue(AbstractMenuSection.SECTION_COLOR, ShadedLabel.ORANGE);
		return action;
	}
}
