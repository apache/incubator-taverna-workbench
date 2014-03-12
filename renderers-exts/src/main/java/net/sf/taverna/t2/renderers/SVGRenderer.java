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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherEvent;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherListener;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * This class renders SVG Documents.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @author Ian Dunlop
 * @author Alex Nenadic
 */
public class SVGRenderer implements Renderer {

	private int MEGABYTE = 1024 * 1024;
	
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

	@SuppressWarnings("serial")
	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		
		// Should be a ReferenceSet
		if (reference.getReferenceType() == T2ReferenceType.ReferenceSet) {
			try {

				long approximateSizeInBytes = 0;
				try {
					ReferenceSet refSet = referenceService
							.getReferenceSetService()
							.getReferenceSet(reference);
					approximateSizeInBytes = refSet.getApproximateSizeInBytes()
							.longValue();
				} catch (Exception ex) {
					logger
							.error(
									"Failed to get the size of the data from Reference Service",
									ex);
					return new JTextArea(
							"Failed to get the size of the data from Reference Service (see error log for more details): \n"
									+ ex.getMessage());
				}

				if (approximateSizeInBytes > MEGABYTE) {
					int response = JOptionPane
							.showConfirmDialog(
									null,
									"Result is approximately "
											+ bytesToMeg(approximateSizeInBytes)
											+ " MB in size, there could be issues with rendering this inside Taverna\nDo you want to continue?",
									"Render as SVG?", JOptionPane.YES_NO_OPTION);

					if (response != JOptionPane.YES_OPTION) {
						return new JTextArea(
								"Rendering cancelled due to size of data. Try saving and viewing in an external application.");
					}
				}

				String resolve = null;
				try {
					// Resolve it as a string
					resolve = (String) referenceService.renderIdentifier(
							reference, String.class, null);
				} catch (Exception e) {
					logger
							.error(
									"Reference Service failed to render data as string",
									e);
					return new JTextArea(
							"Reference Service failed to render data as string (see error log for more details): \n"
									+ e.getMessage());
				}
				
				final JSVGCanvas svgCanvas = new ErrorCapturingSVGCanvas();
				File tmpFile = null;
				try {
					tmpFile = File.createTempFile("taverna", "svg");
					tmpFile.deleteOnExit();
					FileUtils.writeStringToFile(tmpFile, resolve, "utf8");
				} catch (IOException e) {
					logger
					.error(
							"SVG Renderer: Failed to write the data to temporary file",
							e);
					return new JTextArea("Failed to write the data to temporary file (see error log for more details): \n"
							+ e.getMessage());				}
				final JPanel jp = new JPanel(new BorderLayout()){
					@Override
					protected void finalize() throws Throwable {
						svgCanvas.stopProcessing();
						super.finalize();
					}
				};

				jp.add(svgCanvas, BorderLayout.CENTER);
				svgCanvas.addSVGDocumentLoaderListener(new SVGDocumentLoaderListener() {

					@Override
					public void documentLoadingCancelled(
							SVGDocumentLoaderEvent arg0) {
						// nowt
					}

					@Override
					public void documentLoadingCompleted(
							SVGDocumentLoaderEvent arg0) {
						jp.setPreferredSize(svgCanvas.getSize());
					}

					@Override
					public void documentLoadingFailed(
							SVGDocumentLoaderEvent arg0) {
						// nowt
					}

					@Override
					public void documentLoadingStarted(
							SVGDocumentLoaderEvent arg0) {
						// nowt
					}});
				try {
					svgCanvas.setURI(tmpFile.toURI().toASCIIString());
				} catch (Exception e) {
					logger.error("Failed to create SVG renderer", e);
					return new JTextArea("Failed to create SVG renderer (see error log for more details): \n"
							+ e.getMessage());}
				return jp;			
			} catch (Exception e) {
				logger.error("Failed to create SVG renderer", e);
				return new JTextArea("Failed to create SVG renderer (see error log for more details): \n"
						+ e.getMessage());
			}
		}
		else{
			// Else this is not a ReferenceSet so this is not good
			logger.error("SVG Renderer: expected data as ReferenceSet but received as "
					+ reference.getReferenceType().toString());
			return new JTextArea(
			"Reference Service failed to obtain the data to render: data is not a ReferenceSet");	
		}
	}

	/**
	 * Work out size of file in megabytes to 1 decimal place
	 * 
	 * @param bytes
	 * @return
	 */
	private int bytesToMeg(long bytes) {
		float f = bytes / MEGABYTE;
		return Math.round(f);
	}
}
