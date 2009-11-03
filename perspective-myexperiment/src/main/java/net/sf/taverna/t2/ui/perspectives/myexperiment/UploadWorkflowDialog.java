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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.HttpURLConnection;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.sf.taverna.t2.ui.perspectives.myexperiment.model.License;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.ServerResponse;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Util;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Workflow;

import org.apache.log4j.Logger;

/**
 * @author Emmanuel Tagarira, Sergejs Aleksejevs
 */
public class UploadWorkflowDialog extends JDialog implements ActionListener, CaretListener, ComponentListener, KeyListener {
  // components for accessing application's main elements
  private MainComponent pluginMainComponent;
  private MyExperimentClient myExperimentClient;
  private Logger logger;

  // COMPONENTS
  private JTextArea taDescription;
  private JTextField tfTitle;
  private JButton bUpload;
  private JButton bCancel;
  private JLabel lStatusMessage;
  JComboBox jcbLicences;
  JComboBox jcbSharingPermissions;
  private String licence;
  private String sharing;

  // STORAGE
  private File workflowFile; // the workflow file to be uploaded
  private Resource updateResource; // the workflow resource that is to be
  // updated

  private String strDescription = null;
  private String strTitle = null;
  private boolean bUploadingSuccessful = false;

  private int gridYPositionForStatusLabel;

  private void constructorInit(Resource resource, File file, JFrame owner, MainComponent component, MyExperimentClient client, Logger logger) {
	// set main variables to ensure access to myExperiment, logger and the
	// parent component
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;

	// set the resource for which the comment is being added
	this.workflowFile = file;
	this.updateResource = resource;

	// set options of the 'add comment' dialog box
	this.setModal(true);
	this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	this.setTitle("Upload workflow to myExperiment");

	this.initialiseUI();
  }

  public UploadWorkflowDialog(File file, JFrame owner, MainComponent component, MyExperimentClient client, Logger logger) {
	super(owner);
	constructorInit(null, file, owner, component, client, logger);
  }

  public UploadWorkflowDialog(Resource resource, File file, JFrame owner, MainComponent component, MyExperimentClient client, Logger logger) {
	super(owner);
	constructorInit(resource, file, owner, component, client, logger);
  }

  private void initialiseUI() {
	// get content pane
	Container contentPane = this.getContentPane();

	// set up layout
	contentPane.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();

	// add all components
	// title
	JLabel lTitle = new JLabel("Workflow title:");
	c.gridx = 0;
	c.gridy = 0;
	c.anchor = GridBagConstraints.WEST;
	c.gridwidth = 2;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.insets = new Insets(10, 10, 5, 10);
	contentPane.add(lTitle, c);

	this.tfTitle = new JTextField();
	if (this.updateResource != null)
	  this.tfTitle.setText(this.updateResource.getTitle());
	c.gridy++;
	c.insets = new Insets(0, 10, 0, 10);
	contentPane.add(this.tfTitle, c);

	// description
	JLabel lDescription = new JLabel("Workflow description:");
	c.gridy++;
	c.insets = new Insets(10, 10, 5, 10);
	contentPane.add(lDescription, c);

	this.taDescription = new JTextArea(5, 35);
	this.taDescription.setLineWrap(true);
	this.taDescription.setWrapStyleWord(true);
	if (this.updateResource != null)
	  this.taDescription.setText(this.updateResource.getDescription());

	JScrollPane spDescription = new JScrollPane(this.taDescription);
	c.gridy++;
	c.insets = new Insets(0, 10, 0, 10);
	contentPane.add(spDescription, c);

	// licences
	String[] licenseText = new String[License.SUPPORTED_TYPES.length];
	for (int x = 0; x < License.SUPPORTED_TYPES.length; x++)
	  licenseText[x] = License.getInstance(License.SUPPORTED_TYPES[x]).getText();

	jcbLicences = new JComboBox(licenseText);

	if (this.updateResource != null) { // adding a new workflow 
	  Workflow wf = (Workflow) this.updateResource;
	  String wfText = wf.getLicense().getText();
	  for (int x = 0; x < licenseText.length; x++)
		if (wfText.equals(licenseText[x])) {
		  jcbLicences.setSelectedIndex(x);
		  break;
		}
	}

	jcbLicences.addActionListener(this);
	jcbLicences.setEditable(false);

	JLabel lLicense = new JLabel("Please select the licence to apply:");
	c.gridy++;
	c.insets = new Insets(10, 10, 5, 10);
	contentPane.add(lLicense, c);

	c.gridy++;
	c.insets = new Insets(0, 10, 0, 10);
	contentPane.add(jcbLicences, c);

	// sharing - options: private / view / download
	String[] permissions = { "This workflow is private.", "Anyone can view, but noone can download.", "Anyone can view, and anyone can download" };

	this.jcbSharingPermissions = new JComboBox(permissions);

	jcbSharingPermissions.addActionListener(this);
	jcbSharingPermissions.setEditable(false);

	JLabel jSharing = new JLabel("Please select your sharing permissions:");
	c.gridy++;
	c.insets = new Insets(10, 10, 5, 10);
	contentPane.add(jSharing, c);

	c.gridy++;
	c.insets = new Insets(0, 10, 0, 10);
	contentPane.add(jcbSharingPermissions, c);

	// buttons
	this.bUpload = new JButton(updateResource == null ? "Upload Workflow" : "Update Workflow");
	this.bUpload.setDefaultCapable(true);
	this.getRootPane().setDefaultButton(this.bUpload);
	this.bUpload.addActionListener(this);
	this.bUpload.addKeyListener(this);
	c.gridy++;
	c.anchor = GridBagConstraints.EAST;
	c.gridwidth = 1;
	c.fill = GridBagConstraints.NONE;
	c.weightx = 0.5;
	c.insets = new Insets(10, 5, 10, 5);
	contentPane.add(bUpload, c);

	this.bCancel = new JButton("Cancel");
	this.bCancel.setPreferredSize(this.bUpload.getPreferredSize());
	this.bCancel.addActionListener(this);
	c.gridx = 1;
	c.anchor = GridBagConstraints.WEST;
	c.weightx = 0.5;
	contentPane.add(bCancel, c);

	this.pack();
	this.setMinimumSize(this.getPreferredSize());
	this.setMaximumSize(this.getPreferredSize());
	this.addComponentListener(this);

	gridYPositionForStatusLabel = c.gridy;
  }

  /**
   * Opens up a modal dialog where the user can enter the comment text. Window
   * is simply closed if 'Cancel' button is pressed; on pressing 'Post Comment'
   * button the window will turn into 'waiting' state, post the comment and
   * return the resulting XML document (which would contain the newly added
   * comment) back to the caller.
   * 
   * @return String value of the non-empty comment text to be sent to
   *         myExperiment or null if action was cancelled.
   */
  public boolean launchUploadDialogAndPostIfRequired() {
	// makes the 'add comment' dialog visible, then waits until it is closed;
	// control returns to this method when the dialog window is disposed
	this.setVisible(true);
	return (bUploadingSuccessful);
  }

  // *** Callback for ActionListener interface ***
  public void actionPerformed(ActionEvent e) {
	if (e.getSource().equals(this.bUpload)) {
	  // get sharing permission
	  switch (this.jcbSharingPermissions.getSelectedIndex()) {
		case 0:
		  this.sharing = "private";
		  break;
		case 1:
		  this.sharing = "view";
		  break;
		case 2:
		  this.sharing = "download";
		  break;
	  }

	  //get licence
	  this.licence = License.SUPPORTED_TYPES[this.jcbLicences.getSelectedIndex()];

	  // get title
	  this.strTitle = this.tfTitle.getText();
	  this.strTitle = this.strTitle.trim();

	  // get description
	  this.strDescription = this.taDescription.getText();
	  this.strDescription = this.strDescription.trim();

	  // if the description or the title are empty, prompt the user to confirm
	  // the upload
	  boolean proceedWithUpload = false;
	  if ((this.strDescription.length() == 0) && (this.strTitle.length() == 0)) {
		String strInfo = "The workflow 'title' field and the 'description' field\n"
			+ "(or both) are empty.  Any metadata found within the\n"
			+ "workflow will be used instead.  Do you wish to proceed?";
		int confirm = JOptionPane.showConfirmDialog(this, strInfo, "Empty fields", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (confirm == JOptionPane.YES_OPTION)
		  proceedWithUpload = true;
	  } else
		proceedWithUpload = true;

	  if (proceedWithUpload) {
		// the window will stay visible, but should turn into 'waiting' state
		final JRootPane rootPane = this.getRootPane();
		final Container contentPane = this.getContentPane();
		contentPane.remove(this.bUpload);
		contentPane.remove(this.bCancel);
		if (this.lStatusMessage != null)
		  contentPane.remove(this.lStatusMessage);
		this.taDescription.setEditable(false);

		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = gridYPositionForStatusLabel;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(10, 5, 10, 5);
		lStatusMessage = new JLabel((updateResource == null ? "Uploading" : "Updating")
			+ " your workflow...", new ImageIcon(MyExperimentPerspective.getLocalResourceURL("spinner")), SwingConstants.CENTER);
		contentPane.add(lStatusMessage, c);

		// disable the (X) button (ideally, would need to remove it, but there's
		// no way to do this)
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		// revalidate the window
		this.pack();
		this.validate();
		this.repaint();

		new Thread("Posting workflow") {
		  public void run() {
			String workflowFileContent = "";
			if (workflowFile != null) {
			  try {
				BufferedReader reader = new BufferedReader(new FileReader(workflowFile));
				String line;

				while ((line = reader.readLine()) != null)
				  workflowFileContent += line;
			  } catch (Exception e) {
				lStatusMessage = new JLabel("Error occurred:" + e.getMessage(), new ImageIcon(MyExperimentPerspective.getLocalResourceURL("failure_icon")), SwingConstants.LEFT);
			  }
			}

			// *** POST THE WORKFLOW ***
			final ServerResponse response;
			if (updateResource == null) // upload a new workflow
			  response = myExperimentClient.postWorkflow(workflowFileContent, Util.stripAllHTML(strTitle), Util.stripAllHTML(strDescription), licence, sharing);
			else
			  // edit existing workflow
			  response = myExperimentClient.postNewVersionOfWorkflow(updateResource, workflowFileContent, Util.stripAllHTML(strTitle), Util.stripAllHTML(strDescription), licence, sharing);

			bUploadingSuccessful = (response.getResponseCode() == HttpURLConnection.HTTP_OK);

			SwingUtilities.invokeLater(new Runnable() {
			  public void run() {
				// *** REACT TO POSTING RESULT ***
				if (bUploadingSuccessful) {
				  // workflow uploaded successfully
				  setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				  tfTitle.setEnabled(false);
				  taDescription.setEnabled(false);
				  jcbLicences.setEnabled(false);
				  jcbSharingPermissions.setEnabled(false);
				  contentPane.remove(lStatusMessage);

				  c.insets = new Insets(10, 5, 5, 5);
				  lStatusMessage = new JLabel("Your workflow was successfully "
					  + (updateResource == null ? "uploaded." : "updated."), new ImageIcon(MyExperimentPerspective.getLocalResourceURL("success_icon")), SwingConstants.LEFT);
				  contentPane.add(lStatusMessage, c);

				  bCancel.setText("OK");
				  bCancel.setDefaultCapable(true);
				  rootPane.setDefaultButton(bCancel);
				  c.insets = new Insets(5, 5, 10, 5);
				  c.gridy++;
				  contentPane.add(bCancel, c);

				  pack();
				  bCancel.requestFocusInWindow();
				} else {
				  // posting wasn't successful, notify the user
				  // and provide an option to close window or start again
				  setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				  taDescription.setEditable(true);
				  contentPane.remove(lStatusMessage);

				  c.insets = new Insets(10, 5, 5, 5);
				  lStatusMessage = new JLabel("Error occurred while processing your request: "
					  + Util.retrieveReasonFromErrorXMLDocument(response.getResponseBody()), new ImageIcon(MyExperimentPerspective.getLocalResourceURL("failure_icon")), SwingConstants.LEFT);
				  contentPane.add(lStatusMessage, c);

				  bUpload.setText("Try again");
				  bUpload.setToolTipText("Please review your workflow of myExperiment base URL before trying to post it again");
				  c.anchor = GridBagConstraints.EAST;
				  c.insets = new Insets(5, 5, 10, 5);
				  c.gridwidth = 1;
				  c.weightx = 0.5;
				  c.gridx = 0;
				  c.gridy++;
				  contentPane.add(bUpload, c);
				  rootPane.setDefaultButton(bUpload);

				  c.anchor = GridBagConstraints.WEST;
				  c.gridx = 1;
				  bCancel.setPreferredSize(bUpload.getPreferredSize());
				  contentPane.add(bCancel, c);

				  pack();
				  validate();
				  repaint();
				}
			  }
			});
		  }
		}.start();
	  } // if proceedWithUpload
	} else if (e.getSource().equals(this.bCancel)) {
	  // cleanup the input fields if it wasn't posted successfully + simply close and destroy the window
	  if (!this.bUploadingSuccessful) {
		this.strDescription = null;
		this.tfTitle = null;
	  }
	  this.dispose();
	}
  }

  // *** Callbacks for KeyListener interface ***
  public void keyPressed(KeyEvent e) {
	// if TAB was pressed in the text field (title), need to move keyboard focus
	if (e.getSource().equals(this.tfTitle)
		|| e.getSource().equals(this.taDescription)) {
	  if (e.getKeyCode() == KeyEvent.VK_TAB) {
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
  }

  public void keyReleased(KeyEvent e) {
	// not in use
  }

  public void keyTyped(KeyEvent e) {
	// not in use
  }

  // *** Callback for CaretListener interface ***
  public void caretUpdate(CaretEvent e) {
	// not in use
  }

  // *** Callbacks for ComponentListener interface ***
  public void componentShown(ComponentEvent e) {
	// center this dialog box within the preview browser window
	if (updateResource == null) // upload has been pressed from the MAIN
	  // perspective window
	  Util.centerComponentWithinAnother(this.pluginMainComponent, this);
	else
	  // upload pressed from resource preview window
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
