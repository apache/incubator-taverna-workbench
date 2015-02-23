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

import static javax.imageio.ImageIO.getReaderMIMETypes;
import static javax.imageio.ImageIO.getWriterFormatNames;
import static net.sf.taverna.t2.renderers.RendererUtils.getInputStream;
import static net.sf.taverna.t2.renderers.impl.RendererConstants.SEE_LOG_MSG;

import java.awt.Image;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

/**
 * Advanced renderer for mime type image/* that can render tiff files. Uses Java
 * Advanced Imaging (JAI) ImageIO from https://jai-imageio.dev.java.net/.
 * 
 * @author Alex Nenadic
 * @author David Withers
 */
public class AdvancedImageRenderer extends AbstractRenderer {
	private static final String BAD_FORMAT_MSG = "Data does not seem to contain an image in any of the recognised formats:\n";
	private static final String UNRENDERABLE_MSG = "Failed to create image renderer " + SEE_LOG_MSG;

	private Logger logger = Logger.getLogger(AdvancedImageRenderer.class);
	private List<String> mimeTypesList;

	public AdvancedImageRenderer() {
		mimeTypesList = Arrays.asList(getReaderMIMETypes());
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
	public JComponent getRendererComponent(Path path) {
		// Render into a label
		try (InputStream inputStream = getInputStream(path)) {
			Image image = ImageIO.read(inputStream);
			if (image == null)
				return new JTextArea(BAD_FORMAT_MSG
						+ Arrays.asList(getWriterFormatNames()));
			return new JLabel(new ImageIcon(image));
		} catch (Exception e) {
			logger.error("unrenderable: failed to create image renderer", e);
			return new JTextArea(UNRENDERABLE_MSG + e.getMessage());
		}
	}

	@Override
	protected int getSizeLimit() {
		return 4;
	}

	@Override
	protected String getResultType() {
		return "Image";
	}
}
