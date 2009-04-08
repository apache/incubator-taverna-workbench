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
package net.sf.taverna.t2.workbench.ui.activitypalette;

import java.awt.datatransfer.Transferable;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.partition.ActivityItem;
import net.sf.taverna.t2.partition.Partition;
import net.sf.taverna.t2.partition.Query;
import net.sf.taverna.t2.partition.RootPartition;
import net.sf.taverna.t2.partition.SetModelChangeListener;

import org.apache.log4j.Logger;

/**
 * Contains all the activities. Has a {@link TreeModel} based on
 * {@link Partition} (actually a {@link RootPartition}) so that the items in the
 * tree can be filtered and re-ordered. When the {@link TreeModel} is updated
 * the {@link Query}s are rerun which will update the display
 * 
 * @author Ian Dunlop
 * @author Stuart Owen
 * @author David Withers
 */
@SuppressWarnings("serial")
public class ActivityTree extends JTree {

	private final class ActivityTransferHandler extends TransferHandler {
		/**
		 * Triggered when a node ie. an {@link ActivityItem} is dragged out of the
		 * tree. Figures out what node it is being dragged and then starts a drag
		 * action with it
		 */
		protected Transferable createTransferable(JComponent c) {
			Transferable transferable = null;
			TreePath selectionPath = ActivityTree.this.getSelectionPath();
			if (selectionPath != null) {
				Object lastPathComponent = selectionPath.getLastPathComponent();
				if (lastPathComponent instanceof ActivityItem) {
					ActivityItem activityItem = (ActivityItem) lastPathComponent;
					transferable = activityItem.getActivityTransferable();
				}
			}
			return transferable;
		}

		public int getSourceActions(JComponent c) {
		        return COPY_OR_MOVE;
		 }
	}

	private static Logger logger = Logger.getLogger(ActivityTree.class);

	/** A query for each type of activity */
	private List<Query<?>> queryList;

	public ActivityTree(TreeModel newModel) {
		super(newModel);
		setRowHeight(16);
		setEditable(false);
		setExpandsSelectedPaths(false);
		setDragEnabled(true);
		setScrollsOnExpand(false);
		addQueries(getModel());
		addTreeWillExpandListener(new TreeWillExpandListener() {

			/**
			 * If the root partition is double clicked then ignore the event
			 */
			public void treeWillCollapse(TreeExpansionEvent event)
					throws ExpandVetoException {
				if (event.getPath().getLastPathComponent() instanceof RootPartition) {
					throw new ExpandVetoException(event,
							"Activity Palette root not allowed to collapse");
				}
			}

			public void treeWillExpand(TreeExpansionEvent event)
					throws ExpandVetoException {
				// TODO Auto-generated method stub

			}

		});
		
		setTransferHandler(new ActivityTransferHandler());		

	}

	/**
	 * When the tree is first created it gets all the {@link Query}s from the
	 * {@link SetModelChangeListener} belonging to the {@link RootPartition} ie.
	 * its {@link TreeModel}. This means that when the model is updated ie. when
	 * the user wants to filter the activities the tree itself can re-fire the
	 * queries to update the display
	 * 
	 * @param model
	 */
	@SuppressWarnings("unchecked")
	private void addQueries(TreeModel model) {
		queryList = ((RootPartition) model).getSetModelChangeListener()
				.getQueries();
		doQueries();
	}

	/**
	 * Ensures that the {@link Query}s are listening to the
	 * {@link RootPartition} when the user wants to filter the activities. Calls
	 * {@link Query#doQuery()} for each query to get the display to show the
	 * user selected filter.
	 */
	private void doQueries() {

		for (final Query<?> query : queryList) {
			new Thread("Activity query:" + query.toString()) {
				@Override
				public void run() {
					query.doQuery();
				}
			}.start();

		}

	}

	@Override
	/*
	 * Resets the model which means that the user selected filter has probably
	 * changed
	 */
	public void setModel(TreeModel model) {
		if (treeModel == model)
			return;

		TreeModel oldValue = treeModel;
		treeModel = model;

		firePropertyChange(TREE_MODEL_PROPERTY, oldValue, model);
	}

}
