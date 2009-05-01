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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.annotation.annotationbeans.Author;
import net.sf.taverna.t2.annotation.annotationbeans.DescriptiveTitle;
import net.sf.taverna.t2.annotation.annotationbeans.FreeTextDescription;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;

/**
 * A simple workflow launch panel, uses a tabbed layout to display a set of
 * named InputConstructionPanel instances, and a 'run workflow' button.
 * 
 * @author Tom Oinn
 * @author David Withers
 * @author Stian Soiland-Reyes
 * @author Alan R Williams
 */
@SuppressWarnings("serial")
public abstract class WorkflowLaunchPanel extends JPanel {

	private static Logger logger = Logger
	.getLogger(WorkflowLaunchPanel.class);

	private static final String LAUNCH_WORKFLOW = "Launch workflow";

	private final ImageIcon launchIcon = new ImageIcon(getClass().getResource(
			"/icons/start_task.gif"));

	// An action enabled when all inputs are enabled and used to trigger the
	// handleLaunch method
	private final Action launchAction;

	private static final Map<String, Map<String, RegistrationPanel>> workflowInputPanelMap =
		new HashMap<String, Map<String, RegistrationPanel>>();
	private final Map<String, RegistrationPanel> inputPanelMap;
	private final Map<String, T2Reference> inputMap = new HashMap<String, T2Reference>();

	private final JTabbedPane tabs;
	private final Map<String, RegistrationPanel> tabComponents = new HashMap<String, RegistrationPanel>();

	private final WorkflowInstanceFacade facade;
	private final ReferenceService referenceService;
	private final ReferenceContext referenceContext;

	private final static String NO_WORKFLOW_DESCRIPTION = "No description";
	private static final String NO_WORKFLOW_AUTHOR = "No author";

	private static final String NO_WORKFLOW_TITLE = "No title";

	private JTextArea workflowDescription = new JTextArea(NO_WORKFLOW_DESCRIPTION);
	private JTextArea workflowTitle = new JTextArea(NO_WORKFLOW_TITLE);
	private JTextArea workflowAuthor = new JTextArea(NO_WORKFLOW_AUTHOR);

	private JPanel workflowImageComponentHolder = new JPanel();
	private AnnotationTools annotationTools = new AnnotationTools();

	
	@SuppressWarnings("serial")
	public WorkflowLaunchPanel(WorkflowInstanceFacade facade, ReferenceContext context) {
		super(new BorderLayout());
		
		workflowDescription.setBorder(new TitledBorder("Workflow description"));
		workflowDescription.setEditable(false);
		workflowDescription.setRows(5);
		workflowAuthor.setBorder(new TitledBorder("Workflow author"));
		workflowAuthor.setEditable(false);
		workflowTitle.setBorder(new TitledBorder("Workflow title"));
		workflowTitle.setEditable(false);

		String dataflowName = facade.getDataflow().getLocalName();
		if (workflowInputPanelMap.containsKey(dataflowName)) {
			inputPanelMap = workflowInputPanelMap.get(dataflowName);
		} else {
			inputPanelMap = new HashMap<String,RegistrationPanel>();
			workflowInputPanelMap.put(dataflowName, inputPanelMap);
		}
		this.facade = facade;
		this.referenceService = facade.getContext()
		.getReferenceService();
		this.referenceContext = context;

		launchAction = new AbstractAction(LAUNCH_WORKFLOW, launchIcon) {
			public void actionPerformed(ActionEvent ae) {
				registerInputs();
				handleLaunch(inputMap);
			}
		};

		// Construct tab container
		tabs = new JTabbedPane();
		add(tabs, BorderLayout.CENTER);

		// Construct tool bar
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(new JButton(launchAction));
		
		JPanel upperPanel = new JPanel(new BorderLayout());
//		upperPanel.add(toolBar, BorderLayout.SOUTH);
		JPanel annotationsPanel = new JPanel(new BorderLayout());

		String wfDescription = annotationTools.getAnnotationString(facade.getDataflow(), FreeTextDescription.class, "");
		setWorkflowDescription(wfDescription);
		
		String wfTitle = annotationTools.getAnnotationString(facade.getDataflow(), DescriptiveTitle.class, "");
		setWorkflowTitle(wfTitle);
		String wfAuthor = annotationTools.getAnnotationString(facade.getDataflow(), Author.class, "");
		setWorkflowAuthor(wfAuthor);
		
		annotationsPanel.add(workflowTitle, BorderLayout.NORTH);
		annotationsPanel.add(workflowDescription, BorderLayout.CENTER);
		annotationsPanel.add(workflowAuthor, BorderLayout.SOUTH);
		annotationsPanel.setBorder(BorderFactory.createCompoundBorder(
				  BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
		upperPanel.add(annotationsPanel, BorderLayout.CENTER);
//		upperPanel.add(workflowImageComponentHolder, BorderLayout.EAST);
		add(upperPanel, BorderLayout.NORTH);
		JPanel toolBarPanel = new JPanel(new BorderLayout());
		toolBarPanel.add(toolBar, BorderLayout.EAST);
		toolBarPanel.setBorder(new EmptyBorder(5,20,5,20));
		add(toolBarPanel, BorderLayout.SOUTH);
	}

	@SuppressWarnings("serial")
	public synchronized void addInput(final String inputName,
			final int inputDepth) {
		addInput(inputName, inputDepth, null, null);
	}
	
	public void addInput(final String inputName,
			final int inputDepth, String inputDescription, String inputExample) {
		// Don't do anything if we already have this tab
		if (inputMap.containsKey(inputName)) {
// do nothing
		} else {
			RegistrationPanel inputPanel;
			if (inputPanelMap.containsKey(inputName) &&
					(inputPanelMap.get(inputName).getDepth() == inputDepth)) {
				inputPanel = inputPanelMap.get(inputName);
			} else {
				inputPanel = new RegistrationPanel(inputDepth, inputName, inputDescription, inputExample);
				inputPanelMap.put(inputName, inputPanel);
			}
			inputMap.put(inputName, null);
			tabComponents.put(inputName, inputPanel);
			tabs.addTab(inputName, inputPanel);
		}
		
	}


	public synchronized void removeInputTab(final String inputName) {
		// Only do something if we have this tab to begin with
		if (inputMap.containsKey(inputName) == false) {
			return;
		} else {
			Component component = tabComponents.get(inputName);
			tabComponents.remove(inputName);
			inputMap.remove(inputName);
			tabs.remove(component);
		}
	}

	private void registerInputs() {
		for (String input : inputMap.keySet()) {
			RegistrationPanel registrationPanel = tabComponents.get(input);
			Object userInput = registrationPanel.getUserInput();
			int inputDepth = registrationPanel.getDepth();
			T2Reference reference = referenceService.register(userInput,
					inputDepth, true, referenceContext);
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

	
	private String truncateString(String original) {
		String result = "";
		try {
		BufferedReader reader = new BufferedReader(new StringReader(original));
		boolean finished = false;
		for (int i = 0; (i < 5) && !finished; i++) {
			String nextLine;
				nextLine = reader.readLine();
			finished = nextLine == null;
			if (!finished) {
				result = result + nextLine + "\n";
			}
		}
		if (!finished) {
			result = result + "...";
		}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		return result;
	}
	
	private void setWorkflowDescription(String workflowDescription) {
		if ((workflowDescription != null) && (workflowDescription.length() > 0)) {
		this.workflowDescription.setText(truncateString(workflowDescription));
		}
	}

	private void setWorkflowAuthor(String workflowAuthor) {
		if ((workflowAuthor != null) && (workflowAuthor.length() > 0)) {
		this.workflowAuthor.setText(workflowAuthor);
		}
	}

	private void setWorkflowTitle(String workflowTitle) {
		if ((workflowTitle != null) && (workflowTitle.length() > 0)) {
		this.workflowTitle.setText(workflowTitle);
		}
	}

	public void setWorkflowImageComponent(Component workflowImageComponent) {
		synchronized (workflowImageComponentHolder) {
			workflowImageComponentHolder.removeAll();
			workflowImageComponentHolder.add(workflowImageComponent);
			workflowImageComponentHolder.invalidate();
		}
	}

	public Component getWorkflowImageComponent() {
		try {
			return workflowImageComponentHolder.getComponent(0);
		} catch (IndexOutOfBoundsException ex) {
			return null;
		}
	}

	public String getWorkflowDescription() {
		return workflowDescription.getText();
	}

}
