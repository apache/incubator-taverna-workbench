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
