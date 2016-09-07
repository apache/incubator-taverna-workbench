/**********************************************************************
 **********************************************************************/
package org.apache.taverna.ui.menu.items.ports;

import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.OutputWorkflowPort;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.items.activityport.AbstractConnectPortMenuActions;

public class ConnectDataflowOutputPortMenuActions extends
		AbstractConnectPortMenuActions implements ContextualMenuComponent {

	public ConnectDataflowOutputPortMenuActions() {
		super(WorkflowOutputPortSection.outputPort, 20);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof OutputWorkflowPort
				&& getContextualSelection().getParent() instanceof Workflow;
	}

}
