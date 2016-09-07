/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.renderers;

import org.apache.taverna.renderers.Renderer;
import org.apache.taverna.renderers.RendererUtils;
import org.apache.taverna.renderers.RendererException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import org.apache.taverna.databundle.DataBundles;

/**
 * This class renders SVG Documents.
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

	@Override
	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	@Override
	public String getType() {
		return "SVG";
	}

	@SuppressWarnings("serial")
	public JComponent getComponent(Path path) throws RendererException {
		if (DataBundles.isValue(path) || DataBundles.isReference(path)) {
				long approximateSizeInBytes = 0;
				try {
					approximateSizeInBytes = RendererUtils.getSizeInBytes(path);
				} catch (Exception ex) {
					logger.error("Failed to get the size of the data", ex);
					return new JTextArea(
							"Failed to get the size of the data (see error log for more details): \n"
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
					resolve = RendererUtils.getString(path);
				} catch (Exception e) {
					logger.error("Reference Service failed to render data as string", e);
					return new JTextArea(
							"Reference Service failed to render data as string (see error log for more details): \n"
									+ e.getMessage());
				}

				final JSVGCanvas svgCanvas = new JSVGCanvas();
				File tmpFile = null;
				try {
					tmpFile = File.createTempFile("taverna", "svg");
					tmpFile.deleteOnExit();
					FileUtils.writeStringToFile(tmpFile, resolve, "utf8");
				} catch (IOException e) {
					logger.error("SVG Renderer: Failed to write the data to temporary file", e);
					return new JTextArea(
							"Failed to write the data to temporary file (see error log for more details): \n"
									+ e.getMessage());
				}
				try {
					svgCanvas.setURI(tmpFile.toURI().toASCIIString());
				} catch (Exception e) {
					logger.error("Failed to create SVG renderer", e);
					return new JTextArea(
							"Failed to create SVG renderer (see error log for more details): \n"
									+ e.getMessage());
				}
				JPanel jp = new JPanel() {
					@Override
					protected void finalize() throws Throwable {
						svgCanvas.stopProcessing();
						super.finalize();
					}
				};
				jp.add(svgCanvas);
				return jp;
		} else {
			logger.error("Failed to obtain the data to render: data is not a value or reference");
			return new JTextArea(
					"Failed to obtain the data to render: data is not a value or reference");
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
