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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.sf.taverna.t2.ui.perspectives.myexperiment.model.License;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.ServerResponse;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Util;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Workflow;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.impl.actions.SaveWorkflowAsAction;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * @author Emmanuel Tagarira, Sergejs Aleksejevs
 */
public class UploadWorkflowDialog extends HelpEnabledDialog implements ActionListener,
    CaretListener, ComponentListener, KeyListener, FocusListener {
  // components for accessing application's main elements
  private final MainComponent pluginMainComponent = MainComponent.MAIN_COMPONENT;
  private final MyExperimentClient myExperimentClient = MainComponent.MY_EXPERIMENT_CLIENT;
  private final Logger logger = MainComponent.LOGGER;

  // COMPONENTS
  private JTextArea taDescription;
  private JTextField tfTitle;
  private JButton bUpload;
  private JButton bCancel;
  private JLabel lStatusMessage;
  private JComboBox jcbLicences;
  private JComboBox jcbSharingPermissions;
  private String licence;
  private String sharing;

  // STORAGE
  private File localWorkflowFile; // the local workflow file to be uploaded
  private Resource updateResource; // the workflow resource that is to be
  // updated
  private File uploadFile; // THE UPLOAD FILE

  private String strDescription = null;
  private String strTitle = null;
  private boolean bUploadingSuccessful = false;

  // misc.
  private int gridYPositionForStatusLabel;
  private JRadioButton rbSelectLocalFile;
  private JRadioButton rbSelectOpenWorkflow;
  private JButton bSelectFile;
  private JComboBox jcbOpenWorkflows;
  private final JLabel selectedFileLabel = new JLabel(
      "no wokflow file selected");
  private boolean uploadWorkflowFromLocalFile;
  JFileChooser jfsSelectFile = new JFileChooser();

  private boolean userRequestedWorkflowUpload;

  public UploadWorkflowDialog(JFrame parent, boolean doUpload) {
    super(parent, "Upload workflow to myExperiment", true);
    initVarsAndUI(doUpload, null);
  }

  public UploadWorkflowDialog(JFrame parent, boolean doUpload, Resource resource) {
    super(parent, (doUpload ? "Upload new workflow version"
        : "Update workflow information"), true);
    initVarsAndUI(doUpload, resource);
  }

  private void initVarsAndUI(boolean doUpload, Resource resource) {
    // set the resource for which the comment is being added
    this.updateResource = resource;
    this.userRequestedWorkflowUpload = doUpload;

    // set options of the 'add comment' dialog box
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    initialiseUI();
    this.setMinimumSize(new Dimension(525, 375));
  }

  private JPanel createSelectSource() {
    // create radio buttons
    ButtonGroup radioButtons = new ButtonGroup();
    rbSelectOpenWorkflow = new JRadioButton("Already Opened Workflow");
    rbSelectOpenWorkflow.addFocusListener(this);
    rbSelectLocalFile = new JRadioButton("Select Local File");
    rbSelectLocalFile.addFocusListener(this);
    radioButtons.add(rbSelectOpenWorkflow);
    rbSelectOpenWorkflow.setSelected(true);
    radioButtons.add(rbSelectLocalFile);

    // create the source panel and add items
    JPanel source = new JPanel(new GridBagLayout());
    source.setBorder(BorderFactory.createTitledBorder("Workflow source"));

    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.gridy = 0;
    c.gridx = 0;
    c.gridwidth = 1;
    c.weightx = 1;
    c.insets = new Insets(3, 0, 3, 0);
    c.fill = GridBagConstraints.BOTH;

    // add info label
    // JLabel info = new JLabel("Upload a workflow you would like to upload:");
    // source.add(info, c);

    // add open workflow radio button
    c.gridy++;
    source.add(rbSelectOpenWorkflow, c);
    c.gridx = 1;
    c.gridwidth = 2;
    createDropdown();
    source.add(jcbOpenWorkflows, c);

    // add local file radio button
    c.gridwidth = 1;
    c.gridy++;
    c.gridx = 0;
    source.add(rbSelectLocalFile, c);
    c.gridx = 1;
    source.add(selectedFileLabel, c);
    bSelectFile = new JButton(WorkbenchIcons.openIcon);
    bSelectFile.addActionListener(this);
    bSelectFile
        .setToolTipText("Select the file you would like to upload to myExperiment");
    c.gridx = 2;
    source.add(bSelectFile, c);

    return source;
  }

  private void createDropdown() {
    FileManager fileManager = FileManager.getInstance();
    List<DataflowSelection> openDataflows = new ArrayList<DataflowSelection>();

    int currentlyOpenedIndex = 0;
    boolean foundIndex = false;

    for (Dataflow df : fileManager.getOpenDataflows()) {
      Object source = fileManager.getDataflowSource(df);

      String name = "";
      boolean getLocalName = source instanceof InputStream;
      if (source != null)
        name = (getLocalName ? df.getLocalName() : source.toString());

      if (df.equals(fileManager.getCurrentDataflow())) {
        name = "<html><body>" + name + " - "
            + " <i>(current)</i></body></html>";
        foundIndex = true;
      }

      openDataflows.add(new DataflowSelection(df, name));
      if (!foundIndex) currentlyOpenedIndex++;
    }

    jcbOpenWorkflows = new JComboBox(openDataflows.toArray());
    jcbOpenWorkflows.setSelectedIndex(currentlyOpenedIndex);
  }

  private JPanel createMetadataPanel() {
    Insets fieldInset = new Insets(0, 5, 4, 5);
    Insets labelInset = new Insets(3, 5, 4, 5);

    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.weightx = 1;
    c.gridy = 0;
    c.anchor = GridBagConstraints.WEST;
    c.gridwidth = 2;
    c.fill = GridBagConstraints.HORIZONTAL;

    JPanel metaPanel = new JPanel(new GridBagLayout());
    metaPanel.setBorder(BorderFactory
        .createTitledBorder("Workflow information"));

    // title
    JLabel lTitle = new JLabel("Workflow title:");
    c.insets = labelInset;
    c.gridy++;
    metaPanel.add(lTitle, c);

    this.tfTitle = new JTextField();
    if (this.updateResource != null)
      this.tfTitle.setText(this.updateResource.getTitle());
    c.gridy++;
    c.insets = fieldInset;
    metaPanel.add(this.tfTitle, c);

    // description
    JLabel lDescription = new JLabel("Workflow description:");
    c.gridy++;
    c.insets = labelInset;
    metaPanel.add(lDescription, c);

    this.taDescription = new JTextArea(5, 35);
    this.taDescription.setLineWrap(true);
    this.taDescription.setWrapStyleWord(true);
    if (this.updateResource != null)
      this.taDescription.setText(this.updateResource.getDescription());

    JScrollPane spDescription = new JScrollPane(this.taDescription);
    c.gridy++;
    c.insets = fieldInset;
    metaPanel.add(spDescription, c);

    // licences
    String[] licenseText = new String[License.SUPPORTED_TYPES.length];
    for (int x = 0; x < License.SUPPORTED_TYPES.length; x++)
      licenseText[x] = License.getInstance(License.SUPPORTED_TYPES[x])
          .getText();

    jcbLicences = new JComboBox(licenseText);
    String defaultLicenseText = License.getInstance(License.DEFAULT_LICENSE)
    .getText();
    jcbLicences.setSelectedItem(defaultLicenseText);

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
    c.insets = labelInset;
    metaPanel.add(lLicense, c);

    c.gridy++;
    c.insets = fieldInset;
    metaPanel.add(jcbLicences, c);

    // sharing - options: private / view / download
    String[] permissions = { "This workflow is private.",
        "Anyone can view, but noone can download.",
        "Anyone can view, and anyone can download" };

    this.jcbSharingPermissions = new JComboBox(permissions);

    jcbSharingPermissions.addActionListener(this);
    jcbSharingPermissions.setEditable(false);

    JLabel jSharing = new JLabel("Please select your sharing permissions:");
    c.gridy++;
    c.insets = labelInset;
    metaPanel.add(jSharing, c);

    c.gridy++;
    c.insets = fieldInset;
    metaPanel.add(jcbSharingPermissions, c);

    return metaPanel;
  }

  private void initialiseUI() {
    // get content pane
    Container contentPane = this.getContentPane();

    Insets fieldInset = new Insets(3, 5, 3, 5);

    // set up layout
    contentPane.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.gridwidth = 2;
    c.fill = GridBagConstraints.HORIZONTAL;

    // ADD ALL COMPONENTS
    // source for workflow to upload
    if (userRequestedWorkflowUpload) {
      c.insets = fieldInset;
      contentPane.add(createSelectSource(), c);
      c.gridy++;
    }

    // create metadata panel
    contentPane.add(createMetadataPanel(), c);

    // buttons
    this.bUpload = new JButton(userRequestedWorkflowUpload ? "Upload Workflow"
        : "Update Workflow");
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

  private File performSourceCheck() {
    if (!rbSelectLocalFile.isSelected() && !rbSelectOpenWorkflow.isSelected()) { // it
      // is
      // logicall
      // impossible
      // to
      // get
      // this
      // message,
      // have
      // it
      // JUST
      // IN
      // CASE
      JOptionPane.showConfirmDialog(this,
          "You have not selected a source for you workflow.\n"
              + "Please select a source and try again.",
          "Select Workflow Source", JOptionPane.DEFAULT_OPTION,
          JOptionPane.ERROR_MESSAGE);
      return null;
    }

    if (rbSelectOpenWorkflow.isSelected()) { // user requested to use a flow
      // currently open in t2
      Dataflow dataflowToUpload = ((DataflowSelection) jcbOpenWorkflows
          .getSelectedItem()).getDataflow();
      FileManager fileManager = FileManager.getInstance();
      SaveWorkflowAsAction saveAction = new SaveWorkflowAsAction();

      boolean skipPrompt = false;
      if (fileManager.isDataflowChanged(dataflowToUpload)) { // if flow has
        // changed, prompt
        // user to save
        JOptionPane.showConfirmDialog(this,
            "The workflow you are trying to upload has\n"
                + "changed since the last time it was saved.\n\n"
                + "Please save your file to proceed...", "Save Workflow",
            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
        saveAction.saveDataflow(this, dataflowToUpload);
        skipPrompt = true;
      }

      File dataflowFile = (File) fileManager
          .getDataflowSource(dataflowToUpload);
      if (dataflowFile == null && !skipPrompt) {
        JOptionPane.showConfirmDialog(this,
            "You cannot upload an empty workflow.\n"
                + "Please select a different workflow before\n"
                + "you attempt the upload again.", "Upload Error",
            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        return null;
      } else return dataflowFile;

    } else { // user wants to use local file
      if (localWorkflowFile == null) {
        JOptionPane.showConfirmDialog(this,
            "You have not selected a file to upload.\n"
                + "Please select a file and try again.",
            "Select Workflow Source", JOptionPane.DEFAULT_OPTION,
            JOptionPane.ERROR_MESSAGE);
        return null;
      }

      return localWorkflowFile;
    }
  }

  private void getMetadata() {
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

    // get licence
    this.licence = License.SUPPORTED_TYPES[this.jcbLicences.getSelectedIndex()];

    // get title
    this.strTitle = this.tfTitle.getText();
    this.strTitle = this.strTitle.trim();

    // get description
    this.strDescription = this.taDescription.getText();
    this.strDescription = this.strDescription.trim();
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource().equals(this.bUpload)) { // * *** *** *** * UPLOAD BUTTON *
      // *** *** *** *
      // perform source check returns a file if attaining the source was
      // successful
      if (userRequestedWorkflowUpload) {
        uploadFile = performSourceCheck();
        if (uploadFile == null) return;
      }

      // collect and put the metadata values in their respectable vars
      getMetadata();

      // if the description or the title are empty, prompt the user to confirm
      // the upload
      boolean proceedWithUpload = false;
      if ((this.strDescription.length() == 0) && (this.strTitle.length() == 0)) {
        String strInfo = "The workflow 'title' field and the 'description' field\n"
            + "(or both) are empty.  Any metadata found within the\n"
            + "workflow will be used instead.  Do you wish to proceed?";
        int confirm = JOptionPane.showConfirmDialog(this, strInfo,
            "Empty fields", JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) proceedWithUpload = true;
      } else proceedWithUpload = true;

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
        lStatusMessage = new JLabel((updateResource == null ? "Uploading"
            : "Updating")
            + " your workflow...", new ImageIcon(MyExperimentPerspective
            .getLocalResourceURL("spinner")), SwingConstants.CENTER);
        contentPane.add(lStatusMessage, c);

        // disable the (X) button (ideally, would need to remove it, but there's
        // no way to do this)
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // revalidate the window
        this.pack();
        this.validate();
        this.repaint();

        new Thread("Posting workflow") {
          boolean formatRecognized = false;

          @Override
          public void run() {
            String workflowFileContent = "";
            if (userRequestedWorkflowUpload) {
              if (uploadFile != null) {
                try {
                  BufferedReader reader = new BufferedReader(new FileReader(
                      uploadFile));
                  String line;

                  String scuflSchemaDef = "xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"";
                  String t2flowSchemaDef = "xmlns=\"http://taverna.sf.net/2008/xml/t2flow\"";

                  while ((line = reader.readLine()) != null) {
                    if (!formatRecognized
                        && (line.contains(scuflSchemaDef) || line
                            .contains(t2flowSchemaDef)))
                      formatRecognized = true;

                    workflowFileContent += line + "\n";
                  }
                } catch (Exception e) {
                  lStatusMessage = new JLabel("Error occurred:"
                      + e.getMessage(), new ImageIcon(MyExperimentPerspective
                      .getLocalResourceURL("failure_icon")),
                      SwingConstants.LEFT);
                  logger.error(e.getCause() + "\n" + e.getMessage());
                }
              }
            }

            // *** POST THE WORKFLOW ***
            final ServerResponse response;

            if ((userRequestedWorkflowUpload && formatRecognized)
                || !userRequestedWorkflowUpload) {
              if (updateResource == null) // upload a new workflow
              response = myExperimentClient.postWorkflow(workflowFileContent,
                  Util.stripAllHTML(strTitle), Util
                      .stripAllHTML(strDescription), licence, sharing);
              else
              // edit existing workflow
              response = myExperimentClient.updateWorkflowVersionOrMetadata(
                  updateResource, workflowFileContent, Util
                      .stripAllHTML(strTitle), Util
                      .stripAllHTML(strDescription), licence, sharing);

              bUploadingSuccessful = (response.getResponseCode() == HttpURLConnection.HTTP_OK);
            } else {
              bUploadingSuccessful = false;
              response = null;
            }

            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                // *** REACT TO POSTING RESULT ***
                if (bUploadingSuccessful) {
                  // workflow uploaded successfully
                  setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                  // disable all fields in dialog
                  tfTitle.setEnabled(false);
                  taDescription.setEnabled(false);
                  jcbLicences.setEnabled(false);
                  jcbSharingPermissions.setEnabled(false);
                  if (userRequestedWorkflowUpload) {
                    rbSelectOpenWorkflow.setEnabled(false);
                    rbSelectLocalFile.setEnabled(false);
                    selectedFileLabel.setEnabled(false);
                    bSelectFile.setEnabled(false);
                    jcbOpenWorkflows.setEnabled(false);
                  }
                  contentPane.remove(lStatusMessage);

                  c.insets = new Insets(10, 5, 5, 5);
                  lStatusMessage = new JLabel("Your workflow was successfully "
                      + (updateResource == null ? "uploaded." : "updated."),
                      new ImageIcon(MyExperimentPerspective
                          .getLocalResourceURL("success_icon")),
                      SwingConstants.LEFT);
                  contentPane.add(lStatusMessage, c);

                  bCancel.setText("OK");
                  bCancel.setDefaultCapable(true);
                  rootPane.setDefaultButton(bCancel);
                  c.insets = new Insets(5, 5, 10, 5);
                  c.gridy++;
                  contentPane.add(bCancel, c);

                  pack();
                  bCancel.requestFocusInWindow();

                  // update uploaded items history making sure that:
                  // - there's only one occurrence of this item in the history;
                  // - if this item was in the history before, it is moved to
                  // the 'top' now;
                  // - predefined history size is not exceeded
                  MainComponent.MAIN_COMPONENT.getHistoryBrowser()
                      .getUploadedItemsHistoryList().remove(updateResource);
                  MainComponent.MAIN_COMPONENT.getHistoryBrowser()
                      .getUploadedItemsHistoryList().add(updateResource);
                  if (MainComponent.MAIN_COMPONENT.getHistoryBrowser()
                      .getUploadedItemsHistoryList().size() > HistoryBrowserTabContentPanel.UPLOADED_ITEMS_HISTORY_LENGTH) {
                    MainComponent.MAIN_COMPONENT.getHistoryBrowser()
                        .getUploadedItemsHistoryList().remove(0);
                  }

                  // now update the uploaded items history panel in 'History'
                  // tab
                  if (MainComponent.MAIN_COMPONENT.getHistoryBrowser() != null) {
                    MainComponent.MAIN_COMPONENT
                        .getHistoryBrowser()
                        .refreshHistoryBox(
                            HistoryBrowserTabContentPanel.UPLOADED_ITEMS_HISTORY);
                  }

                } else {
                  // posting wasn't successful, notify the user
                  // and provide an option to close window or start again
                  setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                  taDescription.setEditable(true);
                  contentPane.remove(lStatusMessage);

                  c.insets = new Insets(10, 5, 5, 5);

                  String msg;
                  if (!formatRecognized) msg = "Error occured: Invalid Taverna workflow.";
                  else msg = "An error occured while processing your request.";

                  lStatusMessage = new JLabel(msg, new ImageIcon(
                      MyExperimentPerspective
                          .getLocalResourceURL("failure_icon")),
                      SwingConstants.LEFT);
                  contentPane.add(lStatusMessage, c);

                  bUpload.setText("Try again");
                  bUpload
                      .setToolTipText("Please review your workflow or myExperiment base URL");
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
    } else if (e.getSource().equals(this.bCancel)) { // * CANCEL BUTTON *
      // cleanup the input fields if it wasn't posted successfully + simply
      // close and destroy the window
      if (!this.bUploadingSuccessful) {
        this.strDescription = null;
        this.tfTitle = null;
      }
      this.dispose();
    } else if (e.getSource().equals(bSelectFile)) {// * SELECT FILE BUTTON *
      // *
      if (jfsSelectFile.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        localWorkflowFile = jfsSelectFile.getSelectedFile();

        if (localWorkflowFile != null) {
          selectedFileLabel.setText(localWorkflowFile.getAbsolutePath());
          selectedFileLabel.setEnabled(true);
        }
        pack();
      }
    }
  }

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

  public void focusGained(FocusEvent e) {
    if (e.getSource().equals(rbSelectLocalFile)) {
      uploadWorkflowFromLocalFile = true;
      bSelectFile.setEnabled(uploadWorkflowFromLocalFile);
      jcbOpenWorkflows.setEnabled(!uploadWorkflowFromLocalFile);

      if (localWorkflowFile != null) {
        selectedFileLabel.setEnabled(uploadWorkflowFromLocalFile);
        selectedFileLabel.setText(localWorkflowFile.getAbsolutePath());
        pack();
      } else selectedFileLabel.setEnabled(!uploadWorkflowFromLocalFile);

    } else if (e.getSource().equals(rbSelectOpenWorkflow)) {
      uploadWorkflowFromLocalFile = false;
      selectedFileLabel.setEnabled(uploadWorkflowFromLocalFile);
      bSelectFile.setEnabled(uploadWorkflowFromLocalFile);
      jcbOpenWorkflows.setEnabled(!uploadWorkflowFromLocalFile);
    }
  }

  public void componentShown(ComponentEvent e) {
    // center this dialog box within the preview browser window
    if (updateResource == null) // upload has been pressed from the MAIN
    // perspective window
    Util.centerComponentWithinAnother(this.pluginMainComponent, this);
    else
    // upload pressed from resource preview window
    Util.centerComponentWithinAnother(this.pluginMainComponent
        .getPreviewBrowser(), this);
  }

  public void focusLost(FocusEvent e) {
    // not in use
  }

  public void caretUpdate(CaretEvent e) {
    // not in use
  }

  public void componentHidden(ComponentEvent e) {
    // not in use
  }

  public void componentMoved(ComponentEvent e) {
    // not in use
  }

  public void keyReleased(KeyEvent e) {
    // not in use
  }

  public void keyTyped(KeyEvent e) {
    // not in use
  }

  public void componentResized(ComponentEvent e) {
    // not in use
  }

  private class DataflowSelection {
    private final Dataflow dataflow;
    private final String name;

    public DataflowSelection(Dataflow dataflow, String name) {
      this.dataflow = dataflow;
      this.name = name;
    }

    public Dataflow getDataflow() {
      return dataflow;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

}
