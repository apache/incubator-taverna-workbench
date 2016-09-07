/**********************************************************************
 **********************************************************************/
package org.apache.taverna.ui.menu.items.ports;

import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.InputWorkflowPort;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.items.activityport.AbstractConnectPortMenuActions;

public class ConnectDataflowInputPortMenuActions extends
		AbstractConnectPortMenuActions implements ContextualMenuComponent {

	public ConnectDataflowInputPortMenuActions() {
		super(WorkflowInputPortSection.inputPort, 20);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof InputWorkflowPort
				&& getContextualSelection().getParent() instanceof Workflow;
	}

}
