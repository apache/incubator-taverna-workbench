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
package net.sf.taverna.t2.renderers.impl;

import java.nio.file.Path;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.renderers.Renderer;
import net.sf.taverna.t2.renderers.RendererException;
import net.sf.taverna.t2.renderers.RendererUtils;

import org.apache.log4j.Logger;

import uk.org.taverna.databundle.DataBundles;

/**
 * Renderer for mime type text/rtf
 *
 * @author Ian Dunlop
 * @author Alex Nenadic
 * @author David Withers
 */
public class TextRtfRenderer implements Renderer {

	private Logger logger = Logger.getLogger(TextRtfRenderer.class);

	private int MEGABYTE = 1024 * 1024;

	private Pattern pattern;

	public TextRtfRenderer() {
		pattern = Pattern.compile(".*text/rtf.*");
	}

	public boolean isTerminal() {
		return true;
	}

	@Override
	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	@Override
	public String getType() {
		return "Text/RTF";
	}

	@Override
	public JComponent getComponent(Path path) throws RendererException {
		if (DataBundles.isValue(path) || DataBundles.isReference(path)) {
			try {
				JEditorPane editorPane = null;

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
									"Render as RTF?", JOptionPane.YES_NO_OPTION);

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
				editorPane = new JEditorPane("text/rtf", resolve);
				return editorPane;
			} catch (Exception e) {
				logger.error("Failed to create RTF renderer", e);
				return new JTextArea(
						"Failed to create RTF renderer (see error log for more details): \n"
								+ e.getMessage());
			}
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
