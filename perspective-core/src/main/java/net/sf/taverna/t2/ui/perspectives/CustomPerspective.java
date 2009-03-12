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
package net.sf.taverna.t2.ui.perspectives;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * User generated perspective.
 * 
 * @author Stuart Owen
 * 
 */
public class CustomPerspective implements PerspectiveSPI {

	private String name;
	private Element layoutElement;
	private boolean visible = true;

	public CustomPerspective(Element layoutElement) {
		this.name = layoutElement.getAttributeValue("name");
		String v = layoutElement.getAttributeValue("visible");
		if (v != null) {
			this.visible = Boolean.parseBoolean(v);
		}
		this.layoutElement = layoutElement;
	}

	public ImageIcon getButtonIcon() {
		return WorkbenchIcons.importIcon;
	}

	public InputStream getLayoutInputStream() {
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		String xml = outputter.outputString(layoutElement.getChild("basepane"));
		return new ByteArrayInputStream(xml.getBytes());
	}

	public void update(Element paneElement) {
		layoutElement = new Element("layout");
		layoutElement.setAttribute("name", name);
		layoutElement.addContent(paneElement);
	}

	public String getText() {
		return name;
	}

	public String getName() {
		return name;
	}

	/**
	 * returns 101 by default but is generally irrelevant as
	 * CustomPerspectives are always added after built in and plugin
	 * perspectives anyway.
	 */
	public int positionHint() {
		return 101;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

}
