package net.sf.taverna.t2.workbench.file.importworkflow.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.acl.Group;
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
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.actions.OpenWorkflowAction;
import net.sf.taverna.t2.workbench.file.importworkflow.DataflowMerger;
import net.sf.taverna.t2.workbench.file.importworkflow.MergeException;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.log4j.Logger;
import org.w3c.dom.svg.SVGDocument;

public class ImportWorkflowWizard extends JDialog {

	private static FileManager fileManager = FileManager.getInstance();
	private static Logger logger = Logger.getLogger(ImportWorkflowWizard.class);
	private static final long serialVersionUID = -4797758549214437719L;
	private BrowseFileOnClick browseFileOnClick = new BrowseFileOnClick();
	private JButton buttonBrowse;
	private JComboBox chooseDataflow;
	private JSVGCanvas destinationPreview;
	private Thread dataflowOpenerThread;

	private Dataflow destinationDataflow = fileManager.getCurrentDataflow();
	private JTextField fieldFile;

	private JTextField fieldUrl;
	private boolean importEnabled = true;
	private boolean nestedEnabled = true;
	private JSVGCanvas sourcePreview;
	private JTextField prefixField;
	private JRadioButton radioFile;
	private JRadioButton radioNew;
	private JRadioButton radioOpened;
	private JRadioButton radioUrl;
	private Dataflow sourceDataflow;
	private ButtonGroup sourceSelection;
	private ActionListener updateChosenListener = new UpdateChosenListener();
	private Thread updateDestinationThread;
	private Component sourceSelectionPanel;
	private JLabel introduction;
	private JLabel prefixLabel;
	private JLabel prefixHelp;
	private JPanel destinationSelectionPanel;
	private ButtonGroup destinationSelection;
	private JRadioButton radioNewDestination;
	private JRadioButton radioOpenDestination;

	public ImportWorkflowWizard(Frame parentFrame) {
		super(parentFrame, "Import workflow", true);

		setSize(600, 600);
		add(makeContentPane(), BorderLayout.CENTER);
		// Add some space
		add(new JPanel(), BorderLayout.WEST);
		add(new JPanel(), BorderLayout.NORTH);
		add(new JPanel(), BorderLayout.SOUTH);
		add(new JPanel(), BorderLayout.EAST);
	}

	public void setDataflowDestination(Dataflow destinationDataflow) {
		this.destinationDataflow = destinationDataflow;
		updateDestinationSection();
	}

	public void setDataflowSource(Dataflow sourceDataflow) {
		this.sourceDataflow = sourceDataflow;
		updateSourceSection();
	}

	public void setImportEnabled(boolean importEnabled) {
		this.importEnabled = importEnabled;
		updateAll();
	}

	public void setNestedEnabled(boolean nestedEnabled) {
		this.nestedEnabled = nestedEnabled;
		updateAll();
	}

	/**
	 * Silly workaround to avoid
	 * "Cannot call invokeAndWait from the event dispatcher thread" exception.
	 * 
	 * @param runnable
	 */
	private void invokeAndWait(Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
			return;
		}
		try {
			SwingUtilities.invokeAndWait(runnable);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		} catch (InvocationTargetException e) {
			logger.warn("Can't invoke " + runnable, e);
		}
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
		sourcePreview = new JSVGCanvas();
		sourcePreview.setBackground(workflowImages.getBackground());
		workflowImages.add(sourcePreview, gbc);

		JLabel arrow = new JLabel("\u2192");
		arrow.setFont(arrow.getFont().deriveFont(48f));
		workflowImages.add(arrow, gbc);

		destinationPreview = new JSVGCanvas();
		destinationPreview.setBackground(workflowImages.getBackground());

		workflowImages.add(destinationPreview, gbc);

		gbc.weightx = 0.1;
		workflowImages.add(new JPanel(), gbc);
		gbc.weightx = 0.0;

		return workflowImages;
	}

	protected void updateAll() {
		updateHeader();
		updateSourceSection();
		updateDestinationSection();
		updateFooter();
	}

	protected synchronized void updateDestinationSection() {
		destinationSelectionPanel.setVisible(nestedEnabled);
		if (updateDestinationThread != null
				&& updateDestinationThread.isAlive()) {
			updateDestinationThread.interrupt();
		}
		updateDestinationThread = new UpdatePreviewsThread();
		updateDestinationThread.start();
	}

	protected void updateDestinationPreview() {
		updateWorkflowGraphic(destinationPreview, destinationDataflow);
	}

	protected void updateSourcePreview() {
		updateWorkflowGraphic(sourcePreview, sourceDataflow);
	}

	protected void updateFooter() {
		prefixField.setVisible(importEnabled);
		prefixLabel.setVisible(importEnabled);
		prefixHelp.setVisible(importEnabled);
	}

	protected void updateHeader() {
		introduction.setText(makeIntroductionText());
	}

	protected void updateSourceSection() {
		radioNew.setEnabled(nestedEnabled);		
	}

	/**
	 * Create a PNG image of the workflow and place inside an ImageIcon
	 * 
	 * @param dataflow
	 * @return
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	private void updateWorkflowGraphic(final JSVGCanvas svgCanvas,
			Dataflow dataflow) {
		if (dataflow == null) {
			invokeAndWait(new Runnable() {
				public void run() {
					svgCanvas.setVisible(false);
				}
			});
			return;
		}
		SVGGraphController currentWfGraphController = new SVGGraphController(
				dataflow, false, svgCanvas);
		final SVGDocument generateSVGDocument = currentWfGraphController
				.getSVGDocument();
		invokeAndWait(new Runnable() {
			public void run() {
				svgCanvas.setDocument(generateSVGDocument);
				svgCanvas.setVisible(true);
			}
		});
	}

	protected synchronized void findChosenDataflow(Component parentComponent,
			boolean background) {
		if (dataflowOpenerThread != null && dataflowOpenerThread.isAlive()) {
			if (background) {
				// We've changed our mind
				dataflowOpenerThread.interrupt();
			} else {
				// We'll let it finish, we don't need to do it again
				try {
					dataflowOpenerThread.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				return;
			}
		}
		dataflowOpenerThread = new DataflowOpenerThread(parentComponent,
				background);

		if (background) {
			dataflowOpenerThread.start();
		} else {
			dataflowOpenerThread.run();
		}

	}

	protected Container makeContentPane() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.ipadx = 5;
		gbc.ipady = 5;

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;

		introduction = new JLabel(makeIntroductionText());
		panel.add(introduction, gbc);

		gbc.gridy = 1;
		gbc.weightx = 0.1;
		sourceSelectionPanel = makeSourceSelectionPanel();
		panel.add(sourceSelectionPanel, gbc);


		gbc.gridy = 2;
		destinationSelectionPanel = makeDestinationSelectionPanel();
		panel.add(destinationSelectionPanel, gbc);

		
		
		gbc.gridy = 3;
		gbc.weighty = 0.1;
		panel.add(makeImportStylePanel(), gbc);

		return panel;
	}

	private JPanel makeDestinationSelectionPanel() {
		JPanel j = new JPanel(new GridBagLayout());
		j.setBorder(BorderFactory.createTitledBorder("Workflow destination"));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;

		destinationSelection = new ButtonGroup();
		radioNewDestination = new JRadioButton("New workflow");
		gbc.gridy = 0;
		j.add(radioNewDestination, gbc);
		destinationSelection.add(radioNewDestination);		
		radioNewDestination.addActionListener(updateChosenListener);

		
		radioOpenDestination = new JRadioButton("Already opened workflow");
		gbc.gridy = 2;
		j.add(radioOpenDestination, gbc);
		destinationSelection.add(radioOpenDestination);
		radioOpenDestination.addActionListener(updateChosenListener);
		gbc.weightx = 0.1;
		gbc.gridx = 1;
		j.add(makeSelectOpenWorkflowComboBox(), gbc);

		return j;
	}

	private String makeIntroductionText() {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><small>");

		if (nestedEnabled) {
			sb.append("<p>By adding a <strong>nested workflow</strong>, ");
			sb.append("the source workflow will be inserted into the, ");
			sb.append("destination workflow, where it can be connected and  ");
			sb.append("used like any other service. The nested workflow ");
			sb.append("can be edited and replaced separately, and shown ");
			sb.append("expanded or collapsed in the diagram of the parent  ");
			sb.append("workflow. In the parent workflow you can only ");
			sb.append("connect to the input and output ports of the nested ");
			sb.append("workflow. ");
			sb.append("</p>");
		}
		if (importEnabled) {
			sb.append("<p>By <strong>importing</strong> a workflow, ");
			sb.append("all services, ports and links will be copied ");
			sb.append("directly into the destination workflow. This can be  ");
			sb.append("useful for merging smaller workflow fragments. For ");
			sb.append("inclusion of larger workflows you might find using ");
			sb.append("<em>nested workflows</em> more tidy.");
			sb.append("</p>");
		}
		sb.append("</small></html>");
		return sb.toString();
	}

	protected Component makeImportStylePanel() {
		JPanel j = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;

		j.setBorder(BorderFactory.createTitledBorder("Import"));

		prefixLabel = new JLabel("Prefix");
		j.add(prefixLabel, gbc);
		gbc.weightx = 0.1;
		gbc.gridx = 1;

		prefixField = new JTextField(10);
		prefixLabel.setLabelFor(prefixField);
		j.add(prefixField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;

		prefixHelp = new JLabel(
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

				findChosenDataflow(parentComponent, false);
				DataflowMerger merger = new DataflowMerger(destinationDataflow);

				EditManager editManager = EditManager.getInstance();
				String prefix = prefixField.getText();
				if (!prefix.equals("")) {
					if (!prefix.matches("[_.]$")) {
						prefix = prefix + "_";
					}
					if (!prefix.matches("[\\p{L}\\p{Digit}_.]+")) {
						JOptionPane
								.showMessageDialog(
										parentComponent,
										"The merge prefix '"
												+ prefix
												+ "' is not valid. Try "
												+ "using only letters, numbers, underscore and dot.",
										"Invalid merge prefix",
										JOptionPane.ERROR_MESSAGE);
						prefixField.requestFocus();
						return;
					}
				}

				CompoundEdit mergeEdit;
				try {
					mergeEdit = merger.getMergeEdit(
							ImportWorkflowWizard.this.sourceDataflow, prefix);
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
					editManager.doDataflowEdit(destinationDataflow, mergeEdit);
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
				findChosenDataflow(e.getComponent(), true);
			}
		});	
		j.add(fieldFile, gbc);
		radioFile.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					System.out.println("It was selected");
					browseFileOnClick.checkEmptyFile();
				}
			}			
		});
		
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		buttonBrowse = new JButton(new OpenWorkflowAction() {
			@Override
			public void openWorkflows(Component parentComponent, File[] files,
					FileType fileType, OpenCallback openCallback) {				
				if (files.length == 0) {
					radioFile.setSelected(false);
					fieldFile.setText("");
					radioFile.requestFocus();
					return;
				}
				fieldFile.setText(files[0].getPath());
				if (! radioFile.isSelected()) {
					radioFile.setSelected(true);
				}
				findChosenDataflow(parentComponent, true);
			}
		});
		buttonBrowse.setText("Browse");
		j.add(buttonBrowse, gbc);

		radioFile.addActionListener(browseFileOnClick);
		fieldFile.addActionListener(browseFileOnClick);
		return j;
	}

	protected JComboBox makeSelectOpenWorkflowComboBox() {
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
				name = "<html><body>" + name + " <i>(current)</i></body></html>";
			}
			openDataflows.add(new DataflowSelection(df, name));
		}
		JComboBox chooseDataflow = new JComboBox(openDataflows.toArray());
		chooseDataflow.addActionListener(updateChosenListener);		
		return chooseDataflow;

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
				findChosenDataflow(e.getComponent(), true);
			}
		});

		gbc.gridy = 3;
		chooseDataflow = makeSelectOpenWorkflowComboBox();
		chooseDataflow.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				radioOpened.setSelected(true);
			}
		});
		j.add(chooseDataflow, gbc);

		return j;
	}

	private final class UpdatePreviewsThread extends Thread {
		private UpdatePreviewsThread() {
			super("Updating destination previews");
		}

		public void run() {
			if (Thread.interrupted()) {
				return;
			}
			updateSourcePreview();

			if (Thread.interrupted()) {
				return;
			}
			updateDestinationPreview();
		}
	}

	private final class BrowseFileOnClick implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			checkEmptyFile();
		}

		public void checkEmptyFile() {
			if (radioFile.isSelected() && fieldFile.getText().equals("")) {
				// On first label click pop up Browse dialogue.
				buttonBrowse.doClick();
			}
		}
	}

	private final class DataflowOpenerThread extends Thread {
		private final boolean background;
		private final Component parentComponent;
		private boolean shouldStop = false;

		private DataflowOpenerThread(Component parentComponent,
				boolean background) {
			super("Inspecting selected workflow");
			this.parentComponent = parentComponent;
			this.background = background;
		}

		@Override
		public void interrupt() {
			this.shouldStop = true;
			super.interrupt();
		}

		public void run() {
			ButtonModel selection = sourceSelection.getSelection();
			Dataflow chosenDataflow = null;
			if (selection == null) {
				chosenDataflow = null;
			} else if (selection.equals(radioNew.getModel())) {
				chosenDataflow = EditManager.getInstance().getEdits()
						.createDataflow();
			} else if (selection.equals(radioFile.getModel())) {
				String filePath = fieldFile.getText();
				try {
					DataflowInfo opened = fileManager.openDataflowSilently(
							null, new File(filePath));
					if (checkInterrupted()) {
						return;
					}
					chosenDataflow = opened.getDataflow();
				} catch (OpenException e1) {
					if (!background) {
						logger.warn("Could not open workflow for merging: "
								+ filePath, e1);
						JOptionPane.showMessageDialog(parentComponent,
								"An error occured while trying to open "
										+ filePath + "\n" + e1.getMessage(),
								"Could not open workflow",
								JOptionPane.WARNING_MESSAGE);
					}
					ImportWorkflowWizard.this.sourceDataflow = null;
					return;
				}
			} else if (selection.equals(radioUrl.getModel())) {
				String url = fieldUrl.getText();
				try {
					DataflowInfo opened = fileManager.openDataflowSilently(
							null, new URL(url));
					if (checkInterrupted()) {
						return;
					}
					chosenDataflow = opened.getDataflow();
				} catch (OpenException e1) {
					if (!background) {
						logger.warn("Could not open workflow for merging: "
								+ url, e1);
						JOptionPane.showMessageDialog(parentComponent,
								"An error occured while trying to open " + url
										+ "\n" + e1.getMessage(),
								"Could not open workflow",
								JOptionPane.WARNING_MESSAGE);
					}
					if (checkInterrupted()) {
						return;
					}
					ImportWorkflowWizard.this.sourceDataflow = null;
					return;
				} catch (MalformedURLException e1) {
					if (!background) {
						logger.warn("Invalid workflow URL: " + url, e1);
						JOptionPane.showMessageDialog(parentComponent,
								"The workflow location " + url
										+ " is invalid\n"
										+ e1.getLocalizedMessage(),
								"Invalid URL", JOptionPane.ERROR_MESSAGE);
					}
					if (checkInterrupted()) {
						return;
					}
					ImportWorkflowWizard.this.sourceDataflow = null;
					return;
				}
			} else if (selection.equals(radioOpened.getModel())) {
				DataflowSelection chosen = (DataflowSelection) chooseDataflow
						.getSelectedItem();
				chosenDataflow = chosen.getDataflow();
			}
			if (chosenDataflow == null) {
				if (!background) {
					JOptionPane.showMessageDialog(parentComponent,
							"You need to choose a workflow for merging",
							"No workflow chosen", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			if (checkInterrupted()) {
				return;
			}
			if (chosenDataflow != ImportWorkflowWizard.this.sourceDataflow) {
				updateWorkflowGraphic(sourcePreview, chosenDataflow);
				if (checkInterrupted()) {
					return;
				}
				ImportWorkflowWizard.this.sourceDataflow = chosenDataflow;
			}
		}

		private boolean checkInterrupted() {
			if (Thread.interrupted() || this.shouldStop) {
				// ImportWorkflowWizard.this.chosenDataflow = null;
				return true;
			}
			return false;
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

	private final class UpdateChosenListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Component parentComponent;
			if (e.getSource() instanceof Component) {
				parentComponent = (Component) e.getSource();
			} else {
				parentComponent = null;
			}
			findChosenDataflow(parentComponent, true);
		}
	}

}
