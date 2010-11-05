package net.sf.taverna.biocatalogue.ui.previews;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.biocatalogue.x2009.xml.rest.Annotation;
import org.biocatalogue.x2009.xml.rest.Annotations;
import org.biocatalogue.x2009.xml.rest.Location;
import org.biocatalogue.x2009.xml.rest.ResourceLink;
import org.biocatalogue.x2009.xml.rest.ResourceLinkWithString;
import org.biocatalogue.x2009.xml.rest.ResourceType;
import org.biocatalogue.x2009.xml.rest.Service;
import org.biocatalogue.x2009.xml.rest.ServiceSummary;
import org.biocatalogue.x2009.xml.rest.ServiceTechnologyType;
import org.biocatalogue.x2009.xml.rest.SoapInput;
import org.biocatalogue.x2009.xml.rest.SoapOperation;
import org.biocatalogue.x2009.xml.rest.SoapOutput;
import org.biocatalogue.x2009.xml.rest.SoapService;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;

import com.lowagie.text.Font;

import net.sf.taverna.biocatalogue.model.BioCataloguePluginConstants;
import net.sf.taverna.biocatalogue.model.Resource;
import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.biocatalogue.model.ResourcePreviewContent;
import net.sf.taverna.biocatalogue.model.SoapOperationIdentity;
import net.sf.taverna.biocatalogue.model.StringToInputStreamConverter;
import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.biocatalogue.ui.JClickableLabel;
import net.sf.taverna.biocatalogue.ui.JWaitDialog;
import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponent;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.health_check.ServiceMonitoringStatusInterpreter;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.service_panel.BioCatalogueServiceProvider;


public class ServicePreviewFactory
{
  public static final int SERVICE_OVERVIEW_PANEL_PREFERRED_WIDTH = 600;
  public static final int SERVICE_OVERVIEW_PANEL_PREFERRED_HEIGHT = 500;
  
  public static final int OPERATION_PREVIEW_PANEL_PREFERRED_WIDTH = 600;
  public static final int OPERATION_PREVIEW_PANEL_PREFERRED_HEIGHT = 300;
  
  
  private final BioCatalogueClient client;
  private final Logger logger;
  private final ActionListener clickHandler;
  private final String previewActionCommand;
  private final String serviceURL;
  private final String operationNameToInitialise;

  
  // main UI components
  private JPanel jpPreviewMainPanel;
  
  private JTabbedPane tpTabs;
  private JPanel jpOverview;
  private JScrollPane spTagsAndCategories;
  private JPanel jpTagsAndCategories;
  private JPanel jpOperations;
  private XHTMLPanel xhtmlPanel;
  private FSScrollPane xhtmlPanelScrollPane;
  
  private JPanel jpCurrentOperationPreview;
  private XHTMLPanel xhtmlOperationPreview;
  private FSScrollPane xhtmlOperationPreviewScrollPane;
  
  
  // previewed service
  private Service service;
  private SoapService soapService;
  
  
  /**
   * Default constructor.
   * 
   * @param client BioCatalogue API client ready to process requests.
   * @param logger The logger to use.
   * @param clickHandler ActionListener that will take care of clicks on any items within this preview - mostly
   *                     JClickableLabel instances.
   * @param previewActionCommand Command of the service to be previewed (e.g. "preview:http://....")
   * @param operationNameToInitialise Operation with this name will be initialised on load. If <code>null</code>
   *                                  is provided or the operation is not found, the first on the operation list
   *                                  will be initialised.
   */
  public ServicePreviewFactory(BioCatalogueClient client, Logger logger, ActionListener clickHandler,
      String servicePreviewActionCommand, String operationNameToInitialise) {
    this.client = client;
    this.logger = logger;
    this.clickHandler = clickHandler;
    this.previewActionCommand = servicePreviewActionCommand;
    this.operationNameToInitialise = operationNameToInitialise;
    
    this.serviceURL = Resource.extractPureResourceURLFromPreviewActionCommand(servicePreviewActionCommand);
  }
  
  
  public ResourcePreviewContent makePreview()
  {
    initUI();
    initData();
    
    return (new ResourcePreviewContent(new Resource(this.previewActionCommand, service.getName()), jpPreviewMainPanel));
  }


  private void initUI()
  {
    // create XHTML panel to show the general details about a service
    xhtmlPanel = new XHTMLPanel();
    xhtmlPanel.getSharedContext().getTextRenderer().setSmoothingThreshold(0); // Anti-aliasing for all font sizes
    for (Object o : xhtmlPanel.getMouseTrackingListeners()) {
      // remove all default link listeners, as we don't need the XHTMLPanel
      // to navigate to the 'new' page automatically via any clicked link
      if (o instanceof LinkListener) {
        xhtmlPanel.removeMouseTrackingListener((FSMouseListener)o);
      }
    }
    xhtmlPanel.addMouseTrackingListener(new LinkListener() {
      public void onMouseUp(BasicPanel panel, Box box) {
        if (box.getElement().getTagName() == "a") {
          // this will 'catch' clicks on the tag URLs and dispatch the processing
          // of that click to the relevant handler in order to initiate search by tag
          clickHandler.actionPerformed(new ActionEvent(JClickableLabel.getDummyInstance(), 0, box.getElement().getAttribute("href")));
        }
      }
    });
    
    xhtmlPanelScrollPane = new FSScrollPane(xhtmlPanel);
    xhtmlPanelScrollPane.setPreferredSize(new Dimension(
        SERVICE_OVERVIEW_PANEL_PREFERRED_WIDTH,
        SERVICE_OVERVIEW_PANEL_PREFERRED_HEIGHT));
    xhtmlPanelScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    xhtmlPanelScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    
    // this panel holds a "spinner" while data loads and XHTMLPanel with preview when done
    jpOverview = new JPanel();
    jpOverview.setLayout(new GridBagLayout());
    jpOverview.add(new JLabel(ResourceManager.getImageIcon(ResourceManager.BAR_LOADER_ORANGE)), 
                   new GridBagConstraints(0, 0, 1, 1, 0.5, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0)
                  );
    
    // *** Contents of the tabs in the bottom part of the preview ***
    jpTagsAndCategories = new JPanel();
    jpTagsAndCategories.setLayout(new GridBagLayout());
    jpTagsAndCategories.add(new JLabel(ResourceManager.getImageIcon(ResourceManager.BAR_LOADER_ORANGE)), 
               new GridBagConstraints(0, 0, 1, 1, 0.5, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0)
              );
    spTagsAndCategories = new JScrollPane(jpTagsAndCategories);
    spTagsAndCategories.getVerticalScrollBar().setUnitIncrement(BioCataloguePluginConstants.DEFAULT_SCROLL);
    
    jpOperations = new JPanel();
    jpOperations.add(new JLabel("Loading operations..."));
    
    
    // *** Put everything together ***
    
    tpTabs = new JTabbedPane();
    tpTabs.add("Operations", jpOperations); jpOperations.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    tpTabs.add("Categories & Tags", spTagsAndCategories); jpTagsAndCategories.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    
    // overview with general info at the top, tabs at the bottom
    JSplitPane spMainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
    spMainSplitPane.setResizeWeight(0.66); // give a bit more space to the overview
    spMainSplitPane.add(jpOverview);
    spMainSplitPane.add(tpTabs);
    
    this.jpPreviewMainPanel = new JPanel(new GridLayout());
    this.jpPreviewMainPanel.add(spMainSplitPane);
  }
  
  
  private void initData()
  {
    try {
      // TODO - following code assumes that there is only one soap service...
      service = client.getBioCatalogueServiceSummary(serviceURL);
      
      final boolean isSoapService = service.getServiceTechnologyTypes().getTypeArray(0).intValue()
                                    == ServiceTechnologyType.INT_SOAP;
      if (isSoapService) {
        // this is a SOAP service, can fetch further info
        soapService = client.getBioCatalogueSoapService(service.getVariants().getSoapServiceArray(0).getHref());
      }
      
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          // set the content
          try {
            // determine which content to show in the main part of the service preview
            String serviceOverview = (isSoapService ?
                                      constructServiceOverviewHTML(service, soapService) :
                                      "<html><table style=\"width: 100%; height: 20em;\"><tr>" +
                                      "<td style=\"vertical-align: middle; text-align: center;\">" +
                                      "<img style=\"vertical-align: middle; margin-right: 5px;\" src=\"" + ResourceManager.getResourceLocalURL(ResourceManager.INFORMATION_ICON_LARGE) + "\"/>" +
                                      "Previews are only available for SOAP services at the moment" +
                                      "</td></tr></table></html>");
            
            // set service overview
            StringToInputStreamConverter converter = new StringToInputStreamConverter(serviceOverview);
            xhtmlPanel.setDocument(converter.getInputStream(), System.getProperty("user.dir"));
            xhtmlPanel.validate();
            converter.closeAllStreams();  // close all streams
            
            jpOverview.removeAll();
            jpOverview.setLayout(new BorderLayout());
            jpOverview.add(xhtmlPanelScrollPane, BorderLayout.CENTER);
          }
          catch (Exception e) {
            // most likely the data wasn't escaped properly, XHTML renderer refused to
            // accept prepared HTML document
            jpOverview.removeAll();
            jpOverview.add(new JLabel("<html>Some data in this preview was not correctly prepared;<br>" +
            		                      "most likely cause is that it contained some illegal<br>characters " +
            		                      "that couldn't be escaped.<br><br>This is a known problem and will " +
            		                      "be dealt with in future releases.</html>",
            		                      UIManager.getIcon("OptionPane.errorIcon"),
            		                      JLabel.CENTER));
            
            e.printStackTrace();
          }
          
          
          // *** Start further processing to display additional data ***
          // (this is done separately from the main preview to make the whole preview more fail-safe)
          
          // set tags
          fillServiceTagsAndCategoriesTab(service);
          
          // set operations
          fillServiceOperationsTab(soapService);
        }

      });
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
  }
  
  
  private String constructServiceOverviewHTML(Service service, SoapService soapService) throws Exception
  {
    ServiceSummary serviceSummary = service.getSummary();
    
    StringBuilder out = new StringBuilder();
    out.append(
      "<html>" +
        "<head>" +
          "<link href=\"" + ResourceManager.getResourceLocalURL(ResourceManager.STYLES_CSS) + "\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\" />" +
        "</head>" +
        "<body>" +
          "<h1 class=\"service_title\">" +
            "<img style=\"margin-right: 0.3em;\" src=\"" + ResourceManager.getResourceLocalURL(ResourceManager.SERVICE_ICON) + "\"/>" +
            service.getName() +
          "</h1>" +
          "<table style=\"width: 100%;\">" +
            "<tr><td style=\"text-align: left; vertical-align: top;\">" +
              generateAkaNamesHTML(service) +
              generateServiceTypesHTML(service) +
            "</td><td style=\"width: 20%; text-align: right;\">" +
              "<table style=\"text-align: center; float: right;\">" +
                "<tr><td><img src=\"" + ServiceMonitoringStatusInterpreter.getStatusIconURL(service, false) + "\"/></td></tr>" +
                "<tr><td><a class=\"service_status_details\" href=\"" + BioCataloguePluginConstants.ACTION_PREVIEWED_SERVICE_HEALTH_CHECK + "\">Status Details</a></td></tr>" +
              "</table>" +
            "</td></tr>" +
          "</table>" +
          "" +
          "<p style=\"border-top: 1px solid #000000; margin: 0;\"></p>" +
          "" +
          generateDescriptionsHTML(service, soapService) +
          "" +
          "<div class=\"annotation\">" +
            "<p class=\"key\">Provider</p>" +
            generateProviderListHTML(serviceSummary.getProviderList()) +
          "</div>" +
          "" +
          "<div class=\"annotation\">" +
            "<p class=\"key\">Location</p>" +
            generateLocationListHTML(serviceSummary.getLocationList()) +
          "</div>" +
          "" +
          "<div class=\"annotation\">" +
            "<p class=\"key\">Original Submitter / Source</p>" +
            "<p class=\"value\">" +
              "<img src=\"" + ResourceManager.getResourceLocalURL(service.getOriginalSubmitter().getResourceType() == ResourceType.USER ? ResourceManager.USER_ICON : ResourceManager.REGISTRY_ICON) + "\" class=\"link_icon\"/>" +
              "<a href=\"" + (BioCataloguePluginConstants.ACTION_PREVIEW_RESOURCE + service.getOriginalSubmitter().getHref()) + "\">" + service.getOriginalSubmitter().getResourceName() + "</a>" +
              "<span class=\"none_text\" style=\"margin-left: 0.7em; vertical-align: middle;\">(" + BioCatalogueClient.getShortDateFormatter().format(service.getCreated().getTime()) + ")</span>" +
            "</p>" +
          "</div>" +
          "" +
          "<div class=\"annotation\">" +
            "<p class=\"key\">Endpoint</p>" +
            generateValueListHTML(serviceSummary.getEndpointList()) +
          "</div>" +
          "" +
          "<div class=\"annotation\">" +
            "<p class=\"key\">WSDL Location</p>" +
            generateValueListHTML(serviceSummary.getWsdlList()) +
          "</div>" +
          "" +
          generateDocumentationHTML(soapService) +
          "" +
        "</body>" +
      "</html>"
     );
    
    return(out.toString());
  }
  
  
  private String generateAkaNamesHTML(Service service)
  {
    StringBuilder out = new StringBuilder();
    
    if (service.getSummary() != null && service.getSummary().getAlternativeNameList() != null &&
        service.getSummary().getAlternativeNameList().size() > 0)
    {
      out.append("<p class=\"left_indented service_aka_names\"><span class=\"none_text\">also known as:</span>");
      
      List<String> akaNames = service.getSummary().getAlternativeNameList();
      for (int i = 0; i < akaNames.size(); i++) {
        out.append("<span class=\"service_aka_name\"> " + akaNames.get(i) + "</span>");
      }
  
      out.append("</p>");
    }
    
    return(out.toString());
  }
  
  
  private String generateServiceTypesHTML(Service service) {
    StringBuilder out = new StringBuilder();
    out.append("<p class=\"left_indented service_types\"><span class=\"none_text\">Service type:</span>");
    
    for (ServiceTechnologyType.Enum e : service.getServiceTechnologyTypes().getTypeList()) {
       out.append("<a class=\"service_type_badge\" style=\"margin-left: 0.5em\" href=\"\">" + e.toString() + "</a>");
    }
    out.append("</p>");
    
    return (out.toString());
  }
  
  
// CURRENTLY NOT USED  
//  private String generateCategoriesHTML(Service service) {
//    StringBuilder out = new StringBuilder();
//    
//    out.append(
//      "<div class=\"categories_box\">" +
//        "<ul class=\"categories\">" +
//          "<li>" +
//            "<span class=\"title\">" +
//              "Categories:" +
//            "</span>" +
//          "</li>");
//    
//    
//    List<ResourceLinkWithString> foundCategories = service.getSummary().getCategoryList(); 
//    
//    if (foundCategories != null && foundCategories.size() > 0) {
//      for (ResourceLinkWithString category : foundCategories) {
//        out.append(     
//            "<li>" +
//              "<span class=\"main\">" +
//                "<a href=\"\" class=\"category\">" + category.getStringValue() +"</a>" +
//              "</span>" +
//            "</li>");
//      }
//    }
//    else {
//      out.append(
//          "<li>" +
//          "<span class=\"main\">" +
//            "<a href=\"\" class=\"category\">Unknown category</a>" +
//          "</span>" +
//        "</li>");
//    }
//
//    out.append(     
//        "</ul>" +
//      "</div>"
//    );
//    
//    return (out.toString());
//  }
  
  
  private String generateDescriptionsHTML(Service service, SoapService soapService)
  {
    boolean bHasWSDLDescription = (service != null && service.getDescription() != null &&
                                   service.getDescription().length() > 0);
    boolean bHasServiceSummaryDescriptions = (service != null && service.getSummary() != null &&
                                              service.getSummary().getDescriptionList() != null &&
                                              service.getSummary().getDescriptionList().size() > 0);
    
    StringBuilder out = new StringBuilder();
    out.append("<div class=\"annotation\">" +
                 "<p class=\"key\">Description(s)</p>");
    
    if (!bHasWSDLDescription && !bHasServiceSummaryDescriptions) {
      // no description
      out.append("<p class=\"value_with_submitter\">"+
                    "<span class=\"none_text\">No description available</span>" +
                 "</p>");
    }
    else
    {
      // description(s) come from the <soap_service> or <service>
      if (bHasWSDLDescription) {
        out.append("<p class=\"value_with_submitter\">" +  
                     "<span class=\"submitter\">" +
                       "<span style=\"vertical-align: middle;\">from</span>" +
                       "<img src=\"" + ResourceManager.getResourceLocalURL(ResourceManager.WSDL_DOCUMENT_ICON) + "\" class=\"link_icon\"/>" +
                       "from provider's description doc" +
                     "</span>" +
                     StringEscapeUtils.escapeXml(service.getDescription()) +
                   "</p>");
      }
      if (bHasServiceSummaryDescriptions) {
        for (String description : service.getSummary().getDescriptionList()) {
          if (!bHasWSDLDescription ||
              (bHasWSDLDescription && !service.getDescription().equals(description)))
          {
            out.append("<p class=\"value_with_submitter\">" +  
                "<span class=\"submitter\">" +
                  "<span style=\"vertical-align: middle;\">by</span>" +
                  "<img src=\"" + ResourceManager.getResourceLocalURL(soapService.getSubmitter().getResourceType() == ResourceType.USER ? ResourceManager.USER_ICON : ResourceManager.REGISTRY_ICON) + "\" class=\"link_icon\"/>" +
                  "<a href=\"" + (BioCataloguePluginConstants.ACTION_PREVIEW_RESOURCE + soapService.getSubmitter().getHref()) + "\">" + soapService.getSubmitter().getResourceName() + "</a>" +  // TODO - this is not submitter of annotation, but last editor of soap_service!
                "</span>" +
                StringEscapeUtils.escapeXml(description) +
              "</p>");
          }
        }
      }
    }
    
    out.append("</div>");
    return (out.toString());
  }
  
  
  private String generateDocumentationHTML(SoapService soapService)
  {
    StringBuilder out = new StringBuilder();
    
    if (soapService != null && soapService.getDocumentationUrl() != null)
    {
      // documentation URL comes from the <soap_service> submitter - so either a user or registry
      out.append(
        "<div class=\"annotation\">" +
          "<p class=\"key\">Documentation URL(s)</p>" +
          "<p class=\"value_with_submitter\">" +
            (soapService.getDocumentationUrl().length() > 0 ?
            "<span class=\"submitter\">" +
              "<span style=\"vertical-align: middle;\">by</span>" +
              "<img src=\"" + ResourceManager.getResourceLocalURL(soapService.getSubmitter().getResourceType() == ResourceType.USER ? ResourceManager.USER_ICON : ResourceManager.REGISTRY_ICON) + "\" class=\"link_icon\"/>" +
              "<a href=\"" + (BioCataloguePluginConstants.ACTION_PREVIEW_RESOURCE + soapService.getSubmitter().getHref()) + "\">" + soapService.getSubmitter().getResourceName() + "</a>" +
            "</span>" +
            "<a href=\"" + (BioCataloguePluginConstants.ACTION_SHOW_IN_WEB_BROWSER + soapService.getDocumentationUrl()) + "\">" + soapService.getDocumentationUrl() + "</a>" :
            "<span class=\"none_text\">No documentation available</span>") +
          "</p>" +
        "</div>"
      );
    }
    
    return (out.toString());
  }
  
  
  private String generateValueListHTML(List<String> valueList) {
    StringBuilder out = new StringBuilder();
    
    if (valueList != null && valueList.size() > 0) {
      for (String value : valueList) {
        out.append("<p class=\"value\"><a href=\"" + (BioCataloguePluginConstants.ACTION_SHOW_IN_WEB_BROWSER + value) + "\">" + value + "</a></p>");
      }
    }
    else {
      out.append("<p class=\"value\"><span class=\"none_text\">Value not set</span></p>");
    }
    
    return (out.toString());
  }
  
  
  private String generateProviderListHTML(List<ServiceSummary.Provider> providerList) {
    StringBuilder out = new StringBuilder();
    
    if (providerList != null && providerList.size() > 0) {
      for (ServiceSummary.Provider provider : providerList) {
        out.append("<p class=\"value\">" +
            "<img src=\"" + ResourceManager.getResourceLocalURL(ResourceManager.SERVICE_PROVIDER_ICON) + "\"/> " +
            "<a href=\"" + (BioCataloguePluginConstants.ACTION_PREVIEW_RESOURCE + provider.getHref()) + "\">" + provider.getName() + "</a></p>");
      }
    }
    else {
      out.append("<p class=\"value\"><span class=\"none_text\">Provider is unknown</span></p>");
    }
    
    return (out.toString());
  }
  
  
  private String generateLocationListHTML(List<Location> locationList) {
    StringBuilder out = new StringBuilder();
    
    if (locationList != null && locationList.size() > 0) {
      for (Location location : locationList) {
        String curLocation = "";
        curLocation += location.getCity();
        curLocation += (curLocation.length() > 0 && location.getCountry().length() > 0 ? ", " : "");
        curLocation += location.getCountry();
        if (curLocation.trim().length() == 0) curLocation = "Error: location entry exists, but holds no data...";
        
        out.append("<p class=\"value\">" +
                      (location.getFlag() != null ? "<img src=\"" + location.getFlag().getHref() + "\" class=\"link_icon\"/>" : "") +
        		          "<a href=\"\">" + curLocation + "</a>" +
        		        "</p>");
      }
    }
    else {
      out.append("<p class=\"value\"><span class=\"none_text\">Location is unknown</span></p>");
    }
    
    return (out.toString());
  }
  
  
  // **************************** Service Categories Tab *****************************************
  
  private void fillServiceTagsAndCategoriesTab(Service service)
  {
    jpTagsAndCategories.removeAll();
    jpTagsAndCategories.setLayout(new BoxLayout(jpTagsAndCategories, BoxLayout.Y_AXIS));
    
    boolean bHasSummary = (service.getSummary() != null);
    boolean bHasCategoryList = (bHasSummary && service.getSummary().getCategoryList() != null);
    boolean bHasTagList = (bHasSummary && service.getSummary().getTagList() != null);
    
    
    // *** Categories ***
    if (bHasCategoryList && service.getSummary().getCategoryList().size() > 0)
    {
      jpTagsAndCategories.add(new JLabel("Categories:"));
      for (ResourceLinkWithString category : service.getSummary().getCategoryList()) {
        jpTagsAndCategories.add(
            new JClickableLabel(category.getStringValue().trim(),
                                BioCataloguePluginConstants.ACTION_FILTER_BY_CATEGORY + category.getHref(),
                                this.clickHandler, ResourceManager.getImageIcon(ResourceManager.SERVICE_CATEGORY_ICON)));
      }
    }
    else
    {
      JLabel jlMsg = new JLabel("This service is not known to belong to any categories");
      jlMsg.setForeground(Color.GRAY);
      jlMsg.setFont(jlMsg.getFont().deriveFont(Font.ITALIC));
      
      jpTagsAndCategories.add(jlMsg);
    }
    
    // 15px vertical spacing
    jpTagsAndCategories.add(javax.swing.Box.createVerticalStrut(15));
    
    // *** Tags ***
    if (bHasTagList && service.getSummary().getTagList().size() > 0)
    {
      jpTagsAndCategories.add(new JLabel("Tags:"));
      for (ResourceLinkWithString tag : service.getSummary().getTagList()) {
        jpTagsAndCategories.add(
            new JClickableLabel(tag.getStringValue().trim(),
                                BioCataloguePluginConstants.ACTION_TAG_SEARCH_PREFIX + tag.getHref(),
                                this.clickHandler, ResourceManager.getImageIcon(ResourceManager.TAG_ICON)));
      }
    }
    else
    {
      JLabel jlMsg = new JLabel("This service has no associated tags");
      jlMsg.setForeground(Color.GRAY);
      jlMsg.setFont(jlMsg.getFont().deriveFont(Font.ITALIC));
      
      jpTagsAndCategories.add(jlMsg);
    }
    
    
  }
  
  
  // ***************************** Service Operations Tab *****************************************
  
  private void fillServiceOperationsTab(final SoapService soapService)
  {
    if (soapService == null) {
      jpOperations.removeAll();
      jpOperations.add(new JLabel("<html><span color=\"gray\">No data is available</span></html>"));
      jpOperations.validate();
      jpOperations.repaint();
      return;
    }
    
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        // *** Left component ****
        // list of operation names
        final JList jlOperationNames = new JList();
        jlOperationNames.addListSelectionListener(new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent e)
          {
            // NB! This check is a workaround for a known "bug"/"feature" in Java - ListSelectionEvent
            //     is fired twice when selection is done with a mouse: on MousePressed and on MouseReleased.
            //     (This always happens only once for selection change done via keyboard).
            //     Following test only succeeds once per selection change - both with mouse and keyboard.
            if (!jlOperationNames.getSelectionModel().getValueIsAdjusting())
            {
              SoapOperationIdentity selectedOpDetails = 
                SoapOperationIdentity.fromActionString(((JClickableLabel)jlOperationNames.getSelectedValue()).getData());
              
              // this traversal eliminates the need to do "lookup" of the SOAP operation by WSDL location + operation name;
              // instead we simply get the actual SOAP operation URL directly - more reliable and probably faster than lookup
              for (SoapOperation operation : soapService.getOperations().getSoapOperationList()) {
                if (operation.getName().equals(selectedOpDetails.getOperationName()))
                {
                  generateServiceOperationPreview(operation.getHref());
                  break;
                }
              }
            }
          }
        });
        JScrollPane spOperationNames = new JScrollPane(jlOperationNames);
        
        // label and surrounding panel
        ShadedLabel slOperations = new ShadedLabel("Soap operations", ShadedLabel.ORANGE);
        JPanel jpOperationNames = new JPanel(new BorderLayout());
        jpOperationNames.add(slOperations, BorderLayout.NORTH);
        jpOperationNames.add(spOperationNames, BorderLayout.CENTER);
        
        // *** Right component ***
        // toolbar
        JButton bInsertProcessor = new JButton(ResourceManager.getImageIcon(ResourceManager.ADD_PROCESSOR_TO_WORKFLOW_ICON));
        bInsertProcessor.setToolTipText("Insert this operation as a processor into the current workflow");
        bInsertProcessor.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e)
          {
            if (jlOperationNames.getSelectedValue() == null) {
              JOptionPane.showMessageDialog(null, "Please select an operation to add as a\n" +
              		"processor into the current workflow.", "BioCatalogue Plugin - Warning", JOptionPane.WARNING_MESSAGE);
            }
            else {
              // add this operation as a processor into the current workflow
              final JWaitDialog jwd = new JWaitDialog(MainComponent.dummyOwnerJFrame,
                  "Adding Processor",
                  "<html><center>Please wait for a processor to be added into<br>the current workflow.</center></html>");
              
              final SoapOperationIdentity soapOpToInsertDetails =
                        SoapOperationIdentity.fromActionString(((JClickableLabel)jlOperationNames.getSelectedValue()).getData());
              new Thread("Adding processor into workflow") {
                public void run() {
                  // FIXME - instantiate a ResourceLink object with HREF set to the URL of WSDL location
                  //         from soapOpToInsertDetails
//                  Integration.insertProcessorIntoCurrentWorkflow(soapOpToInsertDetails);
//                  jwd.waitFinished(new JLabel("The processor was added successfully.",
//                      ResourceManager.getImageIcon(ResourceManager.TICK_ICON), JLabel.CENTER));
                }
              }.start();
              
              // NB! The modal dialog window needs to be made visible after the background
              //     process (i.e. adding a processor) has already been started!
              jwd.setVisible(true);
            }
          }
        });
        JButton bFavouriteProcessor = new JButton(ResourceManager.getImageIcon(ResourceManager.ADD_PROCESSOR_AS_FAVOURITE_ICON));
        bFavouriteProcessor.setToolTipText("Add this operation into the main Taverna Service Panel");
        bFavouriteProcessor.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e)
          {
            if (jlOperationNames.getSelectedValue() == null) {
              JOptionPane.showMessageDialog(null, "Please select an operation to add into Service Panel.",
                  "BioCatalogue Plugin - Warning", JOptionPane.WARNING_MESSAGE);
            }
            else {
              // add operation into the service panel
              SoapOperationIdentity soapOpIdentity = 
                    SoapOperationIdentity.fromActionString(((JClickableLabel)jlOperationNames.getSelectedValue()).getData());
              BioCatalogueServiceProvider.registerNewWSDLOperation(soapOpIdentity);
            }
          }
        });
        
        JToolBar tbOperationActionsToolbar = new JToolBar(JToolBar.VERTICAL);
        tbOperationActionsToolbar.setFloatable(false);
        tbOperationActionsToolbar.add(bInsertProcessor);
        tbOperationActionsToolbar.add(bFavouriteProcessor);
        
        // operation preview XHTML panel
        xhtmlOperationPreview = new XHTMLPanel();
        xhtmlOperationPreview.getSharedContext().getTextRenderer().setSmoothingThreshold(0); // Anti-aliasing for all font sizes
        for (Object o : xhtmlOperationPreview.getMouseTrackingListeners()) {
          // remove all default link listeners, as we don't need the XHTMLPanel
          // to navigate to the 'new' page automatically via any clicked link
          if (o instanceof LinkListener) {
            xhtmlOperationPreview.removeMouseTrackingListener((FSMouseListener)o);
          }
        }
        xhtmlOperationPreview.addMouseTrackingListener(new LinkListener() {
          public void onMouseUp(BasicPanel panel, Box box) {
            if (box.getElement().getTagName() == "a") {
              // this will 'catch' clicks on the tag URLs and dispatch the processing
              // of that click to the relevant handler in order to initiate search by tag
              clickHandler.actionPerformed(new ActionEvent(JClickableLabel.getDummyInstance(), 0, box.getElement().getAttribute("href")));
            }
          }
        });
        
        xhtmlOperationPreviewScrollPane = new FSScrollPane(xhtmlOperationPreview);
        xhtmlOperationPreviewScrollPane.setPreferredSize(new Dimension(
            OPERATION_PREVIEW_PANEL_PREFERRED_WIDTH,
            OPERATION_PREVIEW_PANEL_PREFERRED_HEIGHT));
        xhtmlOperationPreviewScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        xhtmlOperationPreviewScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // operation preview container panel
        jpCurrentOperationPreview = new JPanel(new GridLayout());
        
        // total layout of right component
        JPanel jpOperationPreviewPlusToolbar = new JPanel(new BorderLayout());
        jpOperationPreviewPlusToolbar.add(jpCurrentOperationPreview, BorderLayout.CENTER);
        jpOperationPreviewPlusToolbar.add(tbOperationActionsToolbar, BorderLayout.EAST);
        
        
        // *** Put everything together ***
        JSplitPane spOperations = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        spOperations.setResizeWeight(0.1);
        spOperations.setLeftComponent(jpOperationNames);
        spOperations.setRightComponent(jpOperationPreviewPlusToolbar);
        
        jpOperations.removeAll();
        jpOperations.setLayout(new GridLayout());
        jpOperations.add(spOperations);
        
        Vector<JClickableLabel> ops = new Vector<JClickableLabel>();
        for (SoapOperation operation : soapService.getOperations().getSoapOperationList()) {
          ops.add(
              new JClickableLabel(operation.getName(),
                                  new SoapOperationIdentity(soapService.getWsdlLocation(),operation.getName(), null).toActionString(),
                                  null, /* it's ok to have no click handler - adding to this JClickableLabel to JList anyway, so will only use .toString() representation of it */
                                  ResourceManager.getIconFromTaverna(ResourceManager.SOAP_OPERATION_ICON)));
        }
        jlOperationNames.setListData(ops);
        
        // iterate through the operations to find one to initialise
        if (operationNameToInitialise != null && operationNameToInitialise.length() > 0) {
          for (int i = 0; i < ops.size(); i++) {
            if (operationNameToInitialise.equals(ops.get(i).getText())) {
              jlOperationNames.setSelectedIndex(i);
              break;
            }
          }
        }
        else {
          // the name of the operation to be initialised at preview load wasn't specified
          jlOperationNames.setSelectedIndex(0);
        }
        
        jpOperations.validate();
        jpOperations.repaint();
      }
    });
  }
  
  
  
  // *********************** Individual Operation Preview *****************************************
  
  /**
   * Populates preview of the currently selected SOAP operation.
   * 
   * @param operationURL URL of the SOAP operation to populate.
   */
  private void generateServiceOperationPreview(final String operationURL)
  {
    // turn into "loading" state
    jpCurrentOperationPreview.setBorder(BorderFactory.createLoweredBevelBorder());
    jpCurrentOperationPreview.removeAll();
    jpCurrentOperationPreview.add(new JLabel(ResourceManager.getImageIcon(ResourceManager.BAR_LOADER_ORANGE)));
    jpCurrentOperationPreview.validate();
    jpCurrentOperationPreview.repaint();
    
    new Thread("fetching SOAP operation details") {
      public void run()
      {
        // prepare values to use throught the preview generation
        boolean hasOperationData = true;
        boolean hasAnnotations = true;
        SoapOperation operation = null;
        Annotations annotations = null;
        
        // try to fetch the SOAP operation data first
        try {
          operation = client.getBioCatalogueSoapOperation(operationURL);
        }
        catch (Exception e) {
          hasOperationData = false;
          System.err.println("ERROR: failed to fetch SOAP operation data from " + operationURL + "; details: ");
          e.printStackTrace();
        }
        
        // if operation data was available, fetch annotations for the operation as well
        if (hasOperationData) {
          try {
            annotations = client.getBioCatalogueAnnotations(operation.getRelated().getAnnotationsOnAll().getHref());
          }
          catch (Exception e) {
            hasAnnotations = false;
            System.err.println("ERROR: failed to fetch annotations for SOAP operation (" + operationURL + "); details: ");
            e.printStackTrace();
          }
        }
        else {
          // operation data was unavailable - report error and terminate preview generation
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              jpCurrentOperationPreview.removeAll();
              jpCurrentOperationPreview.add(new JLabel("<html>An error has occurred while fetching data about this operations.<br>" +
              		                                     "Preview is not available.</html>"));
              jpCurrentOperationPreview.validate();
              jpCurrentOperationPreview.repaint();
            }
          });
          return;
        }
        
        
        // assume that operation data is available, but check just in case
        if (hasOperationData)
        {
          // start building the HTML document with operation preview
          StringBuilder sb = new StringBuilder();
          sb.append("<html>" +
          		        "<head>" +
          		          "<link href=\"" + ResourceManager.getResourceLocalURL(ResourceManager.STYLES_CSS) + "\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\" />" +
          		        "</head>" +
          		        "<body>" +
          		          "<h2 class=\"soap_operation_name\">" +
          		            "<img src=\"" + ResourceManager.getResourceLocalURL(ResourceManager.SOAP_OPERATION_ICON) + "\" style=\"margin-right: 0.1em; vertical-align: bottom;\"/>" +
          		            operation.getName() +
          		          "</h2>" +
          		          "<div class=\"annotation\">" +
          		            "<p class=\"key\">Description</p>" +
          		            "<p class=\"value\">" + (operation.getDescription().length() > 0 ? operation.getDescription() : "<span class=\"\">Description is not available</span>") + "</p>" +
          		          "</div>" +
          		          "<p>" +
          		            "<ul>" +
          		              "<li><b>Inputs: </b>" + operation.getInputs().getSoapInputList().size() + "</li>" +
          		              "<li><b>Outputs: </b>" + operation.getOutputs().getSoapOutputList().size() + "</li>" +
          		            "</ul>" +
          		          "</p>" +
          		          generateSoapInputsHTML(operation, annotations) +
          		          generateSoapOutputsHTML(operation, annotations) +
          		        "</body>" +
          		      "</html>");
          
          
          // HTML preview document generated, insert it into XHTML panel
          jpCurrentOperationPreview.removeAll();
          
          try {
            StringToInputStreamConverter converter = new StringToInputStreamConverter(sb.toString());
            xhtmlOperationPreview.setDocument(converter.getInputStream(), System.getProperty("user.dir"));
            xhtmlOperationPreview.validate();
            converter.closeAllStreams();  // close all streams
            
            jpCurrentOperationPreview.add(xhtmlOperationPreviewScrollPane);
          }
          catch (Exception e) {
            System.err.println("ERROR: failed to display generated operation preview HTML document in XHTML panel; details: ");
            e.printStackTrace();
            
            jpCurrentOperationPreview.add(new JLabel("An error occurred while attemtping to display generated SOAP operation preview."));
          }
          
          // update the UI container panel
          jpCurrentOperationPreview.setBorder(null);
          jpCurrentOperationPreview.validate();
          jpCurrentOperationPreview.repaint();
        }
      }
    }.start();
  }
  
  
  private String generateSoapInputsHTML(SoapOperation operation, Annotations annotations)
  {
    // build HTML preview
    StringBuilder sb = new StringBuilder();
    sb.append("<h3 class=\"soap_operation_inputs_or_outputs_section\">Inputs</h3>");
    
    if (operation.getInputs().getSoapInputList().size() > 0)
    {
      for (SoapInput si : operation.getInputs().getSoapInputList())
      {
        sb.append("<div class=\"soap_input_or_output\">" +
                    "<p class=\"name\">" + si.getName() + "</p>" +
                    generateSingleInputOrOutputAnnotationsHTML(si, annotations) +
                  "</div>");
      }
    }
    else {
      sb.append("<p class=\"none_text\" style=\"text-align: center;\">This operation has no inputs</p>");
    }
    
    return (sb.toString());
  }
  
  
  private String generateSoapOutputsHTML(SoapOperation operation, Annotations annotations)
  {
    // build HTML preview
    StringBuilder sb = new StringBuilder();
    sb.append("<h3 class=\"soap_operation_inputs_or_outputs_section\">Outputs</h3>");
    
    if (operation.getOutputs().getSoapOutputList().size() > 0)
    {
      for (SoapOutput so : operation.getOutputs().getSoapOutputList())
      {
        sb.append("<div class=\"soap_input_or_output\">" +
                    "<p class=\"name\">" + so.getName() + "</p>" +
                    generateSingleInputOrOutputAnnotationsHTML(so, annotations) +
                  "</div>");
      }
    }
    else {
      sb.append("<p class=\"none_text\" style=\"text-align: center;\">This operation has no outputs</p>");
    }
    
    return (sb.toString());
  }
  
  
  /**
   * @param inORout SoapInput or SoapOutput instance
   * @param annotations
   * @return
   */
  public static String generateSingleInputOrOutputAnnotationsHTML(ResourceLink inORout, Annotations annotations)
  {
    // filter annotations for the whole of the operation - and pick only relevant ones for the current input
    List<Annotation> annotationsForCurrent = new ArrayList<Annotation>();
    if (annotations != null) {
      for (Annotation a : annotations.getResults().getAnnotationList()) {
        if (inORout.getHref().equals(a.getAnnotatable().getHref())) {
          annotationsForCurrent.add(a);
        }
      }
    }
    
    // TODO - bad way to do this: currently expect only finite set of hardcoded annotations...
    Annotation annotationDescription = null;
    Annotation annotationFormat = null;
    Annotation annotationExampleData = null;
    for (Annotation a : annotationsForCurrent) {
      if (a.getAnnotationAttribute().getName().equalsIgnoreCase("description")) {
        annotationDescription = a;
      }
      else if (a.getAnnotationAttribute().getName().equalsIgnoreCase("format")) {
        annotationFormat = a;
      }
      else if (a.getAnnotationAttribute().getName().equalsIgnoreCase("example_data")) {
        annotationExampleData = a;
      }
    }
    
    
    StringBuilder sb = new StringBuilder();
    sb.append(  "<p class=\"body\">");
    
    // description
    boolean hasDescription = false;
    sb.append(    "<div class=\"annotation\">" +
                    "<p class=\"key\">Description:</p>");
    String descriptionFromXML = (inORout instanceof SoapInput ? ((SoapInput)inORout).getDescription() : ((SoapOutput)inORout).getDescription());
    if (descriptionFromXML.length() > 0) {
      sb.append(    "<p class=\"value\">" + descriptionFromXML + "</p>");
      hasDescription = true;
    }
    if (annotationDescription != null) {
      sb.append(    "<p class=\"value\">" + annotationDescription.getValue().getContent() + "</p>");
      hasDescription = true;
    }
    if (hasDescription == false) {
      sb.append(    "<p class=\"value\"><span class=\"none_text\">description is not available</span></p>");
    }
    sb.append(    "</div>");
    
    // data format
    sb.append(    "<div class=\"annotation\">" +
                    "<p class=\"key\">Data format:</p>");
    if (annotationFormat != null) {
      sb.append(    "<p class=\"value\">" + annotationFormat.getValue().getContent() + "</p>");
    }
    else {
      sb.append(    "<p class=\"value\"><span class=\"none_text\">unknown</span></p>");
    }
    sb.append(    "</div>");
    
    // example data
    sb.append(    "<div class=\"annotation\">" +
                    "<p class=\"key\">Example data:</p>");
    if (annotationExampleData != null) {
      sb.append(    "<p class=\"value\">" + StringEscapeUtils.escapeXml(annotationExampleData.getValue().getContent()) + "</p>");
    }
    else {
      sb.append(    "<p class=\"value\"><span class=\"none_text\">unavailable</span></p>");
    }
    sb.append(    "</div>");
    
    // computational type
    sb.append(    "<div class=\"annotation\">" +
                    "<p class=\"key\">Computational type:</p>");
    String computationalTypeFromXML = (inORout instanceof SoapInput ? ((SoapInput)inORout).getComputationalType() : ((SoapOutput)inORout).getComputationalType());
    if (computationalTypeFromXML.length() > 0) {
      sb.append(    "<p class=\"value\">" + computationalTypeFromXML + "</p>");
    }
    else {
      sb.append(    "<p class=\"value\"><span class=\"none_text\">unknown</span></p>");
    }
    sb.append(    "</div>");
    
    sb.append(  "</p>");
    
    return (sb.toString());
  }
  
}
