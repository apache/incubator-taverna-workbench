package net.sf.taverna.t2.ui.perspectives.biocatalogue;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

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
		return "myExperiment Main Component Factory";
	}

}
