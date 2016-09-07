/**********************************************************************
 **********************************************************************/
package org.apache.taverna.ui.menu.items.activityport;

import java.net.URI;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuSection;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.ContextualSelection;
import org.apache.taverna.ui.menu.DefaultContextualMenu;
import org.apache.taverna.scufl2.api.port.OutputProcessorPort;

public class ActivityOutputPortSection extends AbstractMenuSection implements
		ContextualMenuComponent {

	private static final String ACTIVITY_OUTPUT_PORT = "Service output port: ";
	public static final URI activityOutputPortSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/activityOutputPort");
	private ContextualSelection contextualSelection;

	public ActivityOutputPortSection() {
		super(DefaultContextualMenu.DEFAULT_CONTEXT_MENU, 10,
				activityOutputPortSection);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		return getContextualSelection().getSelection() instanceof OutputProcessorPort;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
		this.action = null;
	}

	@Override
	protected Action createAction() {
		OutputProcessorPort port = (OutputProcessorPort) getContextualSelection().getSelection();
		String name = ACTIVITY_OUTPUT_PORT + port.getName();
		return new DummyAction(name);
	}

}
