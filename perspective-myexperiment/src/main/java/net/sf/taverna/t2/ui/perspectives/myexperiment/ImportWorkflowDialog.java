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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.dataflow.filemanager.NestedDataflowSource;
import net.sf.taverna.t2.activities.dataflow.servicedescriptions.DataflowTemplateService;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Util;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Workflow;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.importworkflow.DataflowMerger;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;

public class ImportWorkflowDialog extends JDialog implements ActionListener, ComponentListener {

  private JPanel mainPanel;
  private final static FileManager fileManager = FileManager.getInstance();
  private final EditManager editManager = EditManager.getInstance();
  private static JComboBox jcbOpenFiles;
  private JPanel jpButtons;
  private JButton bImportAndMerge;
  private JButton bImportAndNest;
  private final Resource resource;
  private final boolean loadingSuccessful = false;
  private JTextField tfPrefix;

  public ImportWorkflowDialog(ResourcePreviewBrowser parent, Resource r) {
	super(parent, "Import workflow", false);
	this.resource = r;
	initUI();
	setContentPane(mainPanel);
	//	setResizable(false);
	setMinimumSize(new Dimension(470, 270));
	setMaximumSize(new Dimension(470, 750));
	pack();
	this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	Util.centerComponentWithinAnother(parent, this);
  }

  private void initUI() {
	mainPanel = new JPanel(new GridBagLayout());

	GridBagConstraints c = new GridBagConstraints();
	c.gridx = 0;
	c.gridy = 0;
	c.gridwidth = 1;
	c.weightx = 1;
	c.insets = new Insets(5, 10, 5, 5);
	c.fill = GridBagConstraints.BOTH;

	// intro label
	JLabel introduction = new JLabel("<html>By importing a workflow, all services, ports and links will be copied <br/>"
		+ "into the destination workflow. This can be useful for merging smaller <br/>"
		+ "workflow fragments. For inclusion of larger workflows you might find <br/>"
		+ "using nested workflows more tidy." + "</html>");
	mainPanel.add(introduction, c);

	// import into label
	JLabel importInto = new JLabel("<html>Import \"<b>" + resource.getTitle()
		+ "\"</b> into:");
	c.gridy++;
	c.insets = new Insets(15, 10, 5, 5);
	mainPanel.add(importInto, c);

	// panel with dropdown to enable user which workflow to import current on into 
	c.gridy++;
	c.insets = new Insets(5, 10, 5, 5);
	mainPanel.add(createDropdown(), c);

	// panel with text field for prefix capture
	c.gridy++;
	mainPanel.add(createPrefixCapture(), c);

	// panel with IMPORT and NESTING buttons
	c.gridy++;
	createButtons();
	mainPanel.add(jpButtons, c);
  }

  private JPanel createPrefixCapture() {
	JPanel prefixPanel = new JPanel(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.gridy = 0;
	c.gridx = 0;
	c.anchor = GridBagConstraints.WEST;
	c.weightx = 1;
	c.insets = new Insets(5, 5, 5, 5);
	c.fill = GridBagConstraints.HORIZONTAL;
	JLabel label = new JLabel("...with prefix:");
	prefixPanel.add(label, c);

	c.gridy++;
	tfPrefix = new JTextField(30);
	prefixPanel.add(tfPrefix, c);

	return prefixPanel;
  }

  private void createButtons() {
	jpButtons = new JPanel(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.insets = new Insets(10, 25, 10, 25);
	c.fill = GridBagConstraints.HORIZONTAL;

	bImportAndMerge = new JButton("Import and Merge");
	bImportAndMerge.addActionListener(this);

	bImportAndNest = new JButton("Import as nested");
	bImportAndNest.addActionListener(this);

	jpButtons.add(bImportAndMerge, c);
	//	jpButtons.add(bImportAndNest, c);
  }

  // TODO: move to UploadWorkflowDialog AFTER Stian has finished workflow import ie code inbetween /* *** */
  /* ************************************************************************* */
  protected static JComboBox createDropdown() {
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
	  if (!foundIndex)
		currentlyOpenedIndex++;
	}

	jcbOpenFiles = new JComboBox(openDataflows.toArray());
	jcbOpenFiles.setSelectedIndex(currentlyOpenedIndex);
	return jcbOpenFiles;
  }

  /* ************************************************************************* */

  public void actionPerformed(ActionEvent e) {
	// set status bar to reflect process 
	final String strCallerTabClassName = MainComponent.MAIN_COMPONENT.getMainTabs().getSelectedComponent().getClass().getName();
	MainComponent.MAIN_COMPONENT.getStatusBar().setStatus(strCallerTabClassName, "Downloading and importing workflow...");
	MainComponent.LOGGER.debug("Downloading and importing workflow from URI: "
		+ resource.getURI());

	bImportAndNest.setEnabled(false);
	bImportAndMerge.setEnabled(false);
	tfPrefix.setEnabled(false);
	if (e.getSource().equals(bImportAndMerge)) {
	  // TODO: remove sanitization AFTER Stian has finished workflow import ie code inbetween /* *** */
	  /* ************************************************************************* */
	  // sanitize the user input
	  boolean sanitized = false;
	  char[] prefix = tfPrefix.getText().toCharArray();
	  for (int x = 0; x < prefix.length; x++) {
		char c = prefix[x];
		if (!Character.isLetterOrDigit(c) && !((Character) c).equals('_')) {
		  prefix[x] = '_';
		  sanitized = true;
		}
	  }
	  final String sanitizedPrefix = new String(prefix);
	  tfPrefix.setText(sanitizedPrefix);

	  // confirm that user wants this sanitization
	  if (sanitized) {
		String info = "The prefix you have entered contains characters that are not\n"
			+ "letters, numbers, or underscores.  These will be replaced\n"
			+ "by underscores.\n\n"
			+ "The prefix will now read:\n"
			+ sanitizedPrefix + "\n\nDo you wish to proceed?";
		int confirm = JOptionPane.showConfirmDialog(this, info, "Empty fields", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (confirm != JOptionPane.YES_OPTION) {
		  bImportAndNest.setEnabled(true);
		  bImportAndMerge.setEnabled(true);
		  tfPrefix.setEnabled(true);
		  return;
		}
	  }
	  /* ************************************************************************* */

	  // proceed with merge
	  bImportAndNest.setText("---");
	  bImportAndMerge.setText("Importing...");

	  new Thread("Download and import workflow") {
		@Override
		public void run() {
		  try {
			// TODO: proper way to do merge  AFTER Stian has finished workflow import ie code inbetween /* *** */
			/* ************************************************************************* */
			Workflow w = MainComponent.MY_EXPERIMENT_CLIENT.fetchWorkflowBinary(resource.getURI());

			ByteArrayInputStream workflowDataInputStream = new ByteArrayInputStream(w.getContent());
			FileType fileTypeType = (w.isTaverna1Workflow() ? new MainComponent.ScuflFileType() : new MainComponent.T2FlowFileType());

			Dataflow currentDataflow = fileManager.getCurrentDataflow();
			Dataflow toBeImported = fileManager.openDataflowSilently(fileTypeType, workflowDataInputStream).getDataflow();
			DataflowMerger dataflowMerger = new DataflowMerger(currentDataflow);
			editManager.doDataflowEdit(currentDataflow, dataflowMerger.getMergeEdit(toBeImported, sanitizedPrefix));
			fileManager.setDataflowChanged(currentDataflow, true);
			/* ************************************************************************* */
		  } catch (Exception e) {
			javax.swing.JOptionPane.showMessageDialog(null, "An error has occurred while trying to load a workflow from myExperiment.\n\n"
				+ e, "Error", JOptionPane.ERROR_MESSAGE);
			MainComponent.LOGGER.error("Failed to open connection to URL to download and open workflow, from myExperiment.", e);
		  } // try catch
		}; // run
	  }.start(); // thread
	} else if (e.getSource().equals(bImportAndNest)) {
	  bImportAndMerge.setText("---");
	  bImportAndNest.setText("Importing...");
	  new Thread("Download and nest workflow") {
		@Override
		public void run() {
		  try {
			// TODO: proper way to do merge  AFTER Stian has finished workflow import ie code inbetween /* *** */
			/* ************************************************************************* */
			Workflow w = MainComponent.MY_EXPERIMENT_CLIENT.fetchWorkflowBinary(resource.getURI());
			ByteArrayInputStream workflowDataInputStream = new ByteArrayInputStream(w.getContent());
			FileType fileTypeType = (w.isTaverna1Workflow() ? new MainComponent.ScuflFileType() : new MainComponent.T2FlowFileType());

			Dataflow currentDataflow = fileManager.getCurrentDataflow();
			Dataflow toBeImported = fileManager.openDataflowSilently(fileTypeType, workflowDataInputStream).getDataflow();

			// TODO: do nesting
			DataflowActivity dataflowActivity = new DataflowActivity();
			dataflowActivity.configure(toBeImported);

			Processor dataflowProcessor = WorkflowView.importServiceDescription(DataflowTemplateService.getServiceDescription(), false);
			NestedDataflowSource nestedDataflowSource = new NestedDataflowSource(currentDataflow, dataflowActivity);
			/* ************************************************************************* */
		  } catch (Exception e) {
			javax.swing.JOptionPane.showMessageDialog(null, "An error has occurred while trying to load a workflow from myExperiment.\n\n"
				+ e, "Error", JOptionPane.ERROR_MESSAGE);
			MainComponent.LOGGER.error("Failed to open connection to URL to download and open workflow, from myExperiment.", e);
		  } // try catch
		}; // run
	  }.start(); // thread
	}

	setVisible(false);
	MainComponent.MAIN_COMPONENT.getStatusBar().setStatus(strCallerTabClassName, null);
  }

  public boolean launchImportDialogAndLoadIfRequired() {
	// makes the 'add comment' dialog visible, then waits until it is closed;
	// control returns to this method when the dialog window is disposed
	this.setVisible(true);
	return (loadingSuccessful);
  }

  public void componentHidden(ComponentEvent e) {
  }

  public void componentMoved(ComponentEvent e) {
  }

  public void componentResized(ComponentEvent e) {
  }

  public void componentShown(ComponentEvent e) {
	Util.centerComponentWithinAnother(MainComponent.MAIN_COMPONENT.getPreviewBrowser(), this);
  }

  // TODO: move to UploadWorkflowDialog AFTER Stian has finished workflow import ie code inbetween /* *** */
  /* ************************************************************************* */
  protected static class DataflowSelection {
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
  /* ************************************************************************* */
}
