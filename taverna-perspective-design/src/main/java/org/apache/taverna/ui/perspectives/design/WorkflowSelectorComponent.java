/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.taverna.ui.perspectives.design;

import static java.awt.FlowLayout.LEFT;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.SwingAwareObserver;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.selection.events.SelectionManagerEvent;
import org.apache.taverna.workbench.selection.events.WorkflowBundleSelectionEvent;
import org.apache.taverna.workbench.selection.events.WorkflowSelectionEvent;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.common.NamedSet;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.profiles.ProcessorBinding;
import org.apache.taverna.scufl2.api.profiles.Profile;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Component for managing selection of workflows.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class WorkflowSelectorComponent extends JPanel {
	private static final URI NESTED_WORKFLOW_TYPE = URI
			.create("http://ns.taverna.org.uk/2010/activity/nested-workflow");

	private final Scufl2Tools scufl2Tools = new Scufl2Tools();
	private final SelectionManager selectionManager;

	public WorkflowSelectorComponent(SelectionManager selectionManager) {
		super(new FlowLayout(LEFT));
		this.selectionManager = selectionManager;
		update(selectionManager.getSelectedWorkflow());
		selectionManager.addObserver(new SelectionManagerObserver());
	}

	private void update(Workflow workflow) {
		removeAll();
		if (workflow != null
				&& workflow.getParent().getMainWorkflow() != workflow)
			update(workflow, selectionManager.getSelectedProfile());
		revalidate();
		repaint();
	}

	/** @see #update(Workflow) */
	private void update(Workflow workflow, Profile profile) {
		boolean first = true;
		for (final Workflow workflowItem : getPath(
				new NamedSet<>(profile.getActivities()), workflow, profile)) {
			JButton button = new JButton(workflowItem.getName());
//				button.setBorder(null);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectionManager.setSelectedWorkflow(workflowItem);
				}
			});
			if (!first)
				add(new JLabel(">"));
			first = false;
			add(button);
		}
	}

	private List<Workflow> getPath(NamedSet<Activity> activities,
			Workflow workflow, Profile profile) {
		LinkedList<Workflow> path = new LinkedList<>();
		for (Activity activity : activities) {
			if (!activity.getType().equals(NESTED_WORKFLOW_TYPE))
				continue;
			if (getNestedWorkflow(workflow, profile, activity) != workflow)
				continue;

			List<ProcessorBinding> processorBindings = scufl2Tools
					.processorBindingsToActivity(activity);
			for (ProcessorBinding processorBinding : processorBindings) {
				Processor processor = processorBinding.getBoundProcessor();
				Workflow parentWorkflow = processor.getParent();
				if (workflow.getParent().getMainWorkflow() == parentWorkflow)
					path.add(parentWorkflow);
				else {
					activities.remove(activity);
					path.addAll(getPath(activities, parentWorkflow, profile));
				}
				break;
			}
			break;
		}
		path.add(workflow);
		return path;
	}

	private Workflow getNestedWorkflow(Workflow workflow, Profile profile,
			Activity activity) {
		for (Configuration configuration : scufl2Tools.configurationsFor(
				activity, profile)) {
			JsonNode nested = configuration.getJson().get("nestedWorkflow");
			Workflow wf = workflow.getParent().getWorkflows()
					.getByName(nested.asText());
			if (wf != null)
				return wf;
		}
		return null;
	}

	private class SelectionManagerObserver extends
			SwingAwareObserver<SelectionManagerEvent> {
		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender,
				SelectionManagerEvent message) {
			if (message instanceof WorkflowBundleSelectionEvent)
				bundleSelected((WorkflowBundleSelectionEvent) message);
			else if (message instanceof WorkflowSelectionEvent)
				workflowSelected((WorkflowSelectionEvent) message);
		}
	}

	private void workflowSelected(WorkflowSelectionEvent event) {
		update(event.getSelectedWorkflow());
	}

	private void bundleSelected(WorkflowBundleSelectionEvent event) {
		WorkflowBundle workflowBundle = event.getSelectedWorkflowBundle();
		if (workflowBundle == null)
			update((Workflow) null);
		else
			update(selectionManager.getSelectedWorkflow());
	}
}
