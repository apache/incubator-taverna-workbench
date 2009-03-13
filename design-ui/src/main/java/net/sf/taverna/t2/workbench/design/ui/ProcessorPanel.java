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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * UI for editing processors.
 * 
 * @author David Withers
 */
public class ProcessorPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JTextField processorNameField;

	public ProcessorPanel() {
		super(new GridBagLayout());

		processorNameField = new JTextField();
 
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
		add(processorNameField, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.fill = GridBagConstraints.VERTICAL;
		constraints.weighty = 1d;
		add(new JPanel(), constraints);
	}
	
	/**
	 * Returns the processorNameField.
	 *
	 * @return the processorNameField
	 */
	public JTextField getProcessorNameField() {
		return processorNameField;
	}

	/**
	 * Returns the processor name.
	 *
	 * @return the processor name
	 */
	public String getProcessorName() {
		return processorNameField.getText();
	}
	
	/**
	 * Sets the processor name.
	 *
	 * @param name the name of the processor
	 */
	public void setProcessorName(String name) {
		processorNameField.setText(name);
	}
	
}
