package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.health_check;

import java.net.URL;

import javax.swing.Icon;

import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;

import org.biocatalogue.x2009.xml.rest.MonitoringStatusLabel;
import org.biocatalogue.x2009.xml.rest.Service;

/**
 * @author Sergejs Aleksejevs
 */
public class ServiceMonitoringStatusInterpreter
{
  // prevent instantiation of this class
  private ServiceMonitoringStatusInterpreter() { /* do nothing */ }
  
  
  /**
   * @param service
   * @param listingIconRequired True to get a small icon suitable for a JList entry;
   *                            false to get a larger icon.
   * @return
   */
  public static Icon getStatusIcon(Service service, boolean listingIconRequired) {
    return ResourceManager.getImageIcon(getStatusIconURL(service, listingIconRequired));
  }
  
  
  /**
   * @param serviceWithMonitoringData
   * @param listingIconRequired True to get a small icon suitable for a JList entry;
   *                            false to get a larger icon.
   * @return
   */
  public static URL getStatusIconURL(Service serviceWithMonitoringData, boolean listingIconRequired)
  {
    MonitoringStatusLabel.Enum serviceStatusLabel = serviceWithMonitoringData.getLatestMonitoringStatus().getLabel();
    
    switch (serviceStatusLabel.intValue()) {
      case MonitoringStatusLabel.INT_PASSED:
              return ResourceManager.getResourceLocalURL((listingIconRequired ?
                                                          ResourceManager.SERVICE_STATUS_PASSED_ICON :
                                                          ResourceManager.SERVICE_STATUS_PASSED_ICON_LARGE));
      case MonitoringStatusLabel.INT_WARNING:
              return ResourceManager.getResourceLocalURL((listingIconRequired ?
                                                          ResourceManager.SERVICE_STATUS_WARNING_ICON :
                                                          ResourceManager.SERVICE_STATUS_WARNING_ICON_LARGE));
      case MonitoringStatusLabel.INT_FAILED:
              return ResourceManager.getResourceLocalURL((listingIconRequired ?
                                                          ResourceManager.SERVICE_STATUS_FAILED_ICON :
                                                          ResourceManager.SERVICE_STATUS_FAILED_ICON_LARGE));
      case MonitoringStatusLabel.INT_UNCHECKED:
              return ResourceManager.getResourceLocalURL((listingIconRequired ?
                                                          ResourceManager.SERVICE_STATUS_UNCHECKED_ICON :
                                                          ResourceManager.SERVICE_STATUS_UNCHECKED_ICON_LARGE));
      default:
              return (ResourceManager.getResourceLocalURL(ResourceManager.SERVICE_STATUS_UNKNOWN_ICON));
    }
    
  }
  
  
  public static VisitReport.Status translateBioCatalogueStatusForTaverna(MonitoringStatusLabel.Enum monitoringStatusLabelEnum)
  {
    switch (monitoringStatusLabelEnum.intValue()) {
      case MonitoringStatusLabel.INT_PASSED:    return Status.OK;
      case MonitoringStatusLabel.INT_WARNING:   return Status.WARNING;
      case MonitoringStatusLabel.INT_FAILED:    return Status.SEVERE;
      case MonitoringStatusLabel.INT_UNCHECKED: return Status.OK;      // not really OK, but Taverna isn't interested in missing data anyway 
      default:                                  return Status.WARNING; // could be worth to pop up a warning in this case, as it may mean something has changed
    }
  }
  
  
}
