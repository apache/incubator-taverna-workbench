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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
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
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class TreePanel extends JPanel {

	private static int MAX_EXPANSION = 100;
	private static final int SEARCH_WIDTH = 15;

	protected Set<TreePath> expandedPaths = new HashSet<TreePath>();
	protected FilterTreeModel filterTreeModel;
	protected JTextField searchField = new JTextField(SEARCH_WIDTH);
	protected JTree tree = new JTree();
	protected JScrollPane treeScrollPane;

	public TreePanel(FilterTreeModel treeModel) {
		filterTreeModel = treeModel;
		initialize();
	}

	protected void expandTreePaths() throws InterruptedException,
			InvocationTargetException {
		Filter appliedFilter = filterTreeModel.getCurrentFilter();
		if (appliedFilter == null) {
			for (int i = 0; (i < tree.getRowCount()) && (i < MAX_EXPANSION); i++) {
				tree.expandRow(i);
			}
		} else {
			boolean rowsFinished = false;
			for (int i = 0; (!appliedFilter.isSuperseded()) && (!rowsFinished)
					&& (i < MAX_EXPANSION); i++) {
				TreePath tp = tree.getPathForRow(i);
				if (tp == null) {
					rowsFinished = true;
				} else {
					if (!appliedFilter.pass((DefaultMutableTreeNode) tp
							.getLastPathComponent())) {
						tree.expandRow(i);
					}
				}
			}
		}
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		treeScrollPane = new JScrollPane(tree);
		tree.setModel(filterTreeModel);
		tree.addTreeWillExpandListener(new TreeExpandListener());
		tree.setCellRenderer(createCellRenderer());
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JLabel filterLabel = new JLabel("Filter:  ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.LINE_START;
		topPanel.add(filterLabel, c);

		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1.0;
		topPanel.add(searchField, c);

		c.fill = GridBagConstraints.NONE;
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.0;
		topPanel.add(new JButton(new ClearAction()), c);
		add(topPanel, BorderLayout.NORTH);
		add(treeScrollPane, BorderLayout.CENTER);

		searchField.addKeyListener(new SearchFieldKeyAdapter());
	}

	protected TreeCellRenderer createCellRenderer() {
		return new FilterTreeCellRenderer();
	}

	protected synchronized void runFilter() throws InterruptedException,
			InvocationTargetException {
		String text = searchField.getText();
		if (text.length() == 0) {
			setFilter(null);
			for (TreePath tp : expandedPaths) {
				tree.expandPath(tp);
			}
		} else {
			setFilter(createFilter(text));
			expandTreePaths();
		}

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
			try {
				runFilter();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
		Timer timer = new Timer();

		private SearchFieldKeyAdapter() {
			this.runFilterRunnable = new RunFilter();
		}

		public void keyReleased(KeyEvent e) {
			timer.cancel();
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					new Thread(runFilterRunnable).start();
				}

			}, 500);
		}
	}

	protected class TreeExpandListener implements TreeWillExpandListener {
		public void treeWillCollapse(TreeExpansionEvent event)
				throws ExpandVetoException {
			if (searchField.getText().length() == 0) {
				expandedPaths.remove(event.getPath());
			}
		}

		public void treeWillExpand(TreeExpansionEvent event)
				throws ExpandVetoException {
			if (searchField.getText().length() == 0) {
				expandedPaths.add(event.getPath());
			}
		}
	}

}
