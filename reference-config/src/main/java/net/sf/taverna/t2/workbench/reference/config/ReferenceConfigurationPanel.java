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
package net.sf.taverna.t2.workbench.reference.config;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class ReferenceConfigurationPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private ReferenceConfiguration configuration = ReferenceConfiguration
			.getInstance();

	public ReferenceConfigurationPanel() {
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);

		JTextArea storageText = new JTextArea(
				"Select how Taverna stores the data produced when a workflow is run. This includes workflow results and intermediate results.");
		storageText.setLineWrap(true);
		storageText.setWrapStyleWord(true);
		storageText.setEditable(false);
		storageText.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JLabel storageConfig = new JLabel(
				"Store data from workflow runs using:");

		JRadioButton hibernateCache = new JRadioButton("On-disk (with cache) storage");
		JRadioButton hibernate = new JRadioButton("On-disk storage");
		JRadioButton inMemory = new JRadioButton("In-memory storage");

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(hibernateCache);
		buttonGroup.add(hibernate);
		buttonGroup.add(inMemory);

		JTextArea hibernateCacheText = new JTextArea(
		"Stores data on the disk and uses an in-memory cache for faster workflow runs. This is the best option for most users.");
		hibernateCacheText.setLineWrap(true);
		hibernateCacheText.setWrapStyleWord(true);
		hibernateCacheText.setEditable(false);
		hibernateCacheText.setOpaque(false);
		hibernateCacheText.setFont(hibernateCacheText.getFont().deriveFont(Font.PLAIN, 10));
		
		JTextArea hibernateText = new JTextArea(
		"Stores data on the disk. Select this option if you are having problems with Taverna running out of memory.");
		hibernateText.setLineWrap(true);
		hibernateText.setWrapStyleWord(true);
		hibernateText.setEditable(false);
		hibernateText.setOpaque(false);
		hibernateText.setFont(hibernateCacheText.getFont().deriveFont(Font.PLAIN, 10));
		
		JTextArea inMemoryText = new JTextArea(
		"Stores data in-memory - data will not be stored between workbench sessions. This option is intended for testing only.");
		inMemoryText.setLineWrap(true);
		inMemoryText.setWrapStyleWord(true);
		inMemoryText.setEditable(false);
		inMemoryText.setOpaque(false);
		inMemoryText.setFont(hibernateCacheText.getFont().deriveFont(Font.PLAIN, 10));
		
		String context = configuration
				.getProperty(ReferenceConfiguration.REFERENCE_SERVICE_CONTEXT);
		if (context.equals(ReferenceConfiguration.IN_MEMORY_CONTEXT)) {
			inMemory.setSelected(true);
		} else if (context.equals(ReferenceConfiguration.HIBERNATE_CONTEXT)) {
			hibernate.setSelected(true);
		} else if (context
				.equals(ReferenceConfiguration.HIBERNATE_CACHE_CONTEXT)) {
			hibernateCache.setSelected(true);
		}

		inMemory.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					configuration.setProperty(
							ReferenceConfiguration.REFERENCE_SERVICE_CONTEXT,
							ReferenceConfiguration.IN_MEMORY_CONTEXT);
				}
			}
		});
		hibernate.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					configuration.setProperty(
							ReferenceConfiguration.REFERENCE_SERVICE_CONTEXT,
							ReferenceConfiguration.HIBERNATE_CONTEXT);
				}
			}
		});
		hibernateCache.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					configuration.setProperty(
							ReferenceConfiguration.REFERENCE_SERVICE_CONTEXT,
							ReferenceConfiguration.HIBERNATE_CACHE_CONTEXT);
				}
			}
		});
		
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(0, 0, 10, 0);
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.weightx = 1d;
		c.weighty = 0d;
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(storageText, c);
		add(storageText);
		
		c.ipady = 0;

		gridbag.setConstraints(storageConfig, c);
		add(storageConfig);
		
		gridbag.setConstraints(hibernateCache, c);
		add(hibernateCache);

		c.insets = new Insets(0, 20, 15, 20);
		gridbag.setConstraints(hibernateCacheText, c);
		add(hibernateCacheText);
		
		c.insets = new Insets(0, 0, 5, 0);
		gridbag.setConstraints(hibernate, c);
		add(hibernate);
		
		c.insets = new Insets(0, 20, 15, 20);
		gridbag.setConstraints(hibernateText, c);
		add(hibernateText);
		
		c.insets = new Insets(0, 0, 5, 0);
		gridbag.setConstraints(inMemory, c);
		add(inMemory);

		c.insets = new Insets(0, 20, 10, 20);
		c.weighty = 1d;
		gridbag.setConstraints(inMemoryText, c);
		add(inMemoryText);
		
	}
	
	// for testing only
	public static void main(String[] args) {
		JDialog dialog = new JDialog();
		dialog.add(new ReferenceConfigurationPanel());
		dialog.setModal(true);
		dialog.setSize(500, 300);
		dialog.setVisible(true);
		System.exit(0);
	}

}
