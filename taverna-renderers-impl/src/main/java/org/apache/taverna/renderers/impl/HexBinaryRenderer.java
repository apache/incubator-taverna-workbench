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

import static org.apache.taverna.renderers.RendererUtils.getInputStream;
import static org.apache.taverna.renderers.impl.RendererConstants.SEE_LOG_MSG;

import java.io.InputStream;
import java.nio.file.Path;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.apache.taverna.renderers.RendererException;

import org.fife.ui.hex.swing.HexEditor;

/**
 * Renderer for binary data. Uses HexEditor from
 * http://www.fifesoft.com/hexeditor/.
 * 
 * @author Alex Nenadic
 * @author David Withers
 */
public class HexBinaryRenderer extends AbstractRenderer {
	private static final String RENDER_FAILED_MSG = "Failed to create binary hexadecimal renderer "
			+ SEE_LOG_MSG;

	@Override
	public boolean canHandle(String mimeType) {
		return false;
		/*
		 * can handle any data but return false here as we do not want this to
		 * be default renderer
		 */
	}

	@Override
	protected String getSizeQueryTitle() {
		return "Render binary data (in hexadecimal viewer)?";
	}

	@Override
	public JComponent getRendererComponent(Path path) throws RendererException {
		try (InputStream inputStream = getInputStream(path)) {
			HexEditor editor = new HexEditor();
			editor.open(inputStream);
			return editor;
		} catch (Exception e) {
			logger.error("unrenderable: failed to create binhex renderer", e);
			return new JTextArea(RENDER_FAILED_MSG + e.getMessage());
		}
	}

	@Override
	public String getType() {
		return "Binary (HexDec)";
	}
}
