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

import net.sf.taverna.biocatalogue.model.LoadingExpandedResource;
import net.sf.taverna.biocatalogue.model.LoadingResource;
import net.sf.taverna.biocatalogue.model.Resource;
import net.sf.taverna.biocatalogue.model.Resource.TYPE;
import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.biocatalogue.model.Util;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.health_check.ServiceMonitoringStatusInterpreter;

import org.biocatalogue.x2009.xml.rest.Service;
import org.biocatalogue.x2009.xml.rest.ServiceTechnologyType;
import org.biocatalogue.x2009.xml.rest.SoapInput;
import org.biocatalogue.x2009.xml.rest.SoapOperation;
import org.biocatalogue.x2009.xml.rest.SoapOutput;
import org.biocatalogue.x2009.xml.rest.SoapService;
import org.biocatalogue.x2009.xml.rest.Service.ServiceTechnologyTypes;
import org.biocatalogue.x2009.xml.rest.ServiceTechnologyType.Enum;
import org.biocatalogue.x2009.xml.rest.SoapOperation.Ancestors;


/**
 * 
 * 
 * @author Sergejs Aleksejevs
 */
@SuppressWarnings("serial")
public class SOAPOperationListCellRenderer extends ExpandableOnDemandLoadedListCellRenderer
{
  private JLabel jlTypeIcon;
  private JLabel jlItemStatus;
  private JLabel jlItemTitle;
  private JLabel jlPartOf;
  private JLabel jlWsdlLocation;
  private JLabel jlDescription;
  
  private GridBagConstraints c;
  
  private static Resource.TYPE resourceType = Resource.TYPE.SOAPOperation;
  
  
  public SOAPOperationListCellRenderer() {
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
    LoadingResource resource = (LoadingResource)itemToRender;
    
    jlTypeIcon = new JLabel(resourceType.getIcon());
    jlItemStatus = new JLabel(new ImageIcon(ResourceManager.getResourceLocalURL(ResourceManager.SERVICE_STATUS_UNCHECKED_ICON_LARGE)));
       
    jlItemTitle = new JLabel("<html>" + Resource.getDisplayNameForResource(resource) + "<font color=\"gray\"><i>- fetching more information</i></font></html>", JLabel.LEFT);
    jlItemTitle.setFont(jlItemTitle.getFont().deriveFont(Font.PLAIN, jlItemTitle.getFont().getSize() + 2));
   
    jlPartOf = new JLabel(" ");
    jlWsdlLocation = new JLabel(" ");
    jlDescription = new JLabel(" ");
    
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
    SoapOperation soapOp = (SoapOperation)itemToRender;
    
    Ancestors ancestors = soapOp.getAncestors();
    SoapService soapService = ancestors.getSoapService();
    Service service = ancestors.getService();
    String title = "<html>" + Resource.getDisplayNameForResource(soapOp);
    
    if (soapOp.isSetArchived() || service.isSetArchived()) {
    	jlTypeIcon = new JLabel(ResourceManager.getImageIcon(ResourceManager.WARNING_ICON));
    	title = title + "<i> - this operation is archived and probably cannot be used</i></html>";
    } else if (isSoapLab(service)) {
       	jlTypeIcon = new JLabel(ResourceManager.getImageIcon(ResourceManager.WARNING_ICON));
    	title = title + "<i> - this operation can only be used as part of a SoapLab service</i></html>";
    }
    else {
    	jlTypeIcon = new JLabel(resourceType.getIcon());
    	title = title + "</html>";
   }
    
    // service status
    jlItemStatus = new JLabel(new ImageIcon(ServiceMonitoringStatusInterpreter.getStatusIconURL(service, false)));
    jlItemTitle = new JLabel(title, JLabel.LEFT);
    jlItemTitle.setFont(jlItemTitle.getFont().deriveFont(Font.PLAIN, jlItemTitle.getFont().getSize() + 2));
    
    jlPartOf = new JLabel("<html><b>Part of: </b>" + soapOp.getAncestors().getSoapService().getResourceName() + "</html>");
    
    jlWsdlLocation = new JLabel("<html><b>WSDL location: </b>" + soapService.getWsdlLocation() + "</html>");
    
    int descriptionMaxLength = (expandedView ? DESCRIPTION_MAX_LENGTH_EXPANDED : DESCRIPTION_MAX_LENGTH_COLLAPSED);
    String strDescription = (soapOp.getDescription() == null || soapOp.getDescription().length() == 0 ?
                             "<font color=\"gray\">no description</font>" :
                             Util.stripAllHTML(soapOp.getDescription()));
    
    strDescription = Util.ensureLineLengthWithinString(strDescription, LINE_LENGTH, false);
    if (strDescription.length() > descriptionMaxLength) {
      strDescription = strDescription.substring(0, descriptionMaxLength) + "<font color=\"gray\">(...)</font>";
    }
    strDescription = "<html><b>Description: </b>" + strDescription + "</html>";
    jlDescription = new JLabel(strDescription);
    
    return (arrangeLayout(true, expandedView));
  }


private boolean isSoapLab(Service service) {
	boolean result = false;
	ServiceTechnologyTypes serviceTechnologyTypes = service.getServiceTechnologyTypes();
	if (serviceTechnologyTypes == null) {
		return result;
	}
	List<Enum> typeList = serviceTechnologyTypes.getTypeList();
	if (typeList == null) {
		return result;
	}
	result = typeList.contains(ServiceTechnologyType.SOAPLAB);
	return result;
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
	    c.insets = new Insets(8, 6, 6, 3);
	    this.add(jlTypeIcon, c);
	    
	    c.gridx++;
	    c.weightx = 1.0;
	    c.insets = new Insets(8, 3, 6, 3);
	    this.add(jlItemTitle, c);
	    
	    c.gridx++;
	    c.gridheight = 5;
	    c.weightx = 0;
	    c.weighty = 1.0;
	    this.add(jlItemStatus, c);	    
	    
	    if (showActionButtons) {
	      c.gridx++;
	      c.gridheight = 5;
	      c.weightx = 0;
	      c.weighty = 1.0;
	      jlExpand = new JLabel(ResourceManager.getImageIcon((expanded ? ResourceManager.FOLD_ICON : ResourceManager.UNFOLD_ICON)));
	      this.add(jlExpand, c);
	    }
	    
	    c.gridx = 1;
	    c.gridy++;
	    c.gridheight = 1;
	    c.weightx = 0;
	    c.weighty = 0;
	    c.insets = new Insets(3, 3, 8, 3);
	    this.add(jlPartOf, c);
	    
	    c.gridy++;
	    c.insets = new Insets(3, 3, 8, 3);
	    this.add(jlWsdlLocation, c);
	    
	    c.gridy++;
	    c.insets = new Insets(3, 3, 8, 3);
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
      this.add(loaderBarAnimationOrange, c);
    }
    else
    {
      // *** additional data for this SOAP operation ***
      SoapOperation soapOp = (SoapOperation) expandedResource.getAssociatedObj();
      
      // add SOAP inputs
      List<String> names = new ArrayList<String>();
      for (SoapInput soapInput : soapOp.getInputs().getSoapInputList()) {
        names.add(soapInput.getName());
      }
      
      String soapInputs = "<b>" + names.size() + " " + Util.pluraliseNoun("Input", names.size()) + "</b>";
      if(names.size() > 0) {
        soapInputs += ": " + Util.ensureLineLengthWithinString(Util.join(names, ", "), LINE_LENGTH, false);
      }
      soapInputs = "<html>" + soapInputs + "</html>";
      
      c.gridy++;
      this.add(new JLabel(soapInputs), c);
      
      
      // add SOAP outputs
      names.clear();
      for (SoapOutput soapOutput : soapOp.getOutputs().getSoapOutputList()) {
        names.add(soapOutput.getName());
      }
      
      String soapOutputs = "<b>" + names.size() + " " + Util.pluraliseNoun("Output", names.size()) + "</b>";
      if(names.size() > 0) {
        soapOutputs += ": " + Util.ensureLineLengthWithinString(Util.join(names, ", "), LINE_LENGTH, false);
      }
      soapOutputs = "<html>" + soapOutputs + "</html>";
      
      c.gridy++;
      this.add(new JLabel(soapOutputs), c);
    }
  }


@Override
boolean shouldBeHidden(Object itemToRender) {
	if (!(itemToRender instanceof SoapOperation)) {
		return false;
	}
	SoapOperation soapOp = (SoapOperation) itemToRender;
	   Ancestors ancestors = soapOp.getAncestors();
	    Service service = ancestors.getService();
	    if (soapOp.isSetArchived() || service.isSetArchived()) {
	    	return true;
	    } else if (isSoapLab(service)) {
	       	return true;
	    }
	    else {
	    	return false;
	   }

}
  
}
