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
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * A simple workflow launch panel, uses a tabbed layout to display a set of
 * named InputConstructionPanel instances, and a 'run workflow' button.
 * 
 * @author Tom Oinn
 * @author David Withers
 * @author Stian Soiland-Reyes
 */
@SuppressWarnings("serial")
public abstract class WorkflowLaunchPanel extends JPanel {

	private static final String LAUNCH_WORKFLOW = "Launch workflow";

	private final ImageIcon launchIcon = new ImageIcon(getClass().getResource(
			"/icons/start_task.gif"));

	// An action enabled when all inputs are enabled and used to trigger the
	// handleLaunch method
	private final Action launchAction;

	// Hold the current map of name->reference
	private final Map<String, T2Reference> inputMap = new HashMap<String, T2Reference>();

	private final JTabbedPane tabs;
	private final Map<String, RegistrationPanel> tabComponents = new HashMap<String, RegistrationPanel>();

	private final ReferenceService referenceService;
	private final ReferenceContext referenceContext;

	private JLabel workflowDescription = new JLabel();

	private JLabel workflowIcon = new JLabel();

	@SuppressWarnings("serial")
	public WorkflowLaunchPanel(ReferenceService rs, ReferenceContext context) {
		super(new BorderLayout());

		this.referenceService = rs;
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
		upperPanel.add(toolBar, BorderLayout.SOUTH);
		upperPanel.add(workflowDescription, BorderLayout.CENTER);
		upperPanel.add(workflowIcon, BorderLayout.EAST);
		add(upperPanel, BorderLayout.NORTH);
	}

	@SuppressWarnings("serial")
	public synchronized void addInput(final String inputName,
			final int inputDepth) {
		addInput(inputName, inputDepth, null);
	}
	
	public void addInput(final String inputName,
			final int inputDepth, String inputDescription) {
		// Don't do anything if we already have this tab
		if (inputMap.containsKey(inputName)) {
			return;
		} else {
			RegistrationPanel inputPanel = new RegistrationPanel(inputDepth, inputName, inputDescription);
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

	public void setWorkflowDescription(String workflowDescription) {
		this.workflowDescription.setText("<html>"  + workflowDescription + "</html>");
	}

	public void setWorkflowPicture(ImageIcon workflowPicture) {
		this.workflowIcon.setIcon(workflowPicture);
		
	}


}
