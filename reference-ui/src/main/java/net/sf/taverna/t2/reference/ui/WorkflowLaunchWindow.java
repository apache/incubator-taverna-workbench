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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
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
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.reference.ui.referenceactions.ReferenceActionSPI;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.ui.Workbench;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RIOT;
import org.apache.log4j.Logger;
import org.purl.wf4ever.robundle.Bundle;

import uk.org.taverna.configuration.database.DatabaseConfiguration;
import uk.org.taverna.databundle.DataBundles;
import uk.org.taverna.scufl2.api.annotation.Annotation;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.common.URITools;
import uk.org.taverna.scufl2.api.common.WorkflowBean;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.graph.GraphFactory;

/**
 * A simple workflow launch window, uses a tabbed layout to display a set of named RegistrationPanel
 * instances, and a 'run workflow' button. Also shows a pane contining a picture of the workflow,
 * the author and the description.
 * We use one WorkflowLaunchWindow per workflow, multiple runs of the same workflow get the same
 * window.
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

	private static final ImageIcon launchIcon = new ImageIcon(
			WorkflowLaunchWindow.class.getResource("/icons/start_task.gif"));

	private static final ImageIcon addTextIcon = new ImageIcon(
			WorkflowLaunchWindow.class.getResource("/icons/addtext_co.gif"));

	// An action enabled when all inputs are enabled and used to trigger the
	// handleLaunch method
	private Action launchAction;

	// A map of input port names to input registration panels (from the previous run of the same
	// workflow, if any)
	private Map<String, RegistrationPanel> inputPanelMap = new HashMap<String, RegistrationPanel>();

	// A pane holding various tabs for workflow input ports
	private JTabbedPane tabsPane;

	private Workflow workflow;

	private static final String NO_WORKFLOW_DESCRIPTION = "No description";
	private static final String NO_WORKFLOW_AUTHOR = "No author";

	private DialogTextArea workflowDescriptionArea;
	private DialogTextArea workflowAuthorArea;

	private JSVGCanvas createWorkflowGraphic;

	// Whether the original workflow has been modified in the design perspective so we know to
	// refresh this dialog
	private boolean workflowModified = false;

	// Observer of workflow closing events so we can dispose off the window
	private FileManager fileManager;
	private FileManagerObserver fileManagerObserver = new FileManagerObserver();

	private EditManager editManager;
	private EditManagerObserver editManagerObserver = new EditManagerObserver();

	private JPanel overallPanel;

	private JPanel workflowPart;

	private JPanel portsPart;

	private final Workbench workbench;

	private final ReportManager reportManager;

	private final List<ReferenceActionSPI> referenceActionSPIs;

	private final DatabaseConfiguration databaseConfiguration;

	private final Scufl2Tools scufl2Tools = new Scufl2Tools();
	private final URITools uriTools = new URITools();

	public WorkflowLaunchWindow(Workflow workflow, EditManager editManager,
			FileManager fileManager, ReportManager reportManager, Workbench workbench,
			List<ReferenceActionSPI> referenceActionSPIs,
			DatabaseConfiguration databaseConfiguration) {
		super();
		// Initialize RIOT reader
		RIOT.register();

		this.workflow = workflow;
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.reportManager = reportManager;
		this.workbench = workbench;
		this.referenceActionSPIs = referenceActionSPIs;
		this.databaseConfiguration = databaseConfiguration;

		initComponents();

		// Handle refreshing the frame when it receives focus
		this.addWindowFocusListener(new WindowAdapter() {

			@Override
			public void windowGainedFocus(WindowEvent e) {
				if (workflowModified) {

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
		this.addWindowListener(new WindowAdapter() {
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
	 * Set the title of the window to contain the workflow name and its file/url location so that
	 * users can easily identify which workflow is being run.
	 * @param title2 
	 */
	private void setWindowTitle(String title) {
		String windowTitle = "Input values for ";
		if ((title != null) && (!title.isEmpty())) {
			windowTitle += "'" + title + "' ";
		} else {
		    // Fall back to its name
			windowTitle += "'" + workflow.getName() + "' ";
		}

		Object workflowLocation = fileManager.getDataflowSource(workflow.getParent());

		windowTitle += (workflowLocation == null) ? "" : "from " + workflowLocation.toString();

		setTitle(windowTitle);
	}

	/**
	 * Draw the components of the frame.
	 */
	public void initComponents() {

		workflowPart = new JPanel(new GridLayout(3, 1));
		portsPart = new JPanel(new BorderLayout());

		createWorkflowGraphic = createWorkflowGraphic(workflow);
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
				// TODO convert to Scufl2 validation
				// if (!CheckWorkflowStatus.checkWorkflow(dataflowOriginal, workbench, editManager,
				// fileManager, reportManager)) {
				// setVisible(false);
				// return;
				// }

				// Check if user had entered input values for all input ports -
				// otherwise there is no point in attempting to run the workflow
				for (InputWorkflowPort input : workflow.getInputPorts()) {
					RegistrationPanel registrationPanel = inputPanelMap.get(input.getName());
					Object userInput = registrationPanel.getUserInput();
					if (userInput == null) {
						JOptionPane.showMessageDialog(null,
								"You have not provided input values for all workflow inputs",
								"Workflow input value error", JOptionPane.ERROR_MESSAGE);
						// exit
						return;
					}
				}
				setState(Frame.ICONIFIED);

				try {
					Bundle inputDataBundle = createInputDataBundle();
					handleLaunch(inputDataBundle);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(WorkflowLaunchWindow.this,
							"An error occurred while creating input values\n" + e.getMessage(),
							"Error creating inputs", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		};

		WorkflowBundle workflowBundle = workflow.getParent();

        Model annotations = annotationsForBean(workflowBundle, workflow);
        Resource bean = annotations.getResource(uriTools.uriForBean(workflow).toASCIIString());

        
        String title = null;
        Property titleProp = annotations.createProperty("http://purl.org/dc/terms/title");
        if (bean.hasProperty(titleProp)) {
            title = bean.getProperty(titleProp).getString();
        }
        setWindowTitle(title);

        Property descProp = annotations.createProperty("http://purl.org/dc/terms/description");
        if (bean.hasProperty(descProp)) {
            setWorkflowDescription(bean.getProperty(descProp).getString());
        }

        Property creatorProp = annotations.createProperty("http://purl.org/dc/elements/1.1/creator");
        if (bean.hasProperty(creatorProp)) {
            setWorkflowAuthor(bean.getProperty(creatorProp).getString());
        }

		Action useExamplesAction = new AbstractAction("Use examples", addTextIcon) {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				List<InputWorkflowPort> inputPorts = new ArrayList<>(workflow.getInputPorts());
				// Create tabs for input ports (but only for the one that are connected!)
				for (InputWorkflowPort inputPort : inputPorts) {
					RegistrationPanel rp = inputPanelMap.get(inputPort.getName());
					Object example = rp.getExample();
					if ((example != null) && (inputPort.getDepth() == 0) && (rp.getValue() == null)) {
						rp.setValue(example);
					}
				}
			}
		};

		JButton useExamplesButton = new JButton(useExamplesAction);
		useExamplesButton
				.setToolTipText("Use the example value (if any) for ports that you have not set a value for");
		// Construct tool bar
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(useExamplesButton);
		toolBar.add(new JButton(launchAction));
		toolBar.add(new JButton(new AbstractAction("Cancel", WorkbenchIcons.closeIcon) {

			public void actionPerformed(ActionEvent e) {
				handleCancel();
			}
		}));

		JToolBar loadButtonsBar = new JToolBar();
		loadButtonsBar.setFloatable(false);
		for (ReferenceActionSPI spi : referenceActionSPIs) {
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

		List<InputWorkflowPort> inputPorts = new ArrayList<>(workflow.getInputPorts());

		Set<String> inputNames = new HashSet<String>();

		// Create tabs for input ports (but only for the one that are connected!)
		for (InputWorkflowPort inputPort : inputPorts) {
			// Is this input port connected to anything?
			if (scufl2Tools.datalinksFrom(inputPort).isEmpty()) { 
			    continue;
			}
			String portDescription = "";
			String portExample = "";

			annotations = annotationsForBean(workflowBundle, inputPort);
			bean = annotations.getResource(uriTools.uriForBean(inputPort).toASCIIString());
			if (bean.hasProperty(descProp)) {
			    portDescription = bean.getProperty(descProp).getString();
			}
			Property exDataProp = annotations.createProperty("http://biocatalogue.org/attribute/exampleData");
            if (bean.hasProperty(exDataProp)) {
                portExample = bean.getProperty(exDataProp).getString();
            }

			// add tabs for wf input ports
			String name = inputPort.getName();
			inputNames.add(name);
			addInput(name, inputPort.getDepth(), portDescription, portExample);
		}

		portsPart.add(tabsPane, BorderLayout.CENTER);

		workflowPart.setPreferredSize(new Dimension(300, 500));
		portsPart.setPreferredSize(new Dimension(650, 500));

		overallPanel = new JPanel();
		overallPanel.setLayout(new BoxLayout(overallPanel, BoxLayout.X_AXIS));

		overallPanel.add(workflowPart);
		overallPanel.add(portsPart);

		setLayout(new BorderLayout());
		getContentPane().add(new JScrollPane(overallPanel), BorderLayout.CENTER);

		pack();
	}

    private Model annotationsForBean(WorkflowBundle workflowBundle,
            WorkflowBean bean) {
        
        Model model = GraphFactory.makeDefaultModel();
        for (Annotation annotation : scufl2Tools.annotationsFor(bean,
                workflowBundle)) {
//            System.out.println(annotation.getBody());
            URI base = uriTools.uriForBean(workflowBundle);
            URI body = base.resolve(annotation.getBody());
//            System.out.println(body);
            URI path = uriTools.relativePath(workflowBundle.getGlobalBaseURI(),
                    body);
//            System.out.println(path.getPath());
            
            String mediaType = workflowBundle.getResources()
                    .getResourceEntry(path.getPath()).getMediaType();
            Lang lang = RDFLanguages.contentTypeToLang(mediaType);
            if (lang == null) {
                // Safe fallback
                lang = Lang.TURTLE;
            }
//            System.out.println(mediaType);
            try (InputStream inputStream = workflowBundle.getResources()
                    .getResourceAsInputStream(path.getPath())) {
                RDFDataMgr.read(model, inputStream, body.toASCIIString(), lang);
//                model.read(inputStream, body.toASCIIString(), mediaType);                
            } catch (IOException e) {
                logger.warn("Can't read " + body, e);
                continue;
            }
        }
        return model;
    }

	/**
	 * User clicked the cancel button.
	 */
	public void cancelPressed() {
		this.setVisible(true);
	}

	/**
	 * Creates an SVGCanvas loaded with the SVGDocument for the Dataflow.
	 *
	 * @param dataflow
	 * @return
	 */
	private JSVGCanvas createWorkflowGraphic(Workflow worklfow) {
		JSVGCanvas svgCanvas = new JSVGCanvas();
		// SVGGraphController graphController = GraphViewComponent.graphControllerMap.get(worklfow);
		// if (graphController != null) {
		// SVGDocument svgDoc = graphController.getSVGDocument();
		// svgCanvas.setDocument((SVGDocument) svgDoc.cloneNode(true));
		// }
		return svgCanvas;
	}

	public synchronized void addInput(final String inputName, final int inputDepth) {
		addInput(inputName, inputDepth, null, null);
	}

	public void addInput(final String inputName, final int inputDepth, String inputDescription,
			String inputExample) {

		// Don't do anything if we already have the input registration panel for this input port
		RegistrationPanel inputRegistrationPanel = inputPanelMap.get(inputName);
		if ((inputRegistrationPanel == null) || (inputRegistrationPanel.getDepth() != inputDepth)) {
			inputRegistrationPanel = new RegistrationPanel(inputDepth, inputName, inputDescription,
					inputExample, databaseConfiguration);
			inputPanelMap.put(inputName, inputRegistrationPanel);
		} else {
			inputRegistrationPanel.setStatus(
					"Drag to re-arrange, or drag files, URLs, or text to add", null);
			inputRegistrationPanel.setDescription(inputDescription);
			inputRegistrationPanel.setExample(inputExample);
		}
		tabsPane.addTab(inputName, inputRegistrationPanel);
		tabsPane.revalidate();
		tabsPane.repaint();
	}

	public synchronized void removeInputTab(final String inputName) {
		// Only do something if we have a registration panel for this input port to begin with
		if (!inputPanelMap.containsKey(inputName)) {
			return;
		} else {
			RegistrationPanel inputRegistrationPanelToRemove = inputPanelMap.remove(inputName);
			tabsPane.remove(inputRegistrationPanelToRemove);
		}
	}

	private Bundle createInputDataBundle() throws IOException {
		Bundle bundle = DataBundles.createBundle();
		Path inputs = DataBundles.getInputs(bundle);
		for (String input : inputPanelMap.keySet()) {
			RegistrationPanel registrationPanel = inputPanelMap.get(input);
			Object userInput = registrationPanel.getUserInput();

			Path port = DataBundles.getPort(inputs, input);
			setValue(port, userInput);
		}
		return bundle;
	}

	private void setValue(Path port, Object userInput) throws IOException {
		if (userInput instanceof File) {
			DataBundles.setReference(port, ((File) userInput).toURI());
		} else if (userInput instanceof URL) {
			try {
				DataBundles.setReference(port, ((URL) userInput).toURI());
			} catch (URISyntaxException e) {
				logger.warn(String.format("Error converting %1$s to URI", userInput), e);
			}
		} else if (userInput instanceof String){
			DataBundles.setStringValue(port, (String) userInput);
		} else if (userInput instanceof List<?>) {
			DataBundles.createList(port);
			List<?> list = (List<?>) userInput;
			for (Object object : list) {
				setValue(DataBundles.newListItem(port), object);
			}
		} else {
			logger.warn("Unknown input type : " + userInput.getClass().getName());
		}
	}

	/**
	 * Called when the run workflow action has been performed
	 *
	 * @param workflowInputs
	 *            a map of named inputs in the form of T2Reference instances
	 */
	public abstract void handleLaunch(Bundle workflowInputs);

	public abstract void handleCancel();

	private static void selectTopOfTextArea(DialogTextArea textArea) {
		textArea.setSelectionStart(0);
		textArea.setSelectionEnd(0);
	}

	public void setWorkflowDescription(String workflowDescription) {
		if ((workflowDescription != null) && (workflowDescription.length() > 0)) {
			this.workflowDescriptionArea.setText(workflowDescription);
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

	public class FileManagerObserver implements Observer<FileManagerEvent> {
		public void notify(Observable<FileManagerEvent> sender, FileManagerEvent message)
				throws Exception {
			if (message instanceof ClosedDataflowEvent
					&& ((ClosedDataflowEvent) message).getDataflow() == workflow.getParent()) {
				// Remove listeners of various events
				editManager.removeObserver(editManagerObserver);
				fileManager.removeObserver(fileManagerObserver);
				setVisible(false);
				dispose(); // dispose off this window if the original workflow has been closed
			}
		}
	}

	public class EditManagerObserver implements Observer<EditManagerEvent> {
		public void notify(Observable<EditManagerEvent> sender, final EditManagerEvent message)
				throws Exception {

			if (message instanceof AbstractDataflowEditEvent
					&& ((AbstractDataflowEditEvent) message).getDataFlow() == workflow.getParent()) {
				workflowModified = true;
			}
		}
	}

}
