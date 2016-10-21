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
package org.apache.taverna.workbench.ui.zaria;

import java.util.Comparator;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 * SPI representing UI perspectives.
 *
 * @author Stuart Owen
 */
public interface PerspectiveSPI {

	/**
	 * Returns an identifier that uniquely identifies the perspective.
	 *
	 * @return an identifier that uniquely identifies the perspective
	 */
	public String getID();

	/**
	 * Returns the component containing the perspective.
	 *
	 * @return the component containing the perspective
	 */
	public JComponent getPanel();

	/**
	 * Returns the icon image for the toolbar button
	 *
	 * @return the icon image for the toolbar button
	 */
	public ImageIcon getButtonIcon();

	/**
	 *
	 * @return the text for the perspective
	 */
	public String getText();

	/**
	 * Provides a hint for the position of perspective in the toolbar and menu.
	 * The lower the value the earlier it will appear in the list.
	 *
	 * Custom plugins are recommended to start with a value > 100 (allowing for a whopping 100 built in plugins!)
	 */
	public int positionHint();

	public class PerspectiveComparator implements Comparator<PerspectiveSPI> {
		public int compare(PerspectiveSPI o1, PerspectiveSPI o2) {
			return new Integer(o1.positionHint()).compareTo(new Integer(o2
					.positionHint()));
		}
	}

}
