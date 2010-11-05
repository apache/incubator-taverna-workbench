package net.sf.taverna.biocatalogue.ui.search_results;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import net.sf.taverna.biocatalogue.model.LoadingExpandedResource;
import net.sf.taverna.biocatalogue.model.LoadingResource;
import net.sf.taverna.biocatalogue.model.Resource;
import net.sf.taverna.biocatalogue.model.Resource.TYPE;
import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.biocatalogue.model.Util;

import org.biocatalogue.x2009.xml.rest.RestMethod;
import org.biocatalogue.x2009.xml.rest.RestParameter;
import org.biocatalogue.x2009.xml.rest.RestRepresentation;


/**
 * 
 * 
 * @author Sergejs Aleksejevs
 */
public class RESTMethodListCellRenderer extends ExpandableOnDemandLoadedListCellRenderer
{
  private JLabel jlTypeIcon;
  private JLabel jlItemTitle;
  private JLabel jlPartOf;
  private JLabel jlDescription;
  
  private GridBagConstraints c;
  
  
  public RESTMethodListCellRenderer() {
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
    
    jlItemTitle = new JLabel(Resource.getDisplayNameForResource(resource), JLabel.LEFT);
    jlItemTitle.setForeground(Color.decode("#AD0000"));  // very dark red
    jlItemTitle.setFont(jlItemTitle.getFont().deriveFont(Font.PLAIN, jlItemTitle.getFont().getSize() + 2));
    
    jlPartOf = new JLabel((resource.isLoading() ? loaderBarAnimationGrey : loaderBarAnimationGreyStill), JLabel.LEFT);
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
    TYPE resourceType = determineResourceType(itemToRender);
    RestMethod restMethod = (RestMethod)itemToRender;;
    
    jlTypeIcon = new JLabel(resourceType.getIcon());
    
    jlItemTitle = new JLabel(Resource.getDisplayNameForResource(restMethod), JLabel.LEFT);
    jlItemTitle.setForeground(Color.decode("#AD0000"));  // very dark red
    jlItemTitle.setFont(jlItemTitle.getFont().deriveFont(Font.PLAIN, jlItemTitle.getFont().getSize() + 2));
    
    jlPartOf = new JLabel("<html><b>Part of: </b>" + restMethod.getAncestors().getRestService().getResourceName() + "</html>");
    
    int descriptionMaxLength = (expandedView ? DESCRIPTION_MAX_LENGTH_EXPANDED : DESCRIPTION_MAX_LENGTH_COLLAPSED);
    String strDescription = (restMethod.getDescription() == null || restMethod.getDescription().length() == 0 ?
                             "<font color=\"gray\">no description</font>" :
                             Util.stripAllHTML(restMethod.getDescription()));
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
    c.insets = new Insets(8, 6, 6, 3);
    this.add(jlTypeIcon, c);
    
    c.gridx++;
    c.weightx = 1.0;
    c.insets = new Insets(8, 3, 6, 3);
    this.add(jlItemTitle, c);
    
    if (showActionButtons) {
      c.gridx++;
      c.gridheight = 3;
      c.weightx = 0;
      c.weighty = 1.0;
      jlExpand = new JLabel(ResourceManager.getImageIcon((expanded ? ResourceManager.FOLD_ICON : ResourceManager.UNFOLD_ICON)));
      this.add(jlExpand, c);
    }
    
    c.gridx = 1;
    c.gridy++;
    c.gridheight = 1;
    c.weightx = 1.0;
    c.weighty = 0;
    c.insets = new Insets(3, 3, 3, 3);
    this.add(jlPartOf, c);
    
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
      this.add(new JLabel(loaderBarAnimationOrange, JLabel.CENTER), c);
    }
    else
    {
      // *** additional data for this REST method ***
      RestMethod restMethod = (RestMethod) expandedResource.getAssociatedObj();
      
      // HTTP method
      c.gridy++;
      this.add(new JLabel("<html><b>HTTP Method: </b>" + restMethod.getHttpMethodType().toString() + "</html>"), c);
      
      // URL template
      c.gridy++;
      this.add(new JLabel("<html><b>URL Template: </b>" + restMethod.getUrlTemplate() + "</html>"), c);
      
      
      // add REST method parameters
      List<String> names = new ArrayList<String>();
      for (RestParameter restParameter : restMethod.getInputs().getParameters().getRestParameterList()) {
        names.add(restParameter.getName() + (restParameter.getIsOptional() ? " (optional)" : ""));
      }
      
      String methodParameters = "<b>" + names.size() + " " + Util.pluraliseNoun("Parameter", names.size()) + "</b>";
      if(names.size() > 0) {
        methodParameters += ": " + Util.ensureLineLengthWithinString(Util.join(names, ", "), LINE_LENGTH, false);
      }
      methodParameters = "<html>" + methodParameters + "</html>";
      
      c.gridy++;
      this.add(new JLabel(methodParameters), c);
      
      
      // input representations (e.g. content types of data that can be sent)
      if (restMethod.getInputs().getRepresentations().getRestRepresentationList().size() > 0)
      {
        names.clear();
        for (RestRepresentation restRepresentation : restMethod.getInputs().getRepresentations().getRestRepresentationList()) {
          names.add(restRepresentation.getContentType());
        }
        
        String inputRerpresentations = "<b>" + names.size() + " " + Util.pluraliseNoun("Input representation", names.size()) + "</b>";
        if(names.size() > 0) {
          inputRerpresentations += ": " + Util.ensureLineLengthWithinString(Util.join(names, ", "), LINE_LENGTH, false);
        }
        inputRerpresentations = "<html>" + inputRerpresentations + "</html>";
        
        c.gridy++;
        this.add(new JLabel(inputRerpresentations), c);
      }
      
      // output representations
      names.clear();
      for (RestRepresentation restRepresentation : restMethod.getOutputs().getRepresentations().getRestRepresentationList()) {
        names.add(restRepresentation.getContentType());
      }
      
      String outputRerpresentations = "<b>" + names.size() + " " + Util.pluraliseNoun("Output representation", names.size()) + "</b>";
      if(names.size() > 0) {
        outputRerpresentations += ": " + Util.ensureLineLengthWithinString(Util.join(names, ", "), LINE_LENGTH, false);
      }
      outputRerpresentations = "<html>" + outputRerpresentations + "</html>";
      
      c.gridy++;
      this.add(new JLabel(outputRerpresentations), c);
    }
  }
  
}
