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
/*

package org.apache.taverna.workbench.ui.servicepanel.tree;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.WEST;
import static java.awt.Color.GRAY;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static javax.swing.SwingUtilities.invokeLater;
import static org.apache.taverna.lang.ui.EdgeLineBorder.TOP;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.apache.taverna.lang.ui.EdgeLineBorder;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public abstract class TreePanel extends JPanel {
	private static int MAX_EXPANSION = 100;
	private static final int SEARCH_WIDTH = 15;
	private static Logger logger = Logger.getLogger(TreePanel.class);

	protected Set<List<Object>> expandedPaths = new HashSet<>();
	protected FilterTreeModel filterTreeModel;
	protected JTextField searchField = new JTextField(SEARCH_WIDTH);
	protected JTree tree = new JTree();
	protected JScrollPane treeScrollPane;

	private String availableObjectsString = "";
	private String matchingObjectsString = "";
	private String noMatchingObjectsString = "";

	private TreeExpandCollapseListener treeExpandListener = new TreeExpandCollapseListener();
	private Object filterLock = new Object();

	public TreePanel(FilterTreeModel treeModel) {
		filterTreeModel = treeModel;
	}

	public void expandTreePaths() throws InterruptedException,
			InvocationTargetException {
//		Filter appliedFilter = filterTreeModel.getCurrentFilter();
//		if (appliedFilter == null) {
			for (int i = 0; (i < tree.getRowCount()) && (i < MAX_EXPANSION); i++)
				tree.expandRow(i);
//		} else {
//			boolean rowsFinished = false;
//			for (int i = 0; (!appliedFilter.isSuperseded()) && (!rowsFinished)
//					&& (i < MAX_EXPANSION); i++) {
//				TreePath tp = tree.getPathForRow(i);
//				if (tp == null) {
//					rowsFinished = true;
//				} else {
//					if (!appliedFilter.pass((DefaultMutableTreeNode) tp
//							.getLastPathComponent())) {
//						tree.expandRow(i);
//					}
//				}
//			}
//		}
	}

	public void expandAll(FilterTreeNode node, boolean expand) {
        @SuppressWarnings("unused")
		FilterTreeNode root = (FilterTreeNode) tree.getModel().getRoot();

        // Traverse tree from root
        expandAll(new TreePath(node.getPath()), expand);
    }

    @SuppressWarnings("rawtypes")
	private void expandAll(TreePath parent, boolean expand) {
        // Traverse children
        FilterTreeNode node = (FilterTreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0)
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                FilterTreeNode n = (FilterTreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(path, expand);
            }

        // Expansion or collapse must be done bottom-up
        if (expand)
            tree.expandPath(parent);
        else
            tree.collapsePath(parent);
    }

	protected void initialize() {
		setLayout(new BorderLayout());
		treeScrollPane = new JScrollPane(tree);
		tree.setModel(filterTreeModel);
		tree.addTreeExpansionListener(treeExpandListener);
		tree.setCellRenderer(createCellRenderer());
		tree.setSelectionModel(new FilterTreeSelectionModel());

		JPanel topPanel = new JPanel();
		topPanel.setBorder(new CompoundBorder(new EdgeLineBorder(TOP, GRAY), new EmptyBorder(10, 5, 0, 5)));
		topPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JLabel filterLabel = new JLabel("Filter:  ");
		c.fill = NONE;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.LINE_START;
		topPanel.add(filterLabel, c);

		c.fill = HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1.0;
		topPanel.add(searchField, c);


		c.fill = NONE;
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.0;
		final JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchField.setText("");
				invokeLater(new RunFilter());
				clearButton.getParent().requestFocusInWindow();// so that the button does not stay focused after it is clicked on and did its action
			}
		});
		topPanel.add(clearButton, c);

		c.gridx = 3;
		c.weightx = 0.2;
		topPanel.add(new JPanel(), c);

		JPanel topExtraPanel = new JPanel(new BorderLayout());

		topExtraPanel.add(topPanel, NORTH);

		Component extraComponent = createExtraComponent();
		if (extraComponent != null) {
			JPanel extraPanel  = new JPanel();
			extraPanel.setLayout(new BorderLayout());
			extraPanel.add(extraComponent, WEST);
			topExtraPanel.add(extraPanel, CENTER);
		}

		add(topExtraPanel, NORTH);
		add(treeScrollPane, CENTER);

		searchField.addKeyListener(new SearchFieldKeyAdapter());
	}

	protected Component createExtraComponent() {
		return null;
	}

	protected TreeCellRenderer createCellRenderer() {
		return new FilterTreeCellRenderer();
	}

	public void runFilter() throws InterruptedException,
			InvocationTargetException {
		/*
		 * Special lock object, don't do a synchronized model, as the lock on
		 * JComponent might deadlock when painting the panel - see comments at
		 * http://www.mygrid.org.uk/dev/issues/browse/T2-1438
		 */
		synchronized (filterLock) {
			tree.removeTreeExpansionListener(treeExpandListener);
			String text = searchField.getText();
			FilterTreeNode root = (FilterTreeNode) tree.getModel().getRoot();
			if (text.isEmpty()) {
				setFilter(null);
				root.setUserObject(getAvailableObjectsString());
				filterTreeModel.nodeChanged(root);
				for (List<Object> tp : expandedPaths) {
	//				for (int i = 0; i < tp.length; i++)
	//					logger.info("Trying to expand " + tp[i]);
					tree.expandPath(filterTreeModel.getTreePathForObjectPath(tp));
				}
			} else {
				setFilter(createFilter(text));
				root.setUserObject(root.getChildCount() > 0 ? getMatchingObjectsString()
						: getNoMatchingObjectsString());
				filterTreeModel.nodeChanged(root);
				expandTreePaths();
			}
			tree.addTreeExpansionListener(treeExpandListener);
		}
	}

	/**
	 * @return the availableObjectsString
	 */
	public String getAvailableObjectsString() {
		return availableObjectsString;
	}

	/**
	 * @param availableObjectsString the availableObjectsString to set
	 */
	public void setAvailableObjectsString(String availableObjectsString) {
		this.availableObjectsString = availableObjectsString;
	}

	/**
	 * @return the matchingObjectsString
	 */
	public String getMatchingObjectsString() {
		return matchingObjectsString;
	}

	/**
	 * @param matchingObjectsString the matchingObjectsString to set
	 */
	public void setMatchingObjectsString(String matchingObjectsString) {
		this.matchingObjectsString = matchingObjectsString;
	}

	/**
	 * @return the noMatchingObjectsString
	 */
	public String getNoMatchingObjectsString() {
		return noMatchingObjectsString;
	}

	/**
	 * @param noMatchingObjectsString the noMatchingObjectsString to set
	 */
	public void setNoMatchingObjectsString(String noMatchingObjectsString) {
		this.noMatchingObjectsString = noMatchingObjectsString;
	}

	public Filter createFilter(String text) {
		return new MyFilter(text);
	}

	public void setFilter(Filter filter) {
		if (tree.getCellRenderer() instanceof FilterTreeCellRenderer)
			((FilterTreeCellRenderer)tree.getCellRenderer()).setFilter(filter);
		filterTreeModel.setFilter(filter);
	}

	protected class ExpandRowRunnable implements Runnable {
		int rowNumber;

		public ExpandRowRunnable(int rowNumber) {
			this.rowNumber = rowNumber;
		}

		@Override
		public void run() {
			tree.expandRow(rowNumber);
		}
	}

	protected class RunFilter implements Runnable {
		@Override
		public void run() {
			Filter oldFilter = filterTreeModel.getCurrentFilter();
			if (oldFilter != null)
				oldFilter.setSuperseded(true);
			try {
				runFilter();
			} catch (InterruptedException e) {
				Thread.interrupted();
			} catch (InvocationTargetException e) {
				logger.error("", e);
			}
		}
	}

	protected class SearchFieldKeyAdapter extends KeyAdapter {
		private final Runnable runFilterRunnable;
		Timer timer = new Timer("Search field timer", true);

		private SearchFieldKeyAdapter() {
			this.runFilterRunnable = new RunFilter();
		}

		@Override
		public void keyReleased(KeyEvent e) {
			timer.cancel();
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					invokeLater(runFilterRunnable);
				}
			}, 500);
		}
	}

	private void noteExpansions() {
		expandedPaths.clear();
		TreePath rootPath = new TreePath(filterTreeModel.getRoot());
		for (Enumeration<TreePath> e = tree.getExpandedDescendants(rootPath); e.hasMoreElements();) {
			List<Object> userObjects = new ArrayList<>();
			Object[] expandedPath = e.nextElement().getPath();
			for (int i = 0; i < expandedPath.length; i++) {
				FilterTreeNode node = (FilterTreeNode) expandedPath[i];
//				logger.info("The object in the path is a " + expandedPath[i].getClass());
				userObjects.add(node.getUserObject());
//				logger.info("Added " + node.getUserObject() + " to path");
			}
			expandedPaths.add(userObjects);
		}
	}
	
	protected class TreeExpandCollapseListener implements TreeExpansionListener {
		@Override
		public void treeCollapsed(TreeExpansionEvent event) {
			noteExpansions();
		}

		@Override
		public void treeExpanded(TreeExpansionEvent event) {
			noteExpansions();
		}
	}
}
