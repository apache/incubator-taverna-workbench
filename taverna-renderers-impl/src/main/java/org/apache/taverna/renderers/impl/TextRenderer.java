/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.renderers.impl;

import static java.awt.Font.PLAIN;
import static java.lang.String.format;
import static javax.swing.JOptionPane.NO_OPTION;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_CANCEL_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showOptionDialog;
import static org.apache.taverna.renderers.RendererUtils.getInputStream;
import static org.apache.taverna.renderers.RendererUtils.getString;
import static org.apache.taverna.renderers.impl.RendererConstants.CANCELLED_MSG;
import static org.apache.taverna.renderers.impl.RendererConstants.SEE_LOG_MSG;

import java.awt.Font;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.apache.taverna.lang.ui.DialogTextArea;
import org.apache.taverna.renderers.RendererException;

/**
 * Renderer for mime type text/*
 * 
 * @author Ian Dunlop
 * @author Alex Nenadic
 * @author David Withers
 */
public class TextRenderer extends AbstractRenderer {
	private static final String UNREADABLE_MSG = "Reference Service failed to render data "
			+ SEE_LOG_MSG;
	private static final String RENDERER_FAILED_MSG = "Failed to create text renderer "
			+ SEE_LOG_MSG;
	private static final String QUERY_MSG = "Result is approximately %d MB "
			+ "in size, there could be issues with rendering this inside "
			+ "Taverna\nDo you want to cancel, render all of the result, "
			+ "or only the first part?";
	private static final Pattern pattern = Pattern.compile(".*text/.*");

	@Override
	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	@Override
	public String getType() {
		return "Text";
	}

	private JComponent textRender(String text) {
		DialogTextArea theTextArea = new DialogTextArea();
		theTextArea.setWrapStyleWord(true);
		theTextArea.setEditable(false);
		theTextArea.setText(text);
		theTextArea.setFont(new Font("Monospaced", PLAIN, 12));
		theTextArea.setCaretPosition(0);
		return theTextArea;
	}

	private static final Object[] SIZE_OPTIONS = { "Continue rendering",
			"Render partial", "Cancel" };

	@Override
	protected JComponent sizeCheck(Path path, int approximateSizeInMB) {
		// allow partial rendering of text files
		int response = showOptionDialog(null,
				format(QUERY_MSG, approximateSizeInMB),
				"Rendering large result", YES_NO_CANCEL_OPTION,
				QUESTION_MESSAGE, null, SIZE_OPTIONS, SIZE_OPTIONS[2]);
		if (response == YES_OPTION)
			return null;
		else if (response != NO_OPTION)
			// if (response == CANCEL_OPTION) or ESCAPE key pressed
			return new JTextArea(CANCELLED_MSG);

		// Construct a partial result.
		byte[] smallStringBytes = new byte[1048576];
		try (InputStream inputStream = getInputStream(path)) {
			// just copy the first MEGABYTE
			inputStream.read(smallStringBytes);
		} catch (Exception ex) {
			logger.error("unrenderable: Reference Service failed "
					+ "to render data as byte array", ex);
			return new JTextArea(UNREADABLE_MSG + ex.getMessage());
		}
		try {
			// TODO beware of encoding problems!
			return textRender(new String(smallStringBytes));
		} catch (Exception e1) {
			logger.error("Failed to create text renderer", e1);
			return new JTextArea(RENDERER_FAILED_MSG + e1.getMessage());
		}
	}

	@Override
	public JComponent getRendererComponent(Path path) throws RendererException {
		String resolve;
		try {
			// Resolve it as a string
			resolve = getString(path);
		} catch (Exception e) {
			logger.error("unrenderable: Reference Service failed "
					+ "to render data as string", e);
			return new JTextArea(UNREADABLE_MSG + e.getMessage());
		}
		try {
			return textRender(resolve);
		} catch (Exception e1) {
			logger.error("Failed to create text renderer", e1);
			return new JTextArea(RENDERER_FAILED_MSG + e1.getMessage());
		}
	}
}
