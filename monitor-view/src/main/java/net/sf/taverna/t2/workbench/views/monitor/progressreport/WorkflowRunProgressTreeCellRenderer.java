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
package net.sf.taverna.t2.workbench.views.monitor.progressreport;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.MergePort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Cell renderer for Workflow Explorer tree.
 *
 * @author Alex Nenadic
 *
 */

@SuppressWarnings("serial")
public class WorkflowRunProgressTreeCellRenderer extends DefaultTreeCellRenderer {

	private ActivityIconManager activityIconManager;

	public WorkflowRunProgressTreeCellRenderer(ActivityIconManager activityIconManager) {
		this.activityIconManager = activityIconManager;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		Component result = super.getTreeCellRendererComponent(tree, value, sel,
				expanded, leaf, row, hasFocus);

		Object userObject = ((DefaultMutableTreeNode) value).getUserObject();

		WorkflowRunProgressTreeCellRenderer renderer = (WorkflowRunProgressTreeCellRenderer) result;

		if (userObject instanceof Dataflow){ //the root node
			renderer.setIcon(WorkbenchIcons.workflowExplorerIcon);
			renderer.setText(((Dataflow) userObject).getLocalName());
		} else if (userObject instanceof Processor) {
			// Get the activity associated with the procesor - currently only
			// the first one in the list gets displayed
			List<? extends Activity<?>> activityList = ((Processor) userObject)
					.getActivityList();
			String text = ((Processor) userObject).getLocalName();
			if (!activityList.isEmpty()) {
				Activity<?> activity = activityList.get(0);
				Icon icon = activityIconManager.iconForActivity(activity);

				if (icon != null) {
					renderer.setIcon(icon);
				}

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

		return result;
	}

	protected String findName(Port port) {
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
