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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Color.black;
import static java.awt.Color.red;
import static java.lang.Math.round;
import static java.nio.charset.Charset.availableCharsets;
import static javax.swing.Action.NAME;
import static javax.swing.Box.createRigidArea;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.FILES_AND_DIRECTORIES;
import static javax.swing.JSplitPane.BOTTOM;
import static javax.swing.JSplitPane.LEFT;
import static javax.swing.JSplitPane.RIGHT;
import static javax.swing.JSplitPane.TOP;
import static javax.swing.JSplitPane.VERTICAL_SPLIT;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;
import net.sf.taverna.t2.reference.ui.tree.PreRegistrationTree;
import net.sf.taverna.t2.reference.ui.tree.PreRegistrationTreeModel;

import org.apache.log4j.Logger;

import org.apache.taverna.configuration.database.DatabaseConfiguration;

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
	private static final String NO_EXAMPLE_VALUE = "No example value";
	private static final String NO_PORT_DESCRIPTION = "No port description";
	private static final String NEW_VALUE = "Some input data goes here";

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(RegistrationPanel.class);

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
	public static final ImageIcon infoIcon = new ImageIcon(
			RegistrationPanel.class.getResource("/icons/information.gif"));

	// If the depth is greater than 1 we can add sub-folders to the collection
	// structure (it doesn't make sense to do this for single depth lists or
	// individual items). This list is initialized to contain actions to add new
	// folders at each valid collection level
	private final List<Action> addCollectionActions = new ArrayList<>();

	private AddFileAction addFileAction = null;
	private AddTextAction addTextAction = null;
	private AddURLAction addUrlAction = null;
	private DeleteNodeAction deleteNodeAction = null;

	private int depth;
	private JSplitPane splitPaneVertical;
	private JSplitPane splitPaneHorizontal;
	private final JLabel status;
	private DialogTextArea descriptionArea;
	private DialogTextArea exampleArea;
	private DialogTextArea textArea;
	private final PreRegistrationTree tree;
	private final PreRegistrationTreeModel treeModel;
	private TextAreaDocumentListener textAreaDocumentListener;

	@SuppressWarnings("unused")
	private JSpinner datatypeSpinner;
	private JSpinner charsetSpinner;
	@SuppressWarnings("unused")
	private static final Charset UTF8 = Charset.forName("UTF-8");

	private String example;

	@SuppressWarnings("unused")
	private DatabaseConfiguration config;

	private boolean exposedDatanature = false;

	private static String UNKNOWN = "UNKNOWN";

	private static Map<String, String> charsetNameMap = new HashMap<>();

	/**
	 * Construct a new registration panel for an input with the specified depth.
	 *
	 * @param depth
	 *            Depth of the POJO to construct from this panel
	 * @param example
	 * @param inputDescription
	 * @param inputName
	 */
	public RegistrationPanel(int depth, String name, String description,
			String example, DatabaseConfiguration databaseConfiguration) {
		super(new BorderLayout());
		this.depth = depth;
		this.example = example;
		config = databaseConfiguration;
		tree = new PreRegistrationTree(depth, name) {
			@Override
			public void setStatusMessage(String message, boolean isError) {
				setStatus(message, isError ? red : black);
			}
		};
		treeModel = tree.getPreRegistrationTreeModel();

		final UpdateEditorPaneOnSelection treeSelectionListener = new UpdateEditorPaneOnSelection();
		tree.addTreeSelectionListener(treeSelectionListener);

		tree.setRootVisible(false);

		new JPanel(new BorderLayout());

		descriptionArea = new DialogTextArea(NO_PORT_DESCRIPTION, 4, 40);
		descriptionArea.setBorder(new TitledBorder("Port description"));
		descriptionArea.setEditable(false);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);

		exampleArea = new DialogTextArea(NO_EXAMPLE_VALUE, 2, 40);
		exampleArea.setBorder(new TitledBorder("Example value"));
		exampleArea.setEditable(false);
		exampleArea.setLineWrap(true);
		exampleArea.setWrapStyleWord(true);

		setDescription(description);
		setExample(example);

		JPanel annotationsPanel = new JPanel();
		annotationsPanel.setLayout(new BoxLayout(annotationsPanel, Y_AXIS));
		JScrollPane descriptionAreaScrollPane = new JScrollPane(descriptionArea);
		JScrollPane exampleAreaScrollPane = new JScrollPane(exampleArea);
		annotationsPanel.add(descriptionAreaScrollPane);
		annotationsPanel.add(createRigidArea(new Dimension(0, 5))); // add some empty space
		annotationsPanel.add(exampleAreaScrollPane);

		JToolBar toolbar = createToolBar();

		JPanel textAreaPanel = new JPanel();
		textAreaPanel.setLayout(new BorderLayout());

		textArea = new DialogTextArea();
		textAreaDocumentListener = new TextAreaDocumentListener(textArea);
		textArea.setEditable(false);

		textAreaPanel.add(new JScrollPane(textArea), CENTER);
//		if (config.isExposeDatanature()) {
//			textAreaPanel.add(createTypeAccessory(), SOUTH);
//			exposedDatanature = true;
//		}

		splitPaneHorizontal = new JSplitPane();
		splitPaneHorizontal.add(new JScrollPane(tree), LEFT);
		splitPaneHorizontal.add(textAreaPanel, RIGHT);
		splitPaneHorizontal.setDividerLocation(150);
		JPanel toolbarAndInputsPanel = new JPanel(new BorderLayout());
		toolbarAndInputsPanel.add(splitPaneHorizontal, CENTER);
		toolbarAndInputsPanel.add(toolbar, NORTH);

		splitPaneVertical = new JSplitPane(VERTICAL_SPLIT);
		splitPaneVertical.add(annotationsPanel, TOP);
		splitPaneVertical.add(toolbarAndInputsPanel, BOTTOM);
		int dividerPosition = (int) round(annotationsPanel.getPreferredSize()
				.getHeight()) + 10;
		splitPaneVertical.setDividerLocation(dividerPosition);

		add(splitPaneVertical, CENTER);

		// Listen to selections on the tree to enable or disable actions
		tree.addTreeSelectionListener(new UpdateActionsOnTreeSelection());
		status = new JLabel();
		status.setOpaque(false);
		status.setBorder(new EmptyBorder(2, 2, 2, 2));
		setStatus("Drag to re-arrange, or drag files, URLs, or text to add",
				null);
		add(status, SOUTH);
	}

	private static List<String> charsetNames() {
		List<String> result = new ArrayList<>();
		result.add(UNKNOWN);

		for (String name : availableCharsets().keySet()) {
			String upperCase = name.toUpperCase();
			result.add(upperCase);
			charsetNameMap.put(upperCase, name);
		}
		Collections.sort(result);
		return result;
	}

//	private static SpinnerListModel datatypeModel = new SpinnerListModel(new ReferencedDataNature[] {ReferencedDataNature.UNKNOWN, ReferencedDataNature.TEXT, ReferencedDataNature.BINARY});
//	private static SpinnerListModel charsetModel = new SpinnerListModel(charsetNames());

//	private JPanel createTypeAccessory() {
//		JPanel result = new JPanel();
//		result.setLayout(new GridLayout(0,2));
//		result.add(new JLabel("Data type:"));
//		datatypeSpinner = new JSpinner(datatypeModel);
//		datatypeSpinner.setValue(ReferencedDataNature.UNKNOWN);
//		datatypeSpinner.setEnabled(false);
//		result.add(datatypeSpinner);
//		result.add(new JLabel("Character Set:"));
//		charsetSpinner = new JSpinner(charsetModel);
//		charsetSpinner.setValue(UNKNOWN);
//		charsetSpinner.setEnabled(false);
//		datatypeSpinner.addChangeListener(new ChangeListener() {
//
//			@Override
//			public void stateChanged(ChangeEvent e) {
//				DefaultMutableTreeNode selectedNode = getSelectedNode();
//				Object selectedUserObject = null;
//				if (selectedNode != null) {
//					selectedUserObject = selectedNode.getUserObject();
//				}
//				ReferencedDataNature nature = (ReferencedDataNature) datatypeSpinner.getValue();
//				if (nature.equals(ReferencedDataNature.UNKNOWN)) {
//					charsetSpinner.setEnabled(false);
//				} else if (nature.equals(ReferencedDataNature.TEXT)) {
//					charsetSpinner.setEnabled(true);
//				} else if (nature.equals(ReferencedDataNature.BINARY)) {
//					charsetSpinner.setEnabled(false);
//				}
//				if (selectedUserObject instanceof FileReference) {
//					FileReference ref = (FileReference) selectedUserObject;
//					ref.setDataNature(nature);
//				}
//			}});
//		charsetSpinner.addChangeListener(new ChangeListener() {
//
//			@Override
//			public void stateChanged(ChangeEvent e) {
//				DefaultMutableTreeNode selectedNode = getSelectedNode();
//				Object selectedUserObject = null;
//				if (selectedNode != null) {
//					selectedUserObject = selectedNode.getUserObject();
//				}
//				String cSet = (String) charsetSpinner.getValue();
//				if (selectedUserObject instanceof FileReference) {
//					FileReference ref = (FileReference) selectedUserObject;
//					if (cSet.equals(UNKNOWN)) {
//						ref.setCharset(null);
//					} else {
//						ref.setCharset(charsetNameMap.get(cSet));
//					}
//				}
//			}
//
//		});
//
//		result.add(charsetSpinner);
//		return result;
//	}

	private JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();
		buildActions();
		toolBar.setFloatable(false);

		JButton comp = new JButton(deleteNodeAction);
		comp.setToolTipText("Remove the currently selected input");
		toolBar.add(comp);

		JButton comp2 = new JButton(addTextAction);
		if (depth == 0)
			comp2.setToolTipText("Set the input value");
		else
			comp2.setToolTipText("Add a new input value");

		toolBar.add(comp2);
		JButton comp3 = new JButton(addFileAction);
		if (depth == 0)
			comp3.setToolTipText("Set the input value from a file");
		else
			comp3.setToolTipText("Add an input value from a file");
		toolBar.add(comp3);

		JButton comp4 = new JButton(addUrlAction);
		if (depth == 0)
			comp4.setToolTipText("Load the input value from a URL");
		else
			comp4.setToolTipText("Load an input value from a URL");
		toolBar.add(comp4);

		// Do lists...
		if (!addCollectionActions.isEmpty()) {
			if (addCollectionActions.size() == 1) {
				// Single item, add directly
				Action addCollectionAction = addCollectionActions.get(0);
				addCollectionAction.putValue(NAME, "New list");
				toolBar.add(new JButton(addCollectionAction));
			} else {
				// Create pop-up menu
				final JPopupMenu menu = new JPopupMenu();
				for (Action a : addCollectionActions)
					menu.add(a);
				final JButton popup = new JButton("Add list...", addListIcon);
				popup.addActionListener(new ActionListener() {
					@Override
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
		return treeModel.getAsPojo();
	}

	private void buildActions() {
		addFileAction = new AddFileAction();
		addTextAction = new AddTextAction();
		addUrlAction = new AddURLAction();
		deleteNodeAction = new DeleteNodeAction();

		for (int i = 1; i < treeModel.getDepth(); i++) {
			Action addCollectionAction = new NewListAction(i);
			addCollectionActions.add(addCollectionAction);
		}
	}

	/**
	 * @return
	 */
	private DefaultMutableTreeNode getSelectedNode() {
		DefaultMutableTreeNode node = null;
		TreePath selectionPath = tree.getSelectionPath();
		if (selectionPath != null)
			node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
		return node;
	}

	public void setStatus(String statusString, Color textColour) {
		status.setText(statusString);
		if (textColour != null)
			status.setForeground(textColour);
		else
			status.setForeground(black);
	}

	@SuppressWarnings("unused")
	private void setCharsetSpinner(String charsetName) {
		if (charsetName == null) {
			charsetSpinner.setValue(UNKNOWN);
			return;
		}
		String cName = charsetName.toUpperCase();
		if (charsetNames().contains(cName))
			charsetSpinner.setValue(cName);
		else
			charsetSpinner.setValue(UNKNOWN);
	}

	private void updateEditorPane(DefaultMutableTreeNode selection) {
		textArea.setEditable(false);
		textAreaDocumentListener.setSelection(null);

		if (selection == null) {
			textArea.setText("No selection");
			if (exposedDatanature) {
//				datatypeSpinner.setEnabled(false);
//				datatypeSpinner.setValue(ReferencedDataNature.UNKNOWN);
//				charsetSpinner.setEnabled(false);
//				setCharsetSpinner(null);
			}
			return;
		}
		if (!selection.isLeaf()) {
			textArea.setText("List selected");
			if (exposedDatanature) {
//				datatypeSpinner.setEnabled(false);
//				datatypeSpinner.setValue(ReferencedDataNature.UNKNOWN);
//				charsetSpinner.setEnabled(false);
//				setCharsetSpinner(null);
			}
			return;
		}
		Object selectedUserObject = selection.getUserObject();
		if (selectedUserObject == null) {
			textArea.setText("List selected");
			if (exposedDatanature) {
//				datatypeSpinner.setEnabled(false);
//				datatypeSpinner.setValue(ReferencedDataNature.UNKNOWN);
			}
			return;
		}
		if (selectedUserObject instanceof String) {
			textArea.setText((String) selection.getUserObject());
			textAreaDocumentListener.setSelection(selection);
			textArea.setEditable(true);
			textArea.requestFocusInWindow();
			textArea.selectAll();
			if (exposedDatanature) {
//				datatypeSpinner.setEnabled(false);
//				datatypeSpinner.setValue(ReferencedDataNature.TEXT);
//				charsetSpinner.setEnabled(false);
//				setCharsetSpinner(UTF8.name());
			}
		} else if (selectedUserObject instanceof File) {
			File ref = (File) selectedUserObject;
			textArea.setText("File : " + ref);
			if (exposedDatanature) {
//				datatypeSpinner.setEnabled(true);
//				datatypeSpinner.setValue(ref.getDataNature());
//				setCharsetSpinner(ref.getCharset());
//				if (ref.getDataNature().equals(ReferencedDataNature.TEXT)) {
//					charsetSpinner.setEnabled(true);
//				} else {
//					charsetSpinner.setEnabled(false);
//				}
			}
		} else if (selectedUserObject instanceof URL) {
			URL ref = (URL) selectedUserObject;
			textArea.setText("URL : " + ref);
			if (exposedDatanature) {
//				datatypeSpinner.setEnabled(false);
//				datatypeSpinner.setValue(ref.getDataNature());
//				charsetSpinner.setEnabled(false);
//				setCharsetSpinner(ref.getCharset());
			}
		} else
			textArea.setText(selection.getUserObject().toString());
	}

	private final class UpdateEditorPaneOnSelection implements
			TreeSelectionListener {
		TreePath oldSelectionPath = null;

		public void setSelectionPath(TreePath selectionPath) {
			if (oldSelectionPath != null) {
				DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) oldSelectionPath
						.getLastPathComponent();
				if (lastPathComponent != null && textArea.isEditable())
					lastPathComponent.setUserObject(textArea.getText());
			}

			oldSelectionPath = selectionPath;

			DefaultMutableTreeNode selection = null;
			if (selectionPath != null)
				selection = (DefaultMutableTreeNode) selectionPath
						.getLastPathComponent();
			updateEditorPane(selection);
		}

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			setSelectionPath(e.getNewLeadSelectionPath());
		}
	}

	public class NewListAction extends AbstractAction {
		private final int depth;

		private NewListAction(int depth) {
			super("New list (depth " + depth + ")", addListIcon);
			this.depth = depth;
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) treeModel
					.getRoot();
			DefaultMutableTreeNode selection = getSelectedNode();
			if (selection != null)
				parent = selection;
			@SuppressWarnings("unused")
			DefaultMutableTreeNode added = addPojo(parent,
					new ArrayList<Object>(), depth);
			setStatus("Added new collection with depth " + depth, null);
		}
	}

	public class UpdateActionsOnTreeSelection implements
			TreeSelectionListener {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree
					.getLastSelectedPathComponent();
			if (selectedNode == null)
				// Selection cleared
				deleteNodeAction.setEnabled(false);
			else
				deleteNodeAction.setEnabled(selectedNode != treeModel.getRoot());
		}
	}

	public class AddFileAction extends AbstractAction {
		public AddFileAction() {
			super((depth == 0 ? "Set" : "Add") + " file location...",
					addFileIcon);
		}

		@Override
		@SuppressWarnings("unused")
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			String currentDir = prefs.get("currentDir", System
					.getProperty("user.home"));
			fileChooser.setDialogTitle("Choose files or directory");

			fileChooser.setCurrentDirectory(new File(currentDir));
			fileChooser.setMultiSelectionEnabled(true);
			fileChooser.setFileSelectionMode(FILES_AND_DIRECTORIES);

			if (fileChooser.showOpenDialog(RegistrationPanel.this) != APPROVE_OPTION)
				return;
			prefs.put("currentDir", fileChooser.getCurrentDirectory()
					.toString());
			DefaultMutableTreeNode node = getSelectedNode();

			for (File file : fileChooser.getSelectedFiles()) {
				if (!file.isDirectory()) {
					DefaultMutableTreeNode added = addPojo(node, file, 0);
					setStatus("Added file : " + file.getPath(), null);
					continue;
				}

				if (treeModel.getDepth() < 1) {
					// TODO add popup warning
					setStatus("Can't add directory to single item input", null);
					return;
				}

				/*
				 * Try to handle directories as flat lists, don't nest any
				 * deeper for now.
				 */
				List<File> children = new ArrayList<>();
				for (File child : file.listFiles())
					if (child.isFile())
						children.add(child);
				DefaultMutableTreeNode added = addPojo(node, children, 1);
				setStatus("Added directory : " + file.getPath(), null);
			}
		}
	}

	/**
	 * Add a new default text string, adding to the root node (which will
	 * cascade down until it hits the correct level through logic in the model)
	 */
	public class AddTextAction extends AbstractAction {
		public AddTextAction() {
			super((depth == 0 ? "Set" : "Add") + " value", addTextIcon);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode node = getSelectedNode();
			String newValue;
			if (example != null && example.length() > 0)
				newValue = example;
			else
				newValue = NEW_VALUE;

			@SuppressWarnings("unused")
			DefaultMutableTreeNode added = addPojo(node, newValue, 0);
			setStatus("Added new value.  Edit value on right.", null);
		}
	}

	private DefaultMutableTreeNode addPojo(DefaultMutableTreeNode node,
			Object newValue, int position) {
		DefaultMutableTreeNode added = treeModel.addPojoStructure(node, node,
				newValue, position);
		tree.setSelectionPath(new TreePath(added.getPath()));
		updateEditorPane(added);
		return added;
	}

	public class AddURLAction extends AbstractAction {
		private static final String URL_REGEX = "http:\\/\\/(\\w+:{0,1}\\w*@)?(\\S+)(:[0-9]+)?(\\/|\\/([\\w#!:.?+=&%@!\\-\\/]))?";

		public AddURLAction() {
			super((depth == 0 ? "Set" : "Add") + " URL ...", addUrlIcon);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			String currentUrl = prefs.get("currentUrl",
					"http://www.mygrid.org.uk/");

			UrlPanel urlPanel = new UrlPanel();

			ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
					"Add an http URL", urlPanel);
			vuid.addTextComponentValidation(urlPanel.getUrlField(),
					"Set the URL.", null, "", URL_REGEX,
					"Not a valid http URL.");
			vuid.setSize(new Dimension(400, 200));
			urlPanel.setUrl(currentUrl);

			if (vuid.show(RegistrationPanel.this)) {
				String urlString = urlPanel.getUrl();
				try {
					URL url = new URL(urlString);
					prefs.put("currentUrl", url.toString());

					DefaultMutableTreeNode node = getSelectedNode();

					@SuppressWarnings("unused")
					DefaultMutableTreeNode added = addPojo(node, url, 0);
					setStatus("Added URL : " + url, null);
				} catch (MalformedURLException e1) {
					setStatus("Invalid URL.", null);
				}
			}
		}
	}

	/**
	 * Remove any children of the currently selected node
	 */
	public class DeleteNodeAction extends AbstractAction {
		public DeleteNodeAction() {
			super("Delete", deleteNodeIcon);
			// Starts off disabled
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
					.getSelectionPath().getLastPathComponent();
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
					.getParent();
			DefaultMutableTreeNode previousChild = null;
			if (parent != null) {
				int index = parent.getIndex(node);
				if (index > 0)
					previousChild = (DefaultMutableTreeNode) parent
							.getChildAt(index - 1);
			}
			treeModel.removeNodeFromParent(node);
			if (previousChild == null)
				tree.setSelectionPath(null);
			else
				tree.setSelectionPath(new TreePath(previousChild.getPath()));
			setStatus("Deleted node", null);
		}
	}

	private class TextAreaDocumentListener implements DocumentListener {
		private final DialogTextArea textArea;
		private DefaultMutableTreeNode selection;

		public TextAreaDocumentListener(DialogTextArea textArea) {
			this.textArea = textArea;
			textArea.getDocument().addDocumentListener(this);
			this.setSelection(null);
		}

		/**
		 * @param selection
		 *            the selection to set
		 */
		public void setSelection(DefaultMutableTreeNode selection) {
			this.selection = selection;
		}

		private void updateSelection() {
			if (textArea.isEditable() && selection != null) {
				selection.setUserObject(textArea.getText());
				treeModel.nodeChanged(selection);
			}
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateSelection();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateSelection();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updateSelection();
		}
	}

	public void setDescription(String inputDescription) {
		if (inputDescription != null)
			descriptionArea.setText(inputDescription);
		else
			descriptionArea.setText(NO_PORT_DESCRIPTION);
		descriptionArea.setCaretPosition(0);
	}

	public String getExample() {
		return example;
	}

	public void setExample(String inputExample) {
		this.example = inputExample;
		if (inputExample != null)
			exampleArea.setText(inputExample);
		else
			exampleArea.setText(NO_EXAMPLE_VALUE);
		exampleArea.setCaretPosition(0);
	}

	public void setValue(Object o) {
		addPojo(null, o, 0);
	}

	public void setValue(Object o, int depth) {
		addPojo(null, o, depth);
	}

	public Object getValue() {
		return treeModel.getAsPojo();
	}

	public boolean checkUserInput() {
		// TODO Auto-generated method stub
		return false;
	}
}
