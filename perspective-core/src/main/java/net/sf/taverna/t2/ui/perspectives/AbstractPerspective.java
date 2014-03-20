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

import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * An abstract implementation of a perspective that handles the storing of the
 * layout XML if modified via the 'update' method, which once set causes this
 * XML to be used rather than the bundled resource. Concrete subclass should
 * provide getText, getButtonIcon, and getLayoutResourceStream.
 * 
 * @author Stuart Owen
 * 
 */
public abstract class AbstractPerspective implements PerspectiveSPI {

	private Element layoutElement = null;
	private boolean visible = true;

	public InputStream getLayoutInputStream() {
		if (layoutElement == null) {
			return getLayoutResourceStream();
		} else {
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			String xml = outputter.outputString(layoutElement);
			return new ByteArrayInputStream(xml.getBytes());
		}
	}

	public void update(Element layoutElement) {
		this.layoutElement = layoutElement;
	}

	/**
	 * The name of the perspective
	 */
	public abstract String getText();

	/**
	 * The icon for the perspective
	 */
	public abstract ImageIcon getButtonIcon();

	/**
	 * 
	 * @return the resource stream for the perspective layout XML
	 */
	protected abstract InputStream getLayoutResourceStream();

	/**
	 * Position hint, default to 101, meaning that perspective that don't
	 * provide a hint will always appear towards the end (Built-in perspectives
	 * coming first in a controlled order).
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
