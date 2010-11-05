package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.contextual_views;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import org.biocatalogue.x2009.xml.rest.Annotations;
import org.biocatalogue.x2009.xml.rest.SoapInput;
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
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;


public class ProcessorInputPortView extends ContextualView
{
	private final ActivityInputPort inputPort;
	private JPanel jPanel;

	public ProcessorInputPortView(ActivityInputPort inputPort) {
		this.inputPort = inputPort;
		
		jPanel = new JPanel();
		
		// NB! This is required to have the body of this contextual
		// view added to the main view; otherwise, body will be
		// blank
		initView();
	}
	
	@Override
	public JComponent getMainFrame()
	{
		new Thread("loading input port data") {
		  public void run() {
		    SoapOperationPortIdentity inputPortDetails = Integration.extractSoapOperationPortDetailsFromActivityInputOutputPort(inputPort);
		    
		    // report an error...
		    if (inputPortDetails.hasError()) {
		      jPanel.removeAll();
		      jPanel.add(new JLabel(inputPortDetails.getErrorDetails().toString(),
		                            UIManager.getIcon("OptionPane.warningIcon"), JLabel.CENTER));
		    }
		    else {
		      // ...or attempt create a real preview
		      // but first lookup data about the input port
		      BioCatalogueClient client = MainComponentFactory.getSharedInstance().getBioCatalogueClient();
		      try {
            SoapInput soapInput = client.lookupSoapOperationPort(SoapInput.class, inputPortDetails);
            if (soapInput != null)
            {
              Annotations soapInputAnnotations = client.getBioCatalogueAnnotations(soapInput.getRelated().getAnnotations().getHref());
              
              StringBuilder sb = new StringBuilder();
              sb.append("<html>" +
                          "<head>" +
                            "<link href=\"" + ResourceManager.getResourceLocalURL(ResourceManager.STYLES_CSS) + "\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\" />" +
                          "</head>" +
                          "<body>" +
                            "<div class=\"soap_input_or_output\">" +
                              "<p class=\"name\">" + soapInput.getName() + "</p>" +
                              ServicePreviewFactory.generateSingleInputOrOutputAnnotationsHTML(soapInput, soapInputAnnotations) +
                            "</div>" +
                          "</body>" +
                        "</html>");
              
              
              XHTMLPanel xhtmlInputPreview = new XHTMLPanel();
              xhtmlInputPreview.getSharedContext().getTextRenderer().setSmoothingThreshold(0); // Anti-aliasing for all font sizes
              for (Object o : xhtmlInputPreview.getMouseTrackingListeners()) {
                // remove all default link listeners, as we don't need the XHTMLPanel
                // to navigate to the 'new' page automatically via any clicked link
                if (o instanceof LinkListener) {
                  xhtmlInputPreview.removeMouseTrackingListener((FSMouseListener)o);
                }
              }
              
              try {
                StringToInputStreamConverter converter = new StringToInputStreamConverter(sb.toString());
                xhtmlInputPreview.setDocument(converter.getInputStream(), System.getProperty("user.dir"));
                xhtmlInputPreview.validate();
                converter.closeAllStreams();  // close all streams
                
                FSScrollPane xhtmlInputPreviewScrollPane = new FSScrollPane(xhtmlInputPreview);
                xhtmlInputPreviewScrollPane.setPreferredSize(new Dimension(100, 100));
                xhtmlInputPreviewScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                xhtmlInputPreviewScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                
                jPanel.removeAll();
                jPanel.add(xhtmlInputPreviewScrollPane);
                
                jPanel.setPreferredSize(new Dimension(200,300));
              }
              catch (Exception e) {
                System.err.println("ERROR: failed to display generated operation preview HTML document in XHTML panel; details: ");
                e.printStackTrace();
                
                jPanel.removeAll();
                jPanel.add(new JLabel("<html><center>An error occurred while attemtping to display " +
                		                  "generated SOAP input port preview.</center></html>",
                		                  UIManager.getIcon("OptionPane.errorIcon"), JLabel.CENTER));
              }
            }
            else {
              // lookup didn't succeed
              jPanel.removeAll();
              jPanel.add(new JLabel("<html><center>Unable to find data about this input port in BioCatalogue.</center></html>",
                                    UIManager.getIcon("OptionPane.informationIcon"), JLabel.CENTER));
            }
          }
		      catch (Exception e) {
		        System.err.println("ERROR: something went wrong while fetching data on SOAP input; details:");
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
		return "BioCatalogue Information - Input port: " + inputPort.getName();
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
