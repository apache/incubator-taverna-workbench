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
package net.sf.taverna.t2.workbench.ui.impl;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import net.sf.taverna.osx.OSXAdapter;
import net.sf.taverna.osx.OSXApplication;
import net.sf.taverna.raven.SplashScreen;
import net.sf.taverna.raven.appconfig.ApplicationConfig;
import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.log.ConsoleLog;
import net.sf.taverna.raven.log.Log;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.SetCurrentDataflowEvent;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.actions.CloseAllWorkflowsAction;
import net.sf.taverna.t2.workbench.helper.Helper;
import net.sf.taverna.t2.workbench.ui.impl.configuration.ui.T2ConfigurationFrame;

import org.apache.log4j.Logger;

/**
 * The main workbench frame.
 * 
 * @author David Withers
 * @author Stian Soiland-Reyes
 * 
 */
public class Workbench extends JFrame {

	private OSXAppListener osxAppListener = new OSXAppListener();

	private static final String LAUNCHER_LOGO_PNG = "/launcher_logo.png";

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(Workbench.class);

	private ApplicationRuntime appRuntime = ApplicationRuntime.getInstance();
	private ApplicationConfig appConfig = ApplicationConfig.getInstance();

	private static Workbench instance;

	private MenuManager menuManager = MenuManager.getInstance();

	private CloseAllWorkflowsAction closeAllWorkflowsAction = new CloseAllWorkflowsAction();

	private WorkbenchPerspectives perspectives;

	private JToolBar perspectiveToolBar;

	private Workbench() {
		// Initialisation done by getInstance()
	}

	private void makeGUI() {
		setLayout(new GridBagLayout());
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		
		Helper.setKeyCatcher(this);

		URL launcherLogo = getClass().getResource(LAUNCHER_LOGO_PNG);
		if (launcherLogo != null) {
			ImageIcon imageIcon = new ImageIcon(launcherLogo);
			setIconImage(imageIcon.getImage());
		}
		setTitle(appConfig.getTitle());
		
		OSXApplication.setListener(osxAppListener);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLookAndFeel();
		setSize(new Dimension(1000, 800));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.LINE_START;
		JPanel toolbarPanel = makeToolbarPanel();

		add(toolbarPanel, gbc);

		WorkbenchZBasePane basePane = makeBasePane();

		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 1;
		gbc.weightx = 0.1;
		gbc.weighty = 0.1;
		add(basePane, gbc);

		// Need to do this last as it references perspectives
		JMenuBar menuBar = menuManager.createMenuBar();
		setJMenuBar(menuBar);
	}

	protected WorkbenchZBasePane makeBasePane() {
		WorkbenchZBasePane basePane = new WorkbenchZBasePane();
		basePane.setRepository(appRuntime.getRavenRepository());
		perspectives = new WorkbenchPerspectives(basePane, perspectiveToolBar);
		return basePane;
	}

	protected JPanel makeToolbarPanel() {
		JPanel toolbarPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.LINE_START;

		JToolBar generatedToolbar = menuManager.createToolBar();
		generatedToolbar.setFloatable(false);
		toolbarPanel.add(generatedToolbar, gbc);

		perspectiveToolBar = new JToolBar("Perspectives");
		perspectiveToolBar.setFloatable(false);
		gbc.gridy = 1;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		toolbarPanel.add(perspectiveToolBar, gbc);

		return toolbarPanel;
	}

	public static final synchronized Workbench getInstance() {
		if (instance == null) {
			instance = new Workbench();
			instance.initialize();
		}
		return instance;
	}

	protected void initialize() {
		makeGUI();
		FileManager.getInstance().newDataflow();
		EditManager.getInstance().addObserver(DataflowEditsListener.getInstance());
		SplashScreen splash = SplashScreen.getSplashScreen();
		if (splash != null) {
			splash.setClosable();
			splash.requestClose();
		}
		
		// Register a listener with FileManager so whenever a current workflow is set 
		// we make sure we are in the design perspective
		FileManager.getInstance().addObserver(new SwitchToWorkflowPerspective());
	}

	public void exit() {
		// Save the perspectives to an XML file
		try {
			perspectives.saveAll();
		} catch (Exception ex) {
			logger.error("Error saving perspectives when exiting the Workbench.", ex);
		}
		
		if (closeAllWorkflowsAction.closeAllWorkflows(this)) {
			System.exit(0);
		}

	}

	private void setLookAndFeel() {
		// String landf = MyGridConfiguration
		// .getProperty("taverna.workbench.themeclass");
		boolean set = false;

		// if (landf != null) {
		// try {
		// UIManager.setLookAndFeel(landf);
		// logger.info("Using " + landf + " Look and Feel");
		// set = true;
		// } catch (Exception ex) {
		// logger.error(
		// "Error using theme defined by taverna.workbench.themeclass as "
		// + landf, ex);
		// }
		// }

		if (!set) {
			try {
				UIManager
						.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
				logger.info("Using Synthetica Look and Feel");
			} catch (Exception ex) {
				try {
					if (!(System.getProperty("os.name").equals("Linux"))) {
						UIManager.setLookAndFeel(UIManager
								.getSystemLookAndFeelClassName());
						logger.info("Using "
								+ UIManager.getSystemLookAndFeelClassName()
								+ " Look and Feel");
						set = true;
					} else {
						logger.info("Using default Look and Feel");
						set = true;
					}
				} catch (Exception ex2) {
					ex2.printStackTrace();
				}

			}
		}
	}

	public static void main(String[] args) throws IOException {
		System.setProperty("raven.eclipse", "true");
		Log.setImplementation(new ConsoleLog());
		Workbench workbench = getInstance();
		workbench.setVisible(true);
	}

	public WorkbenchPerspectives getPerspectives() {
		return perspectives;
	}

	private final class SwitchToWorkflowPerspective implements
			Observer<FileManagerEvent> {
		// If we currently are not in the design perspective  - switch to it now
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			if (message instanceof SetCurrentDataflowEvent){
				getPerspectives().setWorkflowPerspective();
			}
		}
	}

	protected class OSXAppListener extends OSXAdapter {
		@Override
		public boolean handleQuit() {
			exit();
			return false;
		}

		@Override
		public boolean hasPreferences() {
			return true;
		}

		@Override
		public boolean handlePreferences() {
			T2ConfigurationFrame.showFrame();
			return true;
		}

		@Override
		public boolean handleOpenFile(String filename) {
			try {
				FileManager.getInstance()
						.openDataflow(null, new File(filename));
				return true;
			} catch (OpenException e) {
				logger.warn("Could not open file " + filename, e);
			} catch (IllegalStateException e) {
				logger.warn("Could not open file " + filename, e);
			}
			return false;
		}
	}

}
