package net.sf.taverna.t2.workbench.file.importworkflow.gui;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
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
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.log4j.Logger;
import org.w3c.dom.svg.SVGDocument;

public class ImportWorkflowWizard extends JDialog {
	private static final long serialVersionUID = -8124860319858897065L;
	protected static FileManager fileManager = FileManager.getInstance();
	protected static Logger logger = Logger.getLogger(ImportWorkflowWizard.class);
	protected BrowseFileOnClick browseFileOnClick = new BrowseFileOnClick();
	protected JButton buttonBrowse;
	protected JComboBox chooseDataflow;
	protected Thread dataflowOpenerThread;

	protected Dataflow destinationDataflow;
	protected JTextField fieldFile;

	protected JTextField fieldUrl;
	protected boolean mergeEnabled = true;
	protected boolean nestedEnabled = true;
	protected JSVGCanvas previewSource = new JSVGCanvas();
	protected JSVGCanvas previewDestination = new JSVGCanvas();
	protected JTextField prefixField;
	protected JRadioButton radioFile;
	protected JRadioButton radioNew;
	protected JRadioButton radioOpened;
	protected JRadioButton radioUrl;
	protected Dataflow sourceDataflow;
	protected ButtonGroup sourceSelection;
	protected ActionListener updateChosenListener = new UpdateChosenListener();
	protected Thread updatePreviewsThread;
	protected Component sourceSelectionPanel;
	protected JLabel prefixLabel;
	protected JLabel prefixHelp;
	protected JPanel destinationSelectionPanel;
	protected ButtonGroup destinationSelection;
	protected JRadioButton radioNewDestination;
	protected JRadioButton radioOpenDestination;
	protected JComboBox destinationAlreadyOpen;
	protected JPanel introductionPanel;
	protected ButtonGroup actionSelection;
	protected JRadioButton actionNested;
	protected JRadioButton actionMerge;
	protected JRadioButton radioCustomSource;
	protected JRadioButton radioCustomDestination;

	public ImportWorkflowWizard(Frame parentFrame) {
		super(parentFrame, "Import workflow", true);

		setSize(600, 600);
		add(makeContentPane(), BorderLayout.CENTER);
		// Add some space
		add(new JPanel(), BorderLayout.WEST);
		add(new JPanel(), BorderLayout.NORTH);
		add(new JPanel(), BorderLayout.SOUTH);
		add(new JPanel(), BorderLayout.EAST);
		findChosenDataflow(this, true);
		updateAll();
	}

	public void setMergeEnabled(boolean importEnabled) {
		this.mergeEnabled = importEnabled;
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
	public static void invokeAndWait(Runnable runnable) {
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

	protected Component makeWorkflowImage() {
		JPanel workflowImages = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.1;

		gbc.weightx = 0.1;
		workflowImages.add(new JPanel(), gbc);// filler

		gbc.weightx = 0.0;
		previewSource.setBackground(workflowImages.getBackground());
		workflowImages.add(previewSource, gbc);

		JLabel arrow = new JLabel("\u2192");
		arrow.setFont(arrow.getFont().deriveFont(48f));
		workflowImages.add(arrow, gbc);

		previewDestination.setBackground(workflowImages.getBackground());
		workflowImages.add(previewDestination, gbc);

		gbc.weightx = 0.1;
		workflowImages.add(new JPanel(), gbc);
		gbc.weightx = 0.0;

		return workflowImages;
	}

	protected void updateAll() {
		updatePreviews(); // will go in separate thread anyway, do it first
		updateHeader();
		updateSourceSection();
		updateDestinationSection();
		updateFooter();
	}

	protected void updateDestinationSection() {
		
		radioNewDestination.setVisible(false);

		radioCustomDestination.setText(customDestinationName);
		radioCustomDestination.setVisible(customDestinationDataflow != null);

		// radioNewDestination.setVisible(nestedEnabled);
		// radioNewDestination.setEnabled(actionNested.isSelected());
		
		destinationSelectionPanel.setVisible(destinationEnabled);
		
	}

	protected synchronized void updatePreviews() {
		if (updatePreviewsThread != null && updatePreviewsThread.isAlive()) {
			updatePreviewsThread.interrupt();
		}
		updatePreviewsThread = new UpdatePreviewsThread();
		updatePreviewsThread.start();
	}

	protected void updateDestinationPreview() {
		updateWorkflowGraphic(previewDestination, destinationDataflow);
	}

	protected void updateSourcePreview() {
		updateWorkflowGraphic(previewSource, sourceDataflow);
	}

	protected void updateFooter() {
		prefixField.setVisible(mergeEnabled);
		prefixLabel.setVisible(mergeEnabled);
		prefixHelp.setVisible(mergeEnabled);

		prefixField.setEnabled(actionMerge.isSelected());
		prefixLabel.setEnabled(actionMerge.isSelected());
		prefixHelp.setEnabled(actionMerge.isSelected());
		if (actionMerge.isSelected()) {
			prefixHelp.setForeground(prefixLabel.getForeground());
		} else {
			// Work around
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4303706
			// and assume gray is the 'disabled' colour in our Look n Feel
			prefixHelp.setForeground(Color.gray);
		}

	}

	protected void updateHeader() {
		makeIntroductionPanel();
	}

	protected void updateSourceSection() {
		radioCustomSource.setText(customSourceName);
		radioCustomSource.setVisible(customSourceDataFlow != null);

		radioNew.setVisible(nestedEnabled);
		radioNew.setEnabled(actionNested.isSelected());
		
		sourceSelectionPanel.setVisible(sourceEnabled);
	}

	/**
	 * Create a PNG image of the workflow and place inside an ImageIcon
	 * 
	 * @param dataflow
	 * @return
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	protected void updateWorkflowGraphic(final JSVGCanvas svgCanvas,
			Dataflow dataflow) {
		invokeAndWait(new Runnable() {
			public void run() {
				// Set it to blank while reloading
				svgCanvas.setSVGDocument(null);
			}
		});
		if (dataflow == null) {
			// Already set to blank
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
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.BOTH;

		introductionPanel = makeIntroductionPanel();
		panel.add(introductionPanel, gbc);

		gbc.gridy = 1;
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

	protected JPanel makeIntroductionPanel() {
		if (introductionPanel == null) {
			introductionPanel = new JPanel(new GridBagLayout());
		} else {
			introductionPanel.removeAll();
		}
		boolean bothEnabled = mergeEnabled && nestedEnabled;
		if (bothEnabled) {
			introductionPanel.setBorder(BorderFactory
					.createTitledBorder("Import method"));
		} else {
			introductionPanel.setBorder(BorderFactory.createEmptyBorder());
		}
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		// gbc.gridy = 0;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		StringBuilder nestedHelp = new StringBuilder();
		nestedHelp.append("<html><small>");
		nestedHelp.append("Add a <strong>nested workflow</strong> ");
		nestedHelp.append("into the ");
		nestedHelp.append("destination workflow as a single service. ");
		nestedHelp.append("The nested workflow ");
		nestedHelp.append("can be <em>edited separately</em>, but is shown ");
		nestedHelp.append("expanded in the diagram of the parent  ");
		nestedHelp.append("workflow. In the parent workflow you can ");
		nestedHelp
				.append("connect to the input and output ports of the nested ");
		nestedHelp.append("workflow. ");
		nestedHelp.append("</small></html>");

		StringBuilder mergeHelp = new StringBuilder();
		mergeHelp.append("<html><small>");
		mergeHelp.append("<strong>Merge</strong> a workflow ");
		mergeHelp.append("by copying all services, ports and links ");
		mergeHelp
				.append("directly into the destination workflow. This can be  ");
		mergeHelp.append("useful for merging smaller workflow fragments. For ");
		mergeHelp.append("inclusion of larger workflows you might find using ");
		mergeHelp.append("<em>nested workflows</em> more beneficial.");
		mergeHelp.append("</small></html>");

		actionSelection = new ButtonGroup();
		actionNested = new JRadioButton(nestedHelp.toString());
		ActionListener updateListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateSourceSection();
				updateDestinationSection();
				updateFooter();
			}
		};
		actionNested.addActionListener(updateListener);
		actionSelection.add(actionNested);

		actionMerge = new JRadioButton(mergeHelp.toString());
		actionMerge.addActionListener(updateListener);
		actionSelection.add(actionMerge);

		if (bothEnabled) {
			introductionPanel.add(actionNested, gbc);
			introductionPanel.add(actionMerge, gbc);
			actionNested.setSelected(true);
		} else if (nestedEnabled) {
			introductionPanel.add(new JLabel(nestedHelp.toString()), gbc);
			actionNested.setSelected(true);
		} else if (mergeEnabled) {
			introductionPanel.add(new JLabel(mergeHelp.toString()), gbc);
			actionMerge.setSelected(true);
		}
		return introductionPanel;
	}

	protected JPanel makeDestinationSelectionPanel() {
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
		destinationAlreadyOpen = makeSelectOpenWorkflowComboBox(true);
		j.add(destinationAlreadyOpen, gbc);

		radioCustomDestination = new JRadioButton(customDestinationName);
		radioCustomDestination.setVisible(customDestinationName != null);
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		j.add(radioCustomDestination, gbc);
		destinationSelection.add(radioCustomDestination);
		radioCustomDestination.addActionListener(updateChosenListener);
		gbc.gridwidth = 1;

		radioOpenDestination.setSelected(true);
		return j;
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
				"<html><small>Optional prefix to be prepended to the name of the "
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
		JButton comp = new JButton(new ImportWorkflowAction());
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
				if (!radioFile.isSelected()) {
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

	protected JComboBox makeSelectOpenWorkflowComboBox(boolean selectCurrent) {
		List<DataflowSelection> openDataflows = new ArrayList<DataflowSelection>();
		DataflowSelection current = null;
		for (Dataflow df : fileManager.getOpenDataflows()) {
			Object source = fileManager.getDataflowSource(df);
			String name;
			if (source != null) {
				name = source.toString();
			} else {
				name = df.getLocalName();
			}
			boolean isCurrent = df.equals(fileManager.getCurrentDataflow());
			if (isCurrent) {
				name = "<html><body>" + name
						+ " <i>(current)</i></body></html>";
			}
			DataflowSelection selection = new DataflowSelection(df, name);
			openDataflows.add(selection);
			if (isCurrent) {
				current = selection;
			}
		}
		JComboBox chooseDataflow = new JComboBox(openDataflows.toArray());
		if (selectCurrent) {
			chooseDataflow.setSelectedItem(current);
		}
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

		radioCustomSource = new JRadioButton(customSourceName);
		radioCustomSource.setVisible(customSourceDataFlow != null);
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		j.add(radioCustomSource, gbc);
		sourceSelection.add(radioCustomSource);
		radioCustomSource.addActionListener(updateChosenListener);
		gbc.gridwidth = 1;

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
		chooseDataflow = makeSelectOpenWorkflowComboBox(false);
		chooseDataflow.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				radioOpened.setSelected(true);
			}
		});
		j.add(chooseDataflow, gbc);

		return j;
	}

	private EditManager editManager = EditManager.getInstance();

	private Edits edits = editManager.getEdits();
	private Dataflow customSourceDataFlow = null;
	private Dataflow customDestinationDataflow = null;
	private String customSourceName = "";
	private String customDestinationName = "";

	private boolean sourceEnabled = true;
	private boolean destinationEnabled = true;

	protected Edit<?> makeInsertNestedWorkflowEdit(Dataflow nestedFlow, String name) {
		List<Edit<?>> editList = new ArrayList<Edit<?>>();
		Activity<Dataflow> activity = new DataflowActivity();
		editList.add(edits.getConfigureActivityEdit(activity, nestedFlow));
		Processor p = edits.createProcessor(name);
		editList.add(edits.getDefaultDispatchStackEdit(p));
		editList.add(edits.getAddActivityEdit(p, activity));
		editList.add(edits.getAddProcessorEdit(destinationDataflow, p));
		CompoundEdit edit = new CompoundEdit(editList);
		return edit;
	}
	
	protected class ImportWorkflowAction extends AbstractAction implements
			Runnable {
		private Component parentComponent;
		private ProgressMonitor progressMonitor;

		protected ImportWorkflowAction() {
			super("Import workflow");
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof Component) {
				parentComponent = (Component) e.getSource();
			} else {
				parentComponent = null;
			}
			Thread t = new Thread(this, "Import workflow");
			progressMonitor = new ProgressMonitor(parentComponent,
					"Importing workflow", "", 0, 100);
			progressMonitor.setMillisToDecideToPopup(200);
			progressMonitor.setProgress(5);
			t.start();
			setVisible(false);
		}

		protected void nested() {
			if (progressMonitor.isCanceled()) {
				return;
			}
			progressMonitor.setProgress(15);
			if (!fileManager.isDataflowOpen(destinationDataflow)) {
				fileManager.openDataflow(destinationDataflow);
			} else {
				fileManager.setCurrentDataflow(destinationDataflow);
			}
			if (progressMonitor.isCanceled()) {
				return;
			}

			progressMonitor.setNote("Copying source workflow");
			Dataflow nestedFlow;
			try {
				nestedFlow = DataflowMerger.copyWorkflow(sourceDataflow);
			} catch (Exception ex) {
				logger.warn("Could not copy nested workflow", ex);
				progressMonitor.setProgress(100);
				JOptionPane.showMessageDialog(parentComponent,
						"An error occured while copying workflow:\n"
								+ ex.getLocalizedMessage(),
						"Could not copy nested workflow",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (progressMonitor.isCanceled()) {
				return;
			}

			progressMonitor.setNote("Creating nested workflow");
			progressMonitor.setProgress(45);

			String name = Tools.uniqueProcessorName(nestedFlow.getLocalName(),
					destinationDataflow);

			Edit<?> edit = makeInsertNestedWorkflowEdit(nestedFlow, name);
			if (progressMonitor.isCanceled()) {
				return;
			}

			progressMonitor.setNote("Inserting nested workflow");
			progressMonitor.setProgress(65);
			
			try {
				editManager.doDataflowEdit(destinationDataflow, edit);
			} catch (EditException e) {
				progressMonitor.setProgress(100);
				logger.warn("Could not import nested workflow", e);
				JOptionPane.showMessageDialog(parentComponent,
						"An error occured while importing workflow:\n"
								+ e.getLocalizedMessage(),
						"Could not import workflows",
						JOptionPane.WARNING_MESSAGE);
			}
			progressMonitor.setProgress(100);
		}

	

		protected void merge() {
			progressMonitor.setProgress(10);
			DataflowMerger merger = new DataflowMerger(destinationDataflow);
			progressMonitor.setProgress(25);
			progressMonitor.setNote("Planning workflow merging");

			String prefix = prefixField.getText();
			if (!prefix.equals("")) {
				if (!prefix.matches("[_.]$")) {
					prefix = prefix + "_";
				}
				if (!prefix.matches("[\\p{L}\\p{Digit}_.]+")) {
					progressMonitor.setProgress(100);
					ImportWorkflowWizard.this.setVisible(true);
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
				progressMonitor.setProgress(100);
				logger.warn("Could not merge workflow", e1);
				JOptionPane.showMessageDialog(parentComponent,
						"An error occured while merging workflows:\n"
								+ e1.getLocalizedMessage(),
						"Could not merge workflows",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			progressMonitor.setProgress(55);
			if (!fileManager.isDataflowOpen(destinationDataflow)) {
				fileManager.openDataflow(destinationDataflow);
			} else {
				fileManager.setCurrentDataflow(destinationDataflow);
			}

			progressMonitor.setNote("Merging workflows");
			progressMonitor.setProgress(75);

			if (progressMonitor.isCanceled()) {
				return;
			}

			try {
				editManager.doDataflowEdit(destinationDataflow, mergeEdit);
			} catch (EditException e1) {
				progressMonitor.setProgress(100);
				JOptionPane.showMessageDialog(parentComponent,
						"An error occured while merging workflows:\n"
								+ e1.getLocalizedMessage(),
						"Could not merge workflows",
						JOptionPane.WARNING_MESSAGE);
				return;
			}			
			progressMonitor.setProgress(100);

		}

		public void run() {
			findChosenDataflow(parentComponent, false);
			if (actionMerge.isSelected()) {
				merge();
			} else if (actionNested.isSelected()) {
				nested();
			}
		}
	}

	protected class UpdatePreviewsThread extends Thread {
		protected UpdatePreviewsThread() {
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

	protected class BrowseFileOnClick implements ActionListener {
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

	protected class DataflowOpenerThread extends Thread {
		private final boolean background;
		private final Component parentComponent;
		private boolean shouldStop = false;
		private boolean shownWarning = false;

		protected DataflowOpenerThread(Component parentComponent,
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
			updateSource();
			updateDestination();
		}

		public void updateDestination() {
			ButtonModel selection = destinationSelection.getSelection();
			Dataflow chosenDataflow = null;
			if (selection == null) {
				chosenDataflow = null;
			} else if (selection.equals(radioNewDestination.getModel())) {
				chosenDataflow = EditManager.getInstance().getEdits()
						.createDataflow();
			} else if (selection.equals(radioOpenDestination.getModel())) {
				DataflowSelection chosen = (DataflowSelection) destinationAlreadyOpen
						.getSelectedItem();
				chosenDataflow = chosen.getDataflow();
			} else if (selection.equals(radioCustomDestination.getModel())) {
				chosenDataflow = customDestinationDataflow;
			} else {
				logger.error("Unknown selection " + selection);
			}

			if (chosenDataflow == null) {
				if (!background) {
					setVisible(true);
					JOptionPane.showMessageDialog(parentComponent,
							"You need to choose a destination workflow",
							"No destination workflow chosen",
							JOptionPane.ERROR_MESSAGE);
					shownWarning = true;
					return;
				}
			}
			if (checkInterrupted()) {
				return;
			}
			if (chosenDataflow != ImportWorkflowWizard.this.destinationDataflow) {
				updateWorkflowGraphic(previewDestination, chosenDataflow);
				if (checkInterrupted()) {
					return;
				}
				ImportWorkflowWizard.this.destinationDataflow = chosenDataflow;
			}

		}

		public void updateSource() {
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
					if (!background && !shownWarning) {
						setVisible(true);
						radioFile.requestFocus();
						logger.warn("Could not open workflow for merging: "
								+ filePath, e1);
						JOptionPane.showMessageDialog(parentComponent,
								"An error occured while trying to open "
										+ filePath + "\n" + e1.getMessage(),
								"Could not open workflow",
								JOptionPane.WARNING_MESSAGE);
						shownWarning = true;
					}
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
					if (!background && !shownWarning) {
						logger.warn("Could not open source workflow: " + url,
								e1);
						setVisible(true);
						fieldUrl.requestFocus();
						JOptionPane.showMessageDialog(parentComponent,
								"An error occured while trying to open " + url
										+ "\n" + e1.getMessage(),
								"Could not open workflow",
								JOptionPane.WARNING_MESSAGE);
						shownWarning = true;

					}
					if (checkInterrupted()) {
						return;
					}
				} catch (MalformedURLException e1) {
					if (!background && !shownWarning) {
						logger.warn("Invalid workflow URL: " + url, e1);
						setVisible(true);
						fieldUrl.requestFocus();
						JOptionPane.showMessageDialog(parentComponent,
								"The workflow location " + url
										+ " is invalid\n"
										+ e1.getLocalizedMessage(),
								"Invalid URL", JOptionPane.ERROR_MESSAGE);
						shownWarning = true;
					}
					if (checkInterrupted()) {
						return;
					}
				}
			} else if (selection.equals(radioOpened.getModel())) {
				DataflowSelection chosen = (DataflowSelection) chooseDataflow
						.getSelectedItem();
				chosenDataflow = chosen.getDataflow();
			} else if (selection.equals(radioCustomSource.getModel())) {
				chosenDataflow = customSourceDataFlow;
			} else {
				logger.error("Unknown selection " + selection);
			}
			if (checkInterrupted()) {
				return;
			}
			if (chosenDataflow != ImportWorkflowWizard.this.sourceDataflow) {
				updateWorkflowGraphic(previewSource, chosenDataflow);
				if (checkInterrupted()) {
					return;
				}
				ImportWorkflowWizard.this.sourceDataflow = chosenDataflow;
			}
			if (chosenDataflow == null) {
				if (!background && !shownWarning) {
					setVisible(true);
					JOptionPane.showMessageDialog(parentComponent,
							"You need to choose a workflow for merging",
							"No workflow chosen", JOptionPane.ERROR_MESSAGE);
					shownWarning = true;
				}
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

	public static class DataflowSelection {
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

	protected class UpdateChosenListener implements ActionListener {
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

	public void setCustomSourceDataflow(Dataflow sourceDataflow, String label) {
		this.customSourceDataFlow = sourceDataflow;
		this.customSourceName = label;
		updateSourceSection();
		radioCustomSource.setSelected(true);
	}

	public void setCustomDestinationDataflow(Dataflow destinationDataflow,
			String label) {
		this.customDestinationDataflow = destinationDataflow;
		this.customDestinationName = label;
		updateDestinationSection();
		radioCustomDestination.setSelected(true);
	}

	public void setDestinationEnabled(boolean destinationEnabled) {
		this.destinationEnabled = destinationEnabled;
		updateDestinationSection();
	}
	public void setSourceEnabled(boolean sourceEnabled) {
		this.sourceEnabled = sourceEnabled;
		updateSourceSection();
	}
}
