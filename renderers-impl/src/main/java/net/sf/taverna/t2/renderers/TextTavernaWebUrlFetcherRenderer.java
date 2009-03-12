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

import java.util.regex.Pattern;

import javax.swing.JComponent;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Display the content of a URL.
 * 
 * @author Ian Dunlop
 */
public class TextTavernaWebUrlFetcherRenderer implements Renderer {
	private Pattern pattern;

	public TextTavernaWebUrlFetcherRenderer() {
		pattern = Pattern.compile(".*text/x-taverna-web-url.*");
	}

	public boolean isTerminal() {
		return false;
	}

	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		// FIXME needs to do something here
		// not sure what should happen here
		// {
		// Object dataObject = dataFacade.resolve(entityIdentifier);
		// try {
		// URL url = new URL((String) dataObject);
		// DataThing urlThing = DataThingFactory.fetchFromURL(url);
		// return renderers.getRenderer(urlThing).getComponent(
		// renderers, urlThing);
		// } catch (Exception ex) {
		// JTextArea theTextArea = new JTextArea();
		// theTextArea.setText((String) dataObject);
		// theTextArea.setFont(Font.getFont("Monospaced"));
		// return theTextArea;
		// }
		return null;
	}
}
