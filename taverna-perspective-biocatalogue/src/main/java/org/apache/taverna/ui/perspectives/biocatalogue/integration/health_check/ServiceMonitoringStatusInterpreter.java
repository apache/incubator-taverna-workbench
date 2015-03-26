package org.apache.taverna.ui.perspectives.biocatalogue.integration.health_check;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;

import org.biocatalogue.x2009.xml.rest.MonitoringStatus;
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
   * @param serviceWithMonitoringData
   * @param listingIconRequired True to get a small icon suitable for a JList entry;
   *                            false to get a larger icon.
   * @return
   */
  public static ImageIcon getStatusIcon(Service serviceWithMonitoringData, boolean listingIconRequired)
  {
    MonitoringStatus latestMonitoringStatus = serviceWithMonitoringData.getLatestMonitoringStatus();
    if (latestMonitoringStatus == null) {
    	return ResourceManager.getImageIcon((listingIconRequired ?
                ResourceManager.SERVICE_STATUS_UNCHECKED_ICON :
                    ResourceManager.SERVICE_STATUS_UNCHECKED_ICON_LARGE));
    }
	MonitoringStatusLabel.Enum serviceStatusLabel = latestMonitoringStatus.getLabel();
    
    switch (serviceStatusLabel.intValue()) {
      case MonitoringStatusLabel.INT_PASSED:
              return ResourceManager.getImageIcon((listingIconRequired ?
                                                          ResourceManager.SERVICE_STATUS_PASSED_ICON :
                                                          ResourceManager.SERVICE_STATUS_PASSED_ICON_LARGE));
      case MonitoringStatusLabel.INT_WARNING:
              return ResourceManager.getImageIcon((listingIconRequired ?
                                                          ResourceManager.SERVICE_STATUS_WARNING_ICON :
                                                          ResourceManager.SERVICE_STATUS_WARNING_ICON_LARGE));
      case MonitoringStatusLabel.INT_FAILED:
              return ResourceManager.getImageIcon((listingIconRequired ?
                                                          ResourceManager.SERVICE_STATUS_FAILED_ICON :
                                                          ResourceManager.SERVICE_STATUS_FAILED_ICON_LARGE));
      case MonitoringStatusLabel.INT_UNCHECKED:
              return ResourceManager.getImageIcon((listingIconRequired ?
                                                          ResourceManager.SERVICE_STATUS_UNCHECKED_ICON :
                                                          ResourceManager.SERVICE_STATUS_UNCHECKED_ICON_LARGE));
      default:
              return (ResourceManager.getImageIcon(ResourceManager.SERVICE_STATUS_UNKNOWN_ICON));
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
