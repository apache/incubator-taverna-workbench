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

package org.apache.taverna.workbench.ui.views.contextualviews.activity;

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
