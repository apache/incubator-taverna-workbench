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
 * Filename           $RCSfile: PluginSiteFrame.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/10/27 13:39:56 $
 *               by   $Author: stain $
 * Created on 29 Nov 2006
 *****************************************************************/
package net.sf.taverna.raven.plugins.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;

import net.sf.taverna.raven.plugins.Plugin;
import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.raven.plugins.PluginSite;
import net.sf.taverna.raven.plugins.TavernaPluginSite;


/**
 * 
 * @author David Withers
 */
public class PluginSiteFrame extends JDialog {	

	private static final long serialVersionUID = 1L;

	private PluginManager pluginManager;

	private List<PluginSite> pluginSites;

	private List<Plugin> installationScheduled = new ArrayList<Plugin>(); // @jve:decl-index=0:

	private JButton installButton = null; // @jve:decl-index=0:visual-constraint="446,247"

	private JButton cancelButton = null;

	private JButton addSiteButton = null;

	private Map<Plugin, PluginRepositoryListener> listeners = new HashMap<Plugin, PluginRepositoryListener>();
	
	private AddPluginSiteFrame addSiteFrame = null;
	
	private JScrollPane scrollPane = null;

	/**
	 * This is the default constructor
	 */
	public PluginSiteFrame(Frame owner) {
		super(owner,true);				
		initialize();
	}
	
	/**
	 * This is the default constructor
	 */
	public PluginSiteFrame(JDialog owner) {
		super(owner,true);			
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.pluginManager = PluginManager.getInstance();
		this.pluginSites = this.pluginManager.getPluginSites();
		this.setSize(600, 450);
		this.setContentPane(getJContentPane());
		this.setTitle("Update Sites");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
		gridBagConstraints14.gridx = 1;
		gridBagConstraints14.anchor = GridBagConstraints.SOUTHWEST;
		gridBagConstraints14.gridy = GridBagConstraints.RELATIVE;
		gridBagConstraints14.insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
		gridBagConstraints12.fill = GridBagConstraints.BOTH;
		gridBagConstraints12.gridx = 0;
		gridBagConstraints12.gridy = 0;
		gridBagConstraints12.weightx = 1.0;
		gridBagConstraints12.weighty = 1.0;
		gridBagConstraints12.gridwidth = 3;
		gridBagConstraints12.anchor = GridBagConstraints.NORTHWEST;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
		gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 2;
		gridBagConstraints1.gridy = GridBagConstraints.RELATIVE;
		gridBagConstraints1.anchor = GridBagConstraints.SOUTHEAST;
		gridBagConstraints1.insets = new Insets(5, 5, 5, 5);
		JPanel jContentPane = new JPanel();
		jContentPane.setLayout(new GridBagLayout());
		jContentPane.add(getJScrollPane(), gridBagConstraints12);
		jContentPane.add(getInstallButton(), gridBagConstraints);
		jContentPane.add(getCloseButton(), gridBagConstraints1);
		jContentPane.add(getAddPluginSiteButton(), gridBagConstraints14);
		return jContentPane;
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		scrollPane = new JScrollPane();		
		scrollPane.setViewportView(getJPanel());		
		return scrollPane;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new GridBagLayout());
		for (PluginSite pluginSite : pluginSites) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 0.0;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints.insets = new Insets(5, 5, 0, 5);
			jPanel.add(getJPanel1(pluginSite), gridBagConstraints);
		}
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
		gridBagConstraints.weighty = 1.0;
		jPanel.add(new JPanel(), gridBagConstraints);		
		return jPanel;
	}

	private JPanel getJPanel1(final PluginSite pluginSite) {
		final JPanel pluginSitePanel = new JPanel();
		pluginSitePanel.setBackground(Color.WHITE);
		pluginSitePanel.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.ipadx = 5;
		gridBagConstraints.ipady = 5;
		gridBagConstraints.weightx = 1.0;
		//ShadedLabel siteNameLabel = getSiteLabel(pluginSite);
		JLabel siteNameLabel = getSiteLabel(pluginSite);
		
		pluginSitePanel.add(siteNameLabel, gridBagConstraints);

		final GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.anchor = GridBagConstraints.WEST;
		gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.gridwidth = 2;
		gridBagConstraints1.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints1.weightx = 1.0;
		final JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setString("Checking for new plugins");
		pluginSitePanel.add(progressBar, gridBagConstraints1);

		new Thread("Checking update site " + pluginSite) {
			public void run() {
				try {
					List<Plugin> plugins = pluginManager
							.getUninstalledPluginsFromSite(pluginSite);
					if (plugins.size() > 0) {
						Collections.sort(plugins, new Comparator<Plugin>() {

							public int compare(Plugin o1, Plugin o2) {
								return o1.getName().compareTo(o2.getName());
							}

						});

						pluginSitePanel.remove(progressBar);
						int gridY = 0;
						for (Plugin plugin : plugins) {
							gridY++;
							GridBagConstraints gridBagConstraints1 = new GridBagConstraints();							
							gridBagConstraints1.gridx = 0;
							gridBagConstraints1.gridy = gridY;
							gridBagConstraints1.anchor = GridBagConstraints.WEST;
							gridBagConstraints1.insets = new Insets(5, 20, 0, 0);
							pluginSitePanel.add(getJCheckBox(plugin),
									gridBagConstraints1);
							
							gridY++;
							
							GridBagConstraints gridBagConstraintsDescription = new GridBagConstraints();							
							gridBagConstraintsDescription.gridx = 0;
							gridBagConstraintsDescription.ipadx = 50;
							gridBagConstraintsDescription.gridy = gridY;							
							gridBagConstraintsDescription.anchor = GridBagConstraints.WEST;
							gridBagConstraintsDescription.insets = new Insets(5, 25, 10, 5);							

							gridY++;

							GridBagConstraints gridBagConstraintsProgress = new GridBagConstraints();							
							gridBagConstraintsProgress.gridx = 0;
							gridBagConstraintsProgress.gridy = gridY;							
							gridBagConstraintsProgress.fill = GridBagConstraints.HORIZONTAL;
							gridBagConstraintsProgress.anchor = GridBagConstraints.WEST;
							gridBagConstraintsProgress.insets = new Insets(5, 5, 0, 0);							
							
							JLabel description = new JLabel();							
							description.setText("<html>"+plugin.getDescription());														
							description.setFont(getFont().deriveFont(Font.PLAIN));
							pluginSitePanel.add(description,gridBagConstraintsDescription);
							
							PluginRepositoryListener progress = new PluginRepositoryListener();
							listeners.put(plugin, progress);
							progress.setVisible(false);
							
							pluginSitePanel.add(progress.getProgressBar(),
									gridBagConstraintsProgress);													
						}
					} else {
						pluginSitePanel.remove(progressBar);
						pluginSitePanel.add(new JLabel(
								"This update site contains no new plugins"),
								gridBagConstraints1);
					}
				} catch (Exception e) {
					pluginSitePanel.remove(progressBar);
					pluginSitePanel.add(new JLabel(
							"Unable to contact the update site"),
							gridBagConstraints1);
				} finally {
					pluginSitePanel.revalidate();
					pluginSitePanel.repaint();
				}
			}
		}.start();

		pluginSitePanel.setBorder(new EtchedBorder());		
		return pluginSitePanel;
	}
	
	private JLabel getSiteLabel(final PluginSite pluginSite) {
		//ShadedLabel result = new ShadedLabel(pluginSite.getName(),Color.LIGHT_GRAY);		
		JLabel result = new JLabel(pluginSite.getName());

		
		//add popup remove option, except on the Taverna main plugin site.
		if (!(pluginSite instanceof TavernaPluginSite)) {
			result.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					popup(e);
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					popup(e);
				}
				
				private void popup(MouseEvent e) {				
					if (e.isPopupTrigger()) {									
						JPopupMenu menu=new JPopupMenu();	
						//JMenuItem item = new JMenuItem("Remove site",TavernaIcons.deleteIcon);
						JMenuItem item = new JMenuItem("Remove site");

						menu.add(item);
						menu.show(e.getComponent(), e.getX(), e.getY());
						item.addActionListener(new ActionListener() {
	
							public void actionPerformed(ActionEvent e) {
								int response=JOptionPane.showConfirmDialog(PluginSiteFrame.this, "Are you sure you want to remove the update site:"+pluginSite.getName(),"Remove update site",JOptionPane.YES_NO_OPTION);
								if (response==JOptionPane.YES_OPTION) {
									pluginManager.removePluginSite(pluginSite);
									pluginManager.savePluginSites();
									scrollPane.setViewportView(getJPanel());
								}
							}						
						});
					}
				}			
			});
		}
		return result;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JCheckBox getJCheckBox(Plugin plugin) {
		PluginCheckBox checkBox = new PluginCheckBox();
		checkBox.plugin = plugin;
		checkBox.setText(plugin.getName() + " " + plugin.getVersion());
		checkBox.setOpaque(false);
		checkBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof PluginCheckBox) {
					PluginCheckBox checkBox = (PluginCheckBox) e.getSource();
					if (checkBox.isSelected()) {
						installationScheduled.add(checkBox.plugin);
						
						
						
						getInstallButton().setEnabled(true);
					} else {
						installationScheduled.remove(checkBox.plugin);
						if (installationScheduled.size() == 0) {
							getInstallButton().setEnabled(false);
						}
					}
				}
			}

		});
		return checkBox;
	}

	private final Thread getUpdateRepositoryThread() {
		return new Thread("Update Repository Progress") {

			public void run() {				
				cancelButton.setEnabled(false); 
				for (int i = 0; i < installationScheduled.size(); i++) {
					final Plugin plugin = installationScheduled.get(i);
					PluginRepositoryListener listener = listeners.get(plugin);
					if (listener != null) {
						pluginManager.getRepository().addRepositoryListener(
								listener);
						listener.getProgressBar().setVisible(true);
					}

					pluginManager.addPlugin(plugin);

					if (listener != null) {
						pluginManager.getRepository().removeRepositoryListener(
								listener);
						listener.getProgressBar().setVisible(false);
					}
					plugin.setEnabled(true);

				}
				pluginManager.savePlugins();
				installationScheduled.clear();
				setVisible(false);
				dispose();				
			}

		};
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getInstallButton() {
		if (installButton == null) {
			installButton = new JButton();
			installButton.setText("Install");
			installButton.setEnabled(false);
			installButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							installButton.setEnabled(false);
							getUpdateRepositoryThread().start();
						}

					});
		}
		return installButton;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getCloseButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText("Close");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});
		}
		return cancelButton;
	}

	@SuppressWarnings("serial")
	class PluginCheckBox extends JCheckBox {
		public Plugin plugin;
	}

	private final void addPluginSite() {
			
		addSiteFrame=new AddPluginSiteFrame(this);
		
		addSiteFrame.setLocationRelativeTo(this);
		addSiteFrame.setVisible(true);
		
		if (addSiteFrame.getName()!=null) {
			if (addSiteFrame.getName().length()==0) {
				JOptionPane.showMessageDialog(this, "You must provide a name for your site.","Error adding update site",JOptionPane.ERROR_MESSAGE);
				addPluginSite();
			}
			else {
				if (addSiteFrame.getUrl()!=null) {
					try {
						PluginSite site = new PluginSite(addSiteFrame.getName(), new URL(addSiteFrame.getUrl()));
						pluginManager.addPluginSite(site);
						pluginManager.savePluginSites();
						
						//refresh
						scrollPane.setViewportView(getJPanel());
						addSiteFrame=null; //so that the name and url are reset.
					}
					catch(Exception e) {
						JOptionPane.showMessageDialog(this, "There was a problem adding the site you provided: "+e.getMessage(),"Error adding update site",JOptionPane.ERROR_MESSAGE);						
					}
				}
			}
		}
	}
	
	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getAddPluginSiteButton() {
		if (addSiteButton == null) {
			addSiteButton = new JButton();
			addSiteButton.setText("Add update site");
			addSiteButton.setEnabled(true);
			addSiteButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addSiteButton.setEnabled(false);
					addPluginSite();
					addSiteButton.setEnabled(true);
				}				
			});
			
		}
		return addSiteButton;
	}

} // @jve:decl-index=0:visual-constraint="10,10"
