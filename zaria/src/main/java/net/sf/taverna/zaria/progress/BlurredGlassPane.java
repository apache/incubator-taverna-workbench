/*******************************************************************************
 * Copyright (C) 2007-2010 The University of Manchester   
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
package net.sf.taverna.zaria.progress;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import com.jhlabs.image.BoxBlurFilter;

@SuppressWarnings("serial")
public class BlurredGlassPane extends JComponent {

	private static Logger logger = Logger.getLogger(BlurredGlassPane.class);
	private JFrame frame;
	private boolean active;
	private BufferedImage blur = null;
	private BoxBlurFilter filter = new BoxBlurFilter();
	private ComponentListener listener = null;

	public BlurredGlassPane(JFrame frame) {
		super();
		this.frame = frame;
		setOpaque(true);
		filter.setRadius(1);
		filter.setIterations(3);
		listener = new ComponentListener() {
			public void componentHidden(ComponentEvent e) {
				createBlur();
			}

			public void componentMoved(ComponentEvent e) {
				createBlur();
			}

			public void componentResized(ComponentEvent e) {
				createBlur();
			}

			public void componentShown(ComponentEvent e) {
				createBlur();

			}
		};

	}

	@Override
	public synchronized void paintComponent(Graphics g) {
		if (this.active) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.drawImage(blur, 0, 0, null);
			/**
			 * String text = "Hello"; if (text != null && text.length() > 0) {
			 * FontRenderContext context = g2d.getFontRenderContext();
			 * TextLayout layout = new TextLayout(text, getFont(), context);
			 * Rectangle2D bounds = layout.getBounds();
			 * g2d.setColor(getForeground()); layout.draw(g2d, (float)
			 * (getWidth() - bounds.getWidth()) / 2, (float) (getHeight()/2 +
			 * layout.getLeading() + 2 * layout.getAscent())); }
			 */
		} else {
			super.paintComponent(g);
		}
	}

	public synchronized void setActive(boolean active) {
		if (active != this.active) {
			this.active = active;
			if (this.active) {
				createBlur();
				setVisible(true);
				frame.getContentPane().addComponentListener(listener);
			} else {
				setVisible(false);
				frame.getContentPane().removeComponentListener(listener);
			}
			repaint();
		}
	}

	private synchronized void createBlur() {
		Container contentPane = frame.getContentPane();
		int width = contentPane.getWidth();
		int height = contentPane.getHeight();
		BufferedImage original = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics g = original.getGraphics();
		contentPane.paintAll(g);
		blur = filter.filter(original, null);
		logger.debug("Creating blurred image : " + width + "," + height);
		repaint();
	}

}
