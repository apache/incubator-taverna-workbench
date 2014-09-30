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
package net.sf.taverna.t2.workbench.ui.activitypalette;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.FlowLayout.LEFT;
import static java.awt.FlowLayout.RIGHT;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showInputDialog;
import static javax.swing.border.BevelBorder.LOWERED;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class ActivityPaletteConfigurationPanel extends JPanel {
	private static Logger logger = Logger
			.getLogger(ActivityPaletteConfigurationPanel.class);

	private Map<String,List<String>> values = new HashMap<>();
	private Map<String,String> names = new HashMap<>();
	private DefaultComboBoxModel<String> model;
	private DefaultListModel<String> listModel;
	private JList<String> propertyListItems;
	private String selectedKey;
	private JButton deleteTypeButton;
	private final ActivityPaletteConfiguration config;

	public ActivityPaletteConfigurationPanel(ActivityPaletteConfiguration config) {
		super(new BorderLayout());
		this.config = config;

		model = new DefaultComboBoxModel<>();
		for (String key : config.getInternalPropertyMap().keySet()) {
			if (key.startsWith("taverna.")
					&& config.getPropertyStringList(key) != null) {
				model.addElement(key);
				values.put(key,
						new ArrayList<>(config.getPropertyStringList(key)));
			}
			if (key.startsWith("name.taverna."))
				names.put(key, config.getProperty(key).toString());
		}
		deleteTypeButton = new JButton("Delete");

		final JButton addTypeButton = new JButton("Add");
		final JComboBox<String> comboBox = new JComboBox<>(model);
		comboBox.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				if (value != null && value instanceof String) {
					String name = names.get("name." + value);
					if (name != null)
						value = name;
				}
				return super.getListCellRendererComponent(list, value, index,
						isSelected, cellHasFocus);
			}
		});

		deleteTypeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String displayText = names.get("name." + selectedKey);
				if (displayText == null)
					displayText = selectedKey;
				if (confirm("Confirm removal",
						"Are you sure you wish to remove the type "
								+ displayText + "?")) {
					names.remove("name." + selectedKey);
					values.remove(selectedKey);
					model.removeElement(selectedKey);
					comboBox.setSelectedIndex(0);
				}
			}
		});

		addTypeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String key = input("New key", "Provide the new key.");
				if (key == null)
					return;
				String name = input("Name for the key",
						"Provide the name for the key: " + key);
				if (name == null)
					return;

				values.put(key, new ArrayList<String>());
				names.put("name." + key, name);
				model.addElement(key);
				comboBox.setSelectedItem(key);
			}
		});

		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (comboBox.getSelectedItem() != null
						&& comboBox.getSelectedItem() instanceof String) {
					selectedKey = (String) comboBox.getSelectedItem();
					List<String> selectedList = values.get(selectedKey);
					populateList(selectedList);
					deleteTypeButton.setEnabled(selectedList.size() == 0);
				}
			}
		});

		JPanel propertySelectionPanel = new JPanel(new FlowLayout(LEFT));
		propertySelectionPanel.add(new JLabel("Activity type:"));
		propertySelectionPanel.add(comboBox);
		propertySelectionPanel.add(addTypeButton);
		propertySelectionPanel.add(deleteTypeButton);
		add(propertySelectionPanel, NORTH);

		JPanel listPanel = new JPanel(new BorderLayout());
		listModel = new DefaultListModel<>();
		propertyListItems = new JList<>(listModel);
		propertyListItems.setBorder(new BevelBorder(LOWERED));

		listPanel.add(propertyListItems, CENTER);
		listPanel.add(listButtons(), EAST);

		add(listPanel, CENTER);

		add(applyButtonPanel(), SOUTH);

		if (model.getSize() > 0)
			comboBox.setSelectedItem(model.getElementAt(0));
	}

	private void populateList(List<String> selectedList) {
		listModel.removeAllElements();
		for (String item : selectedList)
			listModel.addElement(item);
	}

	private JPanel applyButtonPanel() {
		JPanel applyPanel = new JPanel(new FlowLayout(RIGHT));
		JButton applyButton = new JButton("Apply");

		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				config.getInternalPropertyMap().clear();
				for (String key : values.keySet()) {
					List<String> properties = values.get(key);
					config.setPropertyStringList(key, new ArrayList<>(
							properties));
				}
				for (String key : names.keySet())
					config.setProperty(key, names.get(key));
				store();
			}
		});

		applyPanel.add(applyButton);
		return applyPanel;
	}

	private void store() {
		try {
			//FIXME
			//ConfigurationManager.getInstance().store(config);
		} catch (Exception e1) {
			logger.error("There was an error storing the configuration:"
					+ config.getFilePrefix() + " (UUID=" + config.getUUID()
					+ ")", e1);
		}
	}

	private JPanel listButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, Y_AXIS));
		JButton addButton = new JButton("+");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String value = input("New property", "Provide new value for: "
						+ selectedKey);
				if (value != null) {
					listModel.addElement(value);
					values.get(selectedKey).add(value);
					deleteTypeButton.setEnabled(false);
				}
			}
		});

		JButton deleteButton = new JButton("-");
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object value = propertyListItems.getSelectedValue();
				if (confirm("Confirm removal",
						"Are you sure you wish to remove " + value + "?")) {
					listModel.removeElement(value);
					values.get(selectedKey).remove(value);
					if (values.get(selectedKey).size() == 0)
						deleteTypeButton.setEnabled(true);
				}
			}
		});

		panel.add(addButton);
		panel.add(deleteButton);

		return panel;
	}

	private boolean confirm(String title, String message) {
		return showConfirmDialog(this, message, title, YES_NO_OPTION,
				WARNING_MESSAGE) == YES_OPTION;
	}

	private String input(String title, String message) {
		return showInputDialog(this, message, title, INFORMATION_MESSAGE);
	}

/*	private JButton getAddTypeButton() {
		JButton result = new JButton("Add");
		result.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String val = input("New property value","New property value");
				if (val!=null) {
					if (values.get(val) == null) {
						model.addElement(val);
						values.put(val, new ArrayList<String>());
					} else
						showMessageDialog(ActivityPaletteConfigurationPanel.this, "This property already exists");
				}
			}
		});
		return result;
	}
*/
}
