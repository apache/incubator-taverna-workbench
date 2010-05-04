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
/*
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.awt.image.RenderedImage;
*/
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
//import java.util.regex.Pattern;

import javax.imageio.ImageIO;
//import javax.media.jai.PlanarImage;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.log4j.Logger;

/*
import com.sun.media.jai.codec.ByteArraySeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;
*/

/**
 * Advanced renderer for mime type image/* that can render tiff files.
 * Uses Java Advanced Imaging (JAI) API.
 * 
 * @author Alex Nenadic
 * 
 */
public class AdvancedImageRenderer implements Renderer

{
	private static Logger logger = Logger
	.getLogger(ImageRenderer.class);

	
	private float MEGABYTE = 1024*1024;
	
	private int meg = 1048576;
	
	//private Pattern pattern;

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
		Object data = null;
		try {
			data = referenceService.renderIdentifier(reference, byte[].class,
					null);
		} catch (Exception e) {
			logger.error("Cannot render identifier", e);
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
					    ImageIcon image = new ImageIcon(load((byte[])data));
					    return (new JLabel(image));						
					} catch (Exception e) {
						throw new RendererException("Unable to generate image", e);
					}
				} else {
					return new JTextArea("Rendering cancelled due to size of image. Try saving and viewing in an external application");
				}
			}
			
			try {
			    ImageIcon image = new ImageIcon(load((byte[])data));
			    return (new JLabel(image));				
			} catch (Exception e) {
				throw new RendererException("Unable to generate image", e);
			}
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

