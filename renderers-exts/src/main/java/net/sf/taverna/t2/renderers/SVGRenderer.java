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

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * This class renders SVG Documents.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @author Ian Dunlop
 */
public class SVGRenderer implements Renderer {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SVGRenderer.class);
	private Pattern pattern;

	public SVGRenderer() {
		pattern = Pattern.compile(".*image/svg[+]xml.*");
	}

	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public String getType() {
		return "SVG";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		final JSVGCanvas svgCanvas = new JSVGCanvas();
		Object resolve = null;
		try {
			resolve = referenceService.renderIdentifier(reference,
					Object.class, null);
		} catch (Exception e) {
			throw new RendererException("Could not resolve " + reference, e);
		}
		if (resolve != null && resolve instanceof String && !resolve.equals("")) {
			String svgContent = (String) resolve;
			File tmpFile = null;
			try {
				tmpFile = File.createTempFile("taverna", "svg");
				tmpFile.deleteOnExit();
				FileUtils.writeStringToFile(tmpFile, svgContent, "utf8");
			} catch (IOException e) {
				throw new RendererException("Could not create SVG renderer", e);
			}
			try {
				svgCanvas.setURI(tmpFile.toURI().toASCIIString());
			} catch (Exception e) {
				throw new RendererException("Could not create SVG renderer", e);
			}
			JPanel jp = new JPanel(){
				@Override
				protected void finalize() throws Throwable {
					svgCanvas.stopProcessing();
					super.finalize();
				}
			};
			jp.add(svgCanvas);
			return jp;
		}
		return null;
	}

}
