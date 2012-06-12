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
import java.awt.GridLayout;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SpinnerListModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;
import net.sf.taverna.t2.reference.ReferencedDataNature;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;
import net.sf.taverna.t2.reference.impl.external.http.HttpReference;
import net.sf.taverna.t2.reference.ui.tree.PreRegistrationTree;
import net.sf.taverna.t2.reference.ui.tree.PreRegistrationTreeModel;

import org.apache.log4j.Logger;

import uk.org.taverna.configuration.database.DatabaseConfiguration;

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
	private final List<Action> addCollectionActions = new ArrayList<Action>();

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

	private JSpinner datatypeSpinner;
	private JSpinner charsetSpinner;
	private static Charset utf8 = Charset.forName("UTF-8");

	private String example;

	private DatabaseConfiguration config;

	private boolean exposedDatanature = false;

	private static String UNKNOWN = "UNKNOWN";

	private static Map<String, String> charsetNameMap = new HashMap<String, String>();

	/**
	 * Construct a new registration panel for an input with the specified depth.
	 *
	 * @param depth
	 *            Depth of the POJO to construct from this panel
	 * @param example
	 * @param inputDescription
	 * @param inputName
	 */
	public RegistrationPanel(int depth, String name, String description, String example, DatabaseConfiguration databaseConfiguration) {
		super(new BorderLayout());
		this.depth = depth;
		this.example = example;
		config = databaseConfiguration;
		tree = new PreRegistrationTree(depth, name) {
			@Override
			public void setStatusMessage(String message, boolean isError) {
				if (isError) {
					setStatus(message, Color.red);
				} else {
					setStatus(message, Color.black);
				}
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
		annotationsPanel.setLayout(new BoxLayout(annotationsPanel, BoxLayout.Y_AXIS));
		JScrollPane descriptionAreaScrollPane = new JScrollPane(descriptionArea);
		JScrollPane exampleAreaScrollPane = new JScrollPane(exampleArea);
		annotationsPanel.add(descriptionAreaScrollPane);
		annotationsPanel.add(Box.createRigidArea(new Dimension(0,5))); // add some empty space
		annotationsPanel.add(exampleAreaScrollPane);

		JToolBar toolbar = createToolBar();

		JPanel textAreaPanel = new JPanel();
		textAreaPanel.setLayout(new BorderLayout());

		textArea = new DialogTextArea();
		textAreaDocumentListener = new TextAreaDocumentListener(textArea);
		textArea.setEditable(false);

		textAreaPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
		if (config.isExposeDatanature()) {
			textAreaPanel.add(createTypeAccessory(), BorderLayout.SOUTH);
			exposedDatanature = true;
		}

		splitPaneHorizontal = new JSplitPane();
		splitPaneHorizontal.add(new JScrollPane(this.tree), JSplitPane.LEFT);
		splitPaneHorizontal.add(textAreaPanel, JSplitPane.RIGHT);
		splitPaneHorizontal.setDividerLocation(150);
		JPanel toolbarAndInputsPanel = new JPanel(new BorderLayout());
		toolbarAndInputsPanel.add(splitPaneHorizontal, BorderLayout.CENTER);
		toolbarAndInputsPanel.add(toolbar, BorderLayout.NORTH);

		splitPaneVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPaneVertical.add(annotationsPanel, JSplitPane.TOP);
		splitPaneVertical.add(toolbarAndInputsPanel, JSplitPane.BOTTOM);
		int dividerPosition = (int) Math.round(annotationsPanel.getPreferredSize().getHeight()) + 10;
		splitPaneVertical.setDividerLocation(dividerPosition);

		add(splitPaneVertical, BorderLayout.CENTER);

		// Listen to selections on the tree to enable or disable actions
		tree.addTreeSelectionListener(new UpdateActionsOnTreeSelection());
		status = new JLabel();
		status.setOpaque(false);
		status.setBorder(new EmptyBorder(2, 2, 2, 2));
		setStatus("Drag to re-arrange, or drag files, URLs, or text to add",
				null);
		add(status, BorderLayout.SOUTH);
	}

	private static List<String> charsetNames() {
		List<String> result = new ArrayList<String> ();
		result.add(UNKNOWN);

		for (String name : Charset.availableCharsets().keySet()) {
			String upperCase = name.toUpperCase();
			result.add(upperCase);
			charsetNameMap.put(upperCase, name);
		}
		Collections.sort(result);
		return result;
	}

	private static SpinnerListModel datatypeModel = new SpinnerListModel(new ReferencedDataNature[] {ReferencedDataNature.UNKNOWN, ReferencedDataNature.TEXT, ReferencedDataNature.BINARY});
	private static SpinnerListModel charsetModel = new SpinnerListModel(charsetNames());

	private JPanel createTypeAccessory() {
		JPanel result = new JPanel();
		result.setLayout(new GridLayout(0,2));
		result.add(new JLabel("Data type:"));
		datatypeSpinner = new JSpinner(datatypeModel);
		datatypeSpinner.setValue(ReferencedDataNature.UNKNOWN);
		datatypeSpinner.setEnabled(false);
		result.add(datatypeSpinner);
		result.add(new JLabel("Character Set:"));
		charsetSpinner = new JSpinner(charsetModel);
		charsetSpinner.setValue(UNKNOWN);
		charsetSpinner.setEnabled(false);
		datatypeSpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				DefaultMutableTreeNode selectedNode = getSelectedNode();
				Object selectedUserObject = null;
				if (selectedNode != null) {
					selectedUserObject = selectedNode.getUserObject();
				}
				ReferencedDataNature nature = (ReferencedDataNature) datatypeSpinner.getValue();
				if (nature.equals(ReferencedDataNature.UNKNOWN)) {
					charsetSpinner.setEnabled(false);
				} else if (nature.equals(ReferencedDataNature.TEXT)) {
					charsetSpinner.setEnabled(true);
				} else if (nature.equals(ReferencedDataNature.BINARY)) {
					charsetSpinner.setEnabled(false);
				}
				if (selectedUserObject instanceof FileReference) {
					FileReference ref = (FileReference) selectedUserObject;
					ref.setDataNature(nature);
				}
			}});
		charsetSpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				DefaultMutableTreeNode selectedNode = getSelectedNode();
				Object selectedUserObject = null;
				if (selectedNode != null) {
					selectedUserObject = selectedNode.getUserObject();
				}
				String cSet = (String) charsetSpinner.getValue();
				if (selectedUserObject instanceof FileReference) {
					FileReference ref = (FileReference) selectedUserObject;
					if (cSet.equals(UNKNOWN)) {
						ref.setCharset(null);
					} else {
						ref.setCharset(charsetNameMap.get(cSet));
					}
				}
			}

		});

		result.add(charsetSpinner);
		return result;
	}

	private JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();
		buildActions();
		toolBar.setFloatable(false);
		JButton comp = new JButton(deleteNodeAction);
		comp.setToolTipText("Remove the currently selected input");
		toolBar.add(comp);
		JButton comp2 = new JButton(addTextAction);
		if (depth == 0) {
			comp2.setToolTipText("Set the input value");
		} else {
			comp2.setToolTipText("Add a new input value");
		}

		toolBar.add(comp2);
		JButton comp3 = new JButton(addFileAction);
		if (depth == 0) {
			comp3.setToolTipText("Set the input value from a file");
		} else {
			comp3.setToolTipText("Add an input value from a file");
		}
		toolBar.add(comp3);
		JButton comp4 = new JButton(addUrlAction);
		if (depth == 0) {
			comp4.setToolTipText("Load the input value from a URL");
		} else {
			comp4.setToolTipText("Load an input value from a URL");
		}
		toolBar.add(comp4);
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
		addFileAction = new AddFileAction();
		addTextAction = new AddTextAction();
		addUrlAction = new AddURLAction();
		deleteNodeAction = new DeleteNodeAction();

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
	private DefaultMutableTreeNode getSelectedNode() {
		DefaultMutableTreeNode node = null;
		TreePath selectionPath = tree.getSelectionPath();
		if (selectionPath != null) {
			node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
		}
		return node;
	}

	public void setStatus(String statusString, Color textColour) {
		status.setText(statusString);
		if (textColour != null) {
			status.setForeground(textColour);
		} else {
			status.setForeground(Color.black);
		}
	}

	private void setCharsetSpinner(String charsetName) {
		if (charsetName == null) {
			charsetSpinner.setValue(UNKNOWN);
			return;
		}
		String cName = charsetName.toUpperCase();
		if (charsetNames().contains(cName)) {
			charsetSpinner.setValue(cName);
		} else {
			charsetSpinner.setValue(UNKNOWN);
		}
	}

	private void updateEditorPane(DefaultMutableTreeNode selection) {
		textArea.setEditable(false);
		textAreaDocumentListener.setSelection(null);

		if (selection == null) {
			textArea.setText("No selection");
			if (exposedDatanature) {
				datatypeSpinner.setEnabled(false);
				datatypeSpinner.setValue(ReferencedDataNature.UNKNOWN);
				charsetSpinner.setEnabled(false);
				setCharsetSpinner(null);
			}
			return;
		}
		if (!selection.isLeaf()) {
			textArea.setText("List selected");
			if (exposedDatanature) {
				datatypeSpinner.setEnabled(false);
				datatypeSpinner.setValue(ReferencedDataNature.UNKNOWN);
				charsetSpinner.setEnabled(false);
				setCharsetSpinner(null);
			}
			return;
		}
		Object selectedUserObject = selection.getUserObject();
		if (selectedUserObject == null) {
			textArea.setText("List selected");
			if (exposedDatanature) {
				datatypeSpinner.setEnabled(false);
				datatypeSpinner.setValue(ReferencedDataNature.UNKNOWN);
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
				datatypeSpinner.setEnabled(false);
				datatypeSpinner.setValue(ReferencedDataNature.TEXT);
				charsetSpinner.setEnabled(false);
				setCharsetSpinner(utf8.name());
			}
		} else if (selectedUserObject instanceof FileReference) {
			FileReference ref = (FileReference) selectedUserObject;
			textArea.setText("File : " + ref.getFilePath());
			if (exposedDatanature) {
				datatypeSpinner.setEnabled(true);
				datatypeSpinner.setValue(ref.getDataNature());
				setCharsetSpinner(ref.getCharset());
				if (ref.getDataNature().equals(ReferencedDataNature.TEXT)) {
					charsetSpinner.setEnabled(true);
				} else {
					charsetSpinner.setEnabled(false);
				}
			}
		} else if (selectedUserObject instanceof HttpReference) {
			HttpReference ref = (HttpReference) selectedUserObject;
			textArea.setText("URL : " + ref.getHttpUrlString());
			if (exposedDatanature) {
				datatypeSpinner.setEnabled(false);
				datatypeSpinner.setValue(ref.getDataNature());
				charsetSpinner.setEnabled(false);
				setCharsetSpinner(ref.getCharset());
			}
		} else {
			textArea.setText(selection.getUserObject().toString());
		}

	}

	private final class UpdateEditorPaneOnSelection implements
			TreeSelectionListener {

		TreePath oldSelectionPath = null;

		public void setSelectionPath(TreePath selectionPath) {
			if (oldSelectionPath != null) {
				DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) oldSelectionPath
						.getLastPathComponent();
				if (lastPathComponent != null && textArea.isEditable()) {
					lastPathComponent.setUserObject(textArea.getText());
				}
			}

			oldSelectionPath = selectionPath;

			DefaultMutableTreeNode selection = null;

			if (selectionPath != null) {
				selection = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
			}
			updateEditorPane(selection);
		}



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

		@SuppressWarnings("unused")
		public void actionPerformed(ActionEvent ae) {
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) treeModel.getRoot();
			DefaultMutableTreeNode selection = getSelectedNode();
			if (selection != null) {
				parent = selection;
			}
			DefaultMutableTreeNode added = addPojo(parent,
					new ArrayList<Object>(), depth);
			setStatus("Added new collection with depth " + depth,
					null);
		}
	}

	public class UpdateActionsOnTreeSelection implements
			TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree
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
			super((depth == 0 ? "Set" : "Add") + " file location...", addFileIcon);
		}

		@SuppressWarnings("unused")
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
				DefaultMutableTreeNode node = getSelectedNode();

				for (File file : fileChooser.getSelectedFiles()) {
					if (!file.isDirectory()) {

						FileReference ref = new FileReference(file);
						ref.setCharset(Charset.defaultCharset().name());
						DefaultMutableTreeNode added = addPojo(node, ref, 0);
						setStatus("Added file : " + file.getPath(),
								null);
					} else {
						if (treeModel.getDepth() < 1) {
							// TODO add popup warning
							setStatus(
									"Can't add directory to single item input",
									null);
							return;
						}
						// Try to handle directories as flat lists, don't
						// nest
						// any deeper for now.
						List<FileReference> children = new ArrayList<FileReference>();
						for (File child : file.listFiles()) {
							if (child.isFile()) {
								FileReference ref = new FileReference((File) child);
								ref.setCharset(Charset.defaultCharset().name());
								children.add(ref);
							}
						}
						DefaultMutableTreeNode added = addPojo(node, children, 1);
						setStatus("Added directory : " + file.getPath(),
								null);
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
			super((depth == 0 ? "Set" : "Add") + " value", addTextIcon);
		}

		@SuppressWarnings("unused")
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode node = getSelectedNode();
			String newValue;
			if (example != null && example.length() > 0) {
				newValue = example;
			} else {
				newValue = NEW_VALUE;
			}

			DefaultMutableTreeNode added = addPojo(node, newValue, 0);

			setStatus("Added new value.  Edit value on right.", null);
		}
	}

	private DefaultMutableTreeNode addPojo (DefaultMutableTreeNode node, Object newValue, int position) {
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

		@SuppressWarnings("unused")
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
						HttpReference ref = new HttpReference();
						ref.setHttpUrlString(urlString);
						prefs.put("currentUrl", urlString);

						DefaultMutableTreeNode node = getSelectedNode();

						DefaultMutableTreeNode added = addPojo(node, ref, 0);
						setStatus("Added URL : " + ref.getHttpUrlString(),
								null);
					} else {
						setStatus("Only http URLs are supported for now.",
								null);
					}
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
		public void actionPerformed(ActionEvent e) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath()
					.getLastPathComponent();
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
				DefaultMutableTreeNode previousChild = null;
				if (parent != null) {
					int index = parent.getIndex(node);
					if (index > 0) {
						previousChild = (DefaultMutableTreeNode) parent.getChildAt(index - 1);
					}
				}
				treeModel.removeNodeFromParent(node);
				if (previousChild == null) {
					tree.setSelectionPath(null);
				} else {
					tree.setSelectionPath(new TreePath(previousChild.getPath()));
				}
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
		 * @param selection the selection to set
		 */
		public void setSelection(DefaultMutableTreeNode selection) {
			this.selection = selection;
		}

		private void updateSelection() {
			if (textArea.isEditable()  && (this.selection != null)) {
            selection.setUserObject(textArea.getText());
            treeModel.nodeChanged(selection);
			}

		}
				public void insertUpdate(DocumentEvent e) {
					updateSelection();
                }

                public void removeUpdate(DocumentEvent e) {
                    updateSelection();
                }

                public void changedUpdate(DocumentEvent e) {
                    updateSelection();
                }
            }


	public void setDescription(String inputDescription) {
		if (inputDescription != null) {
			descriptionArea.setText(inputDescription);
		}
		else {
			descriptionArea.setText(NO_PORT_DESCRIPTION);
		}

		descriptionArea.setCaretPosition(0);
	}

	public void setExample(String inputExample) {
		this.example = inputExample;
		if (inputExample != null) {
			exampleArea.setText(inputExample);
		}
		else {
			exampleArea.setText(NO_EXAMPLE_VALUE);
		}

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
