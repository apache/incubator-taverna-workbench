/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
 * 
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/

package net.sf.taverna.t2.ui.perspectives.myexperiment;

import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

import org.jdom.Element;

/**
 * @author Sergejs Aleksejevs, Jiten Bhagat
 */
public class MyExperimentPerspective implements PerspectiveSPI {
  // CONSTANTS
  // this is where all icons, stylesheet, etc are located
  private static final String BASE_RESOURCE_PATH = "/net/sf/taverna/t2/ui/perspectives/myexperiment/";
  public static final String PERSPECTIVE_NAME = "myExperiment";
  public static final String PLUGIN_VERSION = "0.2beta";

  // COMPONENTS
  private MainComponent perspectiveMainComponent;
  private boolean visible = true;

  public ImageIcon getButtonIcon() {
	URL iconURL = MyExperimentPerspective.getLocalResourceURL("myexp_icon16x16");
	if (iconURL == null) {
	  return null;
	} else {
	  return new ImageIcon(iconURL);
	}
  }

  public InputStream getLayoutInputStream() {
	return getClass().getResourceAsStream("myexperiment-perspective.xml");
  }

  public String getText() {
	return PERSPECTIVE_NAME;
  }

  public boolean isVisible() {
	return visible;
  }

  public int positionHint() {
	// this determines position of myExperiment perspective in the
	// bar with perspective buttons (currently makes it the last in the list)
	return 30;
  }

  public void setVisible(boolean visible) {
	this.visible = visible;

  }

  public void update(Element layoutElement) {
	// Not sure what to do here
  }

  public void setMainComponent(MainComponent component) {
	this.perspectiveMainComponent = component;
  }

  /**
   * Returns the instance of the main component of this perspective.
   */
  public MainComponent getMainComponent() {
	return this.perspectiveMainComponent;
  }

  // a single point in the plugin where all resources are referenced
  public static URL getLocalResourceURL(String strResourceName) {
	String strResourcePath = MyExperimentPerspective.BASE_RESOURCE_PATH;

	if (strResourceName.equals("not_authorized_icon"))
	  strResourcePath += "denied.png";
	if (strResourceName.equals("failure_icon"))
	  strResourcePath += "denied.png";
	else if (strResourceName.equals("success_icon"))
	  strResourcePath += "tick.png";
	else if (strResourceName.equals("spinner"))
	  strResourcePath += "ajax-loader.gif";
	else if (strResourceName.equals("spinner_stopped"))
	  strResourcePath += "ajax-loader-still.gif";
	else if (strResourceName.equals("external_link_small_icon"))
	  strResourcePath += "external_link_listing_small.png";
	else if (strResourceName.equals("back_icon"))
	  strResourcePath += "arrow_left.png";
	else if (strResourceName.equals("forward_icon"))
	  strResourcePath += "arrow_right.png";
	else if (strResourceName.equals("refresh_icon"))
	  strResourcePath += "arrow_refresh.png";
	else if (strResourceName.equals("favourite_icon"))
	  strResourcePath += "star.png";
	else if (strResourceName.equals("add_favourite_icon"))
	  strResourcePath += "favourite_add.png";
	else if (strResourceName.equals("delete_favourite_icon"))
	  strResourcePath += "favourite_delete.png";
	else if (strResourceName.equals("destroy_icon"))
	  strResourcePath += "cross.png";
	else if (strResourceName.equals("add_comment_icon"))
	  strResourcePath += "comment_add.png";
	else if (strResourceName.equals("myexp_icon"))
	  strResourcePath += "myexp_icon.png";
	else if (strResourceName.equals("myexp_icon16x16"))
		  strResourcePath += "myexp_icon16x16.png";
	else if (strResourceName.equals("open_in_my_experiment_icon"))
	  strResourcePath += "open_in_myExperiment.png";
	else if (strResourceName.equals("login_icon"))
	  strResourcePath += "login.png";
	else if (strResourceName.equals("logout_icon"))
	  strResourcePath += "logout.png";
	else if (strResourceName.equals("css_stylesheet"))
	  strResourcePath += "styles.css";
	else {
	  throw new java.lang.IllegalArgumentException("Unknown myExperiment plugin resource requested; requested resource name was: "
		  + strResourceName);
	}

	// no exception was thrown, therefore the supplied resource name was recognised;
	// return the local URL of that resource 
	return (MyExperimentPerspective.class.getResource(strResourcePath));
  }

  //a single point in the plugin where all resources' icons are referenced
  public static URL getLocalIconURL(int iResourceType) {
	String strResourcePath = MyExperimentPerspective.BASE_RESOURCE_PATH;

	switch (iResourceType) {
	  case Resource.WORKFLOW:
		strResourcePath += "workflow.png";
		break;
	  case Resource.FILE:
		strResourcePath += "file.png";
		break;
	  case Resource.PACK:
		strResourcePath += "pack.png";
		break;
	  case Resource.PACK_EXTERNAL_ITEM:
		strResourcePath += "remote_resource.png";
		break;
	  case Resource.USER:
		strResourcePath += "user.png";
		break;
	  case Resource.GROUP:
		strResourcePath += "group.png";
		break;
	  case Resource.TAG:
		strResourcePath += "tag_blue.png";
		break;
	  default:
		throw new java.lang.IllegalArgumentException("Unknown myExperiment plugin resource requested; requested resource name was: "
			+ Resource.getResourceTypeName(iResourceType));
	}

	// no exception was thrown, therefore the supplied resource name was recognised;
	// return the local URL of that resource 
	return (MyExperimentPerspective.class.getResource(strResourcePath));
  }

}
