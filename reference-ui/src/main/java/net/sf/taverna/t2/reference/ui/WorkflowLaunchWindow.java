/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.reference.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import net.sf.taverna.t2.annotation.annotationbeans.Author;
import net.sf.taverna.t2.annotation.annotationbeans.DescriptiveTitle;
import net.sf.taverna.t2.annotation.annotationbeans.ExampleValue;
import net.sf.taverna.t2.annotation.annotationbeans.FreeTextDescription;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.impl.InvocationContextImpl;
import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactoryRegistry;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.ui.referenceactions.ReferenceActionSPI;
import net.sf.taverna.t2.reference.ui.referenceactions.ReferenceActionsSPIRegistry;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.ui.SwingWorkerCompletionWaiter;
import net.sf.taverna.t2.workbench.views.graph.GraphViewComponent;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;
import net.sf.taverna.t2.workflowmodel.utils.PortComparator;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.log4j.Logger;
import org.w3c.dom.svg.SVGDocument;

/**
 * A simple workflow launch window, uses a tabbed layout to display a set of
 * named RegistrationPanel instances, and a 'run workflow' button. Also
 * shows a pane contining a picture of the workflow, the author and the description.
 * 
 * We use one WorkflowLaunchWindow per workflow, multiple runs of the same workflow get the
 * same window.
 * 
 * @author Tom Oinn
 * @author David Withers
 * @author Stian Soiland-Reyes
 * @author Alan R Williams
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public abstract class WorkflowLaunchWindow extends JFrame {

	private static Logger logger = Logger.getLogger(WorkflowLaunchWindow.class);

	private static final String LAUNCH_WORKFLOW = "Run workflow";

	private final ImageIcon launchIcon = new ImageIcon(getClass().getResource(
			"/icons/start_task.gif"));
	
	private static final ImageIcon addTextIcon = new ImageIcon(
			RegistrationPanel.class.getResource("/icons/addtext_co.gif"));


	// An action enabled when all inputs are enabled and used to trigger the
	// handleLaunch method
	private Action launchAction;

	// A map of input port names to input registration panels (from the previous run of the same workflow, if any)
	private Map<String, RegistrationPanel> inputPanelMap =  new HashMap<String, RegistrationPanel>();
	
	// A map of input port names to T2ReferenceS of the input values (the ones entered for the previous run of the same workflow)
	private final Map<String, T2Reference> inputMap = new HashMap<String, T2Reference>();
	
	// A pane holding various tabs for workflow input ports
	private JTabbedPane tabsPane;
	
	private WorkflowInstanceFacade facade;
	private ReferenceService referenceService;
	private InvocationContext invocationContext;
	
	// Original workflow (we create a copy of it when we actually push the 'Run' button)
	private Dataflow dataflowOriginal;
	
	private static final String NO_WORKFLOW_DESCRIPTION = "No description";
	private static final String NO_WORKFLOW_AUTHOR = "No author";

	private DialogTextArea workflowDescriptionArea;
	private DialogTextArea workflowAuthorArea;

	private AnnotationTools annotationTools = new AnnotationTools();
	private JSVGCanvas createWorkflowGraphic;
	
	// Whether the original workflow has been modified in the design perspective so we know to 
	// refresh this dialog
	private boolean workflowModified = false;

	// Observer of workflow closing events so we can dispose off the window
	private FileManager fileManager = FileManager.getInstance();
	private FileManagerObserver fileManagerObserver = new FileManagerObserver();

	private EditManager editManager = EditManager.getInstance();
	private EditManagerObserver editManagerObserver = new EditManagerObserver();

	private JPanel overallPanel;

	private JPanel workflowPart;

	private JPanel portsPart;

	public WorkflowLaunchWindow(Dataflow dataflowOriginal, ReferenceService refService) {
		super();
		
		this.dataflowOriginal = dataflowOriginal;
		this.referenceService = refService;
		
		initComponents();
		
		// Handle refreshing the frame when it receives focus
		this.addWindowFocusListener(new WindowAdapter() {
			
			@Override
			public void windowGainedFocus(WindowEvent e) {
				if (workflowModified){

					// Clear all previous components
					getContentPane().removeAll();
					
					// Redraw the window
					initComponents();

					overallPanel.revalidate();
					overallPanel.repaint();
					
					workflowModified = false;			
				}	
			}
		});
		
		// Handle window closing
		this.addWindowListener(new WindowAdapter(){
			
			@Override
		    public void windowClosing(WindowEvent winEvt) {
				handleCancel(); // do not dispose the window, just hide it
			}
		});

		// Start observing workflow closing events on File Manager
		fileManager.addObserver(fileManagerObserver);
		
		// Start observing edit workflow events on Edit Manager
		editManager.addObserver(editManagerObserver);

	}
	
	/**
	 * Set the title of the window to contain the workflow name and
	 * its file/url location so that users can easily identify which
	 * workflow is being run.
	 */
	private void setWindowTitle() {
		String title = annotationTools.getAnnotationString(dataflowOriginal, DescriptiveTitle.class, "");
		String windowTitle = "Input values for ";
		if ((title != null) && (!title.equals(""))) {
			windowTitle += "'" + title + "' ";
		}
		else{
			windowTitle += "'" + dataflowOriginal.getLocalName() + "' ";
		}
		
		Object workflowLocation = fileManager.getDataflowSource(dataflowOriginal);
		
		windowTitle += (workflowLocation == null)? "" : "from " + workflowLocation.toString();
	
		setTitle(windowTitle);
	}
	
	/**
	 * Draw the components of the frame.
	 */
	public void initComponents(){
			
		setWindowTitle();

		workflowPart = new JPanel(new GridLayout(3,1));
		portsPart = new JPanel(new BorderLayout());

		createWorkflowGraphic = createWorkflowGraphic(dataflowOriginal);
		createWorkflowGraphic.setBorder(new TitledBorder("Diagram"));
		
		workflowPart.add(new JScrollPane(createWorkflowGraphic));

		workflowDescriptionArea = new DialogTextArea(NO_WORKFLOW_DESCRIPTION, 5, 40);
		workflowDescriptionArea.setBorder(new TitledBorder("Workflow description"));
		workflowDescriptionArea.setEditable(false);
		workflowDescriptionArea.setLineWrap(true);
		workflowDescriptionArea.setWrapStyleWord(true);
		
		workflowPart.add(new JScrollPane(workflowDescriptionArea));

		workflowAuthorArea = new DialogTextArea(NO_WORKFLOW_AUTHOR, 1, 40);
		workflowAuthorArea.setBorder(new TitledBorder("Workflow author"));
		workflowAuthorArea.setEditable(false);
		workflowAuthorArea.setLineWrap(true);
		workflowAuthorArea.setWrapStyleWord(true);
		
		workflowPart.add(new JScrollPane(workflowAuthorArea));

		launchAction = new AbstractAction(LAUNCH_WORKFLOW, launchIcon) {
			public void actionPerformed(ActionEvent ae) {
				
				// First of all - is the workflow valid?				
				if (! CheckWorkflowStatus.checkWorkflow(dataflowOriginal)) {
					setVisible(false);
					return;
				}
				
				// Check if user had entered input values for all input ports -
				// otherwise there is no point in attempting to run the workflow
				for (String input : inputMap.keySet()) {
					RegistrationPanel registrationPanel = inputPanelMap.get(input);
					Object userInput = registrationPanel.getUserInput();
					if (userInput instanceof RuntimeException){
						JOptionPane.showMessageDialog(null, "You have not provided input values for all workflow inputs", "Workflow input value error", JOptionPane.ERROR_MESSAGE);
						// exit
						return;
					}
				}			
				setVisible(false);
				
				// Make a copy of the workflow to run so user can still
				// modify the original workflow
				Dataflow dataflowCopy = null;

				// CopyWorkflowSwingWorker will make a copy of the workflow and pop up a
				// modal dialog that will block the GUI while CopyWorkflowSwingWorker is 
				// doing it to let the user know that something is being done. Blocking 
				// of the GUI is needed here so that the user cannot modify the original 
				// workflow while it is being copied.
				CopyWorkflowSwingWorker copyWorkflowSwingWorker = new CopyWorkflowSwingWorker(dataflowOriginal);
				CopyWorkflowInProgressDialog dialog = new CopyWorkflowInProgressDialog();
				copyWorkflowSwingWorker.addPropertyChangeListener(
					     new SwingWorkerCompletionWaiter(dialog));
				copyWorkflowSwingWorker.execute();
				
				// Give a chance to the SwingWorker to finish so we do not have to display 
				// the dialog if copying of the workflow is quick (so it won't flicker on the screen)
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// do nothing
				}
				if (!copyWorkflowSwingWorker.isDone()){
					dialog.setVisible(true); // this will block the GUI
				}
				boolean userCancelled = dialog.hasUserCancelled(); // see if user cancelled the dialog

				if (userCancelled){
					// Stop the CopyWorkflowSwingWorker if it is still working
					copyWorkflowSwingWorker.cancel(true);
					// exit
					return;
				}
				else{ 
					// Get the workflow copy from the CopyWorkflowSwingWorker
					try {
						dataflowCopy = copyWorkflowSwingWorker.get();
					} catch (Exception e) {
						dataflowCopy = null;
						logger.error("Failed to get the workflow copy", e);
					}
					
					if (dataflowCopy == null) {
						InvalidDataflowReport.showErrorDialog(
								"Unable to make a copy of the workflow to run",
								"Workflow copy failed");
						dispose(); // close the workflow launch window
					}
					else {

						// Create provenance connector and facade, similar as in
						// RunWorkflowAction

						// TODO check if the database has been created and create if needed
						// if provenance turned on then add an IntermediateProvLayer to each
						// Processor
						ProvenanceConnector provenanceConnector = null;
					
						// FIXME: All these run-stuff should be done in a general way so it
						// could also be used when running workflows non-interactively
						if (DataManagementConfiguration.getInstance().isProvenanceEnabled()) {
							String connectorType = DataManagementConfiguration
									.getInstance().getConnectorType();

							for (ProvenanceConnectorFactory factory : ProvenanceConnectorFactoryRegistry
									.getInstance().getInstances()) {
								if (connectorType.equalsIgnoreCase(factory
										.getConnectorType())) {
									provenanceConnector = factory
											.getProvenanceConnector();
								}
							}

							// slight change, the init is outside but it also
							// means that the
							// init call has to ensure that the dbURL is set
							// correctly
							try {
								if (provenanceConnector != null) {
									provenanceConnector.init();
									provenanceConnector
											.setReferenceService(referenceService);
								}
							} catch (Exception except) {

							}			
						}
						invocationContext = new InvocationContextImpl(
								referenceService, provenanceConnector);
						if (provenanceConnector != null) {
							provenanceConnector
									.setInvocationContext(invocationContext);
						}

						// Workflow run id will be set on the invocation context
						// from the facade
						try {
							facade = new EditsImpl()
									.createWorkflowInstanceFacade(dataflowCopy,
											invocationContext, "");
						} catch (InvalidDataflowException ex) {
							InvalidDataflowReport.invalidDataflow(ex
									.getDataflowValidationReport());
							return;
						}

						try {
							registerInputs();
							handleLaunch(inputMap);
						} catch (Exception e) {
							logger.error(e);
							JOptionPane.showMessageDialog(null, "Unable to start workflow run", "Workflow run error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		};

		String wfDescription = annotationTools.getAnnotationString(dataflowOriginal, FreeTextDescription.class, "");
		setWorkflowDescription(wfDescription);

		String wfAuthor = annotationTools.getAnnotationString(dataflowOriginal, Author.class, "");
		setWorkflowAuthor(wfAuthor);

		Action useExamplesAction = new AbstractAction ("Use examples", addTextIcon) {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                List<DataflowInputPort> inputPorts = new ArrayList<DataflowInputPort>(
                        dataflowOriginal.getInputPorts());
                // Create tabs for input ports (but only for the one that are connected!)
                for (DataflowInputPort inputPort : inputPorts) {
                    RegistrationPanel rp = inputPanelMap.get(inputPort.getName());
                    Object example = rp.getExample();
                    if ((example != null) && (inputPort.getDepth() == 0) && (rp.getValue() == null)) {
                        rp.setValue(example);
                    }
                }
            }};

        JButton useExamplesButton = new JButton(useExamplesAction);
        useExamplesButton.setToolTipText("Use the example value (if any) for ports that you have not set a value for");
		// Construct tool bar
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(useExamplesButton);
		toolBar.add(new JButton(launchAction));
		toolBar.add(new JButton(new AbstractAction("Cancel", WorkbenchIcons.closeIcon) {

			public void actionPerformed(ActionEvent e) {
				handleCancel();
			}}));
		
		JToolBar loadButtonsBar = new JToolBar();
		loadButtonsBar.setFloatable(false);
		ReferenceActionsSPIRegistry spiRegistry = ReferenceActionsSPIRegistry.getInstance();
		for (ReferenceActionSPI spi : spiRegistry.getInstances()) {
			ReferenceActionSPI action = (ReferenceActionSPI) spi.getAction();
			action.setInputPanelMap(inputPanelMap);
			JButton loadButton = new JButton((AbstractAction) action);
			loadButtonsBar.add(loadButton);
		}
		
		
		JPanel toolBarPanel = new JPanel(new BorderLayout());
		toolBarPanel.add(loadButtonsBar, BorderLayout.WEST);
		toolBarPanel.add(toolBar, BorderLayout.EAST);
		toolBarPanel.setBorder(new EmptyBorder(5, 10, 5, 20));
		portsPart.add(toolBarPanel, BorderLayout.SOUTH);
		
		// Construct tab container - tabs will be populated based on the wf input ports
		tabsPane = new JTabbedPane();
		
		List<DataflowInputPort> inputPorts = new ArrayList<DataflowInputPort>(
				dataflowOriginal.getInputPorts());
		Collections.sort(inputPorts, new PortComparator());
		
		Set<String> inputNames = new HashSet<String>();
		
		// Create tabs for input ports (but only for the one that are connected!)
		for (DataflowInputPort inputPort : inputPorts) {

			// Is this input port connected to anything?
			if (dataflowOriginal.isInputPortConnected(inputPort)){			
				String portDescription = annotationTools.getAnnotationString(
						inputPort, FreeTextDescription.class, null);
				String portExample = annotationTools.getAnnotationString(inputPort,
						ExampleValue.class, null);
				// add tabs for wf input ports
				String name = inputPort.getName();
				inputNames.add(name);
				addInput(name, inputPort.getDepth(), portDescription,
						portExample);
			}
		}
		
		// This is needed to ensure that deleted ports are removed
		Set<String> toRemove = new HashSet<String>();
		for (String n : inputMap.keySet()) {
			if (!inputNames.contains(n)) {
				toRemove.add(n);
			}
		}
		for (String n : toRemove) {
			inputMap.remove(n);
		}
		
		portsPart.add(tabsPane, BorderLayout.CENTER);
		
		workflowPart.setPreferredSize(new Dimension(300,500));
		portsPart.setPreferredSize(new Dimension(650,500));
		
		overallPanel = new JPanel();
		overallPanel.setLayout(new BoxLayout(overallPanel, BoxLayout.X_AXIS));
		

		overallPanel.add(workflowPart);
		overallPanel.add(portsPart);

		setLayout(new BorderLayout());
		getContentPane().add(new JScrollPane(overallPanel), BorderLayout.CENTER);
		
		pack();		
	}

	/**
	 * User clicked the cancel button.
	 */
	public void cancelPressed(){
		this.setVisible(true);
	}

	/**
	 * Creates an SVGCanvas loaded with the SVGDocument for the Dataflow.
	 * 
	 * @param dataflow
	 * @return
	 */
	private JSVGCanvas createWorkflowGraphic(Dataflow dataflow) {
		JSVGCanvas svgCanvas = new JSVGCanvas();
		SVGGraphController graphController = GraphViewComponent.graphControllerMap
				.get(dataflow);
		if (graphController != null) {
			SVGDocument svgDoc = graphController.getSVGDocument();
			svgCanvas.setDocument((SVGDocument) svgDoc.cloneNode(true));
		}
		return svgCanvas;
	}

	public synchronized void addInput(final String inputName,
			final int inputDepth) {
		addInput(inputName, inputDepth, null, null);
	}

	public void addInput(final String inputName, final int inputDepth,
			String inputDescription, String inputExample) {

		// Don't do anything if we already have the input registration panel for this input port
		RegistrationPanel inputRegistrationPanel = inputPanelMap.get(inputName);
		if ((inputRegistrationPanel == null) || (inputRegistrationPanel.getDepth() != inputDepth)) {
			inputRegistrationPanel = new RegistrationPanel(inputDepth, inputName,
					inputDescription, inputExample);
			inputPanelMap.put(inputName, inputRegistrationPanel);
		} else {
			inputRegistrationPanel.setStatus(
					"Drag to re-arrange, or drag files, URLs, or text to add",
					null);
			inputRegistrationPanel.setDescription(inputDescription);
			inputRegistrationPanel.setExample(inputExample);
		}
		inputMap.put(inputName, null);
		tabsPane.addTab(inputName, inputRegistrationPanel);
		tabsPane.revalidate();
		tabsPane.repaint();
	}

	public synchronized void removeInputTab(final String inputName) {
		// Only do something if we have a registration panel for this input port to begin with
		if (!inputMap.containsKey(inputName)) {
			return;
		} else {
			RegistrationPanel inputRegistrationPanelToRemove = inputPanelMap.remove(inputName);
			inputMap.remove(inputName);
			tabsPane.remove(inputRegistrationPanelToRemove);
		}
	}

	private void registerInputs() {
		
		for (String input : inputMap.keySet()) {
			RegistrationPanel registrationPanel = inputPanelMap.get(input);
			Object userInput = registrationPanel.getUserInput();
			int inputDepth = registrationPanel.getDepth();

			T2Reference reference = referenceService.register(userInput,
					inputDepth, true, invocationContext);
			inputMap.put(input, reference);
		}
	}

	/**
	 * Called when the run workflow action has been performed
	 * 
	 * @param workflowInputs
	 *            a map of named inputs in the form of T2Reference instances
	 */
	public abstract void handleLaunch(Map<String, T2Reference> workflowInputs);
	
	public abstract void handleCancel();
	
	private static void selectTopOfTextArea(DialogTextArea textArea) {
		textArea.setSelectionStart(0);
		textArea.setSelectionEnd(0);
	}

	public void setWorkflowDescription(String workflowDescription) {
		if ((workflowDescription != null) && (workflowDescription.length() > 0)) {
			this.workflowDescriptionArea
					.setText(workflowDescription);
			selectTopOfTextArea(this.workflowDescriptionArea);
		}
	}

	void setWorkflowAuthor(String workflowAuthor) {
		if ((workflowAuthor != null) && (workflowAuthor.length() > 0)) {
			this.workflowAuthorArea.setText(workflowAuthor);
			selectTopOfTextArea(this.workflowAuthorArea);
		}
	}

	public String getWorkflowDescription() {
		return workflowDescriptionArea.getText();
	}

	@Override
	protected void finalize() throws Throwable {
		createWorkflowGraphic.stopProcessing();
		super.finalize();
	}

	public WorkflowInstanceFacade getFacade() {
		return facade;
	}

	public void setReferenceService(ReferenceService refService) {
		this.referenceService = refService;
	}

	public class FileManagerObserver implements Observer<FileManagerEvent> {
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			if (message instanceof ClosedDataflowEvent
				&& ((ClosedDataflowEvent) message).getDataflow() == dataflowOriginal) {	
				// Remove listeners of various events
				editManager.removeObserver(editManagerObserver);
				fileManager.removeObserver(fileManagerObserver);
				setVisible(false);
				dispose(); // dispose off this window if the original workflow has been closed
			}
		}
	}
	
	public class EditManagerObserver implements Observer<EditManagerEvent> {
		public void notify(Observable<EditManagerEvent> sender,
				final EditManagerEvent message) throws Exception {
			
			if (message instanceof AbstractDataflowEditEvent
					&& ((AbstractDataflowEditEvent) message).getDataFlow() == dataflowOriginal) {
				workflowModified = true;
			}
		}
	}
	
	public void showFrame() {
		setVisible(true);
		toFront();
	      requestFocus();
//	      setAlwaysOnTop(true);
	      repaint();
	}

	public void hideFrame() {
		setVisible(false);

	      setAlwaysOnTop(false);
	}

}

