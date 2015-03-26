package org.apache.taverna.renderers;

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

import org.apache.taverna.renderers.Renderer;
import org.apache.taverna.renderers.RendererUtils;
import org.apache.taverna.renderers.RendererException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

/**
 * PDF renderer for MIME type application/pdf using the ICE PDF Java viewer.
 *
 * @author Peter Li
 */
public class PDFRenderer implements Renderer {
	private Logger logger = Logger.getLogger(PDFRenderer.class);

	private Pattern pattern;

	private float MEGABYTE = 1024 * 1024;
	private int meg = 1048576;

	public PDFRenderer() {
		pattern = Pattern.compile(".*application/pdf.*");
	}

	@Override
	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public boolean isTerminal() {
		return true;
	}

	@Override
	public String getType() {
		return "PDF";
	}

	@Override
	public JComponent getComponent(Path path) throws RendererException {
		long approximateSizeInBytes = 0;
		try {
			approximateSizeInBytes = RendererUtils.getSizeInBytes(path);
		} catch (Exception ex) {
			logger.error("Failed to get the size of the data", ex);
			return new JTextArea(
					"Failed to get the size of the data (see error log for more details): \n"
							+ ex.getMessage());
		}

		if (approximateSizeInBytes > meg) {
			int response = JOptionPane
					.showConfirmDialog(
							null,
							"Result is approximately "
									+ bytesToMeg(approximateSizeInBytes)
									+ " Mb in size, there could be issues with rendering this inside Taverna\nDo you want to continue?",
							"Render this as text/html?", JOptionPane.YES_NO_OPTION);

			if (response == JOptionPane.NO_OPTION) {
				return new JTextArea(
						"Rendering cancelled due to size of file. Try saving and viewing in an external application");
			}
		}

		try (InputStream inputStream = RendererUtils.getInputStream(path)) {
			// Build a controller
			SwingController controller = new SwingController();
			// Build a SwingViewFactory configured with the controller
			SwingViewBuilder factory = new SwingViewBuilder(controller);
			// Use the factory to build a JPanel that is pre-configured
			// with a complete, active Viewer UI.
			JPanel viewerComponentPanel = factory.buildViewerPanel();
			// Open a PDF document to view
			controller.openDocument(inputStream, "PDF Viewer", null);
			return viewerComponentPanel;
		} catch (Exception e) {
			logger.error("Failed to create PDF renderer", e);
			return new JTextArea(
					"Failed to create PDF renderer (see error log for more details): \n" + e.getMessage());
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
		Math.round(f);
		return Math.round(f);
	}
}
