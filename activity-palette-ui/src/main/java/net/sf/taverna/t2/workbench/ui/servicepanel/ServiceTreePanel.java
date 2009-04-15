/**
 * 
 */
package net.sf.taverna.t2.workbench.ui.servicepanel;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.partition.ActivityItem;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.Filter;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeModel;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeNode;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.TreePanel;

public class ServiceTreePanel extends TreePanel {
	private static final long serialVersionUID = 6611462684296693909L;

	public ServiceTreePanel(FilterTreeModel treeModel) {
		super(treeModel);
	}

	@Override
	protected void initialize() {
		super.initialize();
		tree.setDragEnabled(true);
		tree.setTransferHandler(new ActivityTransferHandler());
		tree.addTreeWillExpandListener(new AvoidRootCollapse());
		tree.expandRow(0);
	}

	@Override
	public Filter createFilter(String text) {
		return new ServiceFilter(text);
	}
	
	@Override
	protected TreeCellRenderer createCellRenderer() {
		return new ServiceTreeCellRenderer();
	}

	public static class AvoidRootCollapse implements TreeWillExpandListener {
		public void treeWillCollapse(TreeExpansionEvent event)
				throws ExpandVetoException {
			if (event.getPath().getPathCount() == 1) {
				throw new ExpandVetoException(event, "Can't collapse root");
			}
		}
		public void treeWillExpand(TreeExpansionEvent event)
				throws ExpandVetoException {
		}
	}

	private final class ActivityTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 4347965626386951176L;

		/**
		 * Triggered when a node ie. an {@link ActivityItem} is dragged out of
		 * the tree. Figures out what node it is being dragged and then starts a
		 * drag action with it
		 */
		protected Transferable createTransferable(JComponent c) {
			Transferable transferable = null;
			TreePath selectionPath = tree.getSelectionPath();
			if (selectionPath != null) {
				FilterTreeNode lastPathComponent = (FilterTreeNode) selectionPath
						.getLastPathComponent();
				if (lastPathComponent.getUserObject() instanceof ActivityItem) {
					ActivityItem activityItem = (ActivityItem) lastPathComponent
							.getUserObject();
					transferable = activityItem.getActivityTransferable();
				}
			}
			return transferable;
		}

		public int getSourceActions(JComponent c) {
			return COPY_OR_MOVE;
		}
	}

}