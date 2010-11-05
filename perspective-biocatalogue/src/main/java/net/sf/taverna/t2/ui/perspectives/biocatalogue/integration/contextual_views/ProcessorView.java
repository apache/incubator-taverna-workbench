package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.contextual_views;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.activation.UnknownObjectException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.biocatalogue.x2009.xml.rest.Service;
import org.biocatalogue.x2009.xml.rest.SoapOperation;

import net.sf.taverna.biocatalogue.model.BioCataloguePluginConstants;
import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.biocatalogue.model.SoapOperationIdentity;
import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.biocatalogue.ui.JClickableLabel;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponentFactory;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.Integration;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.health_check.ServiceHealthChecker;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.health_check.ServiceMonitoringStatusInterpreter;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Processor;


public class ProcessorView extends ContextualView {
	private final Processor processor;
	private JPanel jPanel;

	public ProcessorView(Processor processor) {
		this.processor = processor;
		
		jPanel = new JPanel();
		
		// this is required to have the body of this contextual
		// view added to the main view; otherwise, body will be
		// blank
		initView();
	}
	
	@Override
	public JComponent getMainFrame()
	{
	  new Thread("loading output port data") {
      public void run() {
        final SoapOperationIdentity operationDetails = Integration.extractSoapOperationDetailsFromProcessor(processor);
        
        if (operationDetails.hasError()) {
          jPanel.removeAll();
          jPanel.add(new JLabel(operationDetails.getErrorDetails().toString(),
                                UIManager.getIcon("OptionPane.warningIcon"), JLabel.CENTER));
        }
        else {
          BioCatalogueClient client = MainComponentFactory.getSharedInstance().getBioCatalogueClient();
          
          if (client != null) {
            try {
              SoapOperation soapOperation = client.lookupSoapOperation(operationDetails);
              if (soapOperation == null) throw new UnknownObjectException("This processor is not registered in BioCatalogue");
              
              Service parentService = client.getBioCatalogueService(soapOperation.getAncestors().getService().getHref());
              if (parentService == null) throw new UnknownObjectException("Problem while fetching monitoring data from BioCatalogue");
              
              
              // *** managed to get all necessary data successfully - present it ***
              
              // create status update panel
              JClickableLabel jclServiceStatus = new JClickableLabel(
                  "Latest monitoring status:", "testOperationStatus",
                  new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                      ServiceHealthChecker.checkWSDLProcessor(operationDetails);
                    }
                  });
              JLabel jlStatusMessage = new JLabel("<html>" + parentService.getLatestMonitoringStatus().getMessage() + "</html>");
              
              JPanel jpStatusMessage = new JPanel();
              jpStatusMessage.setAlignmentY(Component.CENTER_ALIGNMENT);
              jpStatusMessage.setLayout(new BoxLayout(jpStatusMessage, BoxLayout.Y_AXIS));
              jpStatusMessage.add(jclServiceStatus);
              jpStatusMessage.add(jlStatusMessage);
              
              JPanel jpServiceStatus = new JPanel();
              jpServiceStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
              jpServiceStatus.setLayout(new BoxLayout(jpServiceStatus, BoxLayout.X_AXIS));
              jpServiceStatus.add(new JLabel(ServiceMonitoringStatusInterpreter.getStatusIcon(parentService, false)));
              jpServiceStatus.add(Box.createHorizontalStrut(10));
              jpServiceStatus.add(jpStatusMessage);
              
              
              // operation description
              JLabel jlOperationDescription = new JLabel("<html><b>Description:</b><br>" +
                  (soapOperation.getDescription().length() > 0 ?
                   soapOperation.getDescription() :
                   "<span style=\"color: gray;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;no description is available for this processor</span>")
                  + "</html>", JLabel.LEFT);
              jlOperationDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
              
              
              // a button to open preview of the service
              JButton jbLaunchProcessorPreview = new JButton("Detailed Preview");
              jbLaunchProcessorPreview.setToolTipText("View detailed preview of this processor in a popup window");
              jbLaunchProcessorPreview.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  if (!operationDetails.hasError()) {
                    MainComponentFactory.getSharedInstance().getPreviewBrowser().preview(
                        BioCataloguePluginConstants.ACTION_PREVIEW_SOAP_OPERATION_AFTER_LOOKUP + operationDetails.toActionString());
                  }
                  else {
                    // this error message comes from Integration class extracting SOAP operation details from the contextual selection
                    JOptionPane.showMessageDialog(null, operationDetails.getErrorDetails(), "BioCatalogue Plugin - Error", JOptionPane.WARNING_MESSAGE);
                  }
                }
              });
              
              JPanel jpPreviewButtonPanel = new JPanel();
              jpPreviewButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
              jpPreviewButtonPanel.add(jbLaunchProcessorPreview);
              
              
              // put everything together
              JPanel jpInnerPane = new JPanel();
              jpInnerPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
              jpInnerPane.setLayout(new BoxLayout(jpInnerPane, BoxLayout.Y_AXIS));
              jpInnerPane.add(jpServiceStatus);
              jpInnerPane.add(Box.createVerticalStrut(10));
              jpInnerPane.add(jlOperationDescription);
              jpInnerPane.add(Box.createVerticalStrut(10));
              jpInnerPane.add(jpPreviewButtonPanel);
              
              JScrollPane spInnerPane = new JScrollPane(jpInnerPane);
              
              jPanel.removeAll();
              jPanel.add(spInnerPane);
              jPanel.setPreferredSize(new Dimension(200,130));
            }
            catch (UnknownObjectException e) {
              // either SoapOperation or its parent Service was not found in BioCatalogue - regular case
              jPanel.removeAll();
              jPanel.add(new JLabel(e.getMessage(),
                                    UIManager.getIcon("OptionPane.informationIcon"), JLabel.CENTER));
            }
            catch (Exception e) {
              // a real error occurred while fetching data about selected processor
              System.err.println("ERROR: unexpected problem while trying to ");
              e.printStackTrace();
              jPanel.removeAll();
              jPanel.add(new JLabel("<html>An unknown problem has prevented BioCatalogue Plugin from loading this preview</html>",
                                    UIManager.getIcon("OptionPane.errorIcon"), JLabel.CENTER));
            }
          }
          else {
            jPanel.removeAll();
            jPanel.add(new JLabel("<html><center>BioCatalogue Plugin has not initialised yet. Please wait and try again.</center></html>",
                                  UIManager.getIcon("OptionPane.warningIcon"), JLabel.CENTER));
          }
        }
        
        refreshView();
      }
	  }.start();
		
	  jPanel.removeAll();
    jPanel.setPreferredSize(new Dimension(200,50));
    jPanel.setLayout(new GridLayout());
    jPanel.add(new JLabel(ResourceManager.getImageIcon(ResourceManager.BAR_LOADER_ORANGE), JLabel.CENTER));
		return jPanel;
	}

	@Override
	public String getViewTitle() {
		return "BioCatalogue Information - Processor: " + processor.getLocalName();
	} 

	@Override
	public void refreshView()
	{
	  // this actually causes the parent container to validate itself,
	  // which is what is needed here
	  this.revalidate();
	  this.repaint();
	}
	
	@Override
	public int getPreferredPosition() {
		return BioCataloguePluginConstants.CONTEXTUAL_VIEW_PREFERRED_POSITION;
	}

}
