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
package net.sf.taverna.t2.ui.perspectives.results;

import java.io.InputStream;

import javax.swing.ImageIcon;

import org.jdom.Element;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

public class ResultsPerspective implements PerspectiveSPI{

	private boolean visible = true;
	
	public ImageIcon getButtonIcon() {
		return WorkbenchIcons.resultsPerspectiveIcon;
	}

	public InputStream getLayoutInputStream() {
		return getClass().getResourceAsStream("results-perspective.xml");
	}

	public String getText() {
		// TODO Auto-generated method stub
		return "Results";
	}

	public boolean isVisible() {
		// TODO Auto-generated method stub
		return visible;
	}

	public int positionHint() {
		// TODO Auto-generated method stub
		return 20;
	}

	public void setVisible(boolean visible) {
		// TODO Auto-generated method stub
		this.visible = visible;
	}

	public void update(Element layoutElement) {
		// TODO Auto-generated method stub
		
	}

}
