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
import javax.swing.JEditorPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * View a URL as a clickable HTML URL.
 * 
 * @author Ian Dunlop
 */
public class TextTavernaWebUrlRenderer implements Renderer {
	private Pattern pattern;

	public TextTavernaWebUrlRenderer() {
		pattern = Pattern.compile(".*text/x-taverna-web-url.*");
	}

	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public String getType() {
		return "URL";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		{
			Object dataObject = null;
			try {
				dataObject = referenceService.renderIdentifier(reference,
						Object.class, null);
			} catch (Exception e) {
				throw new RendererException("Could not resolve " + reference, e);
			}
			try {
				JEditorPane jep = new JEditorPane();
				String url = dataObject.toString();
				jep.setContentType("text/html");
				jep.setText("<a href=\"" + url + "\">" + url + "</a>");
				return jep;
			} catch (Exception ex) {
				JTextArea theTextArea = null;
				try {
					theTextArea = new JTextArea();
					theTextArea.setText((String) dataObject);
					theTextArea.setFont(Font.getFont("Monospaced"));
				} catch (Exception e) {
					throw new RendererException(
							"Could not create URL renderer for " + reference, e);
				}
				return theTextArea;
			}
		}
	}
}
