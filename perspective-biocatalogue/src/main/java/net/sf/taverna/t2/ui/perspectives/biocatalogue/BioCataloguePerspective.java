package net.sf.taverna.t2.ui.perspectives.biocatalogue;

import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

import org.jdom.Element;

/**
 * @author Sergejs Aleksejevs
 */
public class BioCataloguePerspective implements PerspectiveSPI
{
  private MainComponent perspectiveMainComponent;
	private boolean visible = true;

	public ImageIcon getButtonIcon()
	{
	  URL iconURL = ResourceManager.getResourceLocalURL(ResourceManager.FAVICON);
    
	  if (iconURL == null) {
	    return null;
	  } else {
	    return new ImageIcon(iconURL);
    }
	}

	public InputStream getLayoutInputStream() {
	  return getClass().getResourceAsStream("biocatalogue-perspective.xml");
	}

	public String getText() {
		return "BioCatalogue";
	}

	public boolean isVisible() {
		return visible;
	}

	public int positionHint()
	{
	  // this determines position of myExperiment perspective in the
    // bar with perspective buttons (currently makes it the last in
    // the list)
    return 30;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		
	}

	public void update(Element layoutElement) {
		// TODO Auto-generated method stub
		
		// Not sure what to do here
	}
	
  public void setMainComponent(MainComponent component)
  {
    this.perspectiveMainComponent = component;
  }
  
  /**
   * Returns the instance of the main component of this perspective.
   */
  public MainComponent getMainComponent()
  {
    return this.perspectiveMainComponent;
  }

}
