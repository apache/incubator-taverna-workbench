package net.sf.taverna.t2.workbench.file.importworkflow.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.actions.OpenWorkflowAction;
import net.sf.taverna.t2.workbench.file.importworkflow.DataflowMerger;
import net.sf.taverna.t2.workbench.file.importworkflow.MergeException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

public class ImportWorkflowWizard extends JDialog {

	private JComboBox chooseDataflow;
	private JRadioButton radioOpened;
	private JRadioButton radioUrl;
	private JRadioButton radioFile;
	private JRadioButton radioNew;
	private JTextField fieldFile;
	private JButton buttonBrowse;

	FileManager fileManager = FileManager.getInstance();
	private ButtonGroup sourceSelection;

	private static Logger logger = Logger.getLogger(ImportWorkflowWizard.class);
	private JTextField fieldUrl;

	public ImportWorkflowWizard(Frame parentFrame) {
		super(parentFrame, "Import workflow", true);
		setSize(600, 300);
		setContentPane(makeContentPane());
	}

	protected Container makeContentPane() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;

		JLabel introduction = new JLabel(
				"<html>You can import an existing workflow as a <br>"
						+ "<em>nested workflow</em> or by merging it with the current workflow. </html>");
		panel.add(introduction, gbc);

		gbc.gridy = 1;
		gbc.weightx = 0.1;
		panel.add(makeSourceSelectionPanel(), gbc);

		gbc.gridy = 2;
		gbc.weighty = 0.1;
		panel.add(makeImportStylePanel(), gbc);

		return panel;
	}

	protected Component makeSourceSelectionPanel() {
		JPanel j = new JPanel(new GridBagLayout());
		j.setBorder(BorderFactory.createTitledBorder("Workflow source"));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;

		sourceSelection = new ButtonGroup();
		radioNew = new JRadioButton("New workflow");
		gbc.gridy = 0;
		j.add(radioNew, gbc);
		sourceSelection.add(radioNew);

		radioFile = new JRadioButton("Import from file");
		gbc.gridy = 1;
		j.add(radioFile, gbc);
		sourceSelection.add(radioFile);

		radioUrl = new JRadioButton("Import from URL");
		gbc.gridy = 2;
		j.add(radioUrl, gbc);
		sourceSelection.add(radioUrl);

		radioOpened = new JRadioButton("Already opened workflow");
		gbc.gridy = 3;
		j.add(radioOpened, gbc);
		sourceSelection.add(radioOpened);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 0.1;
		j.add(makeSelectFile(), gbc);

		gbc.gridy = 2;
		fieldUrl = new JTextField(20);
		j.add(fieldUrl, gbc);
		fieldUrl.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				radioUrl.setSelected(true);
			}
		});

		gbc.gridy = 3;
		j.add(makeSelectOpenWorkflowPanel(), gbc);

		return j;
	}

	@SuppressWarnings("serial")
	protected Component makeSelectFile() {
		JPanel j = new JPanel(new GridBagLayout());
		j.setBorder(BorderFactory.createEtchedBorder());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.1;

		fieldFile = new JTextField(20);
		fieldFile.setEditable(false);
		fieldFile.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				radioFile.setSelected(true);
			}
		});
		j.add(fieldFile, gbc);

		gbc.gridx = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		buttonBrowse = new JButton(new OpenWorkflowAction() {
			@Override
			public void openWorkflows(Component parentComponent, File[] files,
					FileType fileType, OpenCallback openCallback) {
				radioFile.setSelected(true);
				if (files.length == 0) {
					fieldFile.setText("");
					return;
				}
				fieldFile.setText(files[0].getPath());
			}
		});
		buttonBrowse.setText("Browse");
		j.add(buttonBrowse, gbc);

		radioFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fieldFile.getText().equals("")) {
					// On first label click pop up Browse dialogue.
					buttonBrowse.doClick();
				}
			}
		});

		return j;
	}

	protected Component makeSelectOpenWorkflowPanel() {

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
		chooseDataflow = new JComboBox(openDataflows.toArray());
		chooseDataflow.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				radioOpened.setSelected(true);
			}
		});
		return chooseDataflow;

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

	protected Component makeImportStylePanel() {
		JPanel j = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;

		j.setBorder(BorderFactory.createTitledBorder("Import style"));

		JButton comp = new JButton("Import workflow");
		comp.setAction(new AbstractAction("Import workflow") {

			public void actionPerformed(ActionEvent e) {
				Component parentComponent;
				if (e.getSource() instanceof Component) {
					parentComponent = (Component) e.getSource();
				} else {
					parentComponent = null;
				}

				Dataflow currentDataflow = fileManager.getCurrentDataflow();

				Dataflow chosenDataflow = null;

				ButtonModel selection = sourceSelection.getSelection();
				if (selection.equals(radioNew.getModel())) {
					chosenDataflow = fileManager.newDataflow();
				} else if (selection.equals(radioFile.getModel())) {
					String filePath = fieldFile.getText();
					try {
						chosenDataflow = fileManager.openDataflow(null,
								new File(filePath));
					} catch (OpenException e1) {
						logger.warn("Could not open workflow for merging: "
								+ filePath, e1);
						JOptionPane.showMessageDialog(parentComponent,
								"An error occured while trying to open "
										+ filePath + "\n" + e1.getMessage(),
								"Could not open workflow",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
				} else if (selection.equals(radioUrl.getModel())) {
					String url = fieldUrl.getText();
					try {
						chosenDataflow = fileManager.openDataflow(null,
								new URL(url));
					} catch (OpenException e1) {
						logger.warn("Could not open workflow for merging: "
								+ url, e1);
						JOptionPane.showMessageDialog(parentComponent,
								"An error occured while trying to open " + url
										+ "\n" + e1.getMessage(),
								"Could not open workflow",
								JOptionPane.WARNING_MESSAGE);
						return;
					} catch (MalformedURLException e1) {
						logger.warn("Invalid workflow URL: " + url, e1);
						JOptionPane.showMessageDialog(parentComponent,
								"The workflow location " + url
										+ " is invalid\n"
										+ e1.getLocalizedMessage(),
								"Invalid URL", JOptionPane.ERROR_MESSAGE);
						return;
					}
				} else if (selection.equals(radioOpened.getModel())) {
					DataflowSelection chosen = (DataflowSelection) chooseDataflow
							.getSelectedItem();
					chosenDataflow = chosen.getDataflow();
				}
				if (chosenDataflow == null) {
					JOptionPane.showMessageDialog(parentComponent,
							"You need to choose a workflow for merging",
							"No workflow chosen", JOptionPane.ERROR_MESSAGE);
					return;
				}

				DataflowMerger merger = new DataflowMerger(currentDataflow);
				try {
					merger.merge(chosenDataflow);
				} catch (MergeException e1) {
					logger.warn("Could not merge workflow", e1);
					JOptionPane.showMessageDialog(parentComponent,
							"An error occured while merging workflows:\n"
									+ e1.getLocalizedMessage(),
							"Could not merge workflows",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				setVisible(false);
			}

		});
		j.add(comp, gbc);

		return j;

	}

}
