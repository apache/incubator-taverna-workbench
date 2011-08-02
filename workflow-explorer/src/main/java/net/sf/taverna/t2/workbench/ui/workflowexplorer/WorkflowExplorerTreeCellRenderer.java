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
package net.sf.taverna.t2.workbench.ui.workflowexplorer;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.taverna.t2.lang.ui.icons.Icons;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergePort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Cell renderer for Workflow Explorer tree.
 *
 * @author Alex Nenadic
 *
 */

public class WorkflowExplorerTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -1326663036193567147L;

	private ActivityIconManager activityIconManager = ActivityIconManager.getInstance();

	private final String RUNS_AFTER = " runs after ";

	private Dataflow workflow = null;
	private final ReportManager reportManager;

	public WorkflowExplorerTreeCellRenderer(Dataflow workflow, ReportManager reportManager) {
		super();
		this.workflow = workflow;
		this.reportManager = reportManager;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		Component result = super.getTreeCellRendererComponent(tree, value, sel,
				expanded, leaf, row, hasFocus);

		Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
		Status status = reportManager.getStatus(workflow, userObject);
		WorkflowExplorerTreeCellRenderer renderer = (WorkflowExplorerTreeCellRenderer) result;

		if (userObject instanceof Dataflow){ //the root node
			if (!hasGrandChildren((DefaultMutableTreeNode) value)) {
				renderer.setIcon(WorkbenchIcons.workflowExplorerIcon);
			} else {
				renderer.setIcon(chooseIcon(WorkbenchIcons.workflowExplorerIcon, status));
			}
			renderer.setText(((Dataflow) userObject).getLocalName());
		}
		else if (userObject instanceof DataflowInputPort) {
			renderer.setIcon(chooseIcon(WorkbenchIcons.inputIcon, status));
			renderer.setText(((DataflowInputPort) userObject).getName());
		} else if (userObject instanceof DataflowOutputPort) {
			renderer.setIcon(chooseIcon(WorkbenchIcons.outputIcon, status));
			renderer.setText(((DataflowOutputPort) userObject).getName());
		} else if (userObject instanceof Processor) {
			Processor p = (Processor) userObject;
			// Get the activity associated with the processor - currently only
			// the first one in the list gets displayed
			List<? extends Activity<?>> activityList = p.getActivityList();
			String text = ((Processor) userObject).getLocalName();
			if (!activityList.isEmpty()) {
				Activity<?> activity = activityList.get(0);
				Icon basicIcon = activityIconManager.iconForActivity(activity);
				renderer.setIcon(chooseIcon(basicIcon, status));


				String extraDescription;
				try {
					extraDescription = BeanUtils.getProperty(activity,
							"extraDescription");
					text += " - " + extraDescription;
				} catch (IllegalAccessException e) {
					// no problem
				} catch (InvocationTargetException e) {
					// no problem
				} catch (NoSuchMethodException e) {
					// no problem;
				}
			}
			renderer.setText(text);
		}
		// Processor's child input port (from the associated activity)
		else if (userObject instanceof ActivityInputPort) {
			renderer.setIcon(chooseIcon(WorkbenchIcons.inputPortIcon, status));
			renderer.setText(((ActivityInputPort) userObject).getName());
		}
		// Processor's child output port (from the associated activity)
		else if (userObject instanceof OutputPort) {
			renderer.setIcon(chooseIcon(WorkbenchIcons.outputPortIcon, status));
			renderer.setText(((OutputPort) userObject).getName());
		} else if (userObject instanceof Datalink) {
			renderer.setIcon(chooseIcon(WorkbenchIcons.datalinkIcon, status));
			EventForwardingOutputPort source = ((Datalink) userObject).getSource();
			String sourceName = findName(source);
			EventHandlingInputPort sink = ((Datalink) userObject).getSink();
			String sinkName = findName(sink);
			renderer.setText(sourceName
					+ " -> " + sinkName);
		} else if (userObject instanceof Condition) {
			renderer.setIcon(chooseIcon(WorkbenchIcons.controlLinkIcon, status));
			String htmlText = "<html><head></head><body>"
					+ ((Condition) userObject).getTarget().getLocalName()
					+ " " + RUNS_AFTER  + " "
					+ ((Condition) userObject).getControl().getLocalName()
					+ "</body></html>";
			renderer.setText(htmlText);


		} else if (userObject instanceof Merge) {
			renderer.setIcon(chooseIcon(WorkbenchIcons.mergeIcon, status));
			renderer.setText(((Merge) userObject).getLocalName());
		} else {
			// It one of the main container nodes (inputs, outputs,
			// processors, datalinks) or a nested workflow node
			if (expanded) {
				renderer.setIcon(WorkbenchIcons.folderOpenIcon);
			} else {
				renderer.setIcon(WorkbenchIcons.folderClosedIcon);
			}
		}

		return result;
	}

	private static Icon chooseIcon (final Icon basicIcon, Status status) {
		if (status == null) {
			return basicIcon;
		}
		if (status == Status.OK) {
			return basicIcon;
		}
		else if (status == Status.WARNING) {
			return Icons.warningIcon;
		} else if (status == Status.SEVERE) {
			return Icons.severeIcon;
		}
		return basicIcon;
	}

	private static boolean hasGrandChildren(DefaultMutableTreeNode node) {
		int childCount = node.getChildCount();
		for (int i = 0; i < childCount; i++) {
			if (node.getChildAt(i).getChildCount() > 0) {
				return true;
			}
		}
		return false;
	}

	private String findName(Port port) {
		if (port instanceof ProcessorPort) {
			String sourceProcessorName = ((ProcessorPort)port).getProcessor().getLocalName();
			return sourceProcessorName + ":" + port.getName();
		} else if (port instanceof MergePort) {
			String sourceMergeName = ((MergePort)port).getMerge().getLocalName();
			return sourceMergeName + ":" + port.getName();

		} else {
			return port.getName();
		}
	}
}
