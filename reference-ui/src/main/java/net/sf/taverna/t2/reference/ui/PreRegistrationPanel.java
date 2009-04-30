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
package net.sf.taverna.t2.reference.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.reference.ui.tree.PreRegistrationTree;
import net.sf.taverna.t2.reference.ui.tree.PreRegistrationTreeModel;

/**
 * A JPanel containing a pre-registration tree along with a toolbar for adding
 * collections and strings directly rather than through drag and drop on the
 * tree. Implement the {@link #handleRegistration(Object)} to be notified when
 * the user has selected the 'register with store' or similarly named option.
 * Any runtime exceptions thrown within this method are trapped and displayed as
 * error messages in the status bar.
 * 
 * @author Tom Oinn
 */
public abstract class PreRegistrationPanel extends JPanel {

	private final ImageIcon deleteNodeIcon = new ImageIcon(getClass()
			.getResource("/icons/delete_obj.gif"));
	private final ImageIcon addTextIcon = new ImageIcon(getClass().getResource(
			"/icons/addtext_co.gif"));
	private final ImageIcon addListIcon = new ImageIcon(getClass().getResource(
			"/icons/newfolder_wiz.gif"));
	private final ImageIcon registerPojoIcon = new ImageIcon(getClass()
			.getResource("/icons/repo_rep.gif"));
	private final ImageIcon errorIcon = new ImageIcon(getClass().getResource(
			"/icons/error_tsk.gif"));
	private final ImageIcon infoIcon = new ImageIcon(getClass().getResource(
			"/icons/information.gif"));

	private static final long serialVersionUID = 2304744530333262466L;
	private final PreRegistrationTree tree;
	private final PreRegistrationTreeModel treeModel;
	private final JLabel status;
	private final DateFormat statusDateFormat = new SimpleDateFormat("HH:mm:ss");

	// If the depth is greater than 1 we can add sub-folders to the collection
	// structure (it doesn't make sense to do this for single depth lists or
	// individual items). This list is initialized to contain actions to add new
	// folders at each valid collection level
	private final List<Action> addCollectionActions = new ArrayList<Action>();

	// Remove any children of the currently selected node
	private Action deleteNodeAction;

	// Add a new default text string, adding to the root node (which will
	// cascade down until it hits the correct level through logic in the model)
	private Action addTextAction;

	// Call the data registration hook, enclosing components should override
	// this to link to a reference service and jump to a panel displaying the
	// result (if the registration was successful). If an exception is thrown
	// within the callback of any kind an error message is displayed in the
	// status bar of this component. If the pojo is null, that is to say no data
	// have been defined for this input then a new RuntimeException is
	// registered with the message 'No input data supplied', this will be mapped
	// to an ErrorDocument in the reference system.
	private Action registerPojoAction;

	public abstract void handleRegistration(Object pojo);

	/**
	 * Construct a new pre-registration panel for an input with the specified
	 * depth.
	 * 
	 * @param depth
	 *            Depth of the POJO to construct from this panel
	 */
	@SuppressWarnings("serial")
	public PreRegistrationPanel(int depth) {
		super(new BorderLayout());
		tree = new PreRegistrationTree(depth) {
			@Override
			public void setStatusMessage(String message, boolean isError) {
				if (isError) {
					setStatus(message, errorIcon, Color.red);
				}
				else {
					setStatus(message, infoIcon, Color.black);
				}
			}
		};
		treeModel = tree.getPreRegistrationTreeModel();
		add(new JScrollPane(this.tree), BorderLayout.CENTER);
		buildActions();
		// Listen to selections on the tree to enable or disable actions
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				MutableTreeNode selectedNode = (MutableTreeNode) tree
						.getLastSelectedPathComponent();
				if (selectedNode == null) {
					// Selection cleared
					deleteNodeAction.setEnabled(false);
				} else {
					if (selectedNode == treeModel.getRoot()) {
						deleteNodeAction.setEnabled(false);
					} else {
						deleteNodeAction.setEnabled(true);
					}
				}
			}
		});
		status = new JLabel();
		status.setOpaque(false);
		status.setBorder(new EmptyBorder(2, 2, 2, 2));
		setStatus("Drag to re-arrange, or drag files, URLs, or text to add",
				infoIcon, null);
		add(status, BorderLayout.SOUTH);
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(new JButton(deleteNodeAction));
		toolBar.add(new JButton(addTextAction));
		// Do lists...
		if (addCollectionActions.isEmpty() == false) {
			if (addCollectionActions.size() == 1) {
				// Single item, add directly
				Action addCollectionAction = addCollectionActions.get(0);
				addCollectionAction.putValue(Action.NAME, "New list");
				toolBar.add(new JButton(addCollectionAction));
			} else {
				// Create pop-up menu
				final JPopupMenu menu = new JPopupMenu();
				for (Action a : addCollectionActions) {
					menu.add(a);
				}
				final JButton popup = new JButton("Add list...", addListIcon);
				popup.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						menu.show(popup, 0, popup.getHeight());

					}
				});
				popup.setComponentPopupMenu(menu);
				toolBar.add(popup);
			}
		}
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(new JButton(registerPojoAction));
		add(toolBar, BorderLayout.NORTH);
	}

	private void setStatus(String statusString, Icon icon, Color textColour) {
		status.setText(statusDateFormat.format(new Date()) + " | "
				+ statusString);
		status.setIcon(icon);
		if (textColour != null) {
			status.setForeground(textColour);
		} else {
			status.setForeground(Color.black);
		}
	}

	@SuppressWarnings("serial")
	private void buildActions() {
		deleteNodeAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				MutableTreeNode node = (MutableTreeNode) tree
						.getSelectionPath().getLastPathComponent();
				// Can't delete the root node
				if (node.getParent() == treeModel.getRoot()) {
					return;
				} else {
					treeModel.removeNodeFromParent(node);
					setStatus("Deleted node", infoIcon, null);
				}
			}
		};
		deleteNodeAction.putValue(Action.NAME, "Delete node");
		deleteNodeAction.putValue(Action.SMALL_ICON, deleteNodeIcon);
		// Starts off disabled
		deleteNodeAction.setEnabled(false);

		addTextAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode added = treeModel.addPojoStructure((MutableTreeNode) treeModel
						.getRoot(), "abcd", 0);
				tree.setSelectionPath(new TreePath(added.getPath()));
				
				setStatus("Added new inline string, triple click to edit.",
						infoIcon, null);
			}
		};
		addTextAction.putValue(Action.NAME, "New string");
		addTextAction.putValue(Action.SMALL_ICON, addTextIcon);

		if (treeModel.getDepth() > 1) {
			for (int i = 1; i < treeModel.getDepth(); i++) {
				final int depth = i;
				Action addCollectionAction = new AbstractAction() {
					public void actionPerformed(ActionEvent ae) {
						treeModel.addPojoStructure((MutableTreeNode) treeModel
								.getRoot(), new ArrayList<Object>(), depth);
						setStatus("Added new collection with depth " + depth,
								infoIcon, null);
					}
				};
				addCollectionAction.putValue(Action.NAME, "New list (depth "
						+ depth + ")");
				addCollectionAction.putValue(Action.SMALL_ICON, addListIcon);
				addCollectionActions.add(addCollectionAction);
			}
		}

		registerPojoAction = new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				Object pojo = treeModel.getAsPojo();
				if (pojo == null) {
					pojo = new RuntimeException("No input data supplied");
				}
				try {
					handleRegistration(pojo);
				} catch (Throwable t) {
					t.printStackTrace();
					setStatus(t.getMessage(), errorIcon, Color.red);
				}
			}
		};
		registerPojoAction.putValue(Action.NAME, "Register data");
		registerPojoAction.putValue(Action.SMALL_ICON, registerPojoIcon);

	}

}
