package net.sf.taverna.biocatalogue.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.rest.ui.servicedescription.RESTActivityIcon;
import net.sf.taverna.t2.activities.wsdl.servicedescriptions.WSDLActivityIcon;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.BioCataloguePerspective;

/**
 * This class will be a single point of lookup of all resource files.
 * (Icons, images, CSS files, etc).
 * 
 * @author Sergejs Aleksejevs
 */
public class ResourceManager
{
  // subfolders, where some icons / other resources are kept
  public static final String FAMFAMFAM_PATH = "famfamfam_silk/";    // free collection of icons
  public static final String SERVICE_ICONS_PATH = "service_icons/"; // icons related to web services (e.g. service types)
  public static final String FOLDS_PATH = "folds/";                 // icons for 'folding' menus (like 'Search for...')
  public static final String TRISTATE_TREE_ICONS_PATH = "tristate_checkbox/";  // icons for the tri-state filtering tree
  
  // all known resources to follow
  public static final int FAVICON = 1;
  
  public static final int INFORMATION_ICON_LARGE = 10;
  
  public static final int SPINNER_STILL = 20;
  public static final int SPINNER = 21;
  public static final int BAR_LOADER_GREY = 25;
  public static final int BAR_LOADER_GREY_STILL = 26;
  public static final int BAR_LOADER_ORANGE = 30;
  public static final int BAR_LOADER_ORANGE_STILL = 31;
  
  public static final int FOLD_ICON = 40;
  public static final int UNFOLD_ICON = 41;
  
  public static final int SERVICE_TYPE_SOAP_ICON = 50;
  public static final int SERVICE_TYPE_REST_ICON = 51;
  public static final int SERVICE_TYPE_MULTITYPE_ICON = 65;
  public static final int SERVICE_TYPE_UNKNOWN_ICON = 70;
  
  public static final int TRISTATE_CHECKBOX_CHECKED_ICON = 80;
  public static final int TRISTATE_CHECKBOX_PARTIAL_ICON = 82;
  public static final int TRISTATE_CHECKBOX_UNCHECKED_ICON = 85;
  
  public static final int SERVICE_STATUS_PASSED_ICON = 100;
  public static final int SERVICE_STATUS_PASSED_ICON_LARGE = 101;
  public static final int SERVICE_STATUS_WARNING_ICON = 110;
  public static final int SERVICE_STATUS_WARNING_ICON_LARGE = 111;
  public static final int SERVICE_STATUS_FAILED_ICON = 120;
  public static final int SERVICE_STATUS_FAILED_ICON_LARGE = 121;
  public static final int SERVICE_STATUS_UNCHECKED_ICON = 130;
  public static final int SERVICE_STATUS_UNCHECKED_ICON_LARGE = 131;
  public static final int SERVICE_STATUS_UNKNOWN_ICON = 140;
  
  public static final int UNKNOWN_RESOURCE_TYPE_ICON = 200;
  public static final int USER_ICON = 205;
  public static final int REGISTRY_ICON = 210;
  public static final int SERVICE_PROVIDER_ICON = 215;
  public static final int SERVICE_ICON = 220;
  public static final int SOAP_OPERATION_ICON = 225;
  public static final int REST_METHOD_ICON = 227;
  public static final int SERVICE_CATEGORY_ICON = 230;
  public static final int WSDL_DOCUMENT_ICON = 235;
  public static final int TAG_ICON = 240;
  
  public static final int OPEN_IN_BIOCATALOGUE_ICON = 310;
  public static final int SEARCH_ICON = 315;
  public static final int HISTORY_ICON = 320;
  public static final int REFRESH_ICON = 330;
  public static final int FAVOURITE_ICON = 335;
  public static final int TICK_ICON = 340;
  public static final int CROSS_ICON = 341;
  public static final int WARNING_ICON = 342;
  public static final int ERROR_ICON = 343;
  public static final int SAVE_ICON = 345;
  public static final int DELETE_ITEM_ICON = 350;
  public static final int CLEAR_ICON = 355;
  public static final int LOCKED_ICON = 360;
  public static final int UNLOCKED_ICON = 365;
  
  public static final int BACK_ICON = 370;
  public static final int FORWARD_ICON = 375;
  public static final int FILTER_ICON = 380;
  public static final int PREVIEW_ICON = 385;
  public static final int SUGGESTION_TO_USER_ICON = 390;
  public static final int ADD_PROCESSOR_TO_WORKFLOW_ICON = 395;
  public static final int ADD_PROCESSOR_AS_FAVOURITE_ICON = 396;
  public static final int EXECUTE_HEALTH_CHECK_ICON = 397;
  
  public static final int SELECT_ALL_ICON = 400;
  public static final int DESELECT_ALL_ICON = 405;
  public static final int EXPAND_ALL_ICON = 410;
  public static final int COLLAPSE_ALL_ICON = 420;
  
  public static final int SORT_BY_NAME_ICON = 450;
  public static final int SORT_BY_COUNTS_ICON = 455;
  
  public static final int STYLES_CSS = 1000;
  
  
  /** 
   * Simple method to retrieve relative path of a required resource.
   */
  public static String getResourceRelPath(int resourceId)
  {
    String resPath = "";
    
    switch (resourceId) {
      case FAVICON:                           resPath += "favicon.png";
                                              break;
      case INFORMATION_ICON_LARGE:            resPath += "info-sphere-35.png";
                                              break;
      case SPINNER_STILL:                     resPath += "ajax-loader-still.gif";
                                              break;
      case SPINNER:                           resPath += "ajax-loader.gif";
                                              break;
      case BAR_LOADER_GREY:                   resPath += "ajax-loader-grey-bert2.gif";
                                              break;
      case BAR_LOADER_GREY_STILL:             resPath += "ajax-loader-grey-bert2-still.png";
                                              break;
      case BAR_LOADER_ORANGE:                 resPath += "ajax-loader-orange-bert2.gif";
                                              break;
      case BAR_LOADER_ORANGE_STILL:           resPath += "ajax-loader-orange-bert2-still.png";
                                              break;
      case FOLD_ICON:                         resPath += FOLDS_PATH + "fold.png";
                                              break;
      case UNFOLD_ICON:                       resPath += FOLDS_PATH + "unfold.png";
                                              break;
      case SERVICE_TYPE_SOAP_ICON:            resPath += SERVICE_ICONS_PATH + "service_type_soap.png";
                                              break;
      case SERVICE_TYPE_REST_ICON:            resPath += SERVICE_ICONS_PATH + "service_type_rest.png";
                                              break;
      case SERVICE_TYPE_MULTITYPE_ICON:       resPath += SERVICE_ICONS_PATH + "service_type_multitype.png";
                                              break;
      case SERVICE_TYPE_UNKNOWN_ICON:         resPath += SERVICE_ICONS_PATH + "service_type_unknown.png";
                                              break;
      case SERVICE_STATUS_PASSED_ICON:        resPath += FAMFAMFAM_PATH + "accept.png";
                                              break;
      case SERVICE_STATUS_PASSED_ICON_LARGE:  resPath += "tick-sphere-35.png";
                                              break;
      case SERVICE_STATUS_WARNING_ICON:       resPath += FAMFAMFAM_PATH + "error.png";
                                              break;
      case SERVICE_STATUS_WARNING_ICON_LARGE: resPath += "pling-sphere-35.png";
                                              break;
      case SERVICE_STATUS_FAILED_ICON:        resPath += FAMFAMFAM_PATH + "exclamation.png";
                                              break;
      case SERVICE_STATUS_FAILED_ICON_LARGE:  resPath += "cross-sphere-35.png";
                                              break;
      case SERVICE_STATUS_UNCHECKED_ICON:     resPath += FAMFAMFAM_PATH + "help.png";
                                              break;
      case SERVICE_STATUS_UNCHECKED_ICON_LARGE: resPath += "query-sphere-35.png";
                                              break;
      case SERVICE_STATUS_UNKNOWN_ICON:       resPath += FAMFAMFAM_PATH + "grey_circle.png";
                                              break;
      case TRISTATE_CHECKBOX_CHECKED_ICON:    resPath += TRISTATE_TREE_ICONS_PATH + "tristate_checkbox_checked.png";
                                              break;
      case TRISTATE_CHECKBOX_PARTIAL_ICON:    resPath += TRISTATE_TREE_ICONS_PATH + "tristate_checkbox_partial.png";
                                              break;
      case TRISTATE_CHECKBOX_UNCHECKED_ICON:  resPath += TRISTATE_TREE_ICONS_PATH + "tristate_checkbox_unchecked.png";
                                              break;
      case UNKNOWN_RESOURCE_TYPE_ICON:        resPath += FAMFAMFAM_PATH + "grey_circle.png";
                                              break;                                        
      case USER_ICON:                         resPath += FAMFAMFAM_PATH + "user.png";
                                              break;
      case REGISTRY_ICON:                     resPath += FAMFAMFAM_PATH + "remote_resource.png";
                                              break;
      case SERVICE_PROVIDER_ICON:             resPath += FAMFAMFAM_PATH + "chart_organisation.png";
                                              break;
      case SERVICE_ICON:                      resPath += "favicon.png";
                                              break;
      case SOAP_OPERATION_ICON:               resPath += FAMFAMFAM_PATH + "plugin.png";
                                              break;
      case REST_METHOD_ICON:                  resPath += FAMFAMFAM_PATH + "plugin.png";
                                              break;
      case SERVICE_CATEGORY_ICON:             resPath += FAMFAMFAM_PATH + "text_list_numbers.png";
                                              break;
      case TAG_ICON:                          resPath += FAMFAMFAM_PATH + "tag_blue.png";
                                              break;
      case WSDL_DOCUMENT_ICON:                resPath += FAMFAMFAM_PATH + "page_white_code.png";
                                              break;                                        
      case OPEN_IN_BIOCATALOGUE_ICON:         resPath += "open_in_BioCatalogue.png";
                                              break;
      case SEARCH_ICON:                       resPath += FAMFAMFAM_PATH + "magnifier.png";
                                              break;
      case HISTORY_ICON:                      resPath += FAMFAMFAM_PATH + "folder_explore.png";
                                              break;
      case REFRESH_ICON:                      resPath += FAMFAMFAM_PATH + "arrow_refresh.png";
                                              break;
      case FAVOURITE_ICON:                    resPath += FAMFAMFAM_PATH + "star.png";
                                              break;
      case TICK_ICON:                         resPath += FAMFAMFAM_PATH + "tick.png";
                                              break;
      case CROSS_ICON:                        resPath += FAMFAMFAM_PATH + "cross.png";
                                              break;
      case WARNING_ICON:                      resPath += FAMFAMFAM_PATH + "error.png";
                                              break;
      case ERROR_ICON:                        resPath += FAMFAMFAM_PATH + "exclamation.png";
                                              break;
      case SAVE_ICON:                         resPath += FAMFAMFAM_PATH + "disk.png";
                                              break;
      case DELETE_ITEM_ICON:                  resPath += FAMFAMFAM_PATH + "cross.png";
                                              break;
      case CLEAR_ICON:                        resPath += "trash.png";
                                              break;
      case LOCKED_ICON:                       resPath += FAMFAMFAM_PATH + "lock.png";
                                              break;
      case UNLOCKED_ICON:                     resPath += FAMFAMFAM_PATH + "lock_open.png";
                                              break;
      case BACK_ICON:                         resPath += FAMFAMFAM_PATH + "arrow_left.png";
                                              break;
      case FORWARD_ICON:                      resPath += FAMFAMFAM_PATH + "arrow_right.png";
                                              break;
      case FILTER_ICON:                       resPath += FAMFAMFAM_PATH + "arrow_join (flipped vertically).png";
                                              break;
      case PREVIEW_ICON:                      resPath += FAMFAMFAM_PATH + "magnifier.png";
                                              break;
      case SUGGESTION_TO_USER_ICON:           resPath += FAMFAMFAM_PATH + "lightbulb.png";
                                              break;
      case ADD_PROCESSOR_TO_WORKFLOW_ICON:    resPath += FAMFAMFAM_PATH + "application_form_add.png";
                                              break;
      case ADD_PROCESSOR_AS_FAVOURITE_ICON:   resPath += FAMFAMFAM_PATH + "star.png";
                                              break;
      case EXECUTE_HEALTH_CHECK_ICON:         resPath += FAMFAMFAM_PATH + "information.png";
                                              break;                                        
      case SELECT_ALL_ICON:                   resPath += FAMFAMFAM_PATH + "tick.png";
                                              break;
      case DESELECT_ALL_ICON:                 resPath += FAMFAMFAM_PATH + "cross.png";
                                              break;
      case EXPAND_ALL_ICON:                   resPath += FAMFAMFAM_PATH + "text_linespacing.png";
                                              break;
      case COLLAPSE_ALL_ICON:                 resPath += FAMFAMFAM_PATH + "text_linespacing (collapse).png";
                                              break;
      case SORT_BY_NAME_ICON:                 resPath += FAMFAMFAM_PATH + "style.png";
                                              break;
      case SORT_BY_COUNTS_ICON:               resPath += FAMFAMFAM_PATH + "sum.png";
                                              break;
      case STYLES_CSS:                        resPath += "styles.css";
                                              break;
      default:                                return (null);
    }
    
    return (resPath);
  }
  
  
  public static URL getResourceLocalURL(int resourceId) {
    return (BioCataloguePerspective.class.getResource(getResourceRelPath(resourceId)));
  }
  
  public static ImageIcon getImageIcon(int iconId)
  {
    try
    {
      // attempt to return an icon that was requested
      return (new ImageIcon(getResourceLocalURL(iconId)));
    }
    catch (NullPointerException e)
    {
      // if the regular operation was impossible, return a default
      // icon to avoid an NullPointerException being thrown
      return (drawMissingIcon());
    }
  }
  
  public static ImageIcon getImageIcon(URL resourceLocalURL) {
    return (new ImageIcon(resourceLocalURL));
  }
  
  
  public static Icon getIconFromTaverna(int iconId) {
    switch (iconId) {
      case SOAP_OPERATION_ICON: return (WSDLActivityIcon.getWSDLIcon());
      case REST_METHOD_ICON:    return (RESTActivityIcon.getRESTActivityIcon());
      default:                  return (drawMissingIcon());
    }
  }
  
  
  /**
   * This method would be called by other methods in this class
   * when they were unable to load requested icon.
   * 
   * @return A 16x16 pixel icon that represents a missing icon -
   *         a red cross. 
   */
  private static ImageIcon drawMissingIcon()
  {
    int w = 16;
    int h = 16;
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration gc = gd.getDefaultConfiguration();
    
    BufferedImage image = gc.createCompatibleImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    g.setColor(Color.RED);
    g.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
    g.drawLine(4, 4, 12, 12);
    g.drawLine(12, 4, 4, 12);
    g.dispose();
    
    return new ImageIcon(image); 
  }
}
