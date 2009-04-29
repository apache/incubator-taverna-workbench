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
package net.sf.taverna.t2.workbench.ui.servicepanel;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeNode;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeSelectionModel;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.TreePanel;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * @author alanrw
 *
 */
public class ServiceTreeClickListener extends MouseAdapter {

	private static Logger logger = Logger
	.getLogger(ServiceTreeClickListener.class);

	private JTree tree;
	private TreePanel panel;
	
	public ServiceTreeClickListener (JTree tree, TreePanel panel) {
		this.tree = tree;
		this.panel = panel;
	}
	
	public void mouseClicked(MouseEvent evt) {

		FilterTreeSelectionModel selectionModel = (FilterTreeSelectionModel) tree.getSelectionModel();
			// Discover the tree row that was clicked on
			int selRow = tree.getRowForLocation(evt.getX(), evt
					.getY());
			if (selRow != -1) {
				// Get the selection path for the row
				TreePath selectionPath = tree.getPathForLocation(evt
						.getX(), evt.getY());
				if (selectionPath != null) {
					// Get the selected node
					final FilterTreeNode selectedNode = (FilterTreeNode) selectionPath
							.getLastPathComponent();

					selectionModel.clearSelection();			
					selectionModel.mySetSelectionPath(selectionPath);

					if (evt.getButton() == MouseEvent.BUTTON3) {
						Object selectedObject = selectedNode.getUserObject();
						logger.info(selectedObject.getClass().getName());
						if (selectedObject
								.equals(ServicePanel.AVAILABLE_SERVICES)) {
							JPopupMenu menu = new JPopupMenu();
							menu.add(new ShadedLabel("Tree",
									ShadedLabel.GREEN));
							menu.add(new JMenuItem(new AbstractAction(
									"Expand all", WorkbenchIcons.plusIcon) {
								public void actionPerformed(ActionEvent evt) {
									SwingUtilities
											.invokeLater(new Runnable() {

												public void run() {
													try {
														panel.expandTreePaths();
													} catch (InterruptedException e) {
														// TODO
														// Auto-generated
														// catch block
														e.printStackTrace();
													} catch (InvocationTargetException e) {
														// TODO
														// Auto-generated
														// catch block
														e.printStackTrace();
													}
												}
											});
								}
							}));
							menu.add(new JMenuItem(new AbstractAction(
									"Collapse all",
									WorkbenchIcons.minusIcon) {
								public void actionPerformed(ActionEvent evt) {
									SwingUtilities
											.invokeLater(new Runnable() {
												public void run() {
													for (int i = 0; i < tree
															.getRowCount(); i++) {
														tree.collapseRow(i);
													}
												}

											});
								}
							}));
							menu.show(evt.getComponent(), evt.getX(), evt
									.getY());
						}
						else if (selectedObject instanceof ServiceDescription) {
							final ServiceDescription sd = (ServiceDescription) selectedObject;
							JPopupMenu menu = new JPopupMenu();
							menu.add(new ShadedLabel(sd.getName(),
									ShadedLabel.ORANGE));
							menu.add(new AbstractAction("Add to workflow") {

								public void actionPerformed(ActionEvent e) {
									Dataflow currentDataflow = (Dataflow) ModelMap.getInstance().getModel(ModelMapConstants.CURRENT_DATAFLOW);
									try {
										WorkflowView.importServiceDescription(currentDataflow, sd, ServiceTreeClickListener.this.panel);
									} catch (InstantiationException e1) {
										logger.warn(e1.getMessage());
									} catch (IllegalAccessException e1) {
										logger.warn(e1.getMessage());
									}
									
								}
								
							});
							menu.show(evt.getComponent(), evt.getX(), evt
									.getY());
						}
					}
				}
			}
	}
}
