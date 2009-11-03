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

import java.awt.Font;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Renderer for mime type text/*
 * 
 * @author Ian Dunlop
 */
public class TextRenderer implements Renderer {

	private Pattern pattern;

	private float MEGABYTE = 1024 * 1024;

	private int meg = 1048576;

	public TextRenderer() {
		pattern = Pattern.compile(".*text/.*");
	}

	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public String getType() {
		return "Text";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		DialogTextArea theTextArea = new DialogTextArea();
		String resolve = null;
		try {
			resolve = (String) referenceService.renderIdentifier(reference,
					String.class, null);
			byte[] bytes = resolve.getBytes();
			if (bytes.length > meg) {

				Object[] options = { "Continue rendering", "Render partial",
						"Cancel" };
//allow partial rendering of text files
				int response = JOptionPane
						.showOptionDialog(
								null,
								"Result is approximately "
										+ bytesToMeg(bytes.length)
										+ " Mb in size, there could be issues with rendering this inside Taverna\nDo you want to cancel, render all of the result, or only the first part?",
								"Rendering large result",
								JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[2]);

				if (response == JOptionPane.NO_OPTION) {
					byte[] smallStringBytes = new byte[1048576];
					for (int i = 0; i < meg; i++) {
						smallStringBytes[i] = bytes[i];
					}
					theTextArea.setText(new String(smallStringBytes));
					theTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
					return theTextArea;
				} else if (response == JOptionPane.CANCEL_OPTION) {
					theTextArea
							.setText(new String(
									"Rendering cancelled due to size of file. Try saving and viewing in an external application"));
					return theTextArea;
				}
			}
		} catch (Exception e1) {
			// TODO not a string so inform the use about the problem - should
			// handle this better
			return new JTextArea("Could not render component due to " + e1);
		}
		try {
			theTextArea.setText(resolve);
			theTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		} catch (Exception e) {
			throw new RendererException("Unable to create text renderer", e);
		}

		return theTextArea;
	}

	/**
	 * Work out size of file in megabytes to 1 decimal place
	 * @param bytes
	 * @return
	 */
		private int bytesToMeg(long bytes) {
		float f = bytes / MEGABYTE;
		Math.round(f);
		return Math.round(f);
	}

}
