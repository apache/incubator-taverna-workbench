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

import static org.apache.taverna.renderers.impl.RendererConstants.SEE_LOG_MSG;

import java.nio.file.Path;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;

import org.apache.taverna.renderers.RendererException;
import org.apache.taverna.renderers.RendererUtils;

/**
 * Renderer for mime type text/rtf
 *
 * @author Ian Dunlop
 * @author Alex Nenadic
 * @author David Withers
 */
public class TextRtfRenderer extends AbstractRenderer {
	private static final String UNREADABLE_MSG = "Reference Service failed to render data "
			+ SEE_LOG_MSG;
	private static final String RENDERER_FAILED_MSG = "Failed to create RTF renderer "
			+ SEE_LOG_MSG;
	private static final Pattern pattern = Pattern.compile(".*text/rtf.*");

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
	protected String getSizeQueryTitle() {
		return "Render as RTF?";
	}

	@Override
	public JComponent getRendererComponent(Path path) throws RendererException {
		String resolve;
		try {
			// Resolve it as a string
			resolve = RendererUtils.getString(path);
		} catch (Exception e) {
			logger.error("Reference Service failed to render data as string", e);
			return new JTextArea(UNREADABLE_MSG + e.getMessage());
		}
		try {
			return new JEditorPane("text/rtf", resolve);
		} catch (Exception e) {
			logger.error("Failed to create RTF renderer", e);
			return new JTextArea(RENDERER_FAILED_MSG + e.getMessage());
		}
	}
}
