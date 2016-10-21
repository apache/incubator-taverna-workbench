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
package org.apache.taverna.workbench.views.graph.menu;

import static org.apache.taverna.workbench.views.graph.menu.DiagramZoomMenuSection.DIAGRAM_ZOOM_MENU_SECTION;

import java.net.URI;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuAction;

/**
 * An action that zooms a diagram image
 * 
 * @author Alex Nenadic
 * @author Tom Oinn
 * @author Alan R Williams
 */
public class ZoomInMenuAction extends AbstractMenuAction {
	public static final URI ZOOM_IN_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#diagramMenuZoomIn");

	public ZoomInMenuAction() {
		super(DIAGRAM_ZOOM_MENU_SECTION, 10, ZOOM_IN_URI);
	}

	@Override
	protected Action createAction() {
		return new ZoomInAction();
	}
}
