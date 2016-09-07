package org.apache.taverna.ui.menu.items.activityport;

import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.OutputProcessorPort;


public class ConnectOutputPortMenuActions extends AbstractConnectPortMenuActions  {

	public ConnectOutputPortMenuActions() {
		super(ActivityOutputPortSection.activityOutputPortSection, 20);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof OutputProcessorPort
				&& getContextualSelection().getParent() instanceof Workflow;
	}


}
