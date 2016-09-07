package org.apache.taverna.ui.perspectives.biocatalogue;
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

import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

import org.apache.taverna.biocatalogue.model.ResourceManager;
import org.apache.taverna.workbench.ui.zaria.PerspectiveSPI;

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
		return ResourceManager.getImageIcon(ResourceManager.FAVICON);
	}

	public InputStream getLayoutInputStream() {
	  return getClass().getResourceAsStream("biocatalogue-perspective.xml");
	}

	public String getText() {
		return "Service Catalogue";
	}

	public boolean isVisible() {
		return visible;
	}

	public int positionHint()
	{
	  // this determines position of perspective in the
    // bar with perspective buttons (currently makes it the last in
    // the list)
    return 40;
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
