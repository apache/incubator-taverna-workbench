/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.ui.workflowexplorer;

import static org.apache.taverna.workbench.icons.WorkbenchIcons.controlLinkIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.datalinkIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.folderClosedIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.folderOpenIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.inputIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.inputPortIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.outputIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.outputPortIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.workflowExplorerIcon;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.taverna.lang.ui.icons.Icons;
import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.workbench.report.ReportManager;

import org.apache.commons.beanutils.BeanUtils;

import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.core.BlockingControlLink;
import org.apache.taverna.scufl2.api.core.DataLink;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;
import org.apache.taverna.scufl2.api.port.InputWorkflowPort;
import org.apache.taverna.scufl2.api.port.OutputProcessorPort;
import org.apache.taverna.scufl2.api.port.OutputWorkflowPort;
import org.apache.taverna.scufl2.api.port.Port;
import org.apache.taverna.scufl2.api.port.ProcessorPort;
import org.apache.taverna.scufl2.api.port.ReceiverPort;
import org.apache.taverna.scufl2.api.port.SenderPort;
import org.apache.taverna.scufl2.api.profiles.ProcessorBinding;

/**
 * Cell renderer for Workflow Explorer tree.
 *
 * @author Alex Nenadic
 * @author David Withers
 */
public class WorkflowExplorerTreeCellRenderer extends DefaultTreeCellRenderer {
	// FIXME This enum is just a workaround
	enum Status {
		OK, WARNING, SEVERE
	}

	private static final long serialVersionUID = -1326663036193567147L;
	private static final String RUNS_AFTER = " runs after ";

	private final ActivityIconManager activityIconManager;

	@SuppressWarnings("unused")
	private Workflow workflow = null;
	@SuppressWarnings("unused")
	private final ReportManager reportManager;

	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	public WorkflowExplorerTreeCellRenderer(Workflow workflow, ReportManager reportManager,
			ActivityIconManager activityIconManager) {
		super();
		this.workflow = workflow;
		this.reportManager = reportManager;
		this.activityIconManager = activityIconManager;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
			boolean expanded, boolean leaf, int row, boolean hasFocus) {
		Component result = super.getTreeCellRendererComponent(tree, value, sel,
				expanded, leaf, row, hasFocus);

		Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
		// TODO rewrite report manager to use scufl2 validation
		// Status status = reportManager.getStatus(workflow, userObject);
		Status status = Status.OK;
		WorkflowExplorerTreeCellRenderer renderer = (WorkflowExplorerTreeCellRenderer) result;

		if (userObject instanceof Workflow) { // the root node
			if (!hasGrandChildren((DefaultMutableTreeNode) value))
				renderer.setIcon(workflowExplorerIcon);
			else
				renderer.setIcon(chooseIcon(workflowExplorerIcon, status));
			renderer.setText(((Workflow) userObject).getName());
		} else if (userObject instanceof InputWorkflowPort) {
			renderer.setIcon(chooseIcon(inputIcon, status));
			renderer.setText(((InputWorkflowPort) userObject).getName());
		} else if (userObject instanceof OutputWorkflowPort) {
			renderer.setIcon(chooseIcon(outputIcon, status));
			renderer.setText(((OutputWorkflowPort) userObject).getName());
		} else if (userObject instanceof Processor) {
			Processor p = (Processor) userObject;
			/*
			 * Get the activity associated with the processor - currently only
			 * the first one in the list gets displayed
			 */
			List<ProcessorBinding> processorbindings = scufl2Tools
					.processorBindingsForProcessor(p, p.getParent().getParent()
							.getMainProfile());
			String text = p.getName();
			if (!processorbindings.isEmpty()) {
				Activity activity = processorbindings.get(0).getBoundActivity();
				Icon basicIcon = activityIconManager.iconForActivity(activity);
				renderer.setIcon(chooseIcon(basicIcon, status));

				try {
					String extraDescription = BeanUtils.getProperty(activity,
							"extraDescription");
					text += " - " + extraDescription;
				} catch (IllegalAccessException | InvocationTargetException
						| NoSuchMethodException e) {
					// no problem
				}
			}
			renderer.setText(text);
		}
		// Processor's child input port
		else if (userObject instanceof InputProcessorPort) {
			renderer.setIcon(chooseIcon(inputPortIcon, status));
			renderer.setText(((InputProcessorPort) userObject).getName());
		}
		// Processor's child output port
		else if (userObject instanceof OutputProcessorPort) {
			renderer.setIcon(chooseIcon(outputPortIcon, status));
			renderer.setText(((OutputProcessorPort) userObject).getName());
		} else if (userObject instanceof DataLink) {
			renderer.setIcon(chooseIcon(datalinkIcon, status));
			SenderPort source = ((DataLink) userObject).getReceivesFrom();
			String sourceName = findName(source);
			ReceiverPort sink = ((DataLink) userObject).getSendsTo();
			String sinkName = findName(sink);
			renderer.setText(sourceName + " -> " + sinkName);
		} else if (userObject instanceof BlockingControlLink) {
			renderer.setIcon(chooseIcon(controlLinkIcon, status));
			String htmlText = "<html><head></head><body>"
					+ ((BlockingControlLink) userObject).getBlock().getName()
					+ " "
					+ RUNS_AFTER
					+ " "
					+ ((BlockingControlLink) userObject).getUntilFinished()
							.getName() + "</body></html>";
			renderer.setText(htmlText);

		} else {
			/*
			 * It one of the main container nodes (inputs, outputs, processors,
			 * datalinks) or a nested workflow node
			 */
			if (expanded)
				renderer.setIcon(folderOpenIcon);
			else
				renderer.setIcon(folderClosedIcon);
		}

		return result;
	}

	private static Icon chooseIcon(final Icon basicIcon, Status status) {
		if (status == null)
			return basicIcon;
		if (status == Status.OK)
			return basicIcon;
		else if (status == Status.WARNING)
			return Icons.warningIcon;
		else if (status == Status.SEVERE)
			return Icons.severeIcon;
		return basicIcon;
	}

	private static boolean hasGrandChildren(DefaultMutableTreeNode node) {
		int childCount = node.getChildCount();
		for (int i = 0; i < childCount; i++)
			if (node.getChildAt(i).getChildCount() > 0)
				return true;
		return false;
	}

	private String findName(Port port) {
		if (port instanceof ProcessorPort) {
			String sourceProcessorName = ((ProcessorPort) port).getParent()
					.getName();
			return sourceProcessorName + ":" + port.getName();
		}
		return port.getName();
	}
}
