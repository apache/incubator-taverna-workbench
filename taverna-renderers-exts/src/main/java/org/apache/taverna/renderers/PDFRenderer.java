package org.apache.taverna.renderers;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
