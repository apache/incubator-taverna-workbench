/**
 * 
 */
package net.sf.taverna.t2.workbench.ui.servicepanel;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.partition.ActivityItem;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workbench.ui.servicepanel.actions.RefreshProviderRegistryAction;
import net.sf.taverna.t2.workbench.ui.servicepanel.menu.AddServiceProviderMenu;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.Filter;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeModel;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeNode;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.TreePanel;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper;

import org.apache.log4j.Logger;

public class ServiceTreePanel extends TreePanel {
	private static final long serialVersionUID = 6611462684296693909L;

	private static Logger logger = Logger.getLogger(ServiceTreePanel.class);

	public ServiceTreePanel(FilterTreeModel treeModel) {
		super(treeModel);
	}

	@Override
	protected void initialize() {
		super.initialize();
		tree.setDragEnabled(true);
		tree.setTransferHandler(new ServiceTransferHandler());
		tree.addTreeWillExpandListener(new AvoidRootCollapse());
		tree.expandRow(0);
	}

	@Override
	protected Component createExtraComponent() {
		JComponent buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(new AddServiceProviderMenu());
		buttonPanel.add(new JButton(new RefreshProviderRegistryAction()));
		return buttonPanel;
	}
	
	@Override
	public Filter createFilter(String text) {
		return new ServiceFilter(text, filterTreeModel.getRoot());
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

	private final class ServiceTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 4347965626386951176L;

		/**
		 * Triggered when a node ie. an {@link ActivityItem} is dragged out of
		 * the tree. Figures out what node it is being dragged and then starts a
		 * drag action with it
		 */
		@SuppressWarnings("unchecked")
		protected Transferable createTransferable(JComponent c) {
			TreePath selectionPath = tree.getSelectionPath();
			if (selectionPath != null) {
				FilterTreeNode lastPathComponent = (FilterTreeNode) selectionPath
						.getLastPathComponent();
				if (lastPathComponent.getUserObject() instanceof ServiceDescription) {
					final ServiceDescription serviceDescription = (ServiceDescription) lastPathComponent
							.getUserObject();
					return new Transferable() {
						public Object getTransferData(DataFlavor flavor)
								throws UnsupportedFlavorException, IOException {
							ActivityAndBeanWrapper wrapper = new ActivityAndBeanWrapper();
							try {
								wrapper
										.setActivity((Activity<?>) serviceDescription
												.getActivityClass()
												.newInstance());
							} catch (InstantiationException e) {
								logger.error("Could not instantiate activity "
										+ serviceDescription, e);
								return null;
							} catch (IllegalAccessException e) {
								logger.error("Could not instantiate activity "
										+ serviceDescription, e);
								return null;
							}
							wrapper.setBean(serviceDescription
									.getActivityConfiguration());
							wrapper.setName(serviceDescription.getName());
							return wrapper;
						}

						public DataFlavor[] getTransferDataFlavors() {
							DataFlavor[] flavors = new DataFlavor[1];
							DataFlavor flavor = null;
							try {
								flavor = new DataFlavor(
										DataFlavor.javaJVMLocalObjectMimeType
												+ ";class="
												+ ActivityAndBeanWrapper.class
														.getCanonicalName(),
										"Activity", getClass().getClassLoader());
							} catch (ClassNotFoundException e) {
								logger.error("Error casting Dataflavor", e);
							}
							flavors[0] = flavor;
							return flavors;
						}

						public boolean isDataFlavorSupported(DataFlavor flavor) {
							DataFlavor thisFlavor = null;
							try {
								thisFlavor = new DataFlavor(
										DataFlavor.javaJVMLocalObjectMimeType
												+ ";class="
												+ ActivityAndBeanWrapper.class
														.getCanonicalName(),
										"Activity", getClass().getClassLoader());
							} catch (ClassNotFoundException e) {
								logger.error("Error casting Dataflavor", e);
							}
							return flavor.equals(thisFlavor);
						}

					};
				}
			}
			return null;
		}

		public int getSourceActions(JComponent c) {
			return COPY_OR_MOVE;
		}
	}

}