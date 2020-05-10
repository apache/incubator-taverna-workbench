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
package org.apache.taverna.workbench.ui.impl;

import javax.swing.*;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.lang.Thread.setDefaultUncaughtExceptionHandler;
import static java.util.prefs.Preferences.userNodeForPackage;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.UIManager.getCrossPlatformLookAndFeelClassName;
import static javax.swing.UIManager.getLookAndFeel;
import static javax.swing.UIManager.getLookAndFeelDefaults;
import static javax.swing.UIManager.getSystemLookAndFeelClassName;
import static org.apache.taverna.workbench.MainWindow.setMainWindow;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.errorMessageIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.infoMessageIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.questionMessageIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.warningMessageIcon;
import static org.apache.log4j.Logger.getLogger;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.SwingAwareObserver;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.ui.menu.MenuManager.MenuManagerEvent;
import org.apache.taverna.ui.menu.MenuManager.UpdatedMenuManagerEvent;
import org.apache.taverna.workbench.ShutdownSPI;
import org.apache.taverna.workbench.StartupSPI;
import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;
import org.apache.taverna.workbench.configuration.workbench.ui.T2ConfigurationFrame;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.exceptions.OpenException;
import org.apache.taverna.workbench.helper.Helper;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.Workbench;
import org.apache.taverna.workbench.ui.zaria.PerspectiveSPI;

import org.apache.log4j.Logger;
import org.apache.taverna.configuration.app.ApplicationConfiguration;
import org.apache.taverna.plugin.PluginException;
import org.apache.taverna.plugin.PluginManager;
import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.ApplicationAdapter;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;
import org.simplericity.macify.eawt.DefaultApplication;
//import org.springframework.stereotype.Component;

/**
 * The main workbench frame.
 *
 */

//@Component
public class WorkbenchImpl extends JFrame implements Workbench {
	private static final String NIMBUS = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
	private static final String LAUNCHER_LOGO_PNG = "/launcher_logo.png";
	private static final long serialVersionUID = 1L;
	private static Logger logger = getLogger(WorkbenchImpl.class);
	private static Preferences userPreferences = userNodeForPackage(WorkbenchImpl.class);
	
	private Application osxApp = new DefaultApplication();
	private ApplicationListener osxAppListener = new OSXAppListener();
	private MenuManager menuManager;
	private FileManager fileManager;
	@SuppressWarnings("unused")
	private EditManager editManager;
	private PluginManager pluginManager;
	private SelectionManager selectionManager;
	private WorkbenchConfiguration workbenchConfiguration;
	private ApplicationConfiguration applicationConfiguration;
	private WorkbenchPerspectives workbenchPerspectives;
	private T2ConfigurationFrame t2ConfigurationFrame;
	private JToolBar perspectiveToolBar;
	private List<StartupSPI> startupHooks;
	private List<ShutdownSPI> shutdownHooks;
	private final List<PerspectiveSPI> perspectives;
	private JMenuBar menuBar;
	private JToolBar toolBar;
	private MenuManagerObserver menuManagerObserver;

	public WorkbenchImpl(List<StartupSPI> startupHooks,
			List<ShutdownSPI> shutdownHooks, List<PerspectiveSPI> perspectives) {
		this.perspectives = perspectives;
		this.startupHooks = startupHooks;
		this.shutdownHooks = shutdownHooks;
		setMainWindow(this);
	}

	protected void initialize() {
		setExceptionHandler();
		setLookAndFeel();

		// Set icons for Error, Information, Question and Warning messages
		UIManager.put("OptionPane.errorIcon", errorMessageIcon);
		UIManager.put("OptionPane.informationIcon", infoMessageIcon);
		UIManager.put("OptionPane.questionIcon", questionMessageIcon);
		UIManager.put("OptionPane.warningIcon", warningMessageIcon);

		// Call the startup hooks
		if (!callStartupHooks()) {
			System.exit(0);
		}

		makeGUI();
		fileManager.newDataflow();
		try {
			pluginManager.loadPlugins();
		} catch (PluginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * the DataflowEditsListener changes the WorkflowBundle ID for every
		 * workflow edit and changes the URI so port definitions can't find the
		 * port they refer to
		 */
		// TODO check if it's OK to not update the WorkflowBundle ID
		//editManager.addObserver(new DataflowEditsListener());

		closeTheSplashScreen();
		setVisible(true);
	}

	private void closeTheSplashScreen() {
//		SplashScreen splash = SplashScreen.getSplashScreen();
//		if (splash != null) {
//			splash.setClosable();
//			splash.requestClose();
//		}
	}

	private void showAboutDialog() {
		// TODO implement this!
	}

	private void makeGUI() {
		setLayout(new GridBagLayout());

		addWindowListener(new WindowClosingListener());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		Helper.setKeyCatcher(this);

		URL launcherLogo = getClass().getResource(LAUNCHER_LOGO_PNG);
		if (launcherLogo != null) {
			ImageIcon imageIcon = new ImageIcon(launcherLogo);
			setIconImage(imageIcon.getImage());
		}
		setTitle(applicationConfiguration.getTitle());

		osxApp.setEnabledPreferencesMenu(true);
		osxApp.setEnabledAboutMenu(true);
		osxApp.addApplicationListener(osxAppListener);

		/*
		 * Set the size and position of the Workbench to the last saved values
		 * or use the default ones the first time it is launched
		 */
		loadSizeAndLocationPrefs();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.1;
		gbc.fill = HORIZONTAL;
		gbc.anchor = LINE_START;

		add(makeToolbarPanel(), gbc);

		gbc.anchor = CENTER;
		gbc.fill = BOTH;
		gbc.gridy = 1;
		gbc.weightx = 0.1;
		gbc.weighty = 0.1;

		add(makePerspectivePanel(), gbc);

		menuBar = menuManager.createMenuBar();
		setJMenuBar(menuBar);
	}

	protected JPanel makeToolbarPanel() {
		JPanel toolbarPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.anchor = LINE_START;

		toolBar = menuManager.createToolBar();
		toolBar.setFloatable(false);
		toolbarPanel.add(toolBar, gbc);

		perspectiveToolBar = new JToolBar("Perspectives");
		perspectiveToolBar.setFloatable(false);
		gbc.gridy = 1;
		gbc.weightx = 0.1;
		gbc.fill = HORIZONTAL;
		toolbarPanel.add(perspectiveToolBar, gbc);

		return toolbarPanel;
	}

	private JPanel makePerspectivePanel() {
		CardLayout perspectiveLayout = new CardLayout();
		JPanel perspectivePanel = new JPanel(perspectiveLayout);
		workbenchPerspectives = new WorkbenchPerspectives(perspectiveToolBar,
				perspectivePanel, perspectiveLayout, selectionManager);
		workbenchPerspectives.setPerspectives(perspectives);
		return perspectivePanel;
	}

	@Override
	public void makeNamedComponentVisible(String componentName) {
		// basePane.makeNamedComponentVisible(componentName);
	}

	protected void setExceptionHandler() {
		setDefaultUncaughtExceptionHandler(new ExceptionHandler());
	}

	public void setStartupHooks(List<StartupSPI> startupHooks) {
		this.startupHooks = startupHooks;
	}

	/**
	 * Calls the startup methods on all the {@link StartupSPI}s. If any startup
	 * method returns <code>false</code> (meaning that the Workbench will not
	 * function at all) then this method returns <code>false</code>.
	 */
	private boolean callStartupHooks() {
		for (StartupSPI startupSPI : startupHooks)
			if (!startupSPI.startup())
				return false;
		return true;
	}

	@Override
	public void exit() {
		if (callShutdownHooks())
			System.exit(0);
	}

	public void setShutdownHooks(List<ShutdownSPI> shutdownHooks) {
		this.shutdownHooks = shutdownHooks;
	}

	/**
	 * Calls all the shutdown on all the {@link ShutdownSPI}s. If a shutdown
	 * returns <code>false</code> (meaning that the shutdown process should be
	 * aborted) then this method returns with a value of <code>false</code>
	 * immediately.
	 *
	 * @return <code>true</code> if all the <code>ShutdownSPIs</code> return
	 *         <code>true</code> and the workbench shutdown should proceed
	 */
	private boolean callShutdownHooks() {
		for (ShutdownSPI shutdownSPI : shutdownHooks)
			if (!shutdownSPI.shutdown())
				return false;
		return true;
	}

	/**
	 * Store current Workbench position and size.
	 */
	@Override
	public void storeSizeAndLocationPrefs() throws IOException {
		userPreferences.putInt("width", getWidth());
		userPreferences.putInt("height", getHeight());
		userPreferences.putInt("x", getX());
		userPreferences.putInt("y", getY());
	}

	/**
	 * Loads last saved Workbench position and size.
	 */
	private void loadSizeAndLocationPrefs() {
		Dimension screen = getToolkit().getScreenSize();

		int width = userPreferences.getInt("width", (int) (screen.getWidth() * 0.75));
		int height = userPreferences.getInt("height", (int) (screen.getHeight() * 0.75));
		int x = userPreferences.getInt("x", 0);
		int y = userPreferences.getInt("y", 0);

		// Make sure our window is not too big
		width = Math.min((int) screen.getWidth(), width);
		height = Math.min((int) screen.getHeight(), height);

		// Move to upper left corner if we are too far off
		if (x > (screen.getWidth() - 50) || x < 0)
			x = 0;
		if (y > (screen.getHeight() - 50) || y < 0)
			y = 0;

		this.setSize(width, height);
		this.setLocation(x, y);
	}

	public static void setLookAndFeel() {
		String defaultLaf = System.getProperty("swing.defaultlaf");
		try {
			if (defaultLaf != null) {
				UIManager.setLookAndFeel(defaultLaf);
				return;
			}
		} catch (Exception e) {
			logger.info("Can't set requested look and feel -Dswing.defaultlaf="
					+ defaultLaf, e);
		}
		String os = System.getProperty("os.name");
		if (os.contains("Mac") || os.contains("Windows")) {
			// For OSX and Windows use the system look and feel
			String systemLF = getSystemLookAndFeelClassName();
			try {
				UIManager.setLookAndFeel(systemLF);
				getLookAndFeelDefaults().put("ClassLoader",
						WorkbenchImpl.class.getClassLoader());
				logger.info("Using system L&F " + systemLF);
				return;
			} catch (Exception ex2) {
				logger.error("Unable to load system look and feel " + systemLF,
						ex2);
			}
		}
		/*
		 * The system look and feel on *NIX
		 * (com.sun.java.swing.plaf.gtk.GTKLookAndFeel) looks like Windows 3.1..
		 * try to use Nimbus (Java 6e10 and later)
		 */
		try {
			UIManager.setLookAndFeel(NIMBUS);
			logger.info("Using Nimbus look and feel");
			return;
		} catch (Exception e) {
		}

		// Metal should be better than GTK still
		try {
			String crossPlatform = getCrossPlatformLookAndFeelClassName();
			UIManager.setLookAndFeel(crossPlatform);
			logger.info("Using cross platform Look and Feel " + crossPlatform);
			return;
		} catch (Exception e){
		}

		// Final fallback
		try {
			String systemLF = getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(systemLF);
			logger.info("Using system platform Look and Feel " + systemLF);
		} catch (Exception e){
			logger.info("Using default Look and Feel " + getLookAndFeel());
		}
	}

	public void setMenuManager(MenuManager menuManager) {
		if (this.menuManager != null && menuManagerObserver != null)
			this.menuManager.removeObserver(menuManagerObserver);
		this.menuManager = menuManager;
		menuManagerObserver = new MenuManagerObserver();
		menuManager.addObserver(menuManagerObserver);
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void refreshPerspectives(Object service, Map<?,?> properties) {
		workbenchPerspectives.refreshPerspectives();
	}

	public void setWorkbenchConfiguration(WorkbenchConfiguration workbenchConfiguration) {
		this.workbenchConfiguration = workbenchConfiguration;
	}

	public void setApplicationConfiguration(ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}

	public void setT2ConfigurationFrame(T2ConfigurationFrame t2ConfigurationFrame) {
		this.t2ConfigurationFrame = t2ConfigurationFrame;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	private final class MenuManagerObserver extends
			SwingAwareObserver<MenuManagerEvent> {
		@Override
		public void notifySwing(Observable<MenuManagerEvent> sender,
				MenuManagerEvent message) {
			if (message instanceof UpdatedMenuManagerEvent
					&& WorkbenchImpl.this.isVisible())
				refreshMenus();
		}
	}
	
	private void refreshMenus() {
		if (menuBar != null) {
			menuBar.revalidate();
			menuBar.repaint();
		}
		if (toolBar != null) {
			toolBar.revalidate();
			toolBar.repaint();
		}
	}

	private final class ExceptionHandler implements UncaughtExceptionHandler {
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			logger.error("Uncaught exception in " + t, e);
			if (e instanceof Exception
					&& !workbenchConfiguration.getWarnInternalErrors()) {
				/*
				 * User preference disables warnings - but we'll show it anyway
				 * if it is an Error (which is more serious)
				 */
				return;
			}
			final JTextArea message;
			final JTextArea title;
			final int style;
			if (t.getClass().getName().equals("java.awt.EventDispatchThread")) {
				final String msg = "The user action could not be completed due to an unexpected error:\n"+ e;
				message= new JTextArea(msg);
 				message.setEditable(true);

				final String ttl = "Could not complete user action";
				title= new JTextArea(ttl);
 				title.setEditable(true);
				style = ERROR_MESSAGE;
			} else {
				final String msg = "An unexpected internal error occured in \n" + t + ":\n" + e;
				message= new JTextArea(msg);
 				message.setEditable(true);

				final String ttl = "Unexpected internal error";
				title= new JTextArea(ttl);
 				title.setEditable(true);
				style = WARNING_MESSAGE;
			}
			invokeLater(new Runnable() {
				@Override
				public void run() {
					showMessageDialog(WorkbenchImpl.this, message, String.valueOf(title), style);
				}
			});
		}
	}

	private class WindowClosingListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			exit();
		}
	}

	public class OSXAppListener extends ApplicationAdapter {
		@Override
		public void handleAbout(ApplicationEvent e) {
			showAboutDialog();
			e.setHandled(true);
		}

		@Override
		public void handleQuit(ApplicationEvent e) {
			e.setHandled(true);
			exit();
		}

		@Override
		public void handlePreferences(ApplicationEvent e) {
			e.setHandled(true);
			t2ConfigurationFrame.showFrame();
		}

		@Override
		public void handleOpenFile(ApplicationEvent e) {
			try {
				if (e.getFilename() != null) {
					fileManager.openDataflow(null, new File(e.getFilename()));
					e.setHandled(true);
				}
			} catch (OpenException | IllegalStateException ex) {
				logger.warn("Could not open file " + e.getFilename(), ex);
			}
		}
	}
}
