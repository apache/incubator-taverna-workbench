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
package org.apache.taverna.renderers.impl;

import static javax.imageio.ImageIO.getReaderMIMETypes;
import static javax.imageio.ImageIO.getWriterFormatNames;
import static org.apache.taverna.renderers.RendererUtils.getInputStream;
import static org.apache.taverna.renderers.impl.RendererConstants.SEE_LOG_MSG;

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
