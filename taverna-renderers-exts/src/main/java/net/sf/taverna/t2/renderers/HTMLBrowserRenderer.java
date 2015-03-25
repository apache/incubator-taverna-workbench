package net.sf.taverna.t2.renderers;

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
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import org.apache.log4j.Logger;

import org.apache.taverna.databundle.DataBundles;

/**
 * Web browser renderer for MIME type text/html.
 *
 * @author Peter Li
 */
public class HTMLBrowserRenderer implements Renderer {
	private Logger logger = Logger.getLogger(HTMLBrowserRenderer.class);
	private Pattern pattern;

	public HTMLBrowserRenderer() {
		pattern = Pattern.compile(".*text/html.*");
	}

	@Override
	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public boolean isTerminal() {
		return true;
	}

	@Override
	public String getType() {
		return "HTML (in Web browser)";
	}

	@Override
	public JComponent getComponent(Path path) throws RendererException {
		URI uri = null;
		try {
			if (DataBundles.isValue(path)) {
				Path tempFile = Files.createTempFile(null, ".html");
				Files.copy(path, tempFile, StandardCopyOption.REPLACE_EXISTING);
				uri = tempFile.toUri();
			} else if (DataBundles.isReference(path)) {
				uri = DataBundles.getReference(path);
			}
		} catch (IOException e) {
			logger.error("Error fetching data value", e);
			return new JEditorPane("text/html", "Error fetching data value\n"
					+ e.toString());
		}

		// Start Web browser
		try {
			Desktop.getDesktop().browse(uri);
		} catch (IOException e) {
			logger.error("Error attempting to launch Web browser", e);
			return new JEditorPane("text/html", "Error attempting to launch Web browser\n"
					+ e.toString());

		}

		return new JEditorPane("text/plain", "Launching a Web browser ...");
	}

}
