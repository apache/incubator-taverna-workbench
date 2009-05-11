/*******************************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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
package net.sf.taverna.t2.workbench.ui.servicepanel.tree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
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

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public abstract class TreePanel extends JPanel {

	private static int MAX_EXPANSION = 100;
	private static final int SEARCH_WIDTH = 15;

	protected Set<List<Object>> expandedPaths = new HashSet<List<Object>>();
	protected FilterTreeModel filterTreeModel;
	protected JTextField searchField = new JTextField(SEARCH_WIDTH);
	protected JTree tree = new JTree();
	protected JScrollPane treeScrollPane;
	
	private String availableObjectsString = "";
	private String matchingObjectsString = "";
	private String noMatchingObjectsString = "";

	private TreeExpandCollapseListener treeExpandListener = new TreeExpandCollapseListener();
	
	private static Logger logger = Logger
	.getLogger(TreePanel.class);

	public TreePanel(FilterTreeModel treeModel) {
		filterTreeModel = treeModel;
		initialize();
	}

	public void expandTreePaths() throws InterruptedException,
			InvocationTargetException {
//		Filter appliedFilter = filterTreeModel.getCurrentFilter();
//		if (appliedFilter == null) {
			for (int i = 0; (i < tree.getRowCount()) && (i < MAX_EXPANSION); i++) {
				tree.expandRow(i);
			}
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
        FilterTreeNode root = (FilterTreeNode) tree.getModel().getRoot();
    
        // Traverse tree from root
        expandAll(new TreePath(node.getPath()), expand);
    }
	
    private void expandAll(TreePath parent, boolean expand) {
        // Traverse children
        FilterTreeNode node = (FilterTreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                FilterTreeNode n = (FilterTreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(path, expand);
            }
        }
    
        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

	protected void initialize() {
		setLayout(new BorderLayout());
		treeScrollPane = new JScrollPane(tree);
		tree.setModel(filterTreeModel);
		tree.addTreeExpansionListener(treeExpandListener);
		tree.setCellRenderer(createCellRenderer());
		tree.setSelectionModel(new FilterTreeSelectionModel());
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JLabel filterLabel = new JLabel("Filter:  ");
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.LINE_START;
		topPanel.add(filterLabel, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1.0;
		topPanel.add(searchField, c);
		

		c.fill = GridBagConstraints.NONE;
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.0;
		topPanel.add(new JButton(new ClearAction()), c);
		
		c.gridx = 3;
		c.weightx = 0.2;
		topPanel.add(new JPanel(), c);
		
		JPanel topExtraPanel = new JPanel();
		topExtraPanel.setLayout(new BorderLayout());

		topExtraPanel.add(topPanel, BorderLayout.NORTH);
		
		Component extraComponent = createExtraComponent();
		if (extraComponent != null) {
			JPanel extraPanel  = new JPanel();
			extraPanel.setLayout(new BorderLayout());
			extraPanel.add(extraComponent, BorderLayout.WEST);
			topExtraPanel.add(extraPanel, BorderLayout.CENTER);
	}
	
		add(topExtraPanel, BorderLayout.NORTH);

		add(treeScrollPane, BorderLayout.CENTER);

		searchField.addKeyListener(new SearchFieldKeyAdapter());
	}

	protected Component createExtraComponent() {
		return null;
	}

	protected TreeCellRenderer createCellRenderer() {
		return new FilterTreeCellRenderer();
	}

	public synchronized void runFilter() throws InterruptedException,
			InvocationTargetException {
		tree.removeTreeExpansionListener(treeExpandListener);
		String text = searchField.getText();
		final FilterTreeNode root = (FilterTreeNode) tree.getModel().getRoot();
		if (text.length() == 0) {
			setFilter(null);
					root.setUserObject(getAvailableObjectsString());
					filterTreeModel.nodeChanged(root);
			for (List<Object> tp : expandedPaths) {
//				for (int i = 0; i < tp.length; i++) {
//					logger.info("Trying to expand " + tp[i].toString());
//				}
				tree.expandPath(filterTreeModel.getTreePathForObjectPath(tp));
			}
		} else {
			setFilter(createFilter(text));
			if (root.getChildCount() > 0) {
				root.setUserObject(getMatchingObjectsString());
				} else {
					root.setUserObject(getNoMatchingObjectsString());
				}
			filterTreeModel.nodeChanged(root);
			expandTreePaths();
		}
		tree.addTreeExpansionListener(treeExpandListener);
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
		if (tree.getCellRenderer() instanceof FilterTreeCellRenderer) {
			((FilterTreeCellRenderer)tree.getCellRenderer()).setFilter(filter);
		}
		filterTreeModel.setFilter(filter);
		
	}

	protected class ClearAction extends AbstractAction {
		private ClearAction() {
			super("Clear");
		}

		public void actionPerformed(ActionEvent e) {
			searchField.setText("");
			SwingUtilities.invokeLater(new RunFilter());
		}
	}

	protected class ExpandRowRunnable implements Runnable {
		int rowNumber;

		public ExpandRowRunnable(int rowNumber) {
			this.rowNumber = rowNumber;
		}

		public void run() {
			tree.expandRow(rowNumber);
		}

	}
	
	protected class RunFilter implements Runnable {
		public void run() {
			Filter oldFilter = filterTreeModel.getCurrentFilter();
			if (oldFilter != null) {
				oldFilter.setSuperseded(true);
			}
			try {
				runFilter();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected class SearchFieldKeyAdapter extends KeyAdapter {
		private final Runnable runFilterRunnable;
		Timer timer = new Timer("Search field timer");

		private SearchFieldKeyAdapter() {
			this.runFilterRunnable = new RunFilter();
		}

		public void keyReleased(KeyEvent e) {
			timer.cancel();
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					SwingUtilities.invokeLater(runFilterRunnable);
				}

			}, 500);
		}
	}

	protected class TreeExpandCollapseListener implements TreeExpansionListener {
		
		private void noteExpansions() {
			expandedPaths.clear();
			TreePath rootPath = new TreePath(filterTreeModel.getRoot());
			for (Enumeration<TreePath> e = tree.getExpandedDescendants(rootPath); e.hasMoreElements();) {
				List<Object> userObjects = new ArrayList<Object>();
				Object[] expandedPath = e.nextElement().getPath();
				for (int i = 0; i < expandedPath.length; i++) {
					FilterTreeNode node = (FilterTreeNode) expandedPath[i];
//					logger.info("The object in the path is a " + expandedPath[i].getClass().getCanonicalName());
					userObjects.add(node.getUserObject());
//					logger.info("Added " + node.getUserObject() + " to path");
				}
				expandedPaths.add(userObjects);
			}
			
		}

		public void treeCollapsed(TreeExpansionEvent event) {
			noteExpansions();
		}
		public void treeExpanded(TreeExpansionEvent event) {
			noteExpansions();
		}
	}

}
