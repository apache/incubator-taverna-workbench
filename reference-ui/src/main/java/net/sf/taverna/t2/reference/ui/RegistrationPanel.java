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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;
import net.sf.taverna.t2.reference.ui.tree.PreRegistrationTree;
import net.sf.taverna.t2.reference.ui.tree.PreRegistrationTreeModel;

import org.apache.log4j.Logger;

/**
 * A JPanel containing a pre-registration tree along with a toolbar for adding
 * collections, strings, files and url's directly rather than through drag and
 * drop on the tree. Any runtime exceptions thrown within this method are
 * trapped and displayed as error messages in the status bar.
 * 
 * @author Tom Oinn
 * @author David Withers
 */
@SuppressWarnings("serial")
public class RegistrationPanel extends JPanel {

	private static final String NEW_VALUE = "Some input data goes here";

	private static Logger logger = Logger
	.getLogger(RegistrationPanel.class);

	
	private static final ImageIcon addFileIcon = new ImageIcon(
			RegistrationPanel.class.getResource("/icons/topic.gif"));
	private static final ImageIcon addListIcon = new ImageIcon(
			RegistrationPanel.class.getResource("/icons/newfolder_wiz.gif"));
	private static final ImageIcon addTextIcon = new ImageIcon(
			RegistrationPanel.class.getResource("/icons/addtext_co.gif"));
	private static final ImageIcon addUrlIcon = new ImageIcon(
			RegistrationPanel.class.getResource("/icons/web.gif"));
	private static final ImageIcon deleteNodeIcon = new ImageIcon(
			RegistrationPanel.class.getResource("/icons/delete_obj.gif"));
	private static final ImageIcon errorIcon = new ImageIcon(
			RegistrationPanel.class.getResource("/icons/error_tsk.gif"));
	private static final ImageIcon infoIcon = new ImageIcon(
			RegistrationPanel.class.getResource("/icons/information.gif"));

	// If the depth is greater than 1 we can add sub-folders to the collection
	// structure (it doesn't make sense to do this for single depth lists or
	// individual items). This list is initialized to contain actions to add new
	// folders at each valid collection level
	private final List<Action> addCollectionActions = new ArrayList<Action>();

	private AddFileAction addFileAction = new AddFileAction();
	private AddTextAction addTextAction = new AddTextAction();
	private AddURLAction addUrlAction = new AddURLAction();
	private DeleteNodeAction deleteNodeAction = new DeleteNodeAction();

	private int depth;
	private JPanel editorPane;
	private JSplitPane splitPane;
	private final JLabel status;
	private JTextArea textArea;
	private JLabel textAreaType;
	private TextAreaFocusListener textAreaFocusListener;
	private final PreRegistrationTree tree;
	private final PreRegistrationTreeModel treeModel;


	private final String example;


	private final String description;

	private String name;

	/**
	 * Construct a new registration panel for an input with the specified depth.
	 * 
	 * @param depth
	 *            Depth of the POJO to construct from this panel
	 * @param example 
	 * @param inputDescription
	 * @param inputName
	 */
	public RegistrationPanel(int depth, String name, String description, String example) {
		super(new BorderLayout());
		this.depth = depth;
		this.name = name;
		this.description = description;
		this.example = example;
		tree = new PreRegistrationTree(depth, name) {
			@Override
			public void setStatusMessage(String message, boolean isError) {
				if (isError) {
					setStatus(message, errorIcon, Color.red);
				} else {
					setStatus(message, infoIcon, Color.black);
				}
			}
		};
		treeModel = tree.getPreRegistrationTreeModel();

		tree.addTreeSelectionListener(new UpdateEditorPaneOnSelection());
		
		tree.setRootVisible(false);

		editorPane = new JPanel(new BorderLayout());

		String header = "<html><b>Port name:</b> " + name;
		if (description != null) {
			header += "<br><b>Description:</b> " + description;
		}
		if (example != null) {
			header += "<br><b>Example:</b> " + example;			
		}
		header += "</html>";
		JLabel editorPaneHeader = new JLabel(header);
		editorPaneHeader.setBorder(BorderFactory.createCompoundBorder(
			  BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));

		JPanel headerAndToolBarPane = new JPanel(new BorderLayout());
		
		headerAndToolBarPane.add(editorPaneHeader, BorderLayout.NORTH);
		headerAndToolBarPane.add(createToolBar(), BorderLayout.SOUTH);
		
		textArea = new JTextArea();
		textAreaFocusListener = new TextAreaFocusListener(textArea);
		textArea.addFocusListener(textAreaFocusListener);
		splitPane = new JSplitPane();
		splitPane.add(new JScrollPane(this.tree), JSplitPane.LEFT);
		splitPane.add(new JScrollPane(textArea), JSplitPane.RIGHT);
		splitPane.setDividerLocation(150);

		add(headerAndToolBarPane, BorderLayout.NORTH);
		add(splitPane, BorderLayout.CENTER);
		
		// Listen to selections on the tree to enable or disable actions
		tree.addTreeSelectionListener(new UpdateActionsOnTreeSelection());
		status = new JLabel();
		status.setOpaque(false);
		status.setBorder(new EmptyBorder(2, 2, 2, 2));
		setStatus("Drag to re-arrange, or drag files, URLs, or text to add",
				infoIcon, null);
		add(status, BorderLayout.SOUTH);
	}
	
	private JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();
		buildActions();
		toolBar.setFloatable(false);
		toolBar.add(new JButton(deleteNodeAction));
		toolBar.add(new JButton(addTextAction));
		toolBar.add(new JButton(addFileAction));
		toolBar.add(new JButton(addUrlAction));
		// Do lists...
		if (!addCollectionActions.isEmpty()) {
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
		// toolBar.add(Box.createHorizontalGlue());
		return toolBar;
	}

	public int getDepth() {
		return depth;
	}

	public Object getUserInput() {
		Object pojo = treeModel.getAsPojo();
		if (pojo == null) {
			pojo = new RuntimeException("No input data supplied");
		}
		return pojo;
	}

	private void buildActions() {
		if (treeModel.getDepth() > 1) {
			for (int i = 1; i < treeModel.getDepth(); i++) {
				final int depth = i;
				Action addCollectionAction = new NewListAction(depth);
				addCollectionActions.add(addCollectionAction);
			}
		}

	}

	/**
	 * 
	 * 
	 * @return
	 */
	private MutableTreeNode getSelectedNode() {
		MutableTreeNode node = null;
		TreePath selectionPath = tree.getSelectionPath();
		if (selectionPath != null) {
			node = (MutableTreeNode) selectionPath.getLastPathComponent();
		}
		return node;
	}

	private void setStatus(String statusString, Icon icon, Color textColour) {
		status.setText(statusString);
		status.setIcon(icon);
		if (textColour != null) {
			status.setForeground(textColour);
		} else {
			status.setForeground(Color.black);
		}
	}

	private final class UpdateEditorPaneOnSelection implements
			TreeSelectionListener {
		
		public void valueChanged(TreeSelectionEvent e) {

			TreePath oldLeadSelectionPath = e.getOldLeadSelectionPath();
			if (oldLeadSelectionPath != null) {
				DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) oldLeadSelectionPath
						.getLastPathComponent();
				if (lastPathComponent != null && textArea.isEditable()) {
					lastPathComponent.setUserObject(textArea.getText());
				}
			}

			DefaultMutableTreeNode selection = (DefaultMutableTreeNode) e
					.getPath().getLastPathComponent();
			textArea.setEditable(false);
			textAreaFocusListener.setNode(selection);
			if (!selection.isLeaf()) {
				textArea.setText("List selected");
			}
			if (selection == null) {
				textArea.setText("No selection");
				return;
			}
			if (selection.getUserObject() == null) {
				textArea.setText("List selected");
				return;
			}
			if (selection.getUserObject() instanceof String) {
				textArea.setText((String) selection.getUserObject());
				textArea.setEditable(true);
				textArea.requestFocusInWindow();
				textArea.selectAll();
			} else {
				textArea.setText("Fixed object: " + selection.getUserObject()
						+ " " + selection.getUserObject().getClass());
			}
		}
	}
	
	public class NewListAction extends AbstractAction {
		private final int depth;

		private NewListAction(int depth) {
			super("New list (depth " + depth + ")", addListIcon);
			this.depth = depth;
		}

		public void actionPerformed(ActionEvent ae) {
			DefaultMutableTreeNode added = treeModel.addPojoStructure((MutableTreeNode) treeModel.getRoot(),
					new ArrayList<Object>(), depth);
			tree.setSelectionPath(new TreePath(added.getPath()));
			setStatus("Added new collection with depth " + depth, infoIcon,
					null);
		}
	}

	public class UpdateActionsOnTreeSelection implements
			TreeSelectionListener {
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
	}

	public class AddFileAction extends AbstractAction {

		public AddFileAction() {
			super("Add file(s)...", addFileIcon);
		}

		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			String currentDir = prefs.get("currentDir", System
					.getProperty("user.home"));
			fileChooser.setDialogTitle("Choose files or directory");

			fileChooser.setCurrentDirectory(new File(currentDir));
			fileChooser.setMultiSelectionEnabled(true);
			fileChooser
					.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

			int returnVal = fileChooser.showOpenDialog(RegistrationPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				prefs.put("currentDir", fileChooser.getCurrentDirectory()
						.toString());
				MutableTreeNode node = getSelectedNode();

				for (File file : fileChooser.getSelectedFiles()) {
					if (!file.isDirectory()) {
						DefaultMutableTreeNode added = treeModel.addPojoStructure(node, file, 0);
						tree.setSelectionPath(new TreePath(added.getPath()));
						setStatus("Added file : " + file.getPath(), infoIcon,
								null);
					} else {
						if (treeModel.getDepth() < 1) {
							// TODO add popup warning
							setStatus(
									"Can't add directory to single item input",
									errorIcon, null);
							return;
						}
						// Try to handle directories as flat lists, don't
						// nest
						// any deeper for now.
						List<File> children = new ArrayList<File>();
						for (File child : file.listFiles()) {
							if (child.isFile()) {
								children.add(child);
							}
						}
						DefaultMutableTreeNode added = treeModel.addPojoStructure(node, children, 1);
						tree.setSelectionPath(new TreePath(added.getPath()));
						setStatus("Added directory : " + file.getPath(),
								infoIcon, null);
					}
				}
			}
		}
	}

	/**
	 * Add a new default text string, adding to the root node (which will
	 * cascade down until it hits the correct level through logic in the model)
	 */
	public class AddTextAction extends AbstractAction {
		public AddTextAction() {
			super("New value", addTextIcon);
		}

		public void actionPerformed(ActionEvent e) {
			MutableTreeNode node = getSelectedNode();
			String newValue;
			if (example != null && example.length() > 0) {
				newValue = example;
			} else {
				newValue = NEW_VALUE;
			}
			
			DefaultMutableTreeNode added = treeModel.addPojoStructure(node,
					newValue, 0);
			tree.setSelectionPath(new TreePath(added.getPath()));
			setStatus("Added new value.", infoIcon, null);
		}
	}

	public class AddURLAction extends AbstractAction {

		private static final String URL_REGEX = "http:\\/\\/(\\w+:{0,1}\\w*@)?(\\S+)(:[0-9]+)?(\\/|\\/([\\w#!:.?+=&%@!\\-\\/]))?";

		public AddURLAction() {
			super("Add URL...", addUrlIcon);
		}

		public void actionPerformed(ActionEvent e) {
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			String currentUrl = prefs.get("currentUrl",
					"http://www.mygrid.org.uk/");

			UrlPanel urlPanel = new UrlPanel();

			ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
					"Add an http URL", urlPanel);
			vuid.addTextComponentValidation(
							urlPanel.getUrlField(), "Set the URL.",
							null, "",URL_REGEX,	"Not a valid http URL.");
			vuid.setSize(new Dimension(400, 200));

			urlPanel.setUrl(currentUrl);

			if (vuid.show(RegistrationPanel.this)) {
				String urlString = urlPanel.getUrl();
				try {
					URL url = new URL(urlString);
					if (url.getProtocol().equalsIgnoreCase("http")) {
						prefs.put("currentUrl", urlString);

						MutableTreeNode node = getSelectedNode();

						DefaultMutableTreeNode added = treeModel.addPojoStructure(node, url, 0);
						tree.setSelectionPath(new TreePath(added.getPath()));
						setStatus("Added URL : " + url.toExternalForm(),
								infoIcon, null);
					} else {
						setStatus("Only http URLs are supported for now.",
								errorIcon, null);
					}
				} catch (MalformedURLException e1) {
					setStatus("Invalid URL.", errorIcon, null);
				}
			}
		}
	}

	/**
	 * Remove any children of the currently selected node
	 */
	public class DeleteNodeAction extends AbstractAction {
		public DeleteNodeAction() {
			super("Delete node", deleteNodeIcon);
			// Starts off disabled
			setEnabled(false);
		}
		public void actionPerformed(ActionEvent e) {
			MutableTreeNode node = (MutableTreeNode) tree.getSelectionPath()
					.getLastPathComponent();
			// Can't delete the root node
			if (node.getParent() == treeModel.getRoot()) {
				return;
			} else {
				treeModel.removeNodeFromParent(node);
				tree.setSelectionPath(null);
				setStatus("Deleted node", infoIcon, null);
			}
		}
	}

	private class TextAreaFocusListener implements FocusListener {

		private DefaultMutableTreeNode selection;
		private JTextArea textArea;

		public TextAreaFocusListener(JTextArea textArea) {
			this.textArea = textArea;
		}

		public void setNode(DefaultMutableTreeNode selection) {
			this.selection = selection;
		}
		
		private void updateSelection() {
			if ((selection != null) && (selection.getUserObject() instanceof String)) {
				selection.setUserObject(textArea.getText());
			}
		}

		public void focusGained(FocusEvent e) {
			//nothing
		}

		public void focusLost(FocusEvent e) {
			updateSelection();
		}
		
	}
}
