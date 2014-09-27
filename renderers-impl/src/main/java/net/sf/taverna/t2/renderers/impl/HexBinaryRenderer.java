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

import static net.sf.taverna.t2.renderers.RendererUtils.getInputStream;
import static net.sf.taverna.t2.renderers.impl.RendererConstants.SEE_LOG_MSG;

import java.io.InputStream;
import java.nio.file.Path;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import net.sf.taverna.t2.renderers.RendererException;

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
