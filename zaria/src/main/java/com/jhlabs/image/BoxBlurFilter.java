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
/**
 * Copyright 2005 Huxtable.com. All rights reserved.
 * From http://www.jhlabs.com/ip/blurring.html, including
 * this file on the basis of the license :
 * 
 * There's source code in Java for pretty well everything I talk 
 * about here. I make no claims that these are optimised in any way 
 * I've opted for simplicity over speed everywhere and you'll 
 * probably be able to make most of these thing go faster with a 
 * bit of effort. You can use the source code for anything you want, 
 * including commercial purposes, but there's no liability. If your 
 * nuclear power station or missile system fails because of an 
 * improper blur, it's not my fault.
 */

package com.jhlabs.image;

import java.awt.image.BufferedImage;

public class BoxBlurFilter extends AbstractBufferedImageOp {

	private int hRadius;
	private int vRadius;
	private int iterations = 1;

	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
		int width = src.getWidth();
		int height = src.getHeight();

		if (dst == null)
			dst = createCompatibleDestImage(src, null);

		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		getRGB(src, 0, 0, width, height, inPixels);

		for (int i = 0; i < iterations; i++) {
			blur(inPixels, outPixels, width, height, hRadius);
			blur(outPixels, inPixels, height, width, vRadius);
		}

		setRGB(dst, 0, 0, width, height, inPixels);
		return dst;
	}

	public static void blur(int[] in, int[] out, int width, int height,
			int radius) {
		int widthMinus1 = width - 1;
		int tableSize = 2 * radius + 1;
		int divide[] = new int[256 * tableSize];

		for (int i = 0; i < 256 * tableSize; i++)
			divide[i] = i / tableSize;

		int inIndex = 0;

		for (int y = 0; y < height; y++) {
			int outIndex = y;
			int ta = 0, tr = 0, tg = 0, tb = 0;

			for (int i = -radius; i <= radius; i++) {
				int rgb = in[inIndex + ImageMath.clamp(i, 0, width - 1)];
				ta += (rgb >> 24) & 0xff;
				tr += (rgb >> 16) & 0xff;
				tg += (rgb >> 8) & 0xff;
				tb += rgb & 0xff;
			}

			for (int x = 0; x < width; x++) {
				out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16)
						| (divide[tg] << 8) | divide[tb];

				int i1 = x + radius + 1;
				if (i1 > widthMinus1)
					i1 = widthMinus1;
				int i2 = x - radius;
				if (i2 < 0)
					i2 = 0;
				int rgb1 = in[inIndex + i1];
				int rgb2 = in[inIndex + i2];

				ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
				tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
				tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
				tb += (rgb1 & 0xff) - (rgb2 & 0xff);
				outIndex += height;
			}
			inIndex += width;
		}
	}

	public void setHRadius(int hRadius) {
		this.hRadius = hRadius;
	}

	public int getHRadius() {
		return hRadius;
	}

	public void setVRadius(int vRadius) {
		this.vRadius = vRadius;
	}

	public int getVRadius() {
		return vRadius;
	}

	public void setRadius(int radius) {
		this.hRadius = this.vRadius = radius;
	}

	public int getRadius() {
		return hRadius;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	public int getIterations() {
		return iterations;
	}

	@Override
	public String toString() {
		return "Blur/Box Blur...";
	}
}
