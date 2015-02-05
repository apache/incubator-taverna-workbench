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

import static net.sf.taverna.t2.renderers.impl.RendererConstants.SEE_LOG_MSG;

import java.nio.file.Path;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import net.sf.taverna.t2.renderers.RendererUtils;

/**
 * Viewer to display XML as a tree.
 * 
 * @author Matthew Pocock
 * @auhor Ian Dunlop
 * @author David Withers
 */
public class TextXMLRenderer extends AbstractRenderer {
	private static final String UNREADABLE_MSG = "Reference Service failed to render data as string " + SEE_LOG_MSG;
	private static final String RENDERER_FAILED_MSG = "Failed to create XML renderer " + SEE_LOG_MSG;
	private Pattern pattern;

	public TextXMLRenderer() {
		pattern = Pattern.compile(".*text/xml.*");
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
		return "XML tree";
	}

	@Override
	protected String getSizeQueryTitle() {
		return "Render this as XML?";
	}

	@Override
	public JComponent getRendererComponent(Path path) {
		String resolve = null;
		try {
			// Resolve it as a string
			resolve = RendererUtils.getString(path);
		} catch (Exception ex) {
			logger.error("unrenderable: Reference Service failed to render data as string",
					ex);
			return new JTextArea(UNREADABLE_MSG + ex.getMessage());
		}
		try {
			return new XMLTree(resolve);
		} catch (Exception e) {
			logger.error("unrenderable: failed to create XML renderer", e);
			return new JTextArea(RENDERER_FAILED_MSG + e.getMessage());
		}
	}
}
