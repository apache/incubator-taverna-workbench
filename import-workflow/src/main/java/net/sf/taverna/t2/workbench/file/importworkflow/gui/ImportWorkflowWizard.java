package net.sf.taverna.t2.workbench.file.importworkflow.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
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

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.actions.OpenWorkflowAction;
import net.sf.taverna.t2.workbench.file.importworkflow.DataflowMerger;
import net.sf.taverna.t2.workbench.file.importworkflow.MergeException;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.views.graph.GraphViewComponent;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.log4j.Logger;
import org.w3c.dom.svg.SVGDocument;

public class ImportWorkflowWizard extends JDialog {

	private static final long serialVersionUID = -4797758549214437719L;
	private JComboBox chooseDataflow;
	private JRadioButton radioOpened;
	private JRadioButton radioUrl;
	private JRadioButton radioFile;
	private JRadioButton radioNew;
	private JTextField fieldFile;
	private JButton buttonBrowse;

	private static FileManager fileManager = FileManager.getInstance();
	private ButtonGroup sourceSelection;

	private static Logger logger = Logger.getLogger(ImportWorkflowWizard.class);
	private JTextField fieldUrl;
	private JTextField prefixField;
	private JSVGCanvas currentWf;
	private Dataflow currentDataflow = fileManager.getCurrentDataflow();
	private JSVGCanvas otherWf;
	private ActionListener updateChosenListener = new UpdateChosenListener();
	private Dataflow chosenDataflow;

	public ImportWorkflowWizard(Frame parentFrame) {
		super(parentFrame, "Import workflow", true);

		setSize(600, 400);
		add(makeContentPane(), BorderLayout.CENTER);
		// Add some space
		add(new JPanel(), BorderLayout.WEST);
		add(new JPanel(), BorderLayout.NORTH);
		add(new JPanel(), BorderLayout.SOUTH);
		add(new JPanel(), BorderLayout.EAST);
		
		
	}

	protected Container makeContentPane() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		
		gbc.ipadx = 5;
		gbc.ipady = 5;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;

		JLabel introduction = new JLabel(
				"<html>By importing a workflow, all services, ports and links will be copied "
						+ "into the destination workflow. This can be useful for merging " +
								"smaller workflow fragments. For inclusion of larger workflows " +
								"you might find using nested workflows more tidy." +
								"</html>");
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

		radioNew.addActionListener(updateChosenListener);

		radioFile = new JRadioButton("Import from file");
		gbc.gridy = 1;
		j.add(radioFile, gbc);
		sourceSelection.add(radioFile);
		radioFile.addActionListener(updateChosenListener);

		radioUrl = new JRadioButton("Import from URL");
		gbc.gridy = 2;
		j.add(radioUrl, gbc);
		sourceSelection.add(radioUrl);
		radioUrl.addActionListener(updateChosenListener);

		radioOpened = new JRadioButton("Already opened workflow");
		gbc.gridy = 3;
		j.add(radioOpened, gbc);
		sourceSelection.add(radioOpened);
		radioOpened.addActionListener(updateChosenListener);

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
			@Override
			public void focusLost(FocusEvent e) {
				findChosenDataflow(e.getComponent(), false);
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

			@Override
			public void focusLost(FocusEvent e) {
				findChosenDataflow(e.getComponent(), false);
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
				findChosenDataflow(parentComponent, true);
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
		chooseDataflow.addActionListener(updateChosenListener);
		chooseDataflow.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				radioOpened.setSelected(true);
			}
		});
		return chooseDataflow;

	}

	private final class UpdateChosenListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Component parentComponent;
			if (e.getSource() instanceof Component) {
				parentComponent = (Component) e.getSource();
			} else {
				parentComponent = null;
			}
			findChosenDataflow(parentComponent, false);
		}
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

		j.setBorder(BorderFactory.createTitledBorder("Import"));

		JLabel prefixLabel = new JLabel("Prefix");
		j.add(prefixLabel, gbc);
		gbc.weightx = 0.1;
		gbc.gridx = 1;

		prefixField = new JTextField(10);
		prefixLabel.setLabelFor(prefixField);
		j.add(prefixField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;

		JLabel prefixHelp = new JLabel(
				"<html><small>Optional prefix, will be prepended to the name of the "
						+ "inserted processors and workflow ports. Even if no prefix is given, duplicate names will be "
						+ "resolved by adding numbers, for instance <code>my_service_2</code> if <code>my_service</code> already "
						+ "existed." + "</small></html>");
		prefixHelp.setLabelFor(prefixField);
		j.add(prefixHelp, gbc);

		gbc.gridy = 2;
		gbc.weightx = 0.1;
		gbc.weighty = 0.1;

		j.add(makeWorkflowImage(), gbc);

		gbc.gridy = 3;
		gbc.weighty = 0.0;
		j.add(new JPanel(), gbc);
		
		gbc.gridy = 4;
		gbc.fill = GridBagConstraints.NONE;
		JButton comp = new JButton("Import workflow");
		comp.setAction(new AbstractAction("Import workflow") {
			public void actionPerformed(ActionEvent e) {
				Component parentComponent;
				if (e.getSource() instanceof Component) {
					parentComponent = (Component) e.getSource();
				} else {
					parentComponent = null;
				}

				Dataflow chosenDataflow = null;

				chosenDataflow = findChosenDataflow(parentComponent, true);
				DataflowMerger merger = new DataflowMerger(currentDataflow);

				EditManager editManager = EditManager.getInstance();

				CompoundEdit mergeEdit;
				try {
					mergeEdit = merger.getMergeEdit(chosenDataflow, prefixField
							.getText());
				} catch (MergeException e1) {
					logger.warn("Could not merge workflow", e1);
					JOptionPane.showMessageDialog(parentComponent,
							"An error occured while merging workflows:\n"
									+ e1.getLocalizedMessage(),
							"Could not merge workflows",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				try {
					editManager.doDataflowEdit(currentDataflow, mergeEdit);
				} catch (EditException e1) {
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

	protected Dataflow findChosenDataflow(Component parentComponent,
			boolean showErrors) {
		ButtonModel selection = sourceSelection.getSelection();
		Dataflow chosenDataflow = null;
		if (selection.equals(radioNew.getModel())) {
			chosenDataflow = EditManager.getInstance().getEdits().createDataflow();
		} else if (selection.equals(radioFile.getModel())) {
			String filePath = fieldFile.getText();
			try {
				DataflowInfo opened = fileManager.openDataflowSilently(null,
						new File(filePath));
				chosenDataflow = opened.getDataflow();
			} catch (OpenException e1) {
				if (showErrors) {
					logger.warn("Could not open workflow for merging: " + filePath,
							e1);
					JOptionPane.showMessageDialog(parentComponent,
							"An error occured while trying to open " + filePath
									+ "\n" + e1.getMessage(),
							"Could not open workflow",
							JOptionPane.WARNING_MESSAGE);
				}
				return null;
			}
		} else if (selection.equals(radioUrl.getModel())) {
			String url = fieldUrl.getText();
			try {
				DataflowInfo opened = fileManager.openDataflowSilently(null,
						new URL(url));
				chosenDataflow = opened.getDataflow();
			} catch (OpenException e1) {
				if (showErrors) {
					logger.warn("Could not open workflow for merging: " + url, e1);
					JOptionPane.showMessageDialog(parentComponent,
							"An error occured while trying to open " + url
									+ "\n" + e1.getMessage(),
							"Could not open workflow",
							JOptionPane.WARNING_MESSAGE);
				}
				return null;
			} catch (MalformedURLException e1) {
				if (showErrors) {
					logger.warn("Invalid workflow URL: " + url, e1);
					JOptionPane.showMessageDialog(parentComponent,
							"The workflow location " + url + " is invalid\n"
									+ e1.getLocalizedMessage(), "Invalid URL",
							JOptionPane.ERROR_MESSAGE);
				}
				return null;

			}
		} else if (selection.equals(radioOpened.getModel())) {
			DataflowSelection chosen = (DataflowSelection) chooseDataflow
					.getSelectedItem();
			chosenDataflow = chosen.getDataflow();
		}
		if (chosenDataflow == null) {
			if (showErrors) {
				JOptionPane.showMessageDialog(parentComponent,
						"You need to choose a workflow for merging",
						"No workflow chosen", JOptionPane.ERROR_MESSAGE);
			}
		}

		if (chosenDataflow != this.chosenDataflow) {
			this.chosenDataflow = chosenDataflow;
			updateWorkflowGraphic(otherWf, chosenDataflow);
		}
		return chosenDataflow;

	}

	private Component makeWorkflowImage() {
		JPanel workflowImages = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.1;

		gbc.weightx = 0.1;
		workflowImages.add(new JPanel(), gbc);

		gbc.weightx = 0.0;
		otherWf = new JSVGCanvas();
		otherWf.setBackground(workflowImages.getBackground());

		workflowImages.add(otherWf, gbc);

		JLabel arrow = new JLabel("\u2192");
		arrow.setFont(arrow.getFont().deriveFont(48f));
		workflowImages.add(arrow, gbc);

		currentWf = new JSVGCanvas();
		currentWf.setBackground(workflowImages.getBackground());
		updateWorkflowGraphic(currentWf, currentDataflow);
		workflowImages.add(currentWf, gbc);

		gbc.weightx = 0.1;
		workflowImages.add(new JPanel(), gbc);
		gbc.weightx = 0.0;

		return workflowImages;
	}

	/**
	 * Create a PNG image of the workflow and place inside an ImageIcon
	 * 
	 * @param dataflow
	 * @return
	 */
	private void updateWorkflowGraphic(final JSVGCanvas svgCanvas,
			Dataflow dataflow) {
		if (dataflow == null) {
			svgCanvas.setVisible(false);
			return;
		}
		SVGGraphController currentWfGraphController = new SVGGraphController(
				dataflow, false, svgCanvas) {
			public void redraw() {
				svgCanvas
						.setDocument(generateSVGDocument(svgCanvas.getBounds()));
			}
		};
		SVGDocument generateSVGDocument = currentWfGraphController
				.generateSVGDocument(new Rectangle(200, 200));

		svgCanvas.setDocument(generateSVGDocument);
		svgCanvas.setVisible(true);
	}

}
