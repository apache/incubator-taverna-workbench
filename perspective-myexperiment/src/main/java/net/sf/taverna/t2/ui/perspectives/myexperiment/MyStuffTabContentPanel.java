/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
 * 
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.ui.perspectives.myexperiment;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.CountDownLatch;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;

import org.apache.log4j.Logger;

/**
 * @author Sergejs Aleksejevs, Emmanuel Tagarira, Jiten Bhagat
 */
public class MyStuffTabContentPanel extends JPanel implements ActionListener, KeyListener {
  private final MainComponent pluginMainComponent;
  private final MyExperimentClient myExperimentClient;
  private final Logger logger;

  // components that should be accessible from anywhere in this class
  private JButton bLogin;
  private JCheckBox cbLoginAutomatically;
  private MyStuffSidebarPanel jpSidebar;
  public JSplitPane spMyStuff;

  // synchronisation latch to let main thread know that all component-creation
  // threads have been finished
  protected CountDownLatch cdlComponentLoadingDone;
  int NUMBER_OF_SUBCOMPONENTS = 2;

  // "return to" type of thing, so that after a certain action has been done,
  // it is possible to switch to another tab in this tabbed view
  protected JComponent cTabContentComponentToSwitchToAfterLogin = null;

  public MyStuffTabContentPanel(MainComponent component, MyExperimentClient client, Logger logger) {
	super();

	// set main variables to ensure access to myExperiment, logger and the
	// parent component
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;
  }

  public void createAndInitialiseInnerComponents() {
	// if there are any components, these will be removed
	this.removeAll();
	cdlComponentLoadingDone = new CountDownLatch(NUMBER_OF_SUBCOMPONENTS);

	// based on the current status (logged in / anonymous user), decide which
	// components to create and display
	if (this.myExperimentClient.isLoggedIn()) {
	  jpSidebar = new MyStuffSidebarPanel(pluginMainComponent, myExperimentClient, logger);
	  JPanel jpSidebarContainer = new JPanel();
	  jpSidebarContainer.setLayout(new BorderLayout());
	  jpSidebarContainer.add(jpSidebar, BorderLayout.NORTH);
	  JScrollPane spSidebar = new JScrollPane(jpSidebarContainer);
	  spSidebar.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);
	  spSidebar.setMinimumSize(new Dimension(jpSidebar.getMyProfileBox().getPreferredSize().width + 30, 0)); // +30
	  // --> 10 for padding and 10 for vertical scroll bar + 10 extra current
	  // user is logged in to myExperiment, display all personal data

	  spMyStuff = new JSplitPane();
	  spMyStuff.setLeftComponent(spSidebar);
	  spMyStuff.setRightComponent(new MyStuffContributionsPanel(pluginMainComponent, myExperimentClient, logger));
	  this.pluginMainComponent.getStatusBar().setCurrentUser(myExperimentClient.getCurrentUser().getName());

	  // set proportional sizes of the two panes as 30/70 percents of the total
	  // width of the SplitPane
	  // this can only be done after the SplitPane is made visible - hence the
	  // need for the listener below
	  pluginMainComponent.addComponentListener(new ComponentAdapter() {
		@Override
		public void componentShown(ComponentEvent e) {
		  javax.swing.JOptionPane.showMessageDialog(null, "component shown");
		  // NB! This is only needed for use with test class, not when Taverna
		  // calls perspective!!
		  // the SplitPane wouldn't have loaded yet - wait until it does
		  try {
			Thread.sleep(50);
		  } // 50ms is a tiny delay -- acceptable
		  catch (Exception ex) { /* do nothing */}

		  // set the proportions in the SplitPane
		  spMyStuff.setDividerLocation(400);
		}
	  });

	  // make sure that both panes will grow/shrink at the same rate if the
	  // size of the whole SplitPane is changed by resizing the window
	  spMyStuff.setResizeWeight(0.3);
	  spMyStuff.setOneTouchExpandable(true);
	  spMyStuff.setDividerLocation(400);
	  spMyStuff.setDoubleBuffered(true);

	  // spMyStuff will be the only component in the Panel
	  this.setLayout(new BorderLayout());
	  this.add(spMyStuff);

	  // wait until two of the components finish loading and set the status to 'ready'
	  // (done in a new thread so that this doesn't freeze the plugin window)
	  new Thread("Waiting for myStuff data to load") {
		@Override
		public void run() {
		  try {
			cdlComponentLoadingDone.await();
			pluginMainComponent.getStatusBar().setStatus(this.getClass().getName(), null);
		  } catch (InterruptedException ex) { /* do nothing for now */
		  }
		}
	  }.start();
	} else { // NOT logged in
	  // reset status in case of unsuccessful autologin
	  this.pluginMainComponent.getStatusBar().setStatus(this.getClass().getName(), null);

	  // user isn't logged in, display login box only
	  JPanel jpLoginBoxContainer = new JPanel();
	  jpLoginBoxContainer.setLayout(new GridBagLayout());
	  GridBagConstraints c = new GridBagConstraints();
	  jpLoginBoxContainer.add(createLoginBox(), c);

	  // put everything together (welcome banner + login box)
	  this.setLayout(new BorderLayout());
	  this.add(new ShadedLabel("Welcome to the myExperiment plugin. Please note that you can still use other tabs even "
		  + "if you don't have a user profile yet!", ShadedLabel.BLUE), BorderLayout.NORTH);
	  this.add(jpLoginBoxContainer, BorderLayout.CENTER);
	}
  }

  // generates JPanel containing a login box
  private JPanel createLoginBox() {
	JPanel jpLoginBox = new JPanel();
	jpLoginBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

	jpLoginBox.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();

	// label "Login to myExp"
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.insets = new Insets(0, 0, 15, 0);
	c.gridx = 0;
	c.gridy = 0;
	JLabel jlHeader = new JLabel("<html><b>Log in to myExperiment</b></html>");
	jlHeader.setFont(jlHeader.getFont().deriveFont((float) 13.0));
	jpLoginBox.add(jlHeader, c);

	// set values
	c.weightx = 1;
	c.gridwidth = 1;
	c.anchor = GridBagConstraints.LINE_START;
	c.insets = new Insets(0, 0, 3, 0);
	c.ipadx = 10;

	// autologin checkbox and label
	c.gridy++;
	c.insets = new Insets(0, 0, 0, 3);
	cbLoginAutomatically = new JCheckBox("Log in automatically (next time)");
	cbLoginAutomatically.setBorder(BorderFactory.createEmptyBorder()); // makes sure that this is aligned with text fields above
	cbLoginAutomatically.addActionListener(this);
	cbLoginAutomatically.addKeyListener(this);
	jpLoginBox.add(cbLoginAutomatically, c);

	// login button
	c.gridy++;
	c.gridx = 0;
	c.anchor = GridBagConstraints.CENTER;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.insets = new Insets(10, 0, 0, 0);
	bLogin = new JButton("Login", new ImageIcon(MyExperimentPerspective.getLocalResourceURL("login_icon")));
	bLogin.setDefaultCapable(true);
	bLogin.addKeyListener(this);
	bLogin.addActionListener(this);
	jpLoginBox.add(bLogin, c);

	// wrap contents into another panel to allow for some extra border around the contents
	return (jpLoginBox);
  }

  public MyStuffSidebarPanel getSidebar() {
	return (this.jpSidebar);
  }

  public void actionPerformed(ActionEvent e) {
	if (e.getSource().equals(this.bLogin)) {
	  // "Login" button clicked
	  pluginMainComponent.getStatusBar().setStatus(this.getClass().getName(), "Logging in");

	  // Make call to myExperiment API in a different thread
	  // (then use SwingUtilities.invokeLater to update the UI when ready).
	  new Thread("Login to myExperiment") {
		@Override
		public void run() {
		  logger.debug("Logging in to myExperiment");

		  try {
			// do the actual "logging in"
			boolean bLoginSuccessful = myExperimentClient.doLogin();

			// check if need to store the login credentials and settings
			if (bLoginSuccessful) {
			  // store the settings anyway (for instance, to clear stored login/password 
			  // - when the 'remember me' tick is not checked anymore);
			  // however, need to check whether to store login details or not

			  myExperimentClient.getSettings().put(MyExperimentClient.INI_AUTO_LOGIN, new Boolean(cbLoginAutomatically.isSelected()).toString());
			  myExperimentClient.storeHistoryAndSettings();

			  // if logging in was successful, set the status to the start of fetching the data
			  pluginMainComponent.getStatusBar().setStatus(this.getClass().getName(), "Fetching user data");

			SwingUtilities.invokeLater(new Runnable() {
			  public void run() {
				if (myExperimentClient.isLoggedIn()) {
				  // login successful, change view to "logged in" one
				  createAndInitialiseInnerComponents();

				  // ..also, load user's tag cloud
				  pluginMainComponent.getTagBrowserTab().setMyTagsShown(true);
				  pluginMainComponent.getTagBrowserTab().getMyTagPanel().refresh();

				  // ..also, refresh tag search results because these my include
				  // much more than
				  // during the previous search when the user was still not
				  // logged-in
				  pluginMainComponent.getTagBrowserTab().rerunLastTagSearch();

				  // ..also, refresh the keyword search results (as more items
				  // can now be found)
				  pluginMainComponent.getSearchTab().rerunLastSearch();

				  // if after logging it is needed to switch to other tab,
				  // that is done now
				  if (cTabContentComponentToSwitchToAfterLogin != null) {
					pluginMainComponent.getMainTabs().setSelectedComponent(cTabContentComponentToSwitchToAfterLogin);
					cTabContentComponentToSwitchToAfterLogin = null;
				  }

				  logger.debug("Logged in to myExperiment successfully");
				} else {
				  // couldn't login - display error message
				  pluginMainComponent.getStatusBar().setStatus(this.getClass().getName(), null);
				  javax.swing.JOptionPane.showMessageDialog(null, "Unable to login to myExperiment - please check your login details", "myExperiment Plugin - Couldn't Login", JOptionPane.ERROR_MESSAGE);
				}
			  }
			});
			}

		  } catch (Exception ex) {
			logger.error("Exception on attempt to login to myExperiment:\n", ex);
		  }
		}
	  }.start();

	} else if (e.getSource().equals(this.jpSidebar.bRefreshMyStuff)) {
	  // this will re-fetch all user profile data and repopulate the whole of the 'My Stuff' tab
	  pluginMainComponent.getStatusBar().setStatus(this.getClass().getName(), "Refreshing user data");

	  new Thread("Refreshing myStuff tab data") {
		@Override
		public void run() {
		  // re-fetch user data first
		  myExperimentClient.setCurrentUser(myExperimentClient.fetchCurrentUser(myExperimentClient.getCurrentUser().getURI()));
		  createAndInitialiseInnerComponents();
		  revalidate();
		}
	  }.start();
	}
  }

  // *** Callbacks for KeyListener interface ***
  public void keyPressed(KeyEvent e) {
	// ENTER pressed - check which element is the source and determine what
	// acion is to be taken
	if (e.getKeyCode() == KeyEvent.VK_ENTER) {
	  if (e.getSource().equals(this.cbLoginAutomatically)
		  || e.getSource().equals(this.bLogin)) {
		// ENTER pressed when focus was on the login button, one of checkboxes or the password field - do logging in
		actionPerformed(new ActionEvent(this.bLogin, 0, ""));
	  }
	}
  }

  public void keyReleased(KeyEvent e) {
	// do nothing
  }

  public void keyTyped(KeyEvent e) {
	// do nothing
  }

}
