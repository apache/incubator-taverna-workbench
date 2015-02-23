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
/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package net.sf.taverna.t2.workbench.iterationstrategy.editor;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.iterationstrategy.IterationStrategyIcons;
import net.sf.taverna.t2.workflowmodel.processor.iteration.CrossProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DotProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategy;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.TerminalNode;

import org.apache.log4j.Logger;

/**
 * A control panel for the iteration tree editor allowing the user to manipulate
 * the tree, removing and adding nodes into the tree based on the context.
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * 
 */
@SuppressWarnings("serial")
public class IterationStrategyEditorControl extends JPanel {

	protected static Set<IterationStrategyNode> descendentsOfNode(
			IterationStrategyNode node) {
		Set<IterationStrategyNode> descendants = new HashSet<IterationStrategyNode>();
		Set<IterationStrategyNode> nodesToVisit = new HashSet<IterationStrategyNode>();
		Set<IterationStrategyNode> visitedNodes = new HashSet<IterationStrategyNode>();

		// Note: Not added to descendants
		nodesToVisit.add(node);
		while (!nodesToVisit.isEmpty()) {
			// pick the first one
			IterationStrategyNode visiting = nodesToVisit.iterator().next();
			visitedNodes.add(visiting);
			nodesToVisit.remove(visiting);

			// Find new and interesting children
			List<IterationStrategyNode> children = visiting.getChildren();
			Set<IterationStrategyNode> newNodes = new HashSet<IterationStrategyNode>(
					children);
			newNodes.removeAll(visitedNodes);

			descendants.addAll(newNodes);
			nodesToVisit.addAll(newNodes);
		}
		return descendants;
	}

	private static Logger logger = Logger
			.getLogger(IterationStrategyEditorControl.class);

	private IterationStrategyNode selectedNode = null;

	private IterationStrategyTree tree;

	protected AddCrossAction addCross = new AddCrossAction();
	protected AddDotAction addDot = new AddDotAction();
	protected ChangeAction change = new ChangeAction();
	protected NormalizeAction normalize = new NormalizeAction();
	protected RemoveAction remove = new RemoveAction();
	protected MoveUpAction moveUp = new MoveUpAction();

	//private static final int ICON_SIZE = 15;

	protected ImageIcon arrowUpIcon = WorkbenchIcons.upArrowIcon;
	protected ImageIcon arrowDownIcon = WorkbenchIcons.downArrowIcon;
	//protected ImageIcon arrowLeft = WorkbenchIcons.leftArrowIcon;
	//protected ImageIcon arrowRight = WorkbenchIcons.rightArrowIcon;
	protected ImageIcon normalizeIcon = WorkbenchIcons.normalizeIcon;

	private final IterationStrategy strategy;

	/**
	 * Create a new panel from the supplied iteration strategy
	 */
	public IterationStrategyEditorControl(IterationStrategy strategy) {

		this.strategy = strategy;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		// Create the components
		tree = new IterationStrategyEditor(strategy);

		JButton addCrossButton = new JButton(addCross);
		addCrossButton.setHorizontalAlignment(SwingConstants.LEFT);
		JButton addDotButton = new JButton(addDot);
		addDotButton.setHorizontalAlignment(SwingConstants.LEFT);
		JButton normalizeButton = new JButton(normalize);
		normalizeButton.setHorizontalAlignment(SwingConstants.LEFT);
		normalizeButton.setIcon(normalizeIcon);
		JButton removeButton = new JButton(remove);
		removeButton.setHorizontalAlignment(SwingConstants.LEFT);
		JButton changeButton = new JButton(change);
		changeButton.setHorizontalAlignment(SwingConstants.LEFT);

		JButton moveUpButton = new JButton(moveUp);
		moveUpButton.setIcon(arrowUpIcon);
		moveUpButton.setHorizontalAlignment(SwingConstants.LEFT);

		// Set the default enabled state to off on all buttons other than the
		// normalizeButton
		// one.
		disableButtons();

		// Create a layout with the tree on the right and the buttons in a grid
		// layout on the left
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		// toolbar.setLayout(new GridLayout(2,2));
		toolbar.add(normalizeButton);
		toolbar.add(addCrossButton);
		toolbar.add(addDotButton);
		toolbar.add(removeButton);
		toolbar.add(changeButton);
		toolbar.add(moveUpButton);

		toolbar.setAlignmentX(LEFT_ALIGNMENT);

		// Listen to tree selection events and enable buttons appropriately
		tree.addTreeSelectionListener(new ButtonEnabler());

		// Add components to the control panel
		add(toolbar);
		JScrollPane treePane = new JScrollPane(tree);
		//treePane.setPreferredSize(new Dimension(0, 0));
		add(treePane);
	}

	public void setIterationStrategy(IterationStrategy iterationStrategy) {
		tree.setIterationStrategy(iterationStrategy);
		disableButtons();
		selectNode(null);
	}

	private void disableButtons() {
		remove.setEnabled(false);
		addCross.setEnabled(false);
		addDot.setEnabled(false);
		change.setEnabled(false);
	}

	private IterationStrategyNode findRoot() {
		IterationStrategyNode root = (IterationStrategyNode) tree.getModel()
				.getRoot();
		if (root.getChildCount() > 0) {
			return root.getChildAt(0);
		}
		return root;
	}

	protected void selectNode(TreeNode newNode) {
		DefaultTreeModel model = tree.getModel();
		if (newNode == null) {
			newNode = (TreeNode) model.getRoot();
		}
		TreeNode[] pathToRoot = model.getPathToRoot(newNode);
		tree.setSelectionPath(new TreePath(pathToRoot));
	}

	/**
	 * Add a cross product node as a child of the selected node
	 */
	protected class AddCrossAction extends AbstractAction {

		public AddCrossAction() {
			super("Add Cross", IterationStrategyIcons.joinIteratorIcon);
		}

		public void actionPerformed(ActionEvent e) {
			CrossProduct newNode = new CrossProduct();
			newNode.setParent(selectedNode);
			tree.refreshModel();
		}
	}

	/**
	 * Add a dot product node as a child of the selected node
	 * 
	 * @author Stian Soiland-Reyes
	 * 
	 */
	protected class AddDotAction extends AbstractAction {

		public AddDotAction() {
			super("Add Dot", IterationStrategyIcons.lockStepIteratorIcon);
		}

		public void actionPerformed(ActionEvent e) {
			DotProduct newNode = new DotProduct();
			newNode.setParent(selectedNode);
			tree.refreshModel();
		}
	}

	protected class ButtonEnabler implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {
			TreePath selectedPath = e.getPath();
			IterationStrategyNode selectedObject = (IterationStrategyNode) selectedPath
					.getLastPathComponent();
			selectedNode = selectedObject;
			if (selectedObject instanceof CrossProduct
					|| selectedObject instanceof DotProduct) {
				if ((selectedObject.getParent() == null) || (selectedObject.getParent() instanceof TerminalNode)) {
					remove.setEnabled(false);
				} else {
					remove.setEnabled(true);
				}
				if (selectedObject instanceof CrossProduct) {
					change.putValue(Action.NAME, "Change to Dot Product");
					change.putValue(Action.SMALL_ICON,
							IterationStrategyIcons.lockStepIteratorIcon);
				} else {
					change.putValue(Action.NAME, "Change to Cross Product");
					change.putValue(Action.SMALL_ICON,
							IterationStrategyIcons.joinIteratorIcon);
				}
				addCross.setEnabled(true);
				addDot.setEnabled(true);
				change.setEnabled(true);
			} else {
				// Top- or leaf node
				remove.setEnabled(false);
				addCross.setEnabled(false);
				addDot.setEnabled(false);
				change.setEnabled(false);
			}
		}
	}

	/**
	 * Add a cross product node as a child of the selected node
	 */
	protected class ChangeAction extends AbstractAction {

		public ChangeAction() {
			super("Switch to...", IterationStrategyIcons.joinIteratorIcon);
		}

		public void actionPerformed(ActionEvent e) {
			IterationStrategyNode newNode;
			if (selectedNode instanceof CrossProduct) {
				newNode = new DotProduct();
			} else {
				newNode = new CrossProduct();
			}

			List<IterationStrategyNode> children = new ArrayList<IterationStrategyNode>(
					selectedNode.getChildren());
			for (IterationStrategyNode child : children) {
				child.setParent(newNode);
			}

			DefaultTreeModel model = tree.getModel();
			if (selectedNode.getParent() == null) {
				model.setRoot(newNode);
				tree.refreshModel();
				newNode.setParent(null);
			} else {
				IterationStrategyNode parent = selectedNode.getParent();
				int index = parent.getIndex(selectedNode);
				selectedNode.setParent(null);
				parent.insert(newNode, index);
				tree.refreshModel();
			}

			selectNode(newNode);
		}

	}

	/**
	 * Normalize the tree when the button is pressed
	 * 
	 */
	protected class NormalizeAction extends AbstractAction {
		public NormalizeAction() {
			super("Normalize", normalizeIcon);
		}

		public void actionPerformed(ActionEvent e) {
			strategy.normalize();
			// Expand all the nodes in the tree
			//DefaultTreeModel model = tree.getModel();
			tree.refreshModel();
		}
	}

	/**
	 * Remove the selected node, moving any descendant leaf nodes to the parent
	 * to prevent them getting lost
	 */
	protected class RemoveAction extends AbstractAction {
		public RemoveAction() {
			super("Remove node", WorkbenchIcons.deleteIcon);
		}

		public void actionPerformed(ActionEvent e) {
			IterationStrategyNode nodeToBeRemoved = selectedNode;

			//DefaultTreeModel model = tree.getModel();

			// Now removeButton the candidate nodes from their parents and
			// put them back into the root node
			IterationStrategyNode root = findRoot();
			if (root == selectedNode) {
				return;
			}
			IterationStrategyNode oldParent = nodeToBeRemoved.getParent();

			for (IterationStrategyNode nodeToMove : descendentsOfNode(nodeToBeRemoved)) {
				nodeToMove.setParent(oldParent);
			}
			nodeToBeRemoved.setParent(null);
			tree.refreshModel();
			// Disable the various buttons, as the current selection
			// is now invalid.
			remove.setEnabled(false);
			addCross.setEnabled(false);
			addDot.setEnabled(false);
			change.setEnabled(false);
			selectNode(oldParent);
		}
	}

	protected class MoveUpAction extends AbstractAction {

		public MoveUpAction() {
			super("Move up", arrowUpIcon);
		}

		public void actionPerformed(ActionEvent e) {
			//DefaultTreeModel model = tree.getModel();

			IterationStrategyNode aboveNode = aboveSelectedNode();
			if ((aboveNode == null) || ((aboveNode instanceof TerminalNode) && (aboveNode.getChildCount() > 0))) {
				logger.warn("Can't move above top");
				return;
			}
			IterationStrategyNode selectedParent = selectedNode.getParent();
			IterationStrategyNode aboveParent = aboveNode.getParent();
			if (selectedParent != null && selectedParent.equals(aboveParent)) {
				// Siblings
				int aboveChildIndex = selectedParent.getIndex(aboveNode);
				selectedParent.insert(selectedNode, aboveChildIndex);
				tree.refreshModel();
				selectNode(selectedNode);
			} else if (aboveNode.equals(selectedParent)) {
				if (aboveParent instanceof TerminalNode
						&& selectedNode.getAllowsChildren()) {
					aboveNode.setParent(selectedNode);
					selectedNode.setParent(aboveParent);
					tree.refreshModel();
					selectNode(selectedNode);
				} else if (!(aboveParent instanceof TerminalNode)){
					int aboveChildIndex = aboveParent.getIndex(aboveNode);
					aboveParent.insert(selectedNode, aboveChildIndex);
					tree.refreshModel();
					selectNode(selectedNode);
				}
			} else {

			}

		}

	}

	protected IterationStrategyNode belowSelectedNode() {
		return offsetFromSelectedNode(1);
	}

	protected IterationStrategyNode offsetFromSelectedNode(int offset) {
		int currentRow = tree.getRowForPath(tree.getSelectionPath());
		int offsetRow = currentRow + offset;
		TreePath offsetPath = tree.getPathForRow(offsetRow);
		if (offsetPath == null) {
			return null;
		}
		IterationStrategyNode offsetNode = (IterationStrategyNode) offsetPath
				.getLastPathComponent();
		if (offsetNode == tree.getModel().getRoot()) {
			return null;
		}
		return offsetNode;
	}

	protected IterationStrategyNode aboveSelectedNode() {
		return offsetFromSelectedNode(-1);
	}

}
