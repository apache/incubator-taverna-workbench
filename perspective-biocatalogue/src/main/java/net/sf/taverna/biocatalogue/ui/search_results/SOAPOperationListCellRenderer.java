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

import org.biocatalogue.x2009.xml.rest.SoapInput;
import org.biocatalogue.x2009.xml.rest.SoapOperation;
import org.biocatalogue.x2009.xml.rest.SoapOutput;


/**
 * 
 * 
 * @author Sergejs Aleksejevs
 */
public class SOAPOperationListCellRenderer extends ExpandableOnDemandLoadedListCellRenderer
{
  private JLabel jlTypeIcon;
  private JLabel jlItemTitle;
  private JLabel jlPartOf;
  private JLabel jlDescription;
  
  private GridBagConstraints c;
  
  
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
    SoapOperation soapOp = (SoapOperation)itemToRender;;
    
    jlTypeIcon = new JLabel(resourceType.getIcon());
    
    jlItemTitle = new JLabel(Resource.getDisplayNameForResource(soapOp), JLabel.LEFT);
    jlItemTitle.setForeground(Color.decode("#AD0000"));  // very dark red
    jlItemTitle.setFont(jlItemTitle.getFont().deriveFont(Font.PLAIN, jlItemTitle.getFont().getSize() + 2));
    
    jlPartOf = new JLabel("<html><b>Part of: </b>" + soapOp.getAncestors().getSoapService().getResourceName() + "</html>");
    
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
  
}
