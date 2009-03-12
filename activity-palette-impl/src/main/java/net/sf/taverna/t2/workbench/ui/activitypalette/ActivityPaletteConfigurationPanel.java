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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class ActivityPaletteConfigurationPanel extends JPanel {

	private static Logger logger = Logger
			.getLogger(ActivityPaletteConfigurationPanel.class);
	
	private Map<String,List<String>> values = new HashMap<String, List<String>>();
	private Map<String,String> names = new HashMap<String, String>();
	private DefaultComboBoxModel model;
	private DefaultListModel listModel;
	private JList propertyListItems;
	private String selectedKey;

	private JButton deleteTypeButton;
	
	
	public ActivityPaletteConfigurationPanel() {
		super();
		
		ActivityPaletteConfiguration config = ActivityPaletteConfiguration.getInstance();
		setLayout(new BorderLayout());
		
		model = new DefaultComboBoxModel();
		for (String key : config.getInternalPropertyMap().keySet()) {
			if (key.startsWith("taverna.")) {
				if (config.getPropertyStringList(key)!=null) {
					model.addElement(key);
					values.put(key,new ArrayList<String>(config.getPropertyStringList(key)));
				}
			}
			if (key.startsWith("name.taverna.")) {
				names.put(key, config.getProperty(key).toString());
			}
		}
		deleteTypeButton = new JButton("Delete");
		
		final JButton addTypeButton = new JButton("Add");
		final JComboBox comboBox = new JComboBox(model);
		comboBox.setRenderer(new DefaultListCellRenderer() {
			
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				if (value!=null && value instanceof String) {
					String name = names.get("name."+value.toString());
					if (name!=null) {
						value=name;
					}
				}
				return super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
			}
			
		});
		
		deleteTypeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				
					String displayText=names.get("name."+selectedKey);
					if (displayText==null) displayText=selectedKey;
					int ret=JOptionPane.showConfirmDialog(ActivityPaletteConfigurationPanel.this,"Are you sure you wish to remove the type "+displayText+" ?","Confirm removal",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
					if (ret==JOptionPane.YES_OPTION) {
						names.remove("name."+selectedKey);
						values.remove(selectedKey);
						model.removeElement(selectedKey);
						comboBox.setSelectedIndex(0);
					}
				
			}
			
		});
		
		addTypeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String key = JOptionPane.showInputDialog(ActivityPaletteConfigurationPanel.this,"Provide the new key","New key",JOptionPane.INFORMATION_MESSAGE);
				if (key!=null) {
					String name = JOptionPane.showInputDialog(ActivityPaletteConfigurationPanel.this,"Provide the name for the key: "+key,"Name for the key",JOptionPane.INFORMATION_MESSAGE);
					if (name!=null) {
						values.put(key,new ArrayList<String>());
						names.put("name."+key,name);
						model.addElement(key);
						comboBox.setSelectedItem(key);
					}
				}
			}
			
		});
		
		comboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (comboBox.getSelectedItem()!=null && comboBox.getSelectedItem() instanceof String) {
					selectedKey = (String)comboBox.getSelectedItem();
					List<String> selectedList = values.get(selectedKey);
					populateList(selectedList);
					if (selectedList.size()==0) {
						deleteTypeButton.setEnabled(true);
					}
					else {
						deleteTypeButton.setEnabled(false);
					}
				}
			}
			
		});
		
		JPanel propertySelectionPanel = new JPanel();
		propertySelectionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		propertySelectionPanel.add(new JLabel("Activity type:"));
		propertySelectionPanel.add(comboBox);
		propertySelectionPanel.add(addTypeButton);
		propertySelectionPanel.add(deleteTypeButton);
		add(propertySelectionPanel,BorderLayout.NORTH);
		
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BorderLayout());
		listModel=new DefaultListModel();
		propertyListItems = new JList(listModel);
		propertyListItems.setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		
		listPanel.add(propertyListItems,BorderLayout.CENTER);
		listPanel.add(listButtons(),BorderLayout.EAST);
		
		add(listPanel,BorderLayout.CENTER);
		
		add(applyButtonPanel(),BorderLayout.SOUTH);
		
		if (model.getSize()>0) {
			comboBox.setSelectedItem(model.getElementAt(0));
		}
	}
	

	private void populateList(List<String> selectedList) {
		listModel.removeAllElements();
		for (String item : selectedList) {
			listModel.addElement(item);
		}
	}
	
	private JPanel applyButtonPanel() {
		JPanel applyPanel = new JPanel();
		applyPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton applyButton = new JButton("Apply");
		
		applyButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Configurable config = ActivityPaletteConfiguration.getInstance();
				config.getInternalPropertyMap().clear();
				for (String key : values.keySet()) {
					List<String> properties = values.get(key);
					config.setPropertyStringList(key, new ArrayList<String>(properties));
				}
				for (String key : names.keySet()) {
					config.setProperty(key, names.get(key));
				}
				try {
					ConfigurationManager.getInstance().store(config);
				} catch (Exception e1) {
					logger.error("There was an error storing the configuration:"+config.getName()+"(UUID="+config.getUUID()+")",e1);
				}
			}
			
		});
		
		applyPanel.add(applyButton);
		return applyPanel;
	}
	
	private JPanel listButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		JButton addButton = new JButton("+");
		addButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String value = JOptionPane.showInputDialog(ActivityPaletteConfigurationPanel.this,"Provide new value for:"+selectedKey,"New property",JOptionPane.INFORMATION_MESSAGE);
				if (value!=null) {
					listModel.addElement(value);
					values.get(selectedKey).add(value);
					deleteTypeButton.setEnabled(false);
				}
			}
			
		});
		
		JButton deleteButton = new JButton("-");
		deleteButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Object value = propertyListItems.getSelectedValue();
				int ret=JOptionPane.showConfirmDialog(ActivityPaletteConfigurationPanel.this,"Are you sure you wish to remove "+value+" ?","Confirm removal",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
				if (ret==JOptionPane.YES_OPTION) {
					listModel.removeElement(value);
					values.get(selectedKey).remove(value);
					if (values.get(selectedKey).size()==0) {
						deleteTypeButton.setEnabled(true);
					}
				}
				
			}
			
		});
		
		panel.add(addButton);
		panel.add(deleteButton);
		
		return panel;
	}
	
/*	private JButton getAddTypeButton() {
		JButton result = new JButton("Add");
		result.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String val = JOptionPane.showInputDialog(ActivityPaletteConfigurationPanel.this, "New property value");
				if (val!=null) {
					if (values.get(val)==null) {
						model.addElement(val);
						values.put(val,new ArrayList<String>());
					}
					else {
						JOptionPane.showMessageDialog(ActivityPaletteConfigurationPanel.this, "This property already exists");
					}
				}
			}
			
		});
		return result;
	}
*/
}
