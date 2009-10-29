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
package net.sf.taverna.t2.reference.ui.tree;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

/**
 * A wrapper around JTree that installs the set of models, renderers and
 * listeners used by the pre-registration tree control. Implements autoscroll
 * and zooms to any new nodes when added. Handles drop of URLs (from e.g.
 * FireFox), File structures and plain text by creating corresponding POJOs.
 * 
 * @author Tom Oinn
 * 
 */
public class PreRegistrationTree extends JTree implements Autoscroll {
	
	private static Logger logger = Logger
	.getLogger(PreRegistrationTree.class);


	private static final long serialVersionUID = -8357524058131749686L;
	private PreRegistrationTreeModel model;
	private int margin = 15;

	/**
	 * Get the PreRegistrationTreeModel for this tree. Used to get the contents
	 * of the tree as a POJO which can then be registered with the
	 * ReferenceService
	 * 
	 * @return a POJO containing the contents of the tree.
	 */
	public PreRegistrationTreeModel getPreRegistrationTreeModel() {
		return this.model;
	}

	/**
	 * Override this to be informed of status messages from the tree
	 */
	public void setStatusMessage(String message, boolean isError) {
		//
	}
	
	/**
	 * Construct with the depth of the collection to be assembled. This will
	 * instantiate an appropriate internal model and set all the drag and drop
	 * handlers, renderers and cell editing components.
	 * 
	 * @param depth
	 *            the collection depth to use, 0 for single items, 1 for lists
	 *            and so on.
	 */
	public PreRegistrationTree(int depth) {
		this(depth, null);
	}

	/**
	 * Construct with the depth of the collection to be assembled. This will
	 * instantiate an appropriate internal model and set all the drag and drop
	 * handlers, renderers and cell editing components.
	 * 
	 * @param depth
	 *            the collection depth to use, 0 for single items, 1 for lists
	 *            and so on.
	 * @param name Name of the top root of the tree (typically the port name)
	 */
	public PreRegistrationTree(int depth, String name) {
		super();
		if (name == null) {
			model = new PreRegistrationTreeModel(depth);
		} else {
			model = new PreRegistrationTreeModel(depth, name);
		}
		setModel(model);
		setInvokesStopCellEditing(true);
		
		getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		DefaultTreeCellRenderer renderer = new PreRegistrationTreeCellRenderer();
		setRowHeight(0);
		setCellRenderer(renderer);
		
		new PreRegistrationTreeDnDHandler(this) {
			@Override
			public void handleNodeMove(MutableTreeNode source,
					MutableTreeNode target) {
				model.moveNode(source, target);
			}

			@Override
			public void handleFileDrop(MutableTreeNode target,
					List<File> fileList) {
				for (File f : fileList) {
					if (f.isDirectory() == false) {
						model.addPojoStructure(target, f, 0);
					} else {
						if (model.getDepth() < 1) {
							setStatusMessage(
									"Can't add directory to single item input",
									true);
							return;
						}
						// Try to handle directories as flat lists, don't nest
						// any deeper for now.
						List<File> children = new ArrayList<File>();
						for (File child : f.listFiles()) {
							if (child.isFile()) {
								children.add(child);
							}
						}
						model.addPojoStructure(target, children, 1);
					}
				}
			}

			@Override
			public void handleUrlDrop(MutableTreeNode target, URL url) {
				if (url.getProtocol().equalsIgnoreCase("http")) {
					model.addPojoStructure(target, url, 0);
					setStatusMessage("Added URL : " + url.toExternalForm(),
							false);
				} else {
					setStatusMessage("Only http URLs are supported for now.",
							true);
				}
			}

			@Override
			public void handleStringDrop(MutableTreeNode target, String string) {
				model.addPojoStructure(target, string, 0);
				setStatusMessage("Added string from drag and drop", false);
			}
		};
	}

	@Override
	public void setModel(TreeModel model) {
		if (treeModel == model)
			return;
		if (treeModelListener == null)
			treeModelListener = new TreeModelHandler() {
				@Override
				public void treeNodesInserted(final TreeModelEvent ev) {

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							TreePath path = ev.getTreePath();
							setExpandedState(path, true);
							fireTreeExpanded(path);
						}
					});
				}
			};
		if (model != null) {
			model.addTreeModelListener(treeModelListener);
		}
		TreeModel oldValue = treeModel;
		treeModel = model;
		firePropertyChange(TREE_MODEL_PROPERTY, oldValue, model);
	}

	public void autoscroll(Point p) {
		int realrow = getRowForLocation(p.x, p.y);
		Rectangle outer = getBounds();
		realrow = (p.y + outer.y <= margin ? realrow < 1 ? 0 : realrow - 1
				: realrow < getRowCount() - 1 ? realrow + 1 : realrow);
		scrollRowToVisible(realrow);
	}

	public Insets getAutoscrollInsets() {
		Rectangle outer = getBounds();
		Rectangle inner = getParent().getBounds();
		return new Insets(inner.y - outer.y + margin, inner.x - outer.x
				+ margin, outer.height - inner.height - inner.y + outer.y
				+ margin, outer.width - inner.width - inner.x + outer.x
				+ margin);
	}
	
	public int getRowCount() {
		int result = super.getRowCount();
		logger.info("Row count is " + result);
		return result;
	}

}
