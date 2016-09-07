/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.reference.ui.referenceactions;

import java.util.Map;

import javax.swing.AbstractAction;

import org.apache.taverna.reference.ui.RegistrationPanel;

public interface ReferenceActionSPI {
	public void setInputPanelMap(Map<String, RegistrationPanel> inputPanelMap);

	/**
	 * Returns the action implementing this interface. The returned action will
	 * be bound to the appropriate UI component used to trigger the reference
	 * action.
	 */
	public AbstractAction getAction();
}
