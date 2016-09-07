package org.apache.taverna.ui.menu.items.activityport;

import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;

public class ConnectInputPortMenuActions extends AbstractConnectPortMenuActions
		implements ContextualMenuComponent {

	public ConnectInputPortMenuActions() {
		super(ActivityInputPortSection.activityInputPortSection, 20);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof InputProcessorPort
				&& getContextualSelection().getParent() instanceof Workflow;
	}

}
