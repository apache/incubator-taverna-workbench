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
package net.sf.taverna.t2.workbench.ui.impl.menu;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sf.taverna.raven.appconfig.ApplicationConfig;
import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.plugins.Plugin;
import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.t2.lang.io.StreamDevourer;
import net.sf.taverna.t2.lang.ui.ReadOnlyTextArea;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workbench.ui.impl.configuration.WorkbenchConfiguration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * MenuItem for feedback
 * 
 * @author alanrw
 *
 */
public class AboutTavernaMenuAction extends AbstractMenuAction {
	
	private static final Runtime RUNTIME = Runtime.getRuntime();

	private static Logger logger = Logger.getLogger(AboutTavernaMenuAction.class);
	
	private static ApplicationConfig appConfig = ApplicationConfig.getInstance();
	
	private static ApplicationRuntime appRuntime = ApplicationRuntime.getInstance();
	
	private static ConfigurationManager configManager = ConfigurationManager.getInstance();

	private static String PROPERTY_FORMAT = "%1$s = %2$s%n";
	
	private static String MESSAGE_FORMAT = "%1$s%n%nDetailed information is copied to your clipboard%n";
	
	private static String NEWLINED_FORMAT = "%1$s%n";
	
	private AboutTavernaAction ACTION = new AboutTavernaAction();
	
	public AboutTavernaMenuAction() {
		super(HelpMenu.HELP_URI, 50);
	}

	@Override
	protected Action createAction() {
		return ACTION;
	}

	@SuppressWarnings("serial")
	private final class AboutTavernaAction extends AbstractAction {
		private AboutTavernaAction() {
			super("About Taverna");
		}

		public void actionPerformed(ActionEvent e) {
				JPanel messagePanel = new JPanel(new BorderLayout());
	
				ReadOnlyTextArea messageArea = new ReadOnlyTextArea(5, 60);
				final String details = gatherDetails();
				
				messageArea.setText(String.format(MESSAGE_FORMAT, appConfig.getTitle()));
				messageArea.setLineWrap(false);
				messageArea.setSize(messageArea.getPreferredSize());
				JScrollPane messageScroll = new JScrollPane(messageArea);
				messagePanel.add(messageScroll, BorderLayout.CENTER);
				JOptionPane.showMessageDialog(Workbench.getInstance(), messagePanel, "About Taverna", JOptionPane.INFORMATION_MESSAGE);
				
				// Copied from http://stackoverflow.com/questions/6710350/copying-text-to-the-clipboard-using-java
				StringSelection stringSelection = new StringSelection (gatherDetails());
				Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
				clpbrd.setContents (stringSelection, null);
		}
	}

	private static String gatherDetails() {
		
		StringBuilder sb = new StringBuilder();
		appendHeader(sb, "Application Configuration");
		appendProperties(sb, appConfig.getProperties());
		
		appendHeader(sb, "System Properties");
		appendProperties(sb, System.getProperties());
		
		appendHeader(sb, "Runtime Properties");
		appendRuntime(sb);
		
		appendConfigurations(sb);
		
		appendHeader(sb, "Dot Information");
		appendDotVersion(sb);
		
		appendServiceProviders(sb);
		
		appendPluginInfo(sb);
		return sb.toString();
	}

	/**
	 * @param sb
	 */
	private static void appendServiceProviders(StringBuilder sb) {
		try {
			appendFile(sb, new File(configManager.getBaseConfigLocation(), "service_providers.xml"));
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private static void appendProperties(StringBuilder sb, Properties properties) {
		TreeMap<String, String> propertiesMap = new TreeMap<String, String>((Map) properties);
		for (Entry<String, String> property : propertiesMap.entrySet()) {
			String propertyName = property.getKey();
			String propertyValue = property.getValue();
			sb.append(String.format(PROPERTY_FORMAT, propertyName, propertyValue));
		}	
	}
	
	private static void appendRuntime(StringBuilder sb) {
		sb.append(String.format(NEWLINED_FORMAT, "Runtime availableProcessors = " + RUNTIME.availableProcessors()));
		sb.append(String.format(NEWLINED_FORMAT, "Runtime freeMemory = " + RUNTIME.freeMemory()));
		sb.append(String.format(NEWLINED_FORMAT, "Runtime totalMemory = " + RUNTIME.totalMemory()));
		sb.append(String.format(NEWLINED_FORMAT, "Runtime maxMemory = " + RUNTIME.maxMemory()));
	}
	
	private static void appendConfigurations(StringBuilder sb) {
		try {
			File configDir = configManager.getBaseConfigLocation();
			for (File f : configDir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith("config");
				}
				
			}) ) {
				appendFile(sb, f);
			}
		} catch (Exception e) {
			logger.error(e);
		}
		
	}

	/**
	 * @param sb
	 * @param f
	 * @throws IOException
	 */
	private static void appendFile(StringBuilder sb, File f) throws IOException {
		appendHeader(sb, f.getName());
		sb.append(FileUtils.readFileToString(f, null));
	}
	
	private static void appendDotVersion(StringBuilder sb) {
		sb.append(String.format(NEWLINED_FORMAT, "Dot version = " + getDot()));
	}

	public static String getDot() {
		String dotLocation = (String) WorkbenchConfiguration.getInstance().getProperty(
				"taverna.dotlocation");
		if (dotLocation == null) {
			dotLocation = "dot";
		}
		logger.debug("Invoking dot...");
		Process dotProcess;
		try {
			dotProcess = Runtime.getRuntime().exec(new String[] { dotLocation, "-V" });
			StreamDevourer devourer = new StreamDevourer(dotProcess.getInputStream());
			devourer.start();
			// Must create an error devourer otherwise stderr fills up and the
			// process stalls!
			StreamDevourer errorDevourer = new StreamDevourer(dotProcess.getErrorStream());
			errorDevourer.start();

			String dot = devourer.blockOnOutput();
			String dotError = errorDevourer.blockOnOutput();
			// logger.info(dot);
			return dot + dotError;
		} catch (IOException e) {
			return e.getMessage();
		}

	}
	
	private static void appendPluginInfo(StringBuilder sb) {
		File pluginsDir = appRuntime.getPluginsDir();
		File pluginSitesFile = new File(pluginsDir, "plugin-sites.xml");
		File pluginsFile = new File(pluginsDir, "plugins.xml");
		try {
			appendFile(sb, pluginSitesFile);
			appendFile(sb, pluginsFile);
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	private static void appendHeader(StringBuilder sb, String header) {
		sb.append(String.format(NEWLINED_FORMAT, ""));
		sb.append(String.format(NEWLINED_FORMAT, ""));
		sb.append(String.format(NEWLINED_FORMAT, header));
		sb.append(String.format(NEWLINED_FORMAT, StringUtils.repeat("=", header.length())));
		sb.append(String.format(NEWLINED_FORMAT, ""));
	}
	
}
