package org.apache.taverna.ui.perspectives.biocatalogue;

import javax.swing.ImageIcon;

import org.apache.taverna.workbench.icons.WorkbenchIcons;
import org.apache.taverna.workbench.ui.zaria.UIComponentFactorySPI;
import org.apache.taverna.workbench.ui.zaria.UIComponentSPI;

/**
 * @author Sergejs Aleksejevs
 */
public class MainComponentFactory implements UIComponentFactorySPI
{
  // this is to ensure that the whole perspective is not re-created
  // each time it is being activated in Taverna, rather it will only
  // happen once during the execution
  private static MainComponent mainPerspectiveComponent = null;
  
	public static MainComponent getSharedInstance()
	{
	  // double-check on existence of the 'mainPerspectiveComponent' ensures
    // that it is really created only once
    if (mainPerspectiveComponent == null) {
      synchronized(MainComponentFactory.class) {
        if (mainPerspectiveComponent == null) {
          mainPerspectiveComponent = new MainComponent();
        }
      }
    }
    return (mainPerspectiveComponent);
	}
	
	public UIComponentSPI getComponent() {
    return (getSharedInstance());
  }
	
	
	public ImageIcon getIcon() {
		return WorkbenchIcons.databaseIcon;
	}

	public String getName() {
		return "Service Catalogue Main Component Factory";
	}

}
