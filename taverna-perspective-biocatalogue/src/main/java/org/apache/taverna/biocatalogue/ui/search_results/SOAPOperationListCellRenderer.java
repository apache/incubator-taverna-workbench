package org.apache.taverna.biocatalogue.ui.search_results;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.apache.taverna.biocatalogue.model.LoadingExpandedResource;
import org.apache.taverna.biocatalogue.model.LoadingResource;
import org.apache.taverna.biocatalogue.model.Resource;
import org.apache.taverna.biocatalogue.model.ResourceManager;
import org.apache.taverna.biocatalogue.model.Util;
import org.apache.taverna.lang.ui.ReadOnlyTextArea;
import org.apache.taverna.ui.perspectives.biocatalogue.integration.health_check.ServiceMonitoringStatusInterpreter;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
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
	
	private JLabel jlTypeIcon = new JLabel();
  private JLabel jlItemStatus = new JLabel();
  private JLabel jlItemTitle = new JLabel("X");
  private JLabel jlPartOf = new JLabel("X");
  private JLabel jlWsdlLocation = new JLabel("X");
  private ReadOnlyTextArea jtDescription = new ReadOnlyTextArea(5,80);
  private JLabel jlSoapInputs = new JLabel("X");
  private JLabel jlSoapOutputs = new JLabel("X");
  
  private GridBagConstraints c;
  
  private static Resource.TYPE resourceType = Resource.TYPE.SOAPOperation;
  
  
  public SOAPOperationListCellRenderer() {
    jlItemTitle.setFont(jlItemTitle.getFont().deriveFont(Font.PLAIN, jlItemTitle.getFont().getSize() + 2));
    jtDescription.setOpaque(false);
    jtDescription.setLineWrap(true);
    jtDescription.setWrapStyleWord(true);
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
    
    jlTypeIcon.setIcon(resourceType.getIcon());
    jlItemStatus.setIcon(ResourceManager.getImageIcon(ResourceManager.SERVICE_STATUS_UNCHECKED_ICON_LARGE));
       
    jlItemTitle.setText("<html>" + StringEscapeUtils.escapeHtml(Resource.getDisplayNameForResource(resource)) + "<font color=\"gray\"><i>- fetching more information</i></font></html>");
   
    jlPartOf.setText(" ");
    jlWsdlLocation.setText(" ");
    jtDescription.setText("");
    jlSoapInputs.setText(" ");
    jlSoapOutputs.setText(" ");
   
    return (arrangeLayout());
  }
  
  
  /**
   * 
   * @param itemToRender
 * @param selected 
   * @param expandedView <code>true</code> to indicate that this method generates the top
   *                     fragment of the expanded list entry for this SOAP operation / REST method.
   * @return
   */
  protected GridBagConstraints prepareLoadedEntry(Object itemToRender, boolean selected)
  {
    SoapOperation soapOp = (SoapOperation)itemToRender;
    
    Ancestors ancestors = soapOp.getAncestors();
    SoapService soapService = ancestors.getSoapService();
    Service service = ancestors.getService();
    String title = StringEscapeUtils.escapeHtml(Resource.getDisplayNameForResource(soapOp));
    
    if (soapOp.isSetArchived() || service.isSetArchived()) {
    	jlTypeIcon.setIcon(ResourceManager.getImageIcon(ResourceManager.WARNING_ICON));
    	title = "<html>" + title + "<i> - this operation is archived and probably cannot be used</i></html>";
    } else if (isSoapLab(service)) {
       	jlTypeIcon.setIcon(ResourceManager.getImageIcon(ResourceManager.WARNING_ICON));
    	title = "<html>" + title + "<i> - this operation can only be used as part of a SoapLab service</i></html>";
    }
    else {
    	jlTypeIcon.setIcon(resourceType.getIcon());
    	title = "<html>" + title + "</html>";
   }
    
    // service status
    jlItemStatus.setIcon(ServiceMonitoringStatusInterpreter.getStatusIcon(service, false));
    jlItemTitle.setText(title);
    
    jlPartOf.setText("<html><b>Part of: </b>" + StringEscapeUtils.escapeHtml(soapOp.getAncestors().getSoapService().getResourceName()) + "</html>");
    
    jlWsdlLocation.setText("<html><b>WSDL location: </b>" + soapService.getWsdlLocation() + "</html>");
    
        String strDescription = (soapOp.getDescription() == null || soapOp.getDescription().length() == 0 ?
                             "No description" :
                            	 Util.stripAllHTML(soapOp.getDescription()));
    
            jtDescription.setText(strDescription);
    
    // add SOAP inputs
    List<String> names = new ArrayList<String>();
    for (SoapInput soapInput : soapOp.getInputs().getSoapInputList()) {
      names.add(soapInput.getName());
    }
    
    String soapInputs = "<b>" + names.size() + " " + Util.pluraliseNoun("Input", names.size()) + "</b>";
    if(names.size() > 0) {
      soapInputs += ": " + StringEscapeUtils.escapeHtml(Util.ensureLineLengthWithinString(Util.join(names, ", "), LINE_LENGTH, false));
    }
    soapInputs = "<html>" + soapInputs + "</html>";
    jlSoapInputs.setText(soapInputs);
    
    c.gridy++;
    this.add(jlSoapInputs, c);
    
    
    // add SOAP outputs
    names.clear();
    for (SoapOutput soapOutput : soapOp.getOutputs().getSoapOutputList()) {
      names.add(soapOutput.getName());
    }
    
    String soapOutputs = "<b>" + names.size() + " " + Util.pluraliseNoun("Output", names.size()) + "</b>";
    if(names.size() > 0) {
      soapOutputs += ": " + StringEscapeUtils.escapeHtml(Util.ensureLineLengthWithinString(Util.join(names, ", "), LINE_LENGTH, false));
    }
    soapOutputs = "<html>" + soapOutputs + "</html>";
    jlSoapOutputs.setText(soapOutputs);
   
    return (arrangeLayout());
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
  private GridBagConstraints arrangeLayout()
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
	    c.gridheight = 7;
	    c.weightx = 0;
	    c.weighty = 1.0;
	    this.add(jlItemStatus, c);
	    
	    c.gridx = 1;
	    c.gridy++;
	    c.gridheight = 1;
	    c.weightx = 0;
	    c.weighty = 0;
	    this.add(jlPartOf, c);
	    
	    c.gridy++;
	    this.add(jlWsdlLocation, c);
	    
	    c.fill = GridBagConstraints.NONE;
	    c.gridy++;
	    this.add(jtDescription, c);
	    
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.gridy++;
	    this.add(jlSoapInputs, c);
	    
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.gridy++;
	    this.add(jlSoapOutputs, c);
	    
	    return (c);
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
