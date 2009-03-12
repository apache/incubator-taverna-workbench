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
package net.sf.taverna.zaria.progress;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

/**
 * Originally taken from Guy Romaine's blog post, since modified extensively
 * 
 * @author Tom
 */
public class InfiniteProgressPanel extends JComponent implements MouseListener {

	protected Area[] ticker = null;
	protected Thread animation = null;
	protected boolean started = false;
	protected int alphaLevel = 0;
	protected int rampDelay = 300;
	protected float shield = 0.70f;
	protected String text = "";
	protected int barsCount = 14;
	protected float fps = 15.0f;
	protected RenderingHints hints = null;

	public InfiniteProgressPanel() {
		this("");
	}

	public InfiniteProgressPanel(String text) {
		this(text, 14);
	}

	public InfiniteProgressPanel(String text, int barsCount) {
		this(text, barsCount, 0.70f);
	}

	public InfiniteProgressPanel(String text, int barsCount, float shield) {
		this(text, barsCount, shield, 15.0f);
	}

	public InfiniteProgressPanel(String text, int barsCount, float shield,
			float fps) {
		this(text, barsCount, shield, fps, 300);
	}

	public InfiniteProgressPanel(String text, int barsCount, float shield,
			float fps, int rampDelay) {
		this.text = text;
		this.rampDelay = rampDelay >= 0 ? rampDelay : 0;
		this.shield = shield >= 0.0f ? shield : 0.0f;
		this.fps = fps > 0.0f ? fps : 15.0f;
		this.barsCount = barsCount > 0 ? barsCount : 14;
		this.hints = new RenderingHints(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		this.hints.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		this.hints.put(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	public String getText() {
		return text;
	}

	public void interrupt() {
		if (animation != null) {
			animation.interrupt();
			animation = null;
			removeMouseListener(this);
			setVisible(false);
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void paintComponent(Graphics g) {
		if (!started) {
			return;
		}
		int width = getWidth();
		double maxY = 0.0;

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHints(hints);

		g2.setColor(new Color(255, 255, 255, (int) (alphaLevel * shield)));
		g2.fillRect(0, 0, getWidth(), getHeight());
		Point2D.Double center = new Point2D.Double((double) getWidth() / 2,
				(double) getHeight() / 2);
		AffineTransform toCenter = AffineTransform.getTranslateInstance(center
				.getX(), center.getY());
		AffineTransform fromCenter = AffineTransform.getTranslateInstance(
				-center.getX(), -center.getY());

		for (int i = 0; i < ticker.length; i++) {
			int channel = 224 - 128 / (i + 1);
			g2.setColor(new Color(channel, channel, channel, alphaLevel));
			synchronized (ticker[i]) {
				ticker[i].transform(toCenter);
				g2.fill(ticker[i]);
				Rectangle2D bounds = ticker[i].getBounds2D();
				if (bounds.getMaxY() > maxY)
					maxY = bounds.getMaxY();
				ticker[i].transform(fromCenter);
			}
		}

		if (text != null && text.length() > 0) {
			FontRenderContext context = g2.getFontRenderContext();
			TextLayout layout = new TextLayout(text, getFont(), context);
			Rectangle2D bounds = layout.getBounds();
			g2.setColor(getForeground());
			layout.draw(g2, (float) (width - bounds.getWidth()) / 2,
					(float) (maxY + layout.getLeading() + 2 * layout
							.getAscent()));
		}

	}

	public void setText(String text) {
		repaint();
		this.text = text;
	}

	public void start() {
		addMouseListener(this);
		setVisible(true);
		ticker = buildTicker();
		animation = new Thread(new Animator(true), "Infinite progress animator");
		animation.setDaemon(true);
		animation.start();
	}

	public void stop() {
		if (animation != null) {
			animation.interrupt();
			animation = null;
			animation = new Thread(new Animator(false),
					"Infinite progress animator");
			animation.start();
		}
	}

	@SuppressWarnings("unused")
	private Area buildPrimitive() {
		Rectangle2D.Double body = new Rectangle2D.Double(6, 0, 30, 12);
		Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 12, 12);
		Ellipse2D.Double tail = new Ellipse2D.Double(30, 0, 12, 12);

		Area tick = new Area(body);
		tick.add(new Area(head));
		tick.add(new Area(tail));
		return tick;
	}

	/**
	 * Build a single area tick
	 * 
	 * @param sectorCount
	 *            Number of ticks to create in total
	 * @param innerDiameter
	 *            Inner radius
	 * @param outerDiameter
	 *            Outer radius
	 * @param filled
	 *            Proportion of the tick that is filled
	 * @return
	 */
	private Area buildSectorPrimitive(int sectorCount, int innerDiameter,
			int outerDiameter, float filled) {
		Area tick = new Area(new Ellipse2D.Double(-outerDiameter / 2,
				-outerDiameter / 2, outerDiameter, outerDiameter));
		tick.subtract(new Area(new Ellipse2D.Double(-innerDiameter / 2,
				-innerDiameter / 2, innerDiameter, innerDiameter)));

		Polygon intersection = new Polygon();
		float angle = filled * (float) Math.PI / ((float) sectorCount);
		AffineTransform rotateClockwise = AffineTransform.getRotateInstance(
				angle, 0.0d, 0.0d);
		intersection.addPoint(0, 0);
		Point2D p1 = rotateClockwise.transform(new Point2D.Double(0,
				outerDiameter * 1.5), null);
		intersection.addPoint((int) p1.getX(), (int) p1.getY());
		intersection.addPoint(-(int) p1.getX(), (int) p1.getY());
		tick.intersect(new Area(intersection));
		return tick;
	}

	private Area[] buildTicker() {
		Area[] ticker = new Area[barsCount];
		double fixedAngle = 2.0 * Math.PI / ((double) barsCount);

		for (int i = 0; i < barsCount; i++) {
			Area primitive = buildSectorPrimitive(barsCount, 60, 100, 0.9f);
			AffineTransform toCircle = AffineTransform.getRotateInstance(
					-(double) i * fixedAngle, 0.0d, 0.0d);
			primitive.transform(toCircle);
			ticker[i] = primitive;
		}

		return ticker;
	}

	protected class Animator implements Runnable {
		private boolean rampUp = true;

		protected Animator(boolean rampUp) {
			this.rampUp = rampUp;
		}

		public void run() {
			double fixedIncrement = 2.0 * Math.PI / ((double) barsCount);
			AffineTransform rotate = AffineTransform.getRotateInstance(
					fixedIncrement, 0.0d, 0.0d);

			long start = System.currentTimeMillis();
			if (rampDelay == 0)
				alphaLevel = rampUp ? 255 : 0;

			started = true;
			boolean inRamp = rampUp;

			while (!Thread.interrupted()) {
				if (!inRamp) {
					for (int i = 0; i < ticker.length; i++)
						synchronized (ticker[i]) {
							ticker[i].transform(rotate);
						}
				}

				repaint();

				if (rampUp) {
					if (alphaLevel < 255) {
						alphaLevel = (int) (255 * (System.currentTimeMillis() - start) / rampDelay);
						if (alphaLevel >= 255) {
							alphaLevel = 255;
							inRamp = false;
						}
					}
				} else if (alphaLevel > 0) {
					alphaLevel = (int) (255 - (255 * (System
							.currentTimeMillis() - start) / rampDelay));
					if (alphaLevel <= 0) {
						alphaLevel = 0;
						break;
					}
				}

				try {
					Thread.sleep(inRamp ? 10 : (int) (1000 / fps));
				} catch (InterruptedException ie) {
					break;
				}
				Thread.yield();
			}

			if (!rampUp) {
				started = false;
				repaint();
				setVisible(false);
				removeMouseListener(InfiniteProgressPanel.this);
			}
		}
	}
}
