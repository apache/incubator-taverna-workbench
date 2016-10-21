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

import javax.swing.ImageIcon;

/**
 * Implementations can construct UI components to be inserted into containers
 * within the workbench. These components may or may not bind to any given model
 * object, the previous approach of forcing all UI components to be model
 * listeners wasn't really very bright.
 * <p>
 * This class is intended to allow minimal information for building menus and
 * the like without having to construct potentially heavy swing objects every
 * time.
 * 
 * @author Tom Oinn
 */
public interface UIComponentFactorySPI {

	/**
	 * Get the preferred name of this component, for titles in windows etc.
	 */
	public String getName();

	/**
	 * Get an icon to be used in window decorations for this component.
	 */
	public ImageIcon getIcon();

	/**
	 * Construct a JComponent from this factory, cast as a UIComponent but must
	 * also implement JComponent (if anyone knows how to define this sensibly
	 * I'm all ears...)
	 */
	public UIComponentSPI getComponent();

}
