
package org.apache.taverna.ui.perspectives.myexperiment;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.apache.taverna.ui.perspectives.myexperiment.model.Resource;
import org.apache.taverna.workbench.ui.zaria.PerspectiveSPI;

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

	public String getText() {
		return PERSPECTIVE_NAME;
	}

	public boolean isVisible() {
		return visible;
	}

	public int positionHint() {
		// this determines position of myExperiment perspective in the
		// bar with perspective buttons (currently makes it the last in the
		// list)
		return 30;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;

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
			throw new java.lang.IllegalArgumentException(
					"Unknown myExperiment plugin resource requested; requested resource name was: "
							+ strResourceName);
		}

		// no exception was thrown, therefore the supplied resource name was
		// recognised;
		// return the local URL of that resource
		return (MyExperimentPerspective.class.getResource(strResourcePath));
	}

	// a single point in the plugin where all resources' icons are referenced
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
			throw new java.lang.IllegalArgumentException(
					"Unknown myExperiment plugin resource requested; requested resource name was: "
							+ Resource.getResourceTypeName(iResourceType));
		}

		// no exception was thrown, therefore the supplied resource name was
		// recognised;
		// return the local URL of that resource
		return (MyExperimentPerspective.class.getResource(strResourcePath));
	}

	@Override
	public String getID() {
		return "myExperiment";
	}

	@Override
	public JComponent getPanel() {
		return getMainComponent();
	}

}
