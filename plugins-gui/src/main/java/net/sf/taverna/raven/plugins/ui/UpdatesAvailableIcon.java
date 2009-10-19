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
 * Filename           $RCSfile: UpdatesAvailableIcon.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/12/01 12:32:40 $
 *               by   $Author: alaninmcr $
 * Created on 12 Dec 2006
 *****************************************************************/
package net.sf.taverna.raven.plugins.ui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.raven.plugins.event.PluginManagerEvent;
import net.sf.taverna.raven.plugins.event.PluginManagerListener;

import org.apache.log4j.Logger;

/**
 * A JLabel that periodically checks for updates, running on a daemon thread. If
 * updates are available it makes itself visible and responds to click events to
 * display the appropriate update response.
 * 
 * Also acts as a pluginmanager listener to refresh itself whenever a new plugin
 * is added.
 * 
 * @author Stuart Owen
 * 
 */

@SuppressWarnings("serial")
public class UpdatesAvailableIcon extends JLabel implements
		PluginManagerListener {

	private UpdatePluginsMouseAdaptor updatePluginMouseAdaptor = new UpdatePluginsMouseAdaptor();
	private static Logger logger = Logger.getLogger(UpdatesAvailableIcon.class);

	private final int CHECK_INTERVAL = 1800000; // every 30 minutes

	public static final Icon updateIcon = new ImageIcon(
			UpdatesAvailableIcon.class.getResource("update.png"));
	public static final Icon updateRecommendedIcon = new ImageIcon(
			UpdatesAvailableIcon.class.getResource("updateRecommended.png"));

	public UpdatesAvailableIcon() {
		super();
		setVisible(false);

		startCheckThread();
		PluginManager.addPluginManagerListener(this);
	}

	public void pluginAdded(PluginManagerEvent event) {
		logger.info("Plugin Added");
		if (!isVisible())
			checkForUpdates();
	}

	public void pluginStateChanged(PluginManagerEvent event) {

	}

	public void pluginUpdated(PluginManagerEvent event) {
		logger.info("Plugin Updated");
	}
	
	public void pluginRemoved(PluginManagerEvent event) {
		logger.info("Plugin Removed");
		if (isVisible())
			checkForUpdates();
	}

	public void pluginIncompatible(PluginManagerEvent event) {
		logger
				.warn("Plugin found to be incompatible with the current version of Taverna: "
						+ event.getPlugin());
	}

	private void startCheckThread() {
		Thread checkThread = new Thread("Check for updates thread") {

			@Override
			public void run() {
				while (true) {
					try {
						checkForUpdates();
						Thread.sleep(CHECK_INTERVAL);
					} catch (InterruptedException e) {
						logger.warn("Interruption exception in checking for updates thread",
										e);
					}
				}
			}
		};
		checkThread.setDaemon(true); // daemon so that taverna will stop the
										// thread and close on exit.
		checkThread.start();
	}

	private Object updateLock = new Object();

	private void checkForUpdates() {
		synchronized (updateLock) {
			if (pluginUpdateAvailable()) {
				logger.info("Plugin update available");
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							// TODO Auto-generated method stub
							setToolTipText("Plugin updates are available");

							setVisible(true);
							setIcon(updateIcon);
							if (!Arrays.asList(getMouseListeners()).contains(
									updatePluginMouseAdaptor)) {
								addMouseListener(updatePluginMouseAdaptor);
							}

						}
					});
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				setToolTipText("");
				setVisible(false);
			
			}
		}

	}

	private boolean pluginUpdateAvailable() {
		return PluginManager.getInstance().checkForUpdates();
	}

	private final class UpdatePluginsMouseAdaptor extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			// FIXME: this assumes the button is on the toolbar.
			Component parent = UpdatesAvailableIcon.this.getParent()
					.getParent();

			final PluginManagerFrame pluginManagerUI = new PluginManagerFrame(
					PluginManager.getInstance());
			pluginManagerUI.setLocationRelativeTo(parent);
			pluginManagerUI.setVisible(true);
		}

	}

}
