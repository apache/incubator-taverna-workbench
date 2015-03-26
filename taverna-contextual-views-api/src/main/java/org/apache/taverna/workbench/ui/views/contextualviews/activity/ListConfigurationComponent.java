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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.FlowLayout.RIGHT;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author David Withers
 */
@SuppressWarnings("serial")
public abstract class ListConfigurationComponent<T> extends JPanel {
	private static final String REMOVE = "Remove";
	private static final String ADD = "Add";

	private String name;
	private List<T> items;
	private JPanel listPanel;

	public ListConfigurationComponent(String name, List<T> items) {
		this.name = name;
		setLayout(new BorderLayout());

		listPanel = new JPanel(new ListLayout());
		JPanel buttonPanel = new JPanel(new FlowLayout(RIGHT));
		buttonPanel.add(new JButton(createAddAction()));

		add(new JScrollPane(listPanel), CENTER);
		add(buttonPanel, SOUTH);

		setItems(items);
	}

	protected void setItems(List<T> items) {
		this.items = items;
		listPanel.removeAll();
		for (T item : items)
			addItemComponent(item);
	}

	protected void addItem(T item) {
		items.add(item);
		addItemComponent(item);
	}

	protected void addItemComponent(T item) {
		JComponent itemPanel = new JPanel(new BorderLayout());
		itemPanel.add(createItemComponent(item), CENTER);
		itemPanel.add(new JButton(createRemoveAction(item)), EAST);
		listPanel.add(itemPanel);
		listPanel.revalidate();
		listPanel.repaint();
	}

	protected void removeItem(T item) {
		int index = items.indexOf(item);
		if (index >= 0) {
			items.remove(index);
			listPanel.remove(index);
			listPanel.revalidate();
			listPanel.repaint();
		}
	}

	private Action createRemoveAction(final T item) {
		return new AbstractAction(REMOVE) {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeItem(item);
			}
		};
	}

	private Action createAddAction() {
		return new AbstractAction(ADD + " " + name) {
			@Override
			public void actionPerformed(ActionEvent e) {
				addItem(createDefaultItem());
			}
		};
	}

	protected abstract Component createItemComponent(T item);

	protected abstract T createDefaultItem();
}
