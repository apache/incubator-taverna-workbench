/**********************************************************************
 **********************************************************************/
package org.apache.taverna.ui.menu.items.activityport;

import java.net.URI;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuSection;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.ContextualSelection;
import org.apache.taverna.ui.menu.DefaultContextualMenu;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;

public class ActivityInputPortSection extends AbstractMenuSection implements
		ContextualMenuComponent {

	private static final String ACTIVITY_INPUT_PORT = "Service input port: ";
	public static final URI activityInputPortSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/activityInputPort");
	private ContextualSelection contextualSelection;

	public ActivityInputPortSection() {
		super(DefaultContextualMenu.DEFAULT_CONTEXT_MENU, 10,
				activityInputPortSection);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		return getContextualSelection().getSelection() instanceof InputProcessorPort;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
		this.action = null;
	}

	@Override
	protected Action createAction() {
		InputProcessorPort port = (InputProcessorPort) getContextualSelection().getSelection();
		String name = ACTIVITY_INPUT_PORT + port.getName();
		return new DummyAction(name);
	}

}
