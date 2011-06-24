package net.sf.taverna.biocatalogue.ui.search_results;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import net.sf.taverna.biocatalogue.model.LoadingResource;
import net.sf.taverna.biocatalogue.model.Resource;
import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.biocatalogue.model.Util;
import net.sf.taverna.t2.lang.ui.ReadOnlyTextArea;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.health_check.ServiceMonitoringStatusInterpreter;

import org.biocatalogue.x2009.xml.rest.RestMethod;
import org.biocatalogue.x2009.xml.rest.RestParameter;
import org.biocatalogue.x2009.xml.rest.RestRepresentation;
import org.biocatalogue.x2009.xml.rest.Service;
import org.biocatalogue.x2009.xml.rest.RestMethod.Ancestors;


/**
 * 
 * 
 * @author Sergejs Aleksejevs
 */
public class RESTMethodListCellRenderer extends ExpandableOnDemandLoadedListCellRenderer
{
  private JLabel jlTypeIcon = new JLabel();
  private JLabel jlItemStatus = new JLabel();
  private JLabel jlItemTitle = new JLabel("X");
  private JLabel jlPartOf = new JLabel("X");
  private ReadOnlyTextArea jlDescription = new ReadOnlyTextArea(5, 80);
  private JLabel jlMethodType = new JLabel("X");
  private JLabel jlUrlTemplate = new JLabel("X");
  private JLabel jlMethodParameters = new JLabel("X");
  private JLabel jlInputRepresentations = new JLabel("X");
  private JLabel jlOutputRepresentations = new JLabel("X");
  
  private GridBagConstraints c;
  
  private static Resource.TYPE resourceType = Resource.TYPE.RESTMethod;

  
  
  public RESTMethodListCellRenderer() {
	   jlItemTitle.setFont(jlItemTitle.getFont().deriveFont(Font.PLAIN, jlItemTitle.getFont().getSize() + 2));
	    jlDescription.setOpaque(false);
	    jlDescription.setLineWrap(true);
	    jlDescription.setWrapStyleWord(true);
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
    
    jlItemTitle.setText("<html>" + Resource.getDisplayNameForResource(resource) + "<font color=\"gray\"><i>- fetching more information</i></font></html>");
    
    jlPartOf.setText("");
    jlDescription.setText(" ");
    jlMethodType.setText(" ");
    jlUrlTemplate.setText(" ");
    jlMethodParameters.setText(" ");
    jlInputRepresentations.setText(" ");
    jlOutputRepresentations.setText(" ");
    
    return (arrangeLayout());
  }
  
  
  /**
   * 
   * @param itemToRender
   * @param expandedView <code>true</code> to indicate that this method generates the top
   *                     fragment of the expanded list entry for this SOAP operation / REST method.
   * @return
   */
  protected GridBagConstraints prepareLoadedEntry(Object itemToRender, boolean selected)
  {
    RestMethod restMethod = (RestMethod)itemToRender;;
    
    Ancestors ancestors = restMethod.getAncestors();
    Service service = ancestors.getService();
    String title = "<html>" + Resource.getDisplayNameForResource(restMethod);

    if (restMethod.isSetArchived() || service.isSetArchived()) {
    	jlTypeIcon.setIcon(ResourceManager.getImageIcon(ResourceManager.WARNING_ICON));
    	title = title + "<i> - this operation is archived and probably cannot be used</i></html>";
    }
    else {
    	jlTypeIcon.setIcon(resourceType.getIcon());
    	title = title + "</html>";
    }
    
    // service status
    jlItemStatus.setIcon(ServiceMonitoringStatusInterpreter.getStatusIcon(service, false));
    jlItemTitle.setText(title);
     
    jlPartOf.setText("<html><b>Part of: </b>" + restMethod.getAncestors().getRestService().getResourceName() + "</html>");
    
    String strDescription = (restMethod.getDescription() == null || restMethod.getDescription().length() == 0 ?
                             "<font color=\"gray\">no description</font>" :
                             Util.stripAllHTML(restMethod.getDescription()));
    jlDescription.setText(strDescription);
    
    jlMethodType.setText("<html><b>HTTP Method: </b>" + restMethod.getHttpMethodType().toString() + "</html>");
    jlUrlTemplate.setText("<html><b>URL Template: </b>" + restMethod.getUrlTemplate() + "</html>");
    
    List<String> names = new ArrayList<String>();
    for (RestParameter restParameter : restMethod.getInputs().getParameters().getRestParameterList()) {
      names.add(restParameter.getName() + (restParameter.getIsOptional() ? " (optional)" : ""));
    }
    
    String methodParameters = "<b>" + names.size() + " " + Util.pluraliseNoun("Parameter", names.size()) + "</b>";
    if(names.size() > 0) {
      methodParameters += ": " + Util.ensureLineLengthWithinString(Util.join(names, ", "), LINE_LENGTH, false);
    }
    methodParameters = "<html>" + methodParameters + "</html>";
    jlMethodParameters.setText(methodParameters);
    
       names.clear();
      for (RestRepresentation restRepresentation : restMethod.getInputs().getRepresentations().getRestRepresentationList()) {
        names.add(restRepresentation.getContentType());
      }
      
      String inputRepresentations = "<b>" + names.size() + " " + Util.pluraliseNoun("Input representation", names.size()) + "</b>";
      if(names.size() > 0) {
        inputRepresentations += ": " + Util.ensureLineLengthWithinString(Util.join(names, ", "), LINE_LENGTH, false);
      }
      inputRepresentations = "<html>" + inputRepresentations + "</html>";
      
      jlInputRepresentations.setText(inputRepresentations);

      // output representations
      names.clear();
      for (RestRepresentation restRepresentation : restMethod.getOutputs().getRepresentations().getRestRepresentationList()) {
        names.add(restRepresentation.getContentType());
      }
      
      String outputRepresentations = "<b>" + names.size() + " " + Util.pluraliseNoun("Output representation", names.size()) + "</b>";
      if(names.size() > 0) {
        outputRepresentations += ": " + Util.ensureLineLengthWithinString(Util.join(names, ", "), LINE_LENGTH, false);
      }
      outputRepresentations = "<html>" + outputRepresentations + "</html>";
      
      jlOutputRepresentations.setText(outputRepresentations);
    
    return (arrangeLayout());
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
    c.gridheight = 8;
    c.weightx = 0;
    c.weighty = 1.0;
    this.add(jlItemStatus, c);
    
    c.gridx = 1;
    c.gridy++;
    c.gridheight = 1;
    c.weightx = 1.0;
    c.weighty = 0;
    this.add(jlPartOf, c);
    
    c.fill = GridBagConstraints.NONE;
    c.gridy++;
    this.add(jlDescription, c);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridy++;
    this.add(jlMethodType, c);
    
    c.gridy++;
    this.add(jlUrlTemplate, c);
    
    c.gridy++;
    this.add(jlMethodParameters, c);
    
    c.gridy++;
    this.add(jlInputRepresentations, c);
    
    c.gridy++;
    this.add(jlOutputRepresentations, c);
    return (c);
  }
  
@Override
boolean shouldBeHidden(Object itemToRender) {
	if (!(itemToRender instanceof RestMethod)) {
		return false;
	}
    RestMethod restMethod = (RestMethod)itemToRender;;
    
    Ancestors ancestors = restMethod.getAncestors();
    Service service = ancestors.getService();
    String title = Resource.getDisplayNameForResource(restMethod);

    if (restMethod.isSetArchived() || service.isSetArchived()) {
    	return true;
    }
    else {
    	return false;
    }

}
  
}
