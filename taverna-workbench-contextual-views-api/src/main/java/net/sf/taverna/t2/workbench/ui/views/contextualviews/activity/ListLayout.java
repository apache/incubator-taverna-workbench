/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * Lays out components vertically using their preferred height and the available
 * width.
 * 
 * @author David Withers
 */
public class ListLayout implements LayoutManager {
	private static final int DEFAULT_GAP = 5;
	private final int gap;

	public ListLayout() {
		this(DEFAULT_GAP);
	}

	public ListLayout(int gap) {
		this.gap = gap;
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
		int x = insets.left;
		int y = insets.top;
		int width = parent.getWidth() - insets.left - insets.right;
		Component[] components = parent.getComponents();
		for (int i = 0; i < components.length; i++) {
			components[i].setLocation(x, y);
			components[i].setSize(width,
					components[i].getPreferredSize().height);
			y = y + gap + components[i].getHeight();
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		Insets insets = parent.getInsets();
		int minimumWidth = 0;
		int minimumHeight = 0;
		Component[] components = parent.getComponents();
		for (int i = 0; i < components.length; i++) {
			Dimension size = components[i].getPreferredSize();
			if (size.width > minimumWidth)
				minimumWidth = size.width;
			minimumHeight = minimumHeight + size.height + gap;
		}
		minimumWidth = minimumWidth + insets.left + insets.right;
		minimumHeight = minimumHeight + insets.top + insets.bottom;

		return new Dimension(minimumWidth, minimumHeight);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return minimumLayoutSize(parent);
	}
}
