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

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;

/**
 * Advanced renderer for mime type image/* that can render tiff files.
 * Uses Java Advanced Imaging (JAI) ImageIO from https://jai-imageio.dev.java.net/.
 * 
 * @author Alex Nenadic
 * 
 */
public class AdvancedImageRenderer implements Renderer

{
	private Logger logger = Logger.getLogger(AdvancedImageRenderer.class);

	private int MEGABYTE = 1024*1024;

	private List<String> mimeTypesList;

	public AdvancedImageRenderer() {
		//pattern = Pattern.compile(".*image/.*");
		mimeTypesList = Arrays.asList(ImageIO.getReaderMIMETypes());
	}

	public boolean canHandle(String mimeType) {
		//return pattern.matcher(mimeType).matches();
		return mimeTypesList.contains(mimeType);
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
						"Failed to get the size of the data from Reference Service (see error log for more details): \n" + ex.getMessage());
			}
			
			// 4 megabyte limit for image viewing?
			if (approximateSizeInBytes > (MEGABYTE * 4)) {
				int response = JOptionPane
						.showConfirmDialog(
								null,
								"Image is approximately "
										+ bytesToMeg(approximateSizeInBytes)
										+ " MB in size, there could be issues with rendering this inside Taverna\nDo you want to continue?",
								"Render this image?", JOptionPane.YES_NO_OPTION);

				if (response != JOptionPane.YES_OPTION) { // NO_OPTION or ESCAPE key
					return new JTextArea(
							"Rendering cancelled due to size of image. Try saving and viewing in an external application.");
				}
			}

			byte[] data = null;
			try{
				data = (byte[])referenceService.renderIdentifier(reference, byte[].class,
					null);
			}
			catch(Exception e){
				logger.error(
						"Reference Service failed to render data as byte array",
						e);
				return new JTextArea("Reference Service failed to render data as byte array (see error log for more details): \n" + e.getMessage());
			}
			try {
				Image image = load(data);
				if (image == null){
					return new JTextArea(
							"Data does not seem to contain an image in any of the recognised formats: \n" + Arrays.asList(ImageIO.getWriterFormatNames()));
				}
				else{
					ImageIcon imageIcon = new ImageIcon(image);
					return (new JLabel(imageIcon));
				}
			} catch (Exception e) {
				logger.error("Failed to create image renderer", e);
				return new JTextArea(
						"Failed to create image renderer (see error log for more details): \n" + e.getMessage());
			}
		}// Else this is not a ReferenceSet so this is not good
		logger.error("Advanced Image Renderer: expected data as ReferenceSet but received as " + reference.getReferenceType().toString());
		return new JTextArea("Reference Service failed to obtain the data to render: data is not a ReferenceSet");	
	}
	
	/**
	 * Work out size of file in megabytes to 1 decimal place
	 * @param bytes
	 * @return
	 */
		private int bytesToMeg(long bytes) {
		float f = bytes / MEGABYTE;
		return Math.round(f);
	}
		
		
	private Image load(byte[] data) throws Exception {
						
		Image image = null;
		
		/*
		SeekableStream stream = new ByteArraySeekableStream(data);
		String[] names = ImageCodec.getDecoderNames(stream);
		ImageDecoder dec = ImageCodec
				.createImageDecoder(names[0], stream, null);
		RenderedImage im = dec.decodeAsRenderedImage();
		image = PlanarImage.wrapRenderedImage(im).getAsBufferedImage();
		return image;
		*/
		/*
		System.out.println("Mime types: " + Arrays.asList(ImageIO.getReaderMIMETypes()));
		System.out.println(Arrays.asList(ImageIO.getWriterFormatNames()));
		System.out.println("Can read:" + Arrays.asList(ImageIO.getReaderFormatNames()));
		 */
		image = ImageIO.read(new ByteArrayInputStream(data));
		return image;
	}

		
}

