package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.menus;

import java.awt.event.ActionEvent;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.biocatalogue.model.BioCataloguePluginConstants;
import net.sf.taverna.biocatalogue.model.SoapOperationIdentity;
import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponentFactory;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.Integration;


public class MenuActionProcessorPreview extends AbstractContextualMenuAction {

  public MenuActionProcessorPreview() throws URISyntaxException {
    super(BioCatalogueContextualMenuSection.BIOCATALOGUE_MENU_SECTION_ID, 10);
  }

  @SuppressWarnings("serial")
@Override
  protected Action createAction()
  {
    Action action = new AbstractAction("See service details") {
      public void actionPerformed(ActionEvent e) {
        SoapOperationIdentity soapOperationDetails = Integration.extractSoapOperationDetailsFromProcessorContextualSelection(getContextualSelection());
        
        if (!soapOperationDetails.hasError()) {
          MainComponentFactory.getSharedInstance().getPreviewBrowser().preview(
              BioCataloguePluginConstants.ACTION_PREVIEW_SOAP_OPERATION_AFTER_LOOKUP + soapOperationDetails.toActionString());
        }
        else {
          // this error message comes from Integration class extracting SOAP operation details from the contextual selection
          JOptionPane.showMessageDialog(null, soapOperationDetails.getErrorDetails(), "BioCatalogue Plugin - Error", JOptionPane.WARNING_MESSAGE);
        }
      }
    };
    action.putValue(Action.SHORT_DESCRIPTION, "Open preview for this service");
    return (action);
  }

  @Override
  public boolean isEnabled()
  {
//    // FIXME - this will only work for SOAP processors for now..
//    boolean isEnabled = super.isEnabled() && getContextualSelection().getSelection() instanceof Processor;
//    
//    if (isEnabled) {
//      SoapOperationIdentity soapOperationDetails = Integration.extractSoapOperationDetailsFromProcessorContextualSelection(getContextualSelection());
//      isEnabled = !soapOperationDetails.hasError();
//    }
//    
//    return isEnabled;
    
    // TODO - previews are currently not available, so this menu item is disabled for now
    return false;
  }
	
	
}
