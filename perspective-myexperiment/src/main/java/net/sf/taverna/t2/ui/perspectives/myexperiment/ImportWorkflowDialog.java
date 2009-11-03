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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Util;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Workflow;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.importworkflow.DataflowMerger;
import net.sf.taverna.t2.workflowmodel.Dataflow;

// import net.sf.taverna.t2.activities.dataflow.DataflowActivity;

public class ImportWorkflowDialog extends JDialog implements ActionListener, ComponentListener {

  private JPanel mainPanel;
  private final FileManager fileManager = FileManager.getInstance();
  private JComboBox jcbOpenFiles;
  private JPanel jpButtons;
  private JButton bImportAndMerge;
  private JButton bImportAndNest;
  private final Resource resource;

  public ImportWorkflowDialog(ResourcePreviewBrowser parent, Resource r) {
	super(parent, "Import workflow", false);
	this.resource = r;
	initUI();
	setContentPane(mainPanel);
	setVisible(true);
	setResizable(false);
	this.setMinimumSize(new Dimension(400, 200));
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
	JLabel introduction = new JLabel("<html>You can import an existing workflow as a "
		+ "<em>nested workflow</em><br> or by merging it with the current workflow. </html>");
	mainPanel.add(introduction, c);

	// panel with dropdown to enable user which workflow to import current on into 
	c.gridy++;

	createDropdown();
	mainPanel.add(jcbOpenFiles, c);

	// panel with IMPORT and NESTING buttons
	c.gridy++;
	createButtons();
	mainPanel.add(jpButtons, c);
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
	jpButtons.add(bImportAndNest, c);
  }

  private void createDropdown() {
	List<DataflowSelection> openDataflows = new ArrayList<DataflowSelection>();

	for (Dataflow df : fileManager.getOpenDataflows()) {

	  Object source = fileManager.getDataflowSource(df);
	  String name;
	  if (source != null) {
		name = source.toString();
	  } else {
		name = df.getLocalName();
	  }
	  if (df.equals(fileManager.getCurrentDataflow())) {
		name = "<html>" + name + " <i>(current)</i></html>";
	  }

	  openDataflows.add(new DataflowSelection(df, name));
	}
	jcbOpenFiles = new JComboBox(openDataflows.toArray());
  }

  public void actionPerformed(ActionEvent e) {
	bImportAndNest.setEnabled(false);
	bImportAndMerge.setEnabled(false);
	if (e.getSource().equals(bImportAndMerge)) {
	  bImportAndNest.setText("---");
	  bImportAndMerge.setText("Importing...");
	  new Thread("Download and import workflow") {
		@Override
		public void run() {
		  try {
			Workflow w = MainComponent.MY_EXPERIMENT_CLIENT.fetchWorkflowBinary(resource.getURI());
			ByteArrayInputStream workflowDataInputStream = new ByteArrayInputStream(w.getContent());
			FileType fileTypeType = (w.isTaverna1Workflow() ? new MainComponent.ScuflFileType() : new MainComponent.T2FlowFileType());

			FileManager fileManager = FileManager.getInstance();
			Dataflow currentDataflow = fileManager.getCurrentDataflow();

			Dataflow toBeImported = fileManager.openDataflow(fileTypeType, workflowDataInputStream);

			DataflowMerger dataflowMerger = new DataflowMerger(currentDataflow);
			dataflowMerger.merge(toBeImported);
			fileManager.closeDataflow(toBeImported, true);

			setVisible(false);
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
			Workflow w = MainComponent.MY_EXPERIMENT_CLIENT.fetchWorkflowBinary(resource.getURI());
			ByteArrayInputStream workflowDataInputStream = new ByteArrayInputStream(w.getContent());
			FileType fileTypeType = (w.isTaverna1Workflow() ? new MainComponent.ScuflFileType() : new MainComponent.T2FlowFileType());

			FileManager fileManager = FileManager.getInstance();
			Dataflow currentDataflow = fileManager.getCurrentDataflow();

			Dataflow toBeImported = fileManager.openDataflow(fileTypeType, workflowDataInputStream);
			DataflowActivity dataflowActivity = new DataflowActivity();
			dataflowActivity.configure(toBeImported);

			// TODO: find out how to create a new processor inside a workflow
			fileManager.closeDataflow(toBeImported, true);

			setVisible(false);
		  } catch (Exception e) {
			javax.swing.JOptionPane.showMessageDialog(null, "An error has occurred while trying to load a workflow from myExperiment.\n\n"
				+ e, "Error", JOptionPane.ERROR_MESSAGE);
			MainComponent.LOGGER.error("Failed to open connection to URL to download and open workflow, from myExperiment.", e);
		  } // try catch
		}; // run
	  }.start(); // thread
	}
  }

  public void componentHidden(ComponentEvent e) {
	// TODO Auto-generated method stub

  }

  public void componentMoved(ComponentEvent e) {
	// TODO Auto-generated method stub

  }

  public void componentResized(ComponentEvent e) {
	// TODO Auto-generated method stub

  }

  public void componentShown(ComponentEvent e) {
	Util.centerComponentWithinAnother(MainComponent.MAIN_COMPONENT.getPreviewBrowser(), this);
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
