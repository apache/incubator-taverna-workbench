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
/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: ProfileVersionCellRenderer.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/09/04 14:52:06 $
 *               by   $Author: sowen70 $
 * Created on 16 Jan 2007
 *****************************************************************/
package net.sf.taverna.raven.profile.ui;

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
		version.setText("version");
		
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
		description.setText("plugin description");
		
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
		name.setText("plugin name");
		
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
