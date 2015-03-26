/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.lang.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

/**
 *
 *
 * @author David Withers
 */
public class EdgeLineBorder implements Border {

	public static final int TOP = 1;
	public static final int BOTTOM = 2;
	public static final int LEFT = 3;
	public static final int RIGHT = 4;
	private final int edge;
	private final Color color;

	public EdgeLineBorder(int edge, Color color) {
		this.edge = edge;
		this.color = color;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Color oldColor = g.getColor();
		g.setColor(color);
		switch (edge) {
		case TOP:
			g.drawLine(x, y, x+width, y);
			break;
		case BOTTOM:
			g.drawLine(x, y+height-2, x+width, y+height-2);
			break;
		case LEFT:
			g.drawLine(x, y, x+width, y+height);
			break;
		case RIGHT:
			g.drawLine(x+width, y, x+width, y+height);
			break;
		}
		g.setColor(oldColor);
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(0, 0, 0, 0);
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setSize(500, 500);
		JPanel panel = new JPanel();
		panel.setBorder(new EdgeLineBorder(TOP, Color.GRAY));
		frame.add(panel);
		frame.setVisible(true);
	}
}
