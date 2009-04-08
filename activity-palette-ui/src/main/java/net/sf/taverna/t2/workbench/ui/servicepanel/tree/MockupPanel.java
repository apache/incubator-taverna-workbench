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
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

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
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class MockupPanel extends JPanel {

	private static final int SEARCH_WIDTH = 15;

	private static int MAX_EXPANSION = 100;

	JTree tree = new JTree();
	JTextField searchField = new JTextField(SEARCH_WIDTH);
	FilterTreeModel filterTreeModel;
	Set<TreePath> expandedPaths = new HashSet<TreePath>();
	JScrollPane treeScrollPane;

	public MockupPanel(FilterTreeModel treeModel) {
		filterTreeModel = treeModel;
		initialize();
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		treeScrollPane = new JScrollPane(tree);
		tree.setModel(filterTreeModel);
		tree.addTreeWillExpandListener(new TreeExpandListener());
		tree.setCellRenderer(new FilterTreeCellRenderer(filterTreeModel));
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

	public MockupPanel() {
		this(new FilterTreeModel(addNodes(null, new File(System
				.getProperty("java.io.tmpdir"))), null));

	}

	private void expandTreePaths() throws InterruptedException,
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

	private synchronized void runFilter() throws InterruptedException,
			InvocationTargetException {
		System.out.println("Actually ran");
		String text = searchField.getText();
		if (text.length() == 0) {
			filterTreeModel.setFilter(null);
			for (TreePath tp : expandedPaths) {
				System.out.println("Expanding a path to "
						+ tp.getLastPathComponent().toString());
				tree.expandPath(tp);
			}
		} else {
			filterTreeModel.setFilter(new MyFilter(text));
			expandTreePaths();
		}

	}

	/** Add nodes from under "dir" into curTop. Highly recursive. */
	static FilterTreeNode addNodes(FilterTreeNode curTop, File dir) {
		String curPath = dir.getPath();
		FilterTreeNode curDir = new FilterTreeNode(dir.getName());
		if (curTop != null) { // should only be null at root
			curTop.add(curDir);
		}
		Vector<String> ol = new Vector<String>();
		String[] tmp = dir.list();
		if (tmp != null) {
			for (int i = 0; i < tmp.length; i++)
				ol.addElement(tmp[i]);
		}
		Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);
		File f;
		Vector<String> files = new Vector<String>();
		// Make two passes, one for Dirs and one for Files. This is #1.
		for (int i = 0; i < ol.size(); i++) {
			String thisObject = (String) ol.elementAt(i);
			String newPath;
			if (curPath.equals("."))
				newPath = thisObject;
			else
				newPath = curPath + File.separator + thisObject;
			if ((f = new File(newPath)).isDirectory())
				addNodes(curDir, f);
			else
				files.addElement(thisObject);
		}
		// Pass two: for files.
		for (int fnum = 0; fnum < files.size(); fnum++)
			curDir.add(new FilterTreeNode(files.elementAt(fnum)));
		return curDir;
	}

	private final class ClearAction extends AbstractAction {
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

	private final class RunFilter implements Runnable {
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

	private final class SearchFieldKeyAdapter extends KeyAdapter {
		private final Runnable runFilterRunnable;
		Timer timer = new Timer();
		private SearchFieldKeyAdapter() {
			this.runFilterRunnable = new RunFilter();
		}

		public void keyReleased(KeyEvent e) {

			System.out.println("Cancelled with " + searchField.getText());
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

	private final class TreeExpandListener implements TreeWillExpandListener {
		public void treeWillCollapse(TreeExpansionEvent event)
				throws ExpandVetoException {
			if (searchField.getText().length() == 0) {
				expandedPaths.remove(event.getPath());
				System.out.println("Collapsed "
						+ event.getPath().getLastPathComponent().toString());
			}
		}

		public void treeWillExpand(TreeExpansionEvent event)
				throws ExpandVetoException {
			if (searchField.getText().length() == 0) {
				expandedPaths.add(event.getPath());
				System.out.println("Expanded "
						+ event.getPath().getLastPathComponent().toString());
			}
		}
	}

	class ExpandRowRunnable implements Runnable {
		int rowNumber;

		public ExpandRowRunnable(int rowNumber) {
			this.rowNumber = rowNumber;
		}

		public void run() {
			tree.expandRow(rowNumber);
		}

	}

}
