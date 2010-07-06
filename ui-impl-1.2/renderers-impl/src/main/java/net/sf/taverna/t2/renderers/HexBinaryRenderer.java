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

import java.io.ByteArrayInputStream;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;
import org.fife.ui.hex.swing.HexEditor;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;

/**
 * Renderer for binary data. Uses HexEditor from http://www.fifesoft.com/hexeditor/.
 * 
 * @author Alex Nenadic
 * 
 */
public class HexBinaryRenderer implements Renderer{

	private int MEGABYTE = 1024 * 1024;
	private Logger logger = Logger.getLogger(HexBinaryRenderer.class);

	public boolean canHandle(String mimeType) {
		return false; // can handle any data but return false here as we do not want this to be default renderer
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return false; // can handle any data but return false here as we do not want this to be default renderer
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		
		// Should be a ReferenceSet
		if (reference.getReferenceType() == T2ReferenceType.ReferenceSet) {
			long approximateSizeInBytes = 0;
			try {
				ReferenceSet refSet = referenceService.getReferenceSetService()
						.getReferenceSet(reference);
				approximateSizeInBytes = refSet.getApproximateSizeInBytes()
						.longValue();
			} catch (Exception ex) {
				logger.error("Failed to get the size of the data from Reference Service",
								ex);
				return new JTextArea(
						"Failed to get the size of the data from Reference Service (see error log for more details): \n"+ ex.getMessage());
			}

			if (approximateSizeInBytes > MEGABYTE) {
				int response = JOptionPane
						.showConfirmDialog(
								null,
								"Result is approximately "
										+ bytesToMeg(approximateSizeInBytes)
										+ " MB in size, there could be issues with rendering this inside Taverna\nDo you want to continue?",
								"Render binary data (in hexadecimal viewer)?",
								JOptionPane.YES_NO_OPTION);

				if (response != JOptionPane.YES_OPTION) {
					return new JTextArea(
							"Rendering cancelled due to size of data. Try saving and viewing in an external application.");
				}
			}

			byte[] data = null;
			try {
				// Resolve it as a byte[]
				data = (byte[]) referenceService.renderIdentifier(reference,
						byte[].class, null);
			} catch (Exception e) {
				logger
						.error(
								"Reference Service failed to render data as byte array",
								e);
				return new JTextArea(
						"Reference Service failed to render data as byte array (see error log for more details): \n" + e.getMessage());
			}
			try {
				HexEditor editor = new HexEditor();
				editor.open(new ByteArrayInputStream(data));
				return editor;
			} catch (Exception e) {
				logger.error("Failed to create binary hexadecimal renderer", e);
				return new JTextArea(
						"Failed to create binary hexadecimal renderer (see error log for more details): \n" + e.getMessage());
			}
		}
		// Else this is not a ReferenceSet so this is not good
		logger.error("Hexadecimal (Binary) Renderer: expected data as ReferenceSet but received as " + reference.getReferenceType().toString());
		return new JTextArea("Reference Service failed to obtain the data to render: data is not a ReferenceSet");	
	}

	public String getType() {
		return "Binary (HexDec)";
	}
	
	private int bytesToMeg(long bytes) {
		float f = bytes / MEGABYTE;
		return Math.round(f);
	}

}
