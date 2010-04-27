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
package net.sf.taverna.t2.renderers;

import java.awt.Color;
import java.awt.Font;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.apache.log4j.Logger;

import edu.stanford.ejalbert.BrowserLauncher;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Renderer for mime type text/html
 * 
 * @author Ian Dunlop
 */
public class TextHtmlRenderer implements Renderer {

	private Logger logger = Logger.getLogger(TextHtmlRenderer.class);

	private Pattern pattern;

	private float MEGABYTE = 1024 * 1024;

	private int meg = 1048576;

	public TextHtmlRenderer() {
		pattern = Pattern.compile(".*text/html.*");
	}

	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public boolean isTerminal() {
		return true;
	}

	public String getType() {
		return "Text/Html";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		try {
			String resolve = (String) referenceService.renderIdentifier(
					reference, String.class, null);
			JEditorPane editorPane = null;

			byte[] bytes = resolve.getBytes();
			if (bytes.length > meg) {

				int response = JOptionPane
						.showConfirmDialog(
								null,
								"Result is approximately "
										+ bytesToMeg(((byte[]) bytes).length)
										+ " Mb in size, there could be issues with rendering this inside Taverna\nDo you want to continue?",
								"Render this as text/html?",
								JOptionPane.YES_NO_OPTION);

				if (response == JOptionPane.NO_OPTION) {
					return new JTextArea(
							"Rendering cancelled due to size of file. Try saving and viewing in an external application");
				}
			}

			try {
				//editorPane = new JEditorPane("text/html", "<pre>" + resolve
					//	+ "</pre>");
				
				Font baseFont = new JLabel("base font").getFont();
				editorPane = new JEditorPane();
				editorPane.setEditable(false);
				editorPane.setBackground(Color.WHITE);
				editorPane.setFocusable(false);
				HTMLEditorKit kit = new HTMLEditorKit();
				editorPane.setEditorKit(kit);
				StyleSheet styleSheet = kit.getStyleSheet();
				//styleSheet.addRule("body {font-family:"+baseFont.getFamily()+"; font-size:"+baseFont.getSize()+";}"); // base font looks bigger when rendered as HTML
				styleSheet.addRule("body {font-family:"+ baseFont.getFamily()+"; font-size:9px;}");
				Document doc = kit.createDefaultDocument();
				editorPane.setDocument(doc);
				editorPane.setText(resolve);
				editorPane.addHyperlinkListener(new HyperlinkListener() {

					public void hyperlinkUpdate(HyperlinkEvent he) {
						HyperlinkEvent.EventType type = he.getEventType();
					    if (type == HyperlinkEvent.EventType.ACTIVATED) {
							// Open a Web browser
							try{
								BrowserLauncher launcher = new BrowserLauncher();
								String url = he.getDescription();
								if (url.toLowerCase().startsWith("http://")){
									launcher.openURLinBrowser(url);
								}
								else{
									launcher.openURLinBrowser("http://"+url);
								}
							}catch(Exception ex){
								logger.error("HTML Renderer: Failed to launch browser to follow a link " + he.getURL().toExternalForm(), ex);
							}
					    }				
					}
				});
							
				
			} catch (Exception e) {
				throw new RendererException(
						"Unable to generate text/html renderer", e);
			}
			return editorPane;
		} catch (Exception e) {
			throw new RendererException("Could not resolve " + reference, e);
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
		Math.round(f);
		return Math.round(f);
	}
}
