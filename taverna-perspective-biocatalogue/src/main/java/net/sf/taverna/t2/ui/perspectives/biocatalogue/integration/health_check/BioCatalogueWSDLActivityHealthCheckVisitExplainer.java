package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.health_check;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.biocatalogue.model.SoapOperationIdentity;
import net.sf.taverna.t2.lang.ui.ReadOnlyTextArea;
import net.sf.taverna.t2.visit.VisitKind;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.workbench.report.explainer.VisitExplainer;

// import status constants
import static net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.health_check.BioCatalogueWSDLActivityHealthCheck.*;

/**
 * 
 * @author Sergejs Aleksejevs
 */
public class BioCatalogueWSDLActivityHealthCheckVisitExplainer implements VisitExplainer
{
  
  public boolean canExplain(VisitKind vk, int resultId) {
    return (vk instanceof BioCatalogueWSDLActivityHealthCheck);
  }
  
  
  /**
   * This class only handles {@link VisitReport} instances that are of
   * {@link BioCatalogueWSDLActivityHealthCheck} kind. Therefore, decisions on
   * the explanations / solutions are made solely by visit result IDs.
   */
  public JComponent getExplanation(final VisitReport vr)
  {
    int resultId = vr.getResultId();
    String explanation = null;
    
    switch (resultId) {
      case MESSAGE_IN_VISIT_REPORT:
        explanation = (String) vr.getProperty(EXPLANATION_MSG_PROPERTY); break;
        
      default:
        explanation = "Unknown issue - no expalanation available"; break;
    }
    
    
    JButton bRunBioCatalogueHealthCheck = new JButton("View monitoring status details");
    bRunBioCatalogueHealthCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SoapOperationIdentity soapOpIdentity = 
              new SoapOperationIdentity((String)vr.getProperty(BioCatalogueWSDLActivityHealthCheck.WSDL_LOCATION_PROPERTY),
                                        (String)vr.getProperty(BioCatalogueWSDLActivityHealthCheck.OPERATION_NAME_PROPERTY), null);
        
        ServiceHealthChecker.checkWSDLProcessor(soapOpIdentity);
      }
    });
    JPanel jpButton = new JPanel();
    jpButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    jpButton.add(bRunBioCatalogueHealthCheck);
    jpButton.setOpaque(false);
    
    JPanel jpExplanation = new JPanel(new BorderLayout());
    jpExplanation.add(new ReadOnlyTextArea(explanation), BorderLayout.CENTER);
    jpExplanation.add(jpButton, BorderLayout.SOUTH);
    
    return (jpExplanation);
  }
  
  
  
  /**
   * This class only handles {@link VisitReport} instances that are of
   * {@link BioCatalogueWSDLActivityHealthCheck} kind. Therefore, decisions on
   * the explanations / solutions are made solely by visit result IDs.
   */
  public JComponent getSolution(VisitReport vr)
  {
    String explanation = null;
    
    // instead of switching between possible health check resultIDs,
    // simply choose from possible statuses: for all failures there's
    // nothing specific that can be done, so no need to differentiate
    // displayed messages
    switch (vr.getStatus()) {
      case OK:
        explanation = "This WSDL service works fine - no change necessary"; break;
        
      case WARNING:
      case SEVERE:
        explanation = "This remote WSDL service appears to have an internal problem. There is nothing " +
        		          "specific that can be done to fix it locally.\n\n" +
        		          "It is possible that the current state of the service will still allow to execute " +
        		          "the workflow successfully. Also, the service may have already recovered since the " +
        		          "last time it's monitoring status has been checked.\n\n" +
        		          "If this problem does affect the current workflow, it may be resolved by the " +
        		          "service provider. It may be worth contacting them to report the issue.";
                      break;
      
      default:
        explanation = "Unknown issue - no solution available"; break;
    }
    
    return (new ReadOnlyTextArea(explanation));
  }
  
}
