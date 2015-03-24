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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.WEST;
import static java.awt.Font.BOLD;
import static java.lang.Math.round;
import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;
import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

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

import net.sf.taverna.t2.renderers.RendererRegistry;
import net.sf.taverna.t2.workbench.ui.Updatable;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI;
import net.sf.taverna.t2.workbench.views.results.workflow.FilteredDataBundleTreeModel.FilterType;
import org.apache.taverna.databundle.DataBundles;
import org.apache.taverna.scufl2.api.port.Port;

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
	/** Tree model of results */
	private DataBundleTreeModel resultModel;
	private FilteredDataBundleTreeModel filteredTreeModel;
	/** Rendered result component */
	private RenderedResultComponent renderedResultComponent;
	private JTree tree;
	private JComboBox<FilterType> filterChoiceBox;
	private final RendererRegistry rendererRegistry;
	private final List<SaveIndividualResultSPI> saveActions;
	private final Port port;
	private Path value;

	public PortResultsViewTab(Port port, Path value, RendererRegistry rendererRegistry,
			List<SaveIndividualResultSPI> saveActions) {
		super(new BorderLayout());
		this.port = port;
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
				font = font.deriveFont(round(font.getSize() * 1.5)).deriveFont(BOLD);
				noDataMessage.setFont(font);
			}
			add(noDataMessage, CENTER);
		} else if (DataBundles.isList(value)) {
			state = State.LIST;
			initListComponents();
		} else {
			state = State.SINGLE_VALUE;
			// Component for rendering individual results
			renderedResultComponent = new RenderedResultComponent(rendererRegistry, saveActions);
			renderedResultComponent.setPath(value);
			add(renderedResultComponent, CENTER);
		}
		revalidate();
	}

	private void initListComponents() {
		// Results tree (containing DataBundle Paths to all individual results for this port)
		resultModel = new DataBundleTreeModel(value);
		filteredTreeModel = new FilteredDataBundleTreeModel(resultModel);
		tree = new JTree(filteredTreeModel);
		tree.getSelectionModel().setSelectionMode(SINGLE_TREE_SELECTION);
		tree.setExpandsSelectedPaths(true);
		tree.setRootVisible(true);
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new PortResultCellRenderer());

		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				TreePath selectionPath = e.getNewLeadSelectionPath();
				if (selectionPath != null) {
					// Get the selected node
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectionPath
							.getLastPathComponent();
					renderedResultComponent.setPath((Path) selectedNode.getUserObject());
				}
			}
		});

		filteredTreeModel.addTreeModelListener(new TreeModelListener() {
			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				TreePath treePath = e.getTreePath();
				tree.expandPath(treePath);
				// If nothing is currently selected in the tree - select the first item in the result list
				if (tree.getSelectionRows() == null
						|| tree.getSelectionRows().length == 0) {
					DefaultMutableTreeNode firstLeaf = ((DefaultMutableTreeNode) filteredTreeModel
							.getRoot()).getFirstLeaf();
					tree.setSelectionPath(new TreePath(firstLeaf.getPath()));
				}
			}

			@Override
			public void treeNodesInserted(TreeModelEvent e) {
			}

			@Override
			public void treeNodesRemoved(TreeModelEvent e) {
			}

			@Override
			public void treeStructureChanged(TreeModelEvent e) {
			}
		});

		// Component for rendering individual results
		renderedResultComponent = new RenderedResultComponent(rendererRegistry, saveActions);

		/*
		 * Split pane containing a tree with all results from an output port and
		 * rendered result component for individual result rendered currently
		 * selected from the tree
		 */
		JSplitPane splitPanel = new JSplitPane(HORIZONTAL_SPLIT);

		JPanel leftPanel = new JPanel(new BorderLayout());

		JPanel treeSubPanel = new JPanel(new BorderLayout());
		treeSubPanel.add(new JLabel("Click in tree to"), WEST);
		filterChoiceBox = new JComboBox<>(new FilterType[] { FilterType.ALL,
				FilterType.RESULTS, FilterType.ERRORS });
		filterChoiceBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateTree();
			}
		});
		treeSubPanel.add(filterChoiceBox);
		leftPanel.add(treeSubPanel, NORTH);
		leftPanel.add(new JScrollPane(tree), CENTER);
		splitPanel.setTopComponent(leftPanel);
		splitPanel.setBottomComponent(renderedResultComponent);
		splitPanel.setDividerLocation(400);

		// Add all to main panel
		add(splitPanel, CENTER);

		DefaultMutableTreeNode firstLeaf = ((DefaultMutableTreeNode) filteredTreeModel
				.getRoot()).getFirstLeaf();
		tree.setSelectionPath(new TreePath(firstLeaf.getPath()));
	}

	@Override
	public void update() {
		if (value == null || DataBundles.isMissing(value)) {
			if (state != State.NO_DATA)
				scheduleInitComponents();
		} else if (DataBundles.isList(value)) {
			if (state != State.LIST)
				scheduleInitComponents();
			else if (resultModel != null)
				resultModel.update();
		} else {
			if (state != State.SINGLE_VALUE)
				scheduleInitComponents();
		}
	}

	private void scheduleInitComponents() {
		invokeLater(new Runnable() {
			@Override
			public void run() {
				initComponents();
			}
		});
	}

	public Port getPort() {
		return port;
	}

	public FilteredDataBundleTreeModel getModel() {
		return filteredTreeModel;
	}

	private List<TreePath> expandedPaths = new ArrayList<>();
	private TreePath selectionPath = null;

	private void rememberPaths() {
		expandedPaths.clear();
		for (Enumeration<TreePath> e = tree
				.getExpandedDescendants(new TreePath(filteredTreeModel
						.getRoot())); (e != null) && e.hasMoreElements();)
			expandedPaths.add(e.nextElement());
		selectionPath = tree.getSelectionPath();
	}

	private void reinstatePaths() {
		for (TreePath path : expandedPaths)
			if (filteredTreeModel.isShown((DefaultMutableTreeNode) path
					.getLastPathComponent()))
				tree.expandPath(path);
		if (selectionPath != null) {
			if (filteredTreeModel
					.isShown((DefaultMutableTreeNode) selectionPath
							.getLastPathComponent()))
				tree.setSelectionPath(selectionPath);
			else {
				tree.clearSelection();
				renderedResultComponent.clearResult();
			}
		}
	}

	private void updateTree() {
		filteredTreeModel.setFilter((FilterType) filterChoiceBox
				.getSelectedItem());
		rememberPaths();
		filteredTreeModel.reload();
		tree.setModel(filteredTreeModel);
		reinstatePaths();
	}

	public void expandTree() {
		if (tree != null)
			for (int row = 0; row < tree.getRowCount(); row++)
				tree.expandRow(row);
	}
}
