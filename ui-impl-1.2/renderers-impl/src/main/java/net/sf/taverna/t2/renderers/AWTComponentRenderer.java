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
/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package net.sf.taverna.t2.renderers;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Class that renders objects which have a Java Component subclass as their user
 * object
 * 
 * @author Tom Oinn
 * @author Ian Dunlop
 */
public class AWTComponentRenderer implements Renderer {

	public AWTComponentRenderer() {
	}

	public boolean canHandle(String mimeType) {
		return true;
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		Object resolve = null;
		try {
			resolve = referenceService.renderIdentifier(reference,
					Component.class, null);
		} catch (Exception e) {
			throw new RendererException("Could not resolve " + reference, e);
		}
		if (resolve instanceof Component) {
			return true;
		}

		return false;
	}

	public String getType() {
		return "AWT Component";
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		// TODO Auto-generated method stub

		JPanel itemPanel = new JPanel(new BorderLayout());
		Component c = null;
		try {
			c = (Component) referenceService.renderIdentifier(reference,
					Component.class, null);
		} catch (Exception e) {
			throw new RendererException("Could not resolve " + reference, e);
		}
		itemPanel.add(c, BorderLayout.CENTER);
		return itemPanel;
	}

}
