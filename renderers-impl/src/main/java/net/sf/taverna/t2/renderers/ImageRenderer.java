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

import java.awt.image.ImageProducer;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Renderer for mime type image/*
 * 
 * @author Matthew Pocock
 * @author Ian Dunlop
 */
public class ImageRenderer implements Renderer

{
	private float MEGABYTE = 1024*1024;
	
	private int meg = 1048576;
	
	private Pattern pattern;

	public ImageRenderer() {
		pattern = Pattern.compile(".*image/.*");
	}

	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public String getType() {
		return "Image";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		Object data = null;
		try {
			data = referenceService.renderIdentifier(reference, byte[].class,
					null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (data instanceof byte[]) {
			//3 megabyte limit for jpeg viewing?
			if (((byte[])data).length > (meg*4)) {
				int response = JOptionPane
				.showConfirmDialog(
						null,
						"Image is approximately "  + bytesToMeg(((byte[])data).length) + " Mb in size, there could be issues with rendering this inside Taverna\nDo you want to continue?",
						"Render this image?",
						JOptionPane.YES_NO_OPTION);
				if (response == JOptionPane.YES_OPTION) {
					try {
						ImageIcon theImage = new ImageIcon((byte[]) data);
						return new JLabel(theImage);
					} catch (Exception e) {
						throw new RendererException("Unable to generate image", e);
					}
				} else {
					return new JTextArea("Rendering cancelled due to size of image. Try saving and viewing in an external application");
				}
			}
			ImageIcon theImage = new ImageIcon((byte[]) data);
			return new JLabel(theImage);
			// JLabel or something else?
		} else if (data instanceof ImageProducer) {
			//TODO really not sure what this is or how to find out how big it is
			JLabel label = new JLabel();
			java.awt.Image image = label.createImage((ImageProducer) data);
			ImageIcon icon = new ImageIcon(image);
			label.setIcon(icon);
			return label;
		}

		return null;
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
