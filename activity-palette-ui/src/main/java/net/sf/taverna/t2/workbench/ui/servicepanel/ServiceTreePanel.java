/**
 * 
 */
package net.sf.taverna.t2.workbench.ui.servicepanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.partition.ActivityItem;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workbench.ui.servicepanel.menu.AddServiceProviderMenu;
import net.sf.taverna.t2.workbench.ui.servicepanel.servicetree.ServiceTreeModel;
import net.sf.taverna.t2.workbench.ui.servicepanel.servicetree.ServiceTreeNode;

import org.apache.log4j.Logger;


public class ServiceTreePanel extends JPanel {
	private static final long serialVersionUID = 6611462684296693909L;

	private static Logger logger = Logger.getLogger(ServiceTreePanel.class);

	private final ServiceDescriptionRegistry serviceDescriptionRegistry;

	protected JTree tree = new JTree();
	protected JScrollPane treeScrollPane;

	private final ServiceTreeModel serviceTreeModel;

	private final ServiceTreeModel filteredTreeModel;

	private static int MAX_EXPANSION = 100;
	private static final int SEARCH_WIDTH = 15;

	protected JTextField searchField = new JTextField(SEARCH_WIDTH);

	private final Object filterLock = new Object();

	protected Set<TreePath> expandedPaths = new HashSet<TreePath>();

	public ServiceTreePanel(final ServiceTreeModel treeModel,
			final ServiceDescriptionRegistry serviceDescriptionRegistry) {
		super();
		this.serviceTreeModel = treeModel;
		this.filteredTreeModel = treeModel;
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
		initialize();
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		treeScrollPane = new JScrollPane(tree);
		tree.expandRow(0);
		tree.setModel(filteredTreeModel);
		tree.setRowHeight(0);
		tree.setCellRenderer(new ServiceTreeCellRenderer());
		tree.setSelectionModel(new FilterTreeSelectionModel());

		final JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();

		final JLabel filterLabel = new JLabel("Filter:  ");
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
		final JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				searchField.setText("");
				SwingUtilities.invokeLater(new RunFilter());
				clearButton.getParent().requestFocusInWindow();// so that the
																// button does
																// not stay
																// focused after
																// it is clicked
																// on and did
																// its action
			}
		});
		topPanel.add(clearButton, c);

		c.gridx = 3;
		c.weightx = 0.2;
		topPanel.add(new JPanel(), c);

		final JPanel topExtraPanel = new JPanel();
		topExtraPanel.setLayout(new BorderLayout());

		topExtraPanel.add(topPanel, BorderLayout.NORTH);

		final Component extraComponent = createExtraComponent();
		if (extraComponent != null) {
			final JPanel extraPanel = new JPanel();
			extraPanel.setLayout(new BorderLayout());
			extraPanel.add(extraComponent, BorderLayout.WEST);
			topExtraPanel.add(extraPanel, BorderLayout.CENTER);
		}

		add(topExtraPanel, BorderLayout.NORTH);

		add(treeScrollPane, BorderLayout.CENTER);

		searchField.addKeyListener(new SearchFieldKeyAdapter());

		tree.setDragEnabled(true);
		tree.setTransferHandler(new ServiceTransferHandler());
		tree.addTreeWillExpandListener(new AvoidRootCollapse());
		tree.expandRow(0);

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				tree.addMouseListener(new ServiceTreeClickListener(tree,
						ServiceTreePanel.this, serviceDescriptionRegistry));
			}

		});

	}

	public void expandRoot() {
		tree.expandRow(0);
	}

	protected Component createExtraComponent() {
		final JComponent buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(new AddServiceProviderMenu());
		// buttonPanel.add(new JButton(new RefreshProviderRegistryAction()));
		return buttonPanel;
	}

	public ServiceFilter createFilter(final String text) {
		return new ServiceFilter(text.trim(), serviceTreeModel.getRoot());
	}

	protected TreeCellRenderer createCellRenderer() {
		return new ServiceTreeCellRenderer();
	}

	public static class AvoidRootCollapse implements TreeWillExpandListener {
		@Override
		public void treeWillCollapse(final TreeExpansionEvent event)
				throws ExpandVetoException {
			if (event.getPath().getPathCount() == 1) {
				throw new ExpandVetoException(event, "Can't collapse root");
			}
		}

		@Override
		public void treeWillExpand(final TreeExpansionEvent event)
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
		@SuppressWarnings("rawtypes")
		@Override
		protected Transferable createTransferable(final JComponent c) {
			final TreePath selectionPath = tree.getSelectionPath();
			if (selectionPath != null) {
				final ServiceTreeNode lastPathComponent = (ServiceTreeNode) selectionPath
						.getLastPathComponent();
				if (lastPathComponent.getUserObject() instanceof ServiceDescription) {
					final ServiceDescription serviceDescription = (ServiceDescription) lastPathComponent
							.getUserObject();
					return new Transferable() {
						@Override
						public Object getTransferData(final DataFlavor flavor)
								throws UnsupportedFlavorException, IOException {
							return serviceDescription;
						}

						@Override
						public DataFlavor[] getTransferDataFlavors() {
							final DataFlavor[] flavors = new DataFlavor[1];
							DataFlavor flavor = null;
							try {
								flavor = new DataFlavor(
										DataFlavor.javaJVMLocalObjectMimeType
												+ ";class="
												+ ServiceDescription.class
														.getCanonicalName(),
										"ServiceDescription", getClass()
												.getClassLoader());
							} catch (final ClassNotFoundException e) {
								logger.error("Error casting Dataflavor", e);
							}
							flavors[0] = flavor;
							return flavors;
						}

						@Override
						public boolean isDataFlavorSupported(
								final DataFlavor flavor) {
							DataFlavor thisFlavor = null;
							try {
								thisFlavor = new DataFlavor(
										DataFlavor.javaJVMLocalObjectMimeType
												+ ";class="
												+ ServiceDescription.class
														.getCanonicalName(),
										"ServiceDescription", getClass()
												.getClassLoader());
							} catch (final ClassNotFoundException e) {
								logger.error("Error casting Dataflavor", e);
							}
							return flavor.equals(thisFlavor);
						}

					};
				}
			}
			return null;
		}

		@Override
		public int getSourceActions(final JComponent c) {
			return COPY_OR_MOVE;
		}
	}


	public void runFilter() throws InterruptedException,
			InvocationTargetException {
		/*
		 * Special lock object, don't do a synchronized model, as the lock on
		 * JComponent might deadlock when painting the panel - see comments at
		 * http://www.mygrid.org.uk/dev/issues/browse/T2-1438
		 */
		synchronized (filterLock) {
			// tree.removeTreeExpansionListener(treeExpandListener);
			final String text = searchField.getText();
			tree.getModel().getRoot();
			if (text.isEmpty()) {
				if (tree.getModel() != serviceTreeModel) {
					tree.setModel(serviceTreeModel);
					restorePaths(expandedPaths);
				}
			} else {
				if (tree.getModel() == serviceTreeModel) {
					rememberPaths();
				}
				final ServiceTreeModel clonedTreeModel = serviceTreeModel
						.cloneWithFilter(createFilter(text));

				tree.setModel(clonedTreeModel);
				expandTreePaths();
			}
		}
	}

	private void rememberPaths() {
		expandedPaths.clear();
		final int rowCount = tree.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			expandedPaths.add(tree.getPathForRow(i));
		}
	}

	private void restorePaths(final Set<TreePath> paths) {
		for (final TreePath tp : paths) {
			tree.makeVisible(tp);
		}
	}

	public void expandAll(final ServiceTreeNode selectedNode,
			final boolean expand) {
		tree.getModel().getRoot();

		// Traverse tree from root
		expandAll(new TreePath(selectedNode.getPath()), expand);
	}

	@SuppressWarnings("rawtypes")
	private void expandAll(final TreePath parent, final boolean expand) {
		// Traverse children
		final ServiceTreeNode node = (ServiceTreeNode) parent
				.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (final Enumeration e = node.children(); e.hasMoreElements();) {
				final ServiceTreeNode n = (ServiceTreeNode) e.nextElement();
				final TreePath path = parent.pathByAddingChild(n);
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

	public void expandTreePaths() throws InterruptedException,
			InvocationTargetException {
		for (int i = 0; (i < tree.getRowCount()) && (i < MAX_EXPANSION); i++) {
			tree.expandRow(i);
		}

	}

	protected class RunFilter implements Runnable {
		@Override
		public void run() {

			try {
				runFilter();
			} catch (final InterruptedException e) {
				Thread.interrupted();
			} catch (final InvocationTargetException e) {
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
		public void keyReleased(final KeyEvent e) {
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

}