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
package net.sf.taverna.t2.workbench.design.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

/**
 * UI for creating/editing dataflow input ports.
 * 
 * @author David Withers
 */
public class DataflowInputPortPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JTextField portNameField;
	private JRadioButton singleValueButton;
	private JRadioButton listValueButton;
	private JSpinner listDepthSpinner;

	public DataflowInputPortPanel() {
		super(new GridBagLayout());

		portNameField = new JTextField();
		singleValueButton = new JRadioButton("Single value");
		listValueButton = new JRadioButton("List of depth ");
		listDepthSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

		setBorder(new EmptyBorder(10, 10, 10, 10));
		
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.ipadx = 10;
		add(new JLabel("Name:"), constraints);

		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.ipadx = 0;
		constraints.weightx = 1d;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		add(portNameField, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.weightx = 0d;
		constraints.fill = GridBagConstraints.NONE;
		constraints.ipadx = 10;
		constraints.insets = new Insets(10, 0, 0, 0);
		add(new JLabel("Type:"), constraints);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(singleValueButton);
		buttonGroup.add(listValueButton);
		
		final JLabel helpLabel = new JLabel("Depth 1 is a list, 2 is a list of lists, etc.");
		helpLabel.setFont(helpLabel.getFont().deriveFont(11f));

		singleValueButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					listDepthSpinner.setEnabled(false);
					helpLabel.setEnabled(false);
				} else {
					listDepthSpinner.setEnabled(true);
					helpLabel.setEnabled(true);
				}
			}
		});
		
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.ipadx = 0;
		add(singleValueButton, constraints);
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(0, 0, 0, 0);
		add(listValueButton, constraints);
		constraints.gridx = 2;
		add(listDepthSpinner, constraints);
		constraints.gridx = 1;
		constraints.gridy = 3;
		constraints.gridwidth = 2;
		constraints.weighty = 1d;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.insets = new Insets(0, 20, 0, 0);
		add(helpLabel, constraints);
	}

	/**
	 * Returns the portNameField.
	 *
	 * @return the portNameField
	 */
	public JTextField getPortNameField() {
		return portNameField;
	}

	/**
	 * Returns the singleValueButton.
	 *
	 * @return the singleValueButton
	 */
	public JRadioButton getSingleValueButton() {
		return singleValueButton;
	}

	/**
	 * Returns the listValueButton.
	 *
	 * @return the listValueButton
	 */
	public JRadioButton getListValueButton() {
		return listValueButton;
	}

	/**
	 * Returns the port name.
	 *
	 * @return the port name
	 */
	public String getPortName() {
		return portNameField.getText();
	}
	
	/**
	 * Sets the port name.
	 *
	 * @param name the name of the port
	 */
	public void setPortName(String name) {
		portNameField.setText(name);
	}
	
	/**
	 * Returns the port depth.
	 *
	 * @return the port depth
	 */
	public int getPortDepth() {
		if (singleValueButton.isSelected()) {
			return 0;
		} else {
			return (Integer) listDepthSpinner.getValue();
		}
	}

	/**
	 * Sets the port depth.
	 *
	 * @param depth the depth of the port
	 */
	public void setPortDepth(int depth) {
		if (depth == 0) {
			singleValueButton.setSelected(true);
		} else {
			listValueButton.setSelected(true);
			listDepthSpinner.setValue(depth);
		}
	}
	
}
