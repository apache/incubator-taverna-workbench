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
package org.apache.taverna.raven.profile.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.AbstractBorder;

import net.sf.taverna.raven.profile.ProfileHandler;
import net.sf.taverna.raven.profile.ProfileVersion;
import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;

public class ProfileVersionCellRenderer extends JPanel implements
		ListCellRenderer {

	private JLabel name;
	private JLabel version;
	private JLabel description;
	private String currentVersion;
	
	public ProfileVersionCellRenderer() {
		super();
		initialise();
	}
	
	private void initialise() {
		Profile currentProfile = ProfileFactory.getInstance().getProfile();
		if (currentProfile!=null) {
			currentVersion=currentProfile.getVersion();
		}
		else {
			currentVersion="UNKNOWN";
		}
		GridBagConstraints gridBagVersion = new GridBagConstraints();
		gridBagVersion.gridx = 1;
		gridBagVersion.insets = new Insets(3, 8, 3, 3);
		gridBagVersion.anchor = GridBagConstraints.NORTHWEST;
		gridBagVersion.fill = GridBagConstraints.NONE;
		gridBagVersion.gridy = 0;		
		version = new JLabel();
		version.setFont(getFont().deriveFont(Font.BOLD));
		version.setText("Version");
		
		GridBagConstraints gridBagDescription = new GridBagConstraints();
		gridBagDescription.gridx = 0;
		gridBagDescription.anchor = GridBagConstraints.NORTHWEST;
		gridBagDescription.fill = GridBagConstraints.HORIZONTAL;
		gridBagDescription.weightx = 1.0;
		gridBagDescription.insets = new Insets(3, 3, 3, 3);
		gridBagDescription.gridwidth = 2;
		gridBagDescription.gridy = 1;
		description = new JLabel();
		description.setFont(getFont().deriveFont(Font.PLAIN));
		description.setText("Plugin description");
		
		GridBagConstraints gridBagName = new GridBagConstraints();
		gridBagName.gridx = 0;
		gridBagName.anchor = GridBagConstraints.NORTHWEST;
		gridBagName.fill = GridBagConstraints.NONE;
		gridBagName.weightx = 0.0;
		gridBagName.ipadx = 0;
		gridBagName.insets = new Insets(3, 3, 3, 3);
		gridBagName.gridwidth = 1;
		gridBagName.gridy = 0;
		name = new JLabel();
		name.setFont(getFont().deriveFont(Font.PLAIN));
		name.setText("Plugin name");
		
		this.setSize(297, 97);
		this.setLayout(new GridBagLayout());
		this.setBorder(new AbstractBorder() {
			public void paintBorder(Component c, Graphics g, int x, int y,
					int width, int height) {
				Color oldColor = g.getColor();
				g.setColor(Color.LIGHT_GRAY);
				g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
				g.setColor(oldColor);
			}
		});
		this.add(name, gridBagName);
		this.add(description, gridBagDescription);
		this.add(version, gridBagVersion);		
	}
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		
		if (value instanceof ProfileVersion) {
			ProfileVersion version = (ProfileVersion) value;			
			this.name.setText(version.getName());
			if (version.getVersion().equalsIgnoreCase(currentVersion)) {
				this.name.setText(version.getName()+" (Current)");
				this.name.setForeground(Color.BLUE);
			}
			else {
				this.name.setText(version.getName());
				this.name.setForeground(Color.BLACK);
			}
			this.version.setText(version.getVersion());
			this.description.setText("<html>"+version.getDescription());
		}
		
		
		return this;
	}

}
