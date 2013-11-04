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
package net.sf.taverna.t2.workbench.views.results.workflow;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sf.taverna.t2.renderers.RendererRegistry;
import net.sf.taverna.t2.workbench.ui.Updatable;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI;
import net.sf.taverna.t2.workbench.views.results.workflow.FilteredDataBundleTreeModel.FilterType;
import uk.org.taverna.databundle.DataBundles;

/**
 * A tab containing result tree for an output port and a panel with rendered result
 * of the currently selected node in the tree.
 *
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class PortResultsViewTab extends JPanel implements Updatable {

	private enum State {NO_DATA, SINGLE_VALUE, LIST}

	private State state;

	// Tree model of results
	DataBundleTreeModel resultModel;

	FilteredDataBundleTreeModel filteredTreeModel;

	// Rendered result component
	private RenderedResultComponent renderedResultComponent;

	private JTree tree;

	private JComboBox<FilterType> filterChoiceBox;

	private final RendererRegistry rendererRegistry;

	private final List<SaveIndividualResultSPI> saveActions;

	private final String name;

	private Path value;

	public PortResultsViewTab(String name, Path value, RendererRegistry rendererRegistry,
			List<SaveIndividualResultSPI> saveActions) {
		super(new BorderLayout());
		this.name = name;
		this.value = value;
		this.rendererRegistry = rendererRegistry;
		this.saveActions = saveActions;

		initComponents();
	}

	private void initComponents() {
		removeAll();
		if (value == null || DataBundles.isMissing(value)) {
			state = State.NO_DATA;
			JLabel noDataMessage = new JLabel("No data available", JLabel.CENTER);
			Font font = noDataMessage.getFont();
			if (font != null) {
				font = font.deriveFont(Math.round((font.getSize() * 1.5))).deriveFont(Font.BOLD);
				noDataMessage.setFont(font);
			}
			add(noDataMessage, BorderLayout.CENTER);
		} else if (DataBundles.isList(value)) {
			state = State.LIST;
			// Results tree (containing DataBundle Paths to all individual results for this port)
			resultModel = new DataBundleTreeModel(value);
			filteredTreeModel = new FilteredDataBundleTreeModel(resultModel);
			tree = new JTree(filteredTreeModel);
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.setExpandsSelectedPaths(true);
			tree.setRootVisible(true);
			tree.setShowsRootHandles(true);
			tree.setCellRenderer(new PortResultCellRenderer());

			tree.addTreeSelectionListener(new TreeSelectionListener() {

				public void valueChanged(TreeSelectionEvent e) {
					TreePath selectionPath = e.getNewLeadSelectionPath();
					if (selectionPath != null) {
						// Get the selected node
						final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectionPath
								.getLastPathComponent();
						renderedResultComponent.setPath((Path) selectedNode.getUserObject());
					}
				}

			});

			filteredTreeModel.addTreeModelListener(new TreeModelListener() {

				public void treeNodesChanged(TreeModelEvent e) {

					tree.expandPath(e.getTreePath());

					// If nothing is currently selected in the tree - select either the
					// result or the first AVAILABLE item in the result list
					/*
					 * if (tree.getSelectionRows() == null || tree.getSelectionRows().length == 0){
					 * ResultTreeNode parent = (ResultTreeNode)e.getTreePath().getLastPathComponent();
					 * // parent of the changed node(s)
					 * int[] indices = e.getChildIndices(); //indexes of the changed node(s)
					 * ResultTreeNode firstChild = (ResultTreeNode) parent.getChildAt(indices[0]); //
					 * get the first changed node
					 * if
					 * (firstChild.getState().equals(ResultTreeNode.ResultTreeNodeState.RESULT_REFERENCE
					 * )){ // if this is the result node rather than result list placeholder
					 * tree.setSelectionPath(new TreePath(firstChild.getPath())); // select this node
					 * }
					 * }
					 */
				}

				public void treeNodesInserted(TreeModelEvent e) {
				}

				public void treeNodesRemoved(TreeModelEvent e) {
				}

				public void treeStructureChanged(TreeModelEvent e) {
				}
			});

			// Component for rendering individual results
			renderedResultComponent = new RenderedResultComponent(rendererRegistry, saveActions);

			// Split pane containing a tree with all results from an output port and
			// rendered result component for individual result rendered currently selected
			// from the tree
			JSplitPane splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

			JPanel leftPanel = new JPanel();
			leftPanel.setLayout(new BorderLayout());

			JPanel treeSubPanel = new JPanel();
			treeSubPanel.setLayout(new BorderLayout());
			treeSubPanel.add(new JLabel("Click in tree to"), BorderLayout.WEST);
			filterChoiceBox = new JComboBox<>(new FilterType[] { FilterType.ALL, FilterType.RESULTS,
					FilterType.ERRORS });
			filterChoiceBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateTree();
				}
			});
			treeSubPanel.add(filterChoiceBox);
			leftPanel.add(treeSubPanel, BorderLayout.NORTH);
			leftPanel.add(new JScrollPane(tree), BorderLayout.CENTER);
			splitPanel.setTopComponent(leftPanel);
			splitPanel.setBottomComponent(renderedResultComponent);
			splitPanel.setDividerLocation(400);

			// Add all to main panel
			add(splitPanel, BorderLayout.CENTER);
		} else {
			state = State.SINGLE_VALUE;
			// Component for rendering individual results
			renderedResultComponent = new RenderedResultComponent(rendererRegistry, saveActions);
			renderedResultComponent.setPath(value);
			add(renderedResultComponent, BorderLayout.CENTER);
		}

	}

	public void setPath(Path value) {
		this.value = value;
		update();
	}

	public void update() {
		if (value == null || DataBundles.isMissing(value)) {
			if (state != State.NO_DATA) {
				initComponents();
			}
		} else if (DataBundles.isList(value)) {
			if (state != State.LIST) {
				initComponents();
			} else {
				resultModel.update();
			}
		} else {
			if (state != State.SINGLE_VALUE) {
				initComponents();
			} else {
				renderedResultComponent.setPath(value);
			}
		}
	}

	public String getName() {
		return name;
	}

	public FilteredDataBundleTreeModel getModel() {
		return filteredTreeModel;
	}

	private List<TreePath> expandedPaths = new ArrayList<TreePath>();
	private TreePath selectionPath = null;

	private void rememberPaths() {
		expandedPaths.clear();
		for (Enumeration<TreePath> e = tree.getExpandedDescendants(new TreePath(filteredTreeModel
				.getRoot())); (e != null) && e.hasMoreElements();) {
			expandedPaths.add(e.nextElement());
		}
		selectionPath = tree.getSelectionPath();
	}

	private void reinstatePaths() {
		for (TreePath path : expandedPaths) {
			if (filteredTreeModel.isShown((DefaultMutableTreeNode) path.getLastPathComponent())) {
				tree.expandPath(path);
			}
		}
		if (selectionPath != null) {
			if (filteredTreeModel.isShown((DefaultMutableTreeNode) selectionPath
					.getLastPathComponent())) {
				tree.setSelectionPath(selectionPath);
			} else {
				tree.clearSelection();
				renderedResultComponent.clearResult();
			}
		}
	}

	private void updateTree() {
		filteredTreeModel.setFilter((FilterType) filterChoiceBox.getSelectedItem());
		rememberPaths();
		filteredTreeModel.reload();
		tree.setModel(filteredTreeModel);
		reinstatePaths();
	}

	public void expandTree() {

		if (tree != null) {
			for (int row = 0; row < tree.getRowCount(); row++) {
				tree.expandRow(row);
			}
		}
	}

}
