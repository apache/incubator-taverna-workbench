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
 * Filename           $RCSfile: ProfileVersionListFrame.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/09/04 14:52:06 $
 *               by   $Author: sowen70 $
 * Created on 16 Jan 2007
 *****************************************************************/
package net.sf.taverna.raven.profile.ui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.taverna.raven.appconfig.bootstrap.RavenProperties;
import net.sf.taverna.raven.plugins.Plugin;
import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.raven.profile.ProfileHandler;
import net.sf.taverna.raven.profile.ProfileUpdateHandler;
import net.sf.taverna.raven.profile.ProfileVersion;
import net.sf.taverna.raven.spi.ProfileFactory;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class ProfileVersionListFrame extends JDialog {
	
	private JPanel contentPane = null;
	private JScrollPane scrollPane = null;
	private JList list = null;
	private JButton switchButton = null;
	private JButton closeButton = null;
	private String currentVersion = null;
	
	private static Logger logger = Logger
			.getLogger(ProfileVersionListFrame.class);

	public ProfileVersionListFrame() {
		super();
		initialise();
	}
	
	public ProfileVersionListFrame(Frame parent) {
		super(parent,true);
		initialise();
	}
	
	protected JPanel getJContentPane() {
		if (contentPane==null) {
			GridBagConstraints scrollPaneConstraints = new GridBagConstraints();
			scrollPaneConstraints.fill = GridBagConstraints.BOTH;
			scrollPaneConstraints.gridy = 0;
			scrollPaneConstraints.weightx = 1.0;
			scrollPaneConstraints.weighty = 1.0;
			scrollPaneConstraints.gridwidth = 2;
			scrollPaneConstraints.insets = new Insets(5, 5, 5, 5);
			scrollPaneConstraints.gridx = 0;
			scrollPaneConstraints.gridheight = 3;
			
			GridBagConstraints switchButtonConstraints = new GridBagConstraints();			
			switchButtonConstraints.gridy=0;			
			switchButtonConstraints.gridx=2;
			switchButtonConstraints.insets = new Insets(5,5,5,5);
			switchButtonConstraints.fill=GridBagConstraints.HORIZONTAL;
			
			GridBagConstraints closeButtonConstraints = new GridBagConstraints();
			closeButtonConstraints.gridy=2;			
			closeButtonConstraints.gridx=2;
			closeButtonConstraints.insets = new Insets(5,5,5,5);
			closeButtonConstraints.anchor=GridBagConstraints.SOUTH;
			closeButtonConstraints.fill=GridBagConstraints.HORIZONTAL;
			
			contentPane = new JPanel();
			contentPane.setLayout(new GridBagLayout());
			contentPane.add(getScrollPane(),scrollPaneConstraints);
			contentPane.add(getSwitchButton(),switchButtonConstraints);
			contentPane.add(getCloseButton(),closeButtonConstraints);
			
		}
		return contentPane;
	}
	
	protected JScrollPane getScrollPane() {
		if (scrollPane==null) {
			scrollPane=new JScrollPane();
			scrollPane.setViewportView(getJList());
		}
		return scrollPane;
	}
	
	protected JList getJList() {
		if (list==null) {
			list=new JList();
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setModel(new ProfileVersionListModel());	
			list.setCellRenderer(new ProfileVersionCellRenderer());
			list.addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
						respondToSelection();
					}
				}

			});
			if (list.getComponentCount() > 0) {
				list.setSelectedIndex(0);
				respondToSelection();
				
			}
		}
		return list;
	}
	
	protected void respondToSelection() {
		Object selected=list.getSelectedValue();
		if (selected!=null && selected instanceof ProfileVersion) {
			ProfileVersion version = (ProfileVersion)selected;
			if (currentVersion==null || version.getVersion().equals(currentVersion)) {
				getSwitchButton().setEnabled(false);
			}
			else {
				getSwitchButton().setEnabled(true);
			}
		}
	}
	
	protected JButton getSwitchButton() {
		if (switchButton==null) {
			switchButton=new JButton("Switch");
			switchButton.setEnabled(true);
			switchButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {					
					Object selected = getJList().getSelectedValue();
					if (selected!=null && selected instanceof ProfileVersion) {
						performSwitch((ProfileVersion)selected);
					}
				}
				
			});
		}
		return switchButton;
	}
	
	protected JButton getCloseButton() {
		if (closeButton==null) {
			closeButton=new JButton("Close");
			closeButton.setEnabled(true);
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {					
					setVisible(false);
					dispose();
				}				
			});			
		}
		return closeButton;		
	}
	
	protected void performSwitch(ProfileVersion newVersion) {
		List<Plugin> incompatiblePlugins = PluginManager.getInstance().getIncompatiblePlugins(newVersion.getVersion(),true);
		if (incompatiblePlugins.size()>0) {
			int response=JOptionPane.showConfirmDialog(this, "Some plugins will be incompatible with the new version and will be disabled. Do you wish to continue?","Confirm version switch",JOptionPane.YES_OPTION);
			if (response!=JOptionPane.YES_OPTION) {
				return;
			}			
		}
		try {		
			URL localProfile = new URL(RavenProperties.getInstance().getRavenProfileLocation());
			URL profileList = new URL(RavenProperties.getInstance().getRavenProfileListLocation());
			
			ProfileUpdateHandler handler=new ProfileUpdateHandler(profileList,localProfile);
			handler.updateLocalProfile(newVersion,new File(localProfile.toURI()));			
			
			//disable plugins after everything else has been acheived
			for (Plugin plugin : incompatiblePlugins) {
				plugin.setEnabled(false);
			}			
			JOptionPane.showMessageDialog(this, "You must restart taverna for the version switch to be activated");
		}
		catch(Exception e) {
			logger.error("Error occurred switching to a new profile",e);
			JOptionPane.showMessageDialog(this, "An error occurred switching to your new profile, try again later.");
		}				
	}
	
	
	protected void initialise() {
		try {
			currentVersion = ProfileFactory.getInstance().getProfile().getVersion();
		} catch (Exception e) {
			logger.error("Unable to determine current taverna version",e);
			currentVersion=null;
		}
		setSize(600,400);		
		setContentPane(getJContentPane());
		setTitle("Taverna versions");		
	}	
	
}
