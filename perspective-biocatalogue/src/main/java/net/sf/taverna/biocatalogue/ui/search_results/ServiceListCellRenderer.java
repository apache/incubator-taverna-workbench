package net.sf.taverna.biocatalogue.ui.search_results;


import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.biocatalogue.x2009.xml.rest.Location;
import org.biocatalogue.x2009.xml.rest.ResourceLinkWithString;
import org.biocatalogue.x2009.xml.rest.Service;
import org.biocatalogue.x2009.xml.rest.ServiceSummary.Provider;

import net.sf.taverna.biocatalogue.model.LoadingExpandedResource;
import net.sf.taverna.biocatalogue.model.LoadingResource;
import net.sf.taverna.biocatalogue.model.Resource;
import net.sf.taverna.biocatalogue.model.Resource.TYPE;
import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.biocatalogue.model.Util;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.health_check.ServiceMonitoringStatusInterpreter;


/**
 * This list cell renderer is will display (web) Service items in the search results
 * list box.
 * 
 * In the collapsed state, four data items will be shown per service: status (as per monitoring data),
 * service type (SOAP / REST), the name of the service and its description.
 * 
 * In the expanded state, other details are added to the ones above: service categories, locations,
 * endpoints and providers.
 * 
 * @author Sergejs Aleksejevs
 */
public class ServiceListCellRenderer extends ExpandableOnDemandLoadedListCellRenderer
{
  private JLabel jlTypeIcon;
  private JLabel jlItemStatus;
  private JLabel jlItemTitle;
  private JLabel jlDescription;
  
  private GridBagConstraints c;
  
  
  public ServiceListCellRenderer() {
    /* do nothing */
  }
  
  
  /**
   * This entry can be in one of two states:
   * -- containing only the name of the resource and NOT loading further details;
   * -- containing only the name of the resource and LOADING further details.
   * 
   * @param itemToRender
   * @return
   */
  protected GridBagConstraints prepareInitiallyLoadingEntry(Object itemToRender)
  {
    TYPE resourceType = determineResourceType(itemToRender);
    LoadingResource resource = (LoadingResource)itemToRender;
    
    jlTypeIcon = new JLabel(resourceType.getIcon());
    jlItemStatus = new JLabel(ResourceManager.getImageIcon(ResourceManager.SERVICE_STATUS_UNKNOWN_ICON));
    
    jlItemTitle = new JLabel(Resource.getDisplayNameForResource(resource), JLabel.LEFT);
    jlItemTitle.setForeground(Color.decode("#AD0000"));  // very dark red
    jlItemTitle.setFont(jlItemTitle.getFont().deriveFont(Font.PLAIN, jlItemTitle.getFont().getSize() + 2));
    
    jlDescription = new JLabel((resource.isLoading() ? loaderBarAnimationGrey : loaderBarAnimationGreyStill), JLabel.LEFT);
    
    return (arrangeLayout(false, false));
  }
  
  
  /**
   * 
   * @param itemToRender
   * @param expandedView <code>true</code> to indicate that this method generates the top
   *                     fragment of the expanded list entry for this SOAP operation / REST method.
   * @return
   */
  protected GridBagConstraints prepareLoadedCollapsedEntry(Object itemToRender, boolean expandedView)
  {
    TYPE resourceType = determineResourceType(itemToRender);
    Service service = (Service)itemToRender;;
    
    
    // service type
    if (service.getServiceTechnologyTypes() != null && service.getServiceTechnologyTypes().getTypeList().size() > 0)
    {
      if (service.getServiceTechnologyTypes().getTypeList().size() > 1 &&
           !(service.getServiceTechnologyTypes().getTypeList().get(0).toString().equalsIgnoreCase("SOAP")) && 
             service.getServiceTechnologyTypes().getTypeList().get(1).toString().equalsIgnoreCase("SOAPLAB")
         )
      {
        jlTypeIcon = new JLabel(ResourceManager.getImageIcon(ResourceManager.SERVICE_TYPE_MULTITYPE_ICON));
      }
      else if (service.getServiceTechnologyTypes().getTypeArray(0).toString().equalsIgnoreCase("SOAP")) {
        jlTypeIcon = new JLabel(ResourceManager.getImageIcon(ResourceManager.SERVICE_TYPE_SOAP_ICON));
      }
      else if (service.getServiceTechnologyTypes().getTypeArray(0).toString().equalsIgnoreCase("REST")) {
        jlTypeIcon = new JLabel(ResourceManager.getImageIcon(ResourceManager.SERVICE_TYPE_REST_ICON));
      }
    }
    else {
      // can't tell the type - just show as a service of no particular type
      jlTypeIcon = new JLabel(resourceType.getIcon());
    }
    
    
    // service status
    jlItemStatus = new JLabel(new ImageIcon(ServiceMonitoringStatusInterpreter.getStatusIconURL(service, true)));
    
    jlItemTitle = new JLabel(Resource.getDisplayNameForResource(service), JLabel.LEFT);
    jlItemTitle.setForeground(Color.decode("#AD0000"));  // very dark red
    jlItemTitle.setFont(jlItemTitle.getFont().deriveFont(Font.PLAIN, jlItemTitle.getFont().getSize() + 2));
    
    int descriptionMaxLength = (expandedView ? DESCRIPTION_MAX_LENGTH_EXPANDED : DESCRIPTION_MAX_LENGTH_COLLAPSED);
    String strDescription = (service.getDescription() == null || service.getDescription().length() == 0 ?
                             "<font color=\"gray\">no description</font>" :
                             Util.stripAllHTML(service.getDescription()));
    
    strDescription = Util.ensureLineLengthWithinString(strDescription, LINE_LENGTH, false);
    if (strDescription.length() > descriptionMaxLength) {
      strDescription = strDescription.substring(0, descriptionMaxLength) + "<font color=\"gray\">(...)</font>";
    }
    strDescription = "<html><b>Description: </b>" + strDescription + "</html>";
    jlDescription = new JLabel(strDescription);
    
    return (arrangeLayout(true, expandedView));
  }
  
  
  /**
   * @return Final state of the {@link GridBagConstraints} instance
   *         that was used to lay out components in the panel.
   */
  private GridBagConstraints arrangeLayout(boolean showActionButtons, boolean expanded)
  {
    // POPULATE PANEL WITH PREPARED COMPONENTS
    this.setLayout(new GridBagLayout());
    c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0;
    c.insets = new Insets(10, 6, 6, 3);
    this.add(jlTypeIcon, c);
    
    c.gridx++;
    c.insets = new Insets(10, 3, 6, 3);
    this.add(jlItemStatus, c);
    
    c.gridx++;
    c.weightx = 1.0;
    c.insets = new Insets(10, 3, 6, 3);
    this.add(jlItemTitle, c);
    
    if (showActionButtons) {
      c.gridx++;
      c.gridheight = 3;
      c.weightx = 0;
      c.weighty = 1.0;
      jlExpand = new JLabel(ResourceManager.getImageIcon((expanded ? ResourceManager.FOLD_ICON : ResourceManager.UNFOLD_ICON)));
      this.add(jlExpand, c);
    }
    
    c.gridx = 2;
    c.gridy++;
    c.gridheight = 1;
    c.weightx = 1.0;
    c.weighty = 0;
    c.insets = new Insets(3, 3, (expanded ? 3 : 12), 3);
    this.add(jlDescription, c);
    
    return (c);
  }
  
  
  
  protected void prepareLoadingExpandedEntry(Object itemToRender)
  {
    LoadingExpandedResource expandedResource = (LoadingExpandedResource) itemToRender;
    GridBagConstraints c = prepareLoadedCollapsedEntry(expandedResource.getAssociatedObj(), true);
    
    if (expandedResource.isLoading())
    {
      c.gridx = 0;
      c.gridy++;
      c.gridwidth = 3;
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1.0;
      this.add(new JLabel(loaderBarAnimationOrange, JLabel.CENTER), c);
    }
    else
    {
      // *** additional data for this Web Service operations ***
      Service service = (Service) expandedResource.getAssociatedObj();
      
      
      // -- categories --
      int categoryCount = service.getSummary().getCategoryList().size();
      String categoryString = "";
      if (categoryCount > 0) {
        List<String> categoryNames = new ArrayList<String>();
        for (ResourceLinkWithString category : service.getSummary().getCategoryList()) {
          categoryNames.add(category.getStringValue());
        }
        categoryString = "<html><b>" + Util.pluraliseNoun("Category", categoryCount) + ": </b>" + Util.join(categoryNames, ", ") + "</html>";
      }
      else {
        categoryString = "<html><b>Category: </b><font color=\"gray\">unknown</font></html>";
      }
      
      c.gridy++;
      this.add(new JLabel(categoryString),c);
      
      
      // -- endpoints --
      int endpointCount = service.getSummary().getEndpointList().size();
      String endpointString = "";
      if (endpointCount > 0) {
        endpointString = "<html><b>" + Util.pluraliseNoun("Endpoint", endpointCount) + ": </b>" +
                                Util.join(service.getSummary().getEndpointList(), ", ") + "</html>";
      }
      else {
        endpointString = "<html><b>Endpoint: </b><font color=\"gray\">unknown</font></html>";
      }
      
      c.gridy++;
      this.add(new JLabel(endpointString), c);
      
      
      // -- providers --
      int providerCount = service.getSummary().getProviderList().size();
      String providerString = "";
      if (providerCount > 0) {
        List<String> providerNames = new ArrayList<String>();
        for (Provider serviceProvider : service.getSummary().getProviderList()) {
          providerNames.add(serviceProvider.getName());
        }
        providerString = "<html><b>" + Util.pluraliseNoun("Provider", providerCount) + ": </b>" + Util.join(providerNames, ", ") + "</html>";
      }
      else {
        providerString = "<html><b>Provider: </b><font color=\"gray\">unknown</font></html>";
      }
      
      c.gridy++;
      this.add(new JLabel(providerString),c);
      
      
      // -- locations --
      int locationCount = service.getSummary().getLocationList().size();
      String locationString = "";
      List<String> locations = new ArrayList<String>();
      if (locationCount > 0) {
        for (Location location : service.getSummary().getLocationList()) {
          List<String> locationNameFragments = new ArrayList<String>();
          locationNameFragments.add(location.getCity());
          locationNameFragments.add(location.getCountry());
          locations.add(Util.join(locationNameFragments, ", "));
        }
      }
      locationString = "<html><b>" + Util.pluraliseNoun("Location", locations.size()) + ": </b>" +
      (locations.size() > 0 ? Util.join(locations, "; ") : "<font color=\"gray\">unknown</font>") +
      "</html>";
      
      c.gridy++;
      c.insets = new Insets(3, 3, 12, 3);
      this.add(new JLabel(locationString),c);
    }
  }
  
}
