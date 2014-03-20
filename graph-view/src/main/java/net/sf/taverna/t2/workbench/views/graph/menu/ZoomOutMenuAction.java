/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.views.graph.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.views.graph.actions.DesignOnlyAction;

import org.apache.log4j.Logger;

/**
 * An action that zooms a diagram image
 * 
 * @author Alex Nenadic
 * @author Tom Oinn
 * @author Alan R Williams
 *
 */
@SuppressWarnings("serial")
public class ZoomOutMenuAction extends AbstractMenuAction{

	private static Logger logger = Logger.getLogger(ZoomOutMenuAction.class);
	
	private static ModelMap modelMap = ModelMap.getInstance();
	
	public static final URI ZOOM_OUT_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/menu#diagramMenuZoomOut");
	
	public ZoomOutMenuAction(){
		super(DiagramZoomMenuSection.DIAGRAM_ZOOM_MENU_SECTION, 20, ZOOM_OUT_URI);
	}

	@Override
	protected Action createAction() {
		return new ZoomOutAction();
	}

}
