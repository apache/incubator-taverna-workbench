package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.contextual_views;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import org.biocatalogue.x2009.xml.rest.Annotations;
import org.biocatalogue.x2009.xml.rest.SoapOutput;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;

import net.sf.taverna.biocatalogue.model.BioCataloguePluginConstants;
import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.biocatalogue.model.SoapOperationPortIdentity;
import net.sf.taverna.biocatalogue.model.StringToInputStreamConverter;
import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.biocatalogue.ui.previews.ServicePreviewFactory;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponentFactory;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.Integration;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;


public class ProcessorOutputPortView extends ContextualView
{
	private final ActivityOutputPort outputPort;
	private JPanel jPanel;

	public ProcessorOutputPortView(ActivityOutputPort outputPort) {
		this.outputPort = outputPort;
		
		jPanel = new JPanel();
		
		// NB! This is required to have the body of this contextual
		// view added to the main view; otherwise, body will be
		// blank
		initView();
	}
	
	@Override
	public JComponent getMainFrame()
	{
		new Thread("loading output port data") {
		  public void run() {
		    SoapOperationPortIdentity outputPortDetails = Integration.extractSoapOperationPortDetailsFromActivityInputOutputPort(outputPort);
		    
		    // report an error...
		    if (outputPortDetails.hasError()) {
		      jPanel.removeAll();
		      jPanel.add(new JLabel(outputPortDetails.getErrorDetails().toString(),
		                            UIManager.getIcon("OptionPane.warningIcon"), JLabel.CENTER));
		    }
		    else {
		      // ...or attempt create a real preview
		      // but first lookup data about the output port
		      BioCatalogueClient client = MainComponentFactory.getSharedInstance().getBioCatalogueClient();
		      try {
            SoapOutput soapOutput = client.lookupSoapOperationPort(SoapOutput.class, outputPortDetails);
            if (soapOutput != null)
            {
              Annotations soapOutputAnnotations = client.getBioCatalogueAnnotations(soapOutput.getRelated().getAnnotations().getHref());
              
              StringBuilder sb = new StringBuilder();
              sb.append("<html>" +
                          "<head>" +
                            "<link href=\"" + ResourceManager.getResourceLocalURL(ResourceManager.STYLES_CSS) + "\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\" />" +
                          "</head>" +
                          "<body>" +
                            "<div class=\"soap_input_or_output\">" +
                              "<p class=\"name\">" + soapOutput.getName() + "</p>" +
                              ServicePreviewFactory.generateSingleInputOrOutputAnnotationsHTML(soapOutput, soapOutputAnnotations) +
                            "</div>" +
                          "</body>" +
                        "</html>");
              
              
              XHTMLPanel xhtmlOutputPreview = new XHTMLPanel();
              xhtmlOutputPreview.getSharedContext().getTextRenderer().setSmoothingThreshold(0); // Anti-aliasing for all font sizes
              for (Object o : xhtmlOutputPreview.getMouseTrackingListeners()) {
                // remove all default link listeners, as we don't need the XHTMLPanel
                // to navigate to the 'new' page automatically via any clicked link
                if (o instanceof LinkListener) {
                  xhtmlOutputPreview.removeMouseTrackingListener((FSMouseListener)o);
                }
              }
              
              try {
                StringToInputStreamConverter converter = new StringToInputStreamConverter(sb.toString());
                xhtmlOutputPreview.setDocument(converter.getInputStream(), System.getProperty("user.dir"));
                xhtmlOutputPreview.validate();
                converter.closeAllStreams();  // close all streams
                
                FSScrollPane xhtmlOutputPreviewScrollPane = new FSScrollPane(xhtmlOutputPreview);
                xhtmlOutputPreviewScrollPane.setPreferredSize(new Dimension(100, 100));
                xhtmlOutputPreviewScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                xhtmlOutputPreviewScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                
                jPanel.removeAll();
                jPanel.add(xhtmlOutputPreviewScrollPane);
                
                jPanel.setPreferredSize(new Dimension(200,300));
              }
              catch (Exception e) {
                System.err.println("ERROR: failed to display generated operation preview HTML document in XHTML panel; details: ");
                e.printStackTrace();
                
                jPanel.removeAll();
                jPanel.add(new JLabel("<html><center>An error occurred while attemtping to display " +
                		                  "generated SOAP output port preview.</center></html>",
                		                  UIManager.getIcon("OptionPane.errorIcon"), JLabel.CENTER));
              }
            }
            else {
              // lookup didn't succeed
              jPanel.removeAll();
              jPanel.add(new JLabel("<html><center>Unable to find data about this output port in BioCatalogue.</center></html>",
                                    UIManager.getIcon("OptionPane.informationIcon"), JLabel.CENTER));
            }
          }
		      catch (Exception e) {
		        System.err.println("ERROR: something went wrong while fetching data on SOAP output; details:");
            e.printStackTrace();
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
		return "BioCatalogue Information - Output port: " + outputPort.getName();
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
