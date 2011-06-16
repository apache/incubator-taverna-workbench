package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.contextual_views;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.rmi.activation.UnknownObjectException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.sf.taverna.biocatalogue.model.BioCataloguePluginConstants;
import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.biocatalogue.model.SoapOperationIdentity;
import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.t2.lang.ui.DeselectingButton;
import net.sf.taverna.t2.lang.ui.ReadOnlyTextArea;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponentFactory;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.Integration;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.health_check.ServiceHealthChecker;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.health_check.ServiceMonitoringStatusInterpreter;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.apache.log4j.Logger;
import org.biocatalogue.x2009.xml.rest.Service;
import org.biocatalogue.x2009.xml.rest.SoapOperation;


public class ProcessorView extends ContextualView {
	private final Processor processor;
	private JPanel jPanel;
	
	private static Logger logger = Logger.getLogger(ProcessorView.class);
	


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
	  Thread t = new Thread("loading processor data") {
      public void run() {
        final SoapOperationIdentity operationDetails = Integration.extractSoapOperationDetailsFromProcessor(processor);
        
        if (operationDetails.hasError()) {
        	SwingUtilities.invokeLater(new RefreshThread(new JLabel(operationDetails.getErrorDetails().toString(),
                    UIManager.getIcon("OptionPane.warningIcon"), JLabel.CENTER)));
           return;
        }
        else {
          BioCatalogueClient client = BioCatalogueClient.getInstance();
          
          if (client != null) {
            try {
              final SoapOperation soapOperation = client.lookupSoapOperation(operationDetails);
              if (soapOperation == null) {
            	  SwingUtilities.invokeLater(new RefreshThread(new JLabel("This service is not registered in BioCatalogue",
                          UIManager.getIcon("OptionPane.warningIcon"), JLabel.CENTER)));
                 return;
              }
              
              Service parentService = client.getBioCatalogueService(soapOperation.getAncestors().getService().getHref());
              if (parentService == null) {
               	  SwingUtilities.invokeLater(new RefreshThread(new JLabel("Problem while fetching monitoring data from BioCatalogue",
                          UIManager.getIcon("OptionPane.warningIcon"), JLabel.CENTER)));
                 return;
              }
              
              
              // *** managed to get all necessary data successfully - present it ***
              
              // create status update panel
              JButton jclServiceStatus = new DeselectingButton(
                  new AbstractAction("Check monitoring status") {
                    public void actionPerformed(ActionEvent e) {
                      ServiceHealthChecker.checkWSDLProcessor(operationDetails);
                    }
                  });
              jclServiceStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
              JLabel jlStatusMessage = new JLabel(parentService.getLatestMonitoringStatus().getMessage());
              jlStatusMessage.setAlignmentX(Component.LEFT_ALIGNMENT);             
              
              // operation description
              String operationDescription = (soapOperation.getDescription().length() > 0 ?
                      soapOperation.getDescription() :
                      "No description is available for this service");

              ReadOnlyTextArea jlOperationDescription = new ReadOnlyTextArea(operationDescription);
 
              jlOperationDescription.setAlignmentX(Component.LEFT_ALIGNMENT);              
              
              // a button to open preview of the service
              JButton jbLaunchProcessorPreview = new DeselectingButton("Show on BioCatalogue",
            		  new ActionListener() {
                  public void actionPerformed(ActionEvent e) {
                    if (!operationDetails.hasError()) {
                  	  String hrefString = soapOperation.getHref();
     				   try {
  						Desktop.getDesktop().browse(new URI(hrefString));
  					    }
  					    catch (Exception ex) {
  					      logger.error("Failed while trying to open the URL in a standard browser; URL was: " +
  					           hrefString + "\nException was: " + ex + "\n" + ex.getStackTrace());
  					    };
                    }
                    else {
                      // this error message comes from Integration class extracting SOAP operation details from the contextual selection
                      JOptionPane.showMessageDialog(null, operationDetails.getErrorDetails(), "BioCatalogue Plugin - Error", JOptionPane.WARNING_MESSAGE);
                    }
                  }
                },
                "View this service on BioCatalogue");
              
              JPanel jpPreviewButtonPanel = new JPanel();
              jpPreviewButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
              jbLaunchProcessorPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
 //             jpPreviewButtonPanel.add(jbLaunchProcessorPreview);
             // put everything together
              JPanel jpInnerPane = new JPanel();
              jpInnerPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
              jpInnerPane.setLayout(new BoxLayout(jpInnerPane, BoxLayout.Y_AXIS));
              jpInnerPane.add(jlOperationDescription);
              jpInnerPane.add(Box.createVerticalStrut(10));
              jpInnerPane.add(jlStatusMessage);
              jpInnerPane.add(Box.createVerticalStrut(10));
              jpInnerPane.add(jclServiceStatus);
              jpInnerPane.add(Box.createVerticalStrut(10));
              jpInnerPane.add(jbLaunchProcessorPreview);
             
              JScrollPane spInnerPane = new JScrollPane(jpInnerPane);
              
              SwingUtilities.invokeLater(new RefreshThread(spInnerPane));
              return;
           }
            catch (UnknownObjectException e) {
            	SwingUtilities.invokeLater(new RefreshThread(new JLabel(e.getMessage(),
                        UIManager.getIcon("OptionPane.informationIcon"), JLabel.CENTER)));
            	return;
             }
            catch (Exception e) {
              // a real error occurred while fetching data about selected processor
             logger.error("ERROR: unexpected problem while trying to ", e);
             SwingUtilities.invokeLater(new RefreshThread(new JLabel("An unknown problem has prevented BioCatalogue Plugin from loading this preview",
                     UIManager.getIcon("OptionPane.errorIcon"), JLabel.CENTER)));
             return;
            }
          }
          else {
        	  SwingUtilities.invokeLater(new RefreshThread(new JLabel("BioCatalogue Plugin has not initialised yet. Please wait and try again.",
                                  UIManager.getIcon("OptionPane.warningIcon"), JLabel.CENTER)));
        	  return;
         }
        }
      }
	  };
		
	  jPanel.removeAll();
    jPanel.setPreferredSize(new Dimension(200,200));
    jPanel.setLayout(new GridLayout());
    jPanel.add(new JLabel(ResourceManager.getImageIcon(ResourceManager.BAR_LOADER_ORANGE), JLabel.CENTER));
	  t.start();
		return jPanel;
	}

	@Override
	public String getViewTitle() {
		return "BioCatalogue Information";
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
	
	class RefreshThread extends Thread {
		private final Component component;

		public RefreshThread (Component component) {
			this.component = component;		
		}
		
		public void run() {
			jPanel.removeAll();
			if (component != null) {
				jPanel.add(component);
			}
			refreshView();
		}
	}

}
