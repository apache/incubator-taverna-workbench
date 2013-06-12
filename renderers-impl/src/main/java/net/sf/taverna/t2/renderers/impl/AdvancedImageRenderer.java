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

import java.awt.Image;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.renderers.Renderer;
import net.sf.taverna.t2.renderers.RendererException;
import net.sf.taverna.t2.renderers.RendererUtils;

import org.apache.log4j.Logger;

import uk.org.taverna.databundle.DataBundles;

/**
 * Advanced renderer for mime type image/* that can render tiff files.
 * Uses Java Advanced Imaging (JAI) ImageIO from https://jai-imageio.dev.java.net/.
 *
 * @author Alex Nenadic
 * @author David Withers
 */
public class AdvancedImageRenderer implements Renderer

{
	private Logger logger = Logger.getLogger(AdvancedImageRenderer.class);

	private int MEGABYTE = 1024 * 1024;

	private List<String> mimeTypesList;

	public AdvancedImageRenderer() {
		mimeTypesList = Arrays.asList(ImageIO.getReaderMIMETypes());
	}

	@Override
	public boolean canHandle(String mimeType) {
		return mimeTypesList.contains(mimeType);
	}

	@Override
	public String getType() {
		return "Image";
	}

	@Override
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

			// 4 megabyte limit for image viewing?
			if (approximateSizeInBytes > (MEGABYTE * 4)) {
				int response = JOptionPane
						.showConfirmDialog(
								null,
								"Image is approximately "
										+ bytesToMeg(approximateSizeInBytes)
										+ " MB in size, there could be issues with rendering this inside Taverna\nDo you want to continue?",
								"Render this image?", JOptionPane.YES_NO_OPTION);

				if (response != JOptionPane.YES_OPTION) { // NO_OPTION or ESCAPE key
					return new JTextArea(
							"Rendering cancelled due to size of image. Try saving and viewing in an external application.");
				}
			}

			try (InputStream inputStream = RendererUtils.getInputStream(path)) {
				Image image = ImageIO.read(inputStream);
				if (image == null) {
					return new JTextArea(
							"Data does not seem to contain an image in any of the recognised formats: \n"
									+ Arrays.asList(ImageIO.getWriterFormatNames()));
				} else {
					ImageIcon imageIcon = new ImageIcon(image);
					return (new JLabel(imageIcon));
				}
			} catch (Exception e) {
				logger.error("Failed to create image renderer", e);
				return new JTextArea(
						"Failed to create image renderer (see error log for more details): \n"
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
