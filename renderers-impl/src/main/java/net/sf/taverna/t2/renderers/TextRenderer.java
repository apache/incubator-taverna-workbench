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
package net.sf.taverna.t2.renderers;

import java.awt.Font;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * 
 * 
 * @author Ian Dunlop
 */
public class TextRenderer implements Renderer {
	private Pattern pattern;

	public TextRenderer() {
		pattern = Pattern.compile(".*text/.*");
	}

	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public String getType() {
		return "Text";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		JTextArea theTextArea = new JTextArea();
		String resolve = null;
		try {
			resolve = (String) referenceService.renderIdentifier(reference,
					String.class, null);
		} catch (Exception e1) {
			// TODO not a string so break - should handle this better
			return null;
		}
		try {
			theTextArea.setText(resolve);
			theTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		} catch (Exception e) {
			throw new RendererException("Unable to create text renderer", e);
		}

		return theTextArea;
	}
}
