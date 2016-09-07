/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

package org.apache.taverna.lang.ui.tabselector;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;

/**
 * Component for selecting objects using tabs.
 *
 * @author David Withers
 */
public abstract class TabSelectorComponent<T> extends JPanel {

	private static final long serialVersionUID = 1L;

	private Map<T, Tab<T>> tabMap;
	private ButtonGroup tabGroup;
	private ScrollController scrollController;

	public TabSelectorComponent() {
		tabMap = new HashMap<T, Tab<T>>();
		tabGroup = new ButtonGroup();
		setLayout(new BorderLayout());
		scrollController = new ScrollController(this);
		add(scrollController.getScrollLeft());
		add(scrollController.getScrollRight());
		setLayout(new TabLayout(scrollController));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setColor(Tab.midGrey);
		g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
		g2.dispose();
	}

	protected abstract Tab<T> createTab(T object);

	public Tab<T> getTab(T object) {
		return tabMap.get(object);
	}

	public void addObject(T object) {
		Tab<T> button = createTab(object);
		tabMap.put(object, button);
		tabGroup.add(button);
		add(button);
		revalidate();
		repaint();
		button.setSelected(true);
	}

	public void removeObject(T object) {
		Tab<T> button = tabMap.remove(object);
		if (button != null) {
			tabGroup.remove(button);
			remove(button);
			revalidate();
			repaint();
		}
	}

	public void selectObject(T object) {
		Tab<T> button = tabMap.get(object);
		if (button != null) {
			button.setSelected(true);
		} else {
			addObject(object);
		}
	}

}
