package org.apache.taverna.ui.perspectives.biocatalogue.integration.menus;

import java.awt.event.ActionEvent;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.biocatalogue.model.SoapOperationIdentity;
import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.ui.perspectives.biocatalogue.integration.Integration;
import org.apache.taverna.ui.perspectives.biocatalogue.integration.health_check.ServiceHealthChecker;
import org.apache.taverna.workflowmodel.Processor;


public class MenuActionProcessorHealthCheck extends AbstractContextualMenuAction {

  public MenuActionProcessorHealthCheck() throws URISyntaxException {
    super(BioCatalogueContextualMenuSection.BIOCATALOGUE_MENU_SECTION_ID, 20);
  }

  @SuppressWarnings("serial")
@Override
  protected Action createAction()
  {
    Action action = new AbstractAction("Service Health Check") {
      public void actionPerformed(ActionEvent e) {
        SoapOperationIdentity soapOperationDetails = Integration.extractSoapOperationDetailsFromProcessorContextualSelection(getContextualSelection());
        ServiceHealthChecker.checkWSDLProcessor(soapOperationDetails);
      }
    };
    action.putValue(Action.SHORT_DESCRIPTION, "Check monitoring status of this service");
    return (action);
  }

  @Override
  public boolean isEnabled()
  {
    // FIXME - this will only work for SOAP processors for now..
    boolean isEnabled = super.isEnabled() && getContextualSelection().getSelection() instanceof Processor;
    
    if (isEnabled) {
      SoapOperationIdentity soapOperationDetails = Integration.extractSoapOperationDetailsFromProcessorContextualSelection(getContextualSelection());
      isEnabled = !soapOperationDetails.hasError();
    }
    
    return isEnabled;
  }
	
	
}
