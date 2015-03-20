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

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.HttpURLConnection;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.ServerResponse;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Util;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;

import org.apache.log4j.Logger;

/**
 * @author Sergejs Aleksejevs, Jiten Bhagat
 */

public class AddCommentDialog extends HelpEnabledDialog implements ActionListener, CaretListener, ComponentListener, KeyListener {
  // components for accessing application's main elements
  private MainComponent pluginMainComponent;
  private MyExperimentClient myExperimentClient;
  private Logger logger;

  // COMPONENTS
  private DialogTextArea taComment;
  private JButton bPost;
  private JButton bCancel;
  private JLabel lStatusMessage;

  // STORAGE
  private Resource resource; // a resource for which the comment is being posted
  private String strComment = null;
  private boolean bPostingSuccessful = false;

  public AddCommentDialog(JFrame owner, Resource resource, MainComponent component, MyExperimentClient client, Logger logger) {
	super(owner, "Add comment for \"" + resource.getTitle() + "\" "
			+ resource.getItemTypeName(), true);

	// set main variables to ensure access to myExperiment, logger and the parent component
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;

	// set the resource for which the comment is being added
	this.resource = resource;

	// set options of the 'add comment' dialog box
	this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	//this.setIconImage(new ImageIcon(MyExperimentPerspective.getLocalResourceURL("myexp_icon")).getImage());

	this.initialiseUI();
  }

  private void initialiseUI() {
	// get content pane
	Container contentPane = this.getContentPane();

	// set up layout
	contentPane.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();

	// add all components
	JLabel lInfo = new JLabel("Please type in you comment:");
	c.gridx = 0;
	c.gridy = 0;
	c.anchor = GridBagConstraints.WEST;
	c.gridwidth = 2;
	c.fill = GridBagConstraints.NONE;
	c.insets = new Insets(10, 10, 5, 10);
	contentPane.add(lInfo, c);

	this.taComment = new DialogTextArea(5, 35);
	this.taComment.setLineWrap(true);
	this.taComment.setWrapStyleWord(true);
	this.taComment.addKeyListener(this);
	this.taComment.addCaretListener(this);

	JScrollPane spComment = new JScrollPane(this.taComment);
	c.gridy = 1;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.insets = new Insets(0, 10, 0, 10);
	contentPane.add(spComment, c);

	this.bPost = new JButton("Post Comment");
	this.bPost.setEnabled(false);
	this.bPost.setDefaultCapable(true);
	this.getRootPane().setDefaultButton(this.bPost);
	this.bPost.addActionListener(this);
	this.bPost.addKeyListener(this);
	c.gridy = 2;
	c.anchor = GridBagConstraints.EAST;
	c.gridwidth = 1;
	c.fill = GridBagConstraints.NONE;
	c.weightx = 0.5;
	c.insets = new Insets(10, 5, 10, 5);
	contentPane.add(bPost, c);

	this.bCancel = new JButton("Cancel");
	this.bCancel.setPreferredSize(this.bPost.getPreferredSize());
	this.bCancel.addActionListener(this);
	c.gridx = 1;
	c.anchor = GridBagConstraints.WEST;
	c.weightx = 0.5;
	contentPane.add(bCancel, c);

	this.pack();
	this.setMinimumSize(this.getPreferredSize());
	this.setMaximumSize(this.getPreferredSize());
	this.addComponentListener(this);
  }

  /**
   * Opens up a modal dialog where the user can enter the comment text.
   * 
   * Window is simply closed if 'Cancel' button is pressed; on pressing 'Post
   * Comment' button the window will turn into 'waiting' state, post the comment
   * and return the resulting XML document (which would contain the newly added
   * comment) back to the caller.
   * 
   * @return String value of the non-empty comment text to be sent to
   *         myExperiment or null if action was cancelled.
   */
  public String launchAddCommentDialogAndPostCommentIfRequired() {
	// makes the 'add comment' dialog visible, then waits until it is closed;
	// control returns to this method when the dialog window is disposed
	this.setVisible(true);
	return (strComment);
  }

  // *** Callback for ActionListener interface ***
  public void actionPerformed(ActionEvent e) {
	if (e.getSource().equals(this.bPost)) {
	  // 'Post' button is not active when text in the comment field is blank,
	  // so if it was pressed, there should be some text typed in
	  this.strComment = this.taComment.getText();

	  // the window will stay visible, but should turn into 'waiting' state
	  final JRootPane rootPane = this.getRootPane();
	  final Container contentPane = this.getContentPane();
	  contentPane.remove(this.bPost);
	  contentPane.remove(this.bCancel);
	  if (this.lStatusMessage != null)
		contentPane.remove(this.lStatusMessage);
	  this.taComment.setEditable(false);

	  final GridBagConstraints c = new GridBagConstraints();
	  c.gridx = 0;
	  c.gridy = 2;
	  c.gridwidth = 2;
	  c.anchor = GridBagConstraints.CENTER;
	  c.fill = GridBagConstraints.NONE;
	  c.insets = new Insets(10, 5, 10, 5);
	  lStatusMessage = new JLabel("Posting your comment...", new ImageIcon(MyExperimentPerspective.getLocalResourceURL("spinner")), SwingConstants.CENTER);
	  contentPane.add(lStatusMessage, c);

	  // disable the (X) button (ideally, would need to remove it, but there's no way to do this)
	  this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

	  // revalidate the window
	  this.pack();
	  this.validate();
	  this.repaint();

	  new Thread("Posting comment") {
		public void run() {
		  // *** POST THE COMMENT ***
		  final ServerResponse response = myExperimentClient.postComment(resource, Util.stripAllHTML(strComment));
		  bPostingSuccessful = (response.getResponseCode() == HttpURLConnection.HTTP_OK);

		  SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			  // *** REACT TO POSTING RESULT ***
			  if (bPostingSuccessful) {
				// comment posted successfully
				setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				taComment.setEnabled(false);
				contentPane.remove(lStatusMessage);

				c.insets = new Insets(10, 5, 5, 5);
				lStatusMessage = new JLabel("Your comment was posted successfully", new ImageIcon(MyExperimentPerspective.getLocalResourceURL("success_icon")), SwingConstants.LEFT);
				contentPane.add(lStatusMessage, c);

				bCancel.setText("OK");
				bCancel.setDefaultCapable(true);
				rootPane.setDefaultButton(bCancel);
				c.insets = new Insets(5, 5, 10, 5);
				c.gridy += 1;
				contentPane.add(bCancel, c);

				pack();
				bCancel.requestFocusInWindow();
			  } else {
				// posting wasn't successful, notify the user
				// and provide an option to close window or start again
				setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				taComment.setEditable(true);
				contentPane.remove(lStatusMessage);

				c.insets = new Insets(10, 5, 5, 5);
				lStatusMessage = new JLabel("Error occurred while posting comment: "
					+ Util.retrieveReasonFromErrorXMLDocument(response.getResponseBody()), new ImageIcon(MyExperimentPerspective.getLocalResourceURL("failure_icon")), SwingConstants.LEFT);
				contentPane.add(lStatusMessage, c);

				bPost.setText("Try again");
				bPost.setToolTipText("Please review your comment before trying to post it again");
				c.anchor = GridBagConstraints.EAST;
				c.insets = new Insets(5, 5, 10, 5);
				c.gridwidth = 1;
				c.weightx = 0.5;
				c.gridx = 0;
				c.gridy += 1;
				contentPane.add(bPost, c);
				rootPane.setDefaultButton(bPost);

				c.anchor = GridBagConstraints.WEST;
				c.gridx = 1;
				bCancel.setPreferredSize(bPost.getPreferredSize());
				contentPane.add(bCancel, c);

				pack();
				validate();
				repaint();
			  }
			}
		  });

		}
	  }.start();
	} else if (e.getSource().equals(this.bCancel)) {
	  // cleanup the comment if it wasn't posted successfully + simply close and destroy the window
	  if (!this.bPostingSuccessful)
		this.strComment = null;
	  this.dispose();
	}

  }

  // *** Callbacks for KeyListener interface ***
  public void keyPressed(KeyEvent e) {
	// if TAB was pressed in the text area, need to move keyboard focus
	if (e.getSource().equals(this.taComment)
		&& e.getKeyCode() == KeyEvent.VK_TAB) {
	  if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK) {
		// SHIFT + TAB move focus backwards
		((Component) e.getSource()).transferFocusBackward();
	  } else {
		// TAB moves focus forward
		((Component) e.getSource()).transferFocus();
	  }
	  e.consume();
	}
  }

  public void keyReleased(KeyEvent e) {
	// not in use
  }

  public void keyTyped(KeyEvent e) {
	// not in use
  }

  // *** Callback for CaretListener interface ***
  public void caretUpdate(CaretEvent e) {
	// check whether the 'Post' button should be available or not
	this.bPost.setEnabled(this.taComment.getText().length() > 0);
  }

  // *** Callbacks for ComponentListener interface ***
  public void componentShown(ComponentEvent e) {
	// center this dialog box within the preview browser window
	Util.centerComponentWithinAnother(this.pluginMainComponent.getPreviewBrowser(), this);
  }

  public void componentHidden(ComponentEvent e) {
	// not in use
  }

  public void componentMoved(ComponentEvent e) {
	// not in use
  }

  public void componentResized(ComponentEvent e) {
	// not in use
  }

}
