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

import org.apache.commons.beanutils.BeanUtils;

import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
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

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		Component result = super.getTreeCellRendererComponent(tree, value, sel,
				expanded, leaf, row, hasFocus);

		Object userObject = ((DefaultMutableTreeNode) value).getUserObject();

		WorkflowExplorerTreeCellRenderer renderer = (WorkflowExplorerTreeCellRenderer) result;
		
		if (userObject instanceof Dataflow){ //the root node
			renderer.setIcon(WorkbenchIcons.workflowExplorerIcon);
			renderer.setText(((Dataflow) userObject).getLocalName());
		}
		else if (userObject instanceof DataflowInputPort) {
			renderer.setIcon(WorkbenchIcons.inputIcon);
			renderer.setText(((DataflowInputPort) userObject).getName());
		} else if (userObject instanceof DataflowOutputPort) {
			renderer.setIcon(WorkbenchIcons.outputIcon);
			renderer.setText(((DataflowOutputPort) userObject).getName());
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
		// Processor's child input port (from the associated activity)
		else if (userObject instanceof ActivityInputPort) {
			renderer.setIcon(WorkbenchIcons.inputPortIcon);
			renderer.setText(((ActivityInputPort) userObject).getName());
		}
		// Processor's child output port (from the associated activity)
		else if (userObject instanceof OutputPort) {
			renderer.setIcon(WorkbenchIcons.outputPortIcon);
			renderer.setText(((OutputPort) userObject).getName());
		} else if (userObject instanceof Datalink) {
			renderer.setIcon(WorkbenchIcons.datalinkIcon);
			EventForwardingOutputPort source = ((Datalink) userObject).getSource();
			String sourceName = findName(source);
			EventHandlingInputPort sink = ((Datalink) userObject).getSink();
			String sinkName = findName(sink);
			renderer.setText(sourceName
					+ " -> " + sinkName);
		} else if (userObject instanceof Condition) {
			renderer.setIcon(WorkbenchIcons.controlLinkIcon);
			String htmlText = "<html><head></head><body>"
					+ ((Condition) userObject).getTarget().getLocalName()
					+ " " + RUNS_AFTER  + " "
					+ ((Condition) userObject).getControl().getLocalName()
					+ "</body></html>";
			renderer.setText(htmlText);
			
			
		} else if (userObject instanceof Merge) {
			renderer.setIcon(WorkbenchIcons.mergeIcon);
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
