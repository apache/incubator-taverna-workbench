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
package org.apache.taverna.workbench.views.results.workflow;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.Color.GRAY;
import static java.awt.event.ItemEvent.SELECTED;
import static javax.swing.BoxLayout.LINE_AXIS;
import static javax.swing.SwingUtilities.invokeLater;
import static org.apache.taverna.renderers.RendererUtils.getInputStream;
import static org.apache.taverna.results.ResultsUtils;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.refreshIcon;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.taverna.lang.ui.DialogTextArea;
import org.apache.taverna.renderers.Renderer;
import org.apache.taverna.renderers.RendererException;
import org.apache.taverna.renderers.RendererRegistry;
import org.apache.taverna.workbench.views.results.saveactions.SaveIndividualResultSPI;

import org.apache.log4j.Logger;

import org.apache.taverna.databundle.DataBundles;
import org.apache.taverna.databundle.ErrorDocument;
import org.apache.taverna.scufl2.api.port.OutputWorkflowPort;
import eu.medsea.mimeutil.MimeType;

/**
 * Creates a component that renders an individual result from an output port.
 * The component can render the result according to the renderers existing for
 * the output port's MIME type or display an error document.
 *
 * @author Ian Dunlop
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class RenderedResultComponent extends JPanel {
	private static final Logger logger = Logger.getLogger(RenderedResultComponent.class);
	private static final String WRAP_TEXT = "Wrap text";
	private static final String ERROR_DOCUMENT = "Error Document";

	/** Panel containing rendered result*/
	private JPanel renderedResultPanel;
	/** Combo box containing possible result types*/
	private JComboBox<String> renderersComboBox;
	/**
	 * Button to refresh (re-render) the result, especially needed for large
	 * results that are not rendered or are partially rendered and the user
	 * wished to re-render them
	 */
	private JButton refreshButton;
	/**
	 * Preferred result type renderers (the ones recognised to be able to handle
	 * the result's MIME type)
	 */
	private List<Renderer> recognisedRenderersForMimeType;
	/**
	 * All other result type renderers (the ones not recognised to be able to
	 * handle the result's MIME type) in case user wants to use them.
	 */
	private List<Renderer> otherRenderers;
	/** Renderers' registry */
	private final RendererRegistry rendererRegistry;
	/**
	 * List of all MIME strings from all available renderers to be used for
	 * {@link #renderersComboBox}. Those that come from
	 * {@link #recognisedRenderersForMimeType} are the preferred ones. Those
	 * from {@link #otherRenderers} will be greyed-out in the combobox list but
	 * could still be used.
	 */
	private String[] mimeList;
	/**
	 * List of all available renderers but ordered to match the corresponding
	 * MIME type strings in mimeList: first the preferred renderers from
	 * {@link #recognisedRenderersForMimeType} then the ones from
	 * {@link #otherRenderers}.
	 */
	private ArrayList<Renderer> rendererList;
	/**
	 * Remember the MIME type of the last used renderer. Use "
	 * <tt>text/plain</tt>" by default until user changes it - then use that one
	 * for all result items of the port (in case result contains a list). "
	 * <tt>text/plain</tt>" will always be added to the {@link #mimeList}.
	 */
	// text renderer will always be available
	private String lastUsedMIMEtype = "text/plain";
	/** If result is "text/plain" - provide possibility to wrap wide text */
	private JCheckBox wrapTextCheckBox;
	/** Reference to the object being displayed (contained in the tree node) */
	private Path path;
	/**
	 * In case the node can be rendered as "<tt>text/plain</tt>", map the hash
	 * code of the node to the wrap text check box selection value for that node
	 * (that remembers if user wanted the text wrapped or not).
	 */
	private Map<Path, Boolean> nodeToWrapSelection = new HashMap<>();
	/** List of all output ports - needs to be passed to 'save result' actions. */
	List<OutputWorkflowPort> dataflowOutputPorts = null;
	/** Panel containing all 'save results' buttons */
	JPanel saveButtonsPanel = null;

	/**
	 * Creates the component.
	 */
	public RenderedResultComponent(RendererRegistry rendererRegistry,
			List<SaveIndividualResultSPI> saveActions) {
		this.rendererRegistry = rendererRegistry;
		setLayout(new BorderLayout());

		// Results type combo box
		renderersComboBox = new JComboBox<>();
		renderersComboBox.setModel(new DefaultComboBoxModel<String>()); // initially empty

		renderersComboBox.setRenderer(new ColorCellRenderer());
		renderersComboBox.setEditable(false);
		renderersComboBox.setEnabled(false); // initially disabled

		// Set the new listener - listen for changes in the currently selected renderer
		renderersComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED
						&& !ERROR_DOCUMENT.equals(e.getItem()))
					// render the result using the newly selected renderer
					renderResult();
			}
		});

		JPanel resultsTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		resultsTypePanel.add(new JLabel("Value type"));
		resultsTypePanel.add(renderersComboBox);

		// Refresh (re-render) button
		refreshButton = new JButton("Refresh", refreshIcon);
		refreshButton.setEnabled(false);
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				renderResult();
				refreshButton.getParent().requestFocusInWindow();
				/*
				 * so that the button does not stay focused after it is clicked
				 * on and did its action
				 */
			}
		});
		resultsTypePanel.add(refreshButton);

		// Check box for wrapping text if result is of type "text/plain"
		wrapTextCheckBox = new JCheckBox(WRAP_TEXT);
		wrapTextCheckBox.setVisible(false);
		wrapTextCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// Should have only one child component holding the rendered result
				// Check for empty just as well
				if (renderedResultPanel.getComponents().length == 0)
					return;
				Component component = renderedResultPanel.getComponent(0);
				if (component instanceof DialogTextArea) {
					nodeToWrapSelection.put(path,
							e.getStateChange() == SELECTED);
					renderResult();
				}
			}
		});

		resultsTypePanel.add(wrapTextCheckBox);

		// 'Save result' buttons panel
		saveButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		for (SaveIndividualResultSPI action : saveActions) {
			action.setResultReference(null);
			final JButton saveButton = new JButton(action.getAction());
			saveButton.setEnabled(false);
			saveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveButton.getParent().requestFocusInWindow();
					/*
					 * so that the button does not stay focused after it is
					 * clicked on and did its action
					 */
				}
			});
			saveButtonsPanel.add(saveButton);
		}

		// Top panel contains result type combobox and various save buttons
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, LINE_AXIS));
		topPanel.add(resultsTypePanel);
		topPanel.add(saveButtonsPanel);

		// Rendered results panel - initially empty
		renderedResultPanel = new JPanel(new BorderLayout());

		// Add all components
		add(topPanel, NORTH);
		add(new JScrollPane(renderedResultPanel), CENTER);
	}

	/**
	 * Sets the path this components renders the results for, and update the
	 * rendered results panel.
	 */
	public void setPath(final Path path) {
		this.path = path;
		invokeLater(new Runnable() {
			@Override
			public void run() {
				if (path == null || DataBundles.isList(path))
					clearResult();
				else
					updateResult();
			}
		});
	}

	/**
	 * Update the component based on the node selected from the
	 * ResultViewComponent tree.
	 */
	public void updateResult() {
		if (recognisedRenderersForMimeType == null)
			recognisedRenderersForMimeType = new ArrayList<>();
		if (otherRenderers == null)
			otherRenderers = new ArrayList<>();

		// Enable the combo box
		renderersComboBox.setEnabled(true);

		/*
		 * Update the 'save result' buttons appropriately as the result node had
		 * changed
		 */
		for (int i = 0; i < saveButtonsPanel.getComponents().length; i++) {
			JButton saveButton = (JButton) saveButtonsPanel.getComponent(i);
			SaveIndividualResultSPI action = (SaveIndividualResultSPI) saveButton
					.getAction();
			// Update the action with the new result reference
			action.setResultReference(path);
			saveButton.setEnabled(true);
		}

		if (DataBundles.isValue(path) || DataBundles.isReference(path)) {
			// Enable refresh button
			refreshButton.setEnabled(true);

			List<MimeType> mimeTypes = new ArrayList<>();
			try (InputStream inputstream = getInputStream(path)) {
				mimeTypes.addAll(getMimeTypes(inputstream));
			} catch (IOException e) {
				logger.warn("Error getting mimetype", e);
			}

			if (mimeTypes.isEmpty())
				// If MIME types is empty - add "plain/text" MIME type
				mimeTypes.add(new MimeType("text/plain"));
			else if (mimeTypes.size() == 1
					&& mimeTypes.get(0).toString().equals("chemical/x-fasta")) {
				/*
				 * If MIME type is recognised as "chemical/x-fasta" only then
				 * this might be an error from MIME magic (i.e., sometimes it
				 * recognises stuff that is not "chemical/x-fasta" as
				 * "chemical/x-fasta" and then Seq Vista renderer is used that
				 * causes errors) - make sure we also add the renderers for
				 * "text/plain" and "text/xml" as it is most probably just
				 * normal xml text and push the "chemical/x-fasta" to the bottom
				 * of the list.
				 */
				mimeTypes.add(0, new MimeType("text/plain"));
				mimeTypes.add(1, new MimeType("text/xml"));
			}

			for (MimeType mimeType : mimeTypes) {
				List<Renderer> renderersList = rendererRegistry.getRenderersForMimeType(mimeType
						.toString());
				for (Renderer renderer : renderersList)
					if (!recognisedRenderersForMimeType.contains(renderer))
						recognisedRenderersForMimeType.add(renderer);
			}
			// if there are no renderers then force text/plain
			if (recognisedRenderersForMimeType.isEmpty())
				recognisedRenderersForMimeType = rendererRegistry
						.getRenderersForMimeType("text/plain");

			/*
			 * Add all other available renderers that are not recognised to be
			 * able to handle the MIME type of the result
			 */
			otherRenderers = new ArrayList<>(rendererRegistry.getRenderers());
			otherRenderers.removeAll(recognisedRenderersForMimeType);

			mimeList = new String[recognisedRenderersForMimeType.size()
					+ otherRenderers.size()];
			rendererList = new ArrayList<>();

			/*
			 * First add the ones that can handle the MIME type of the result
			 * item
			 */
			for (int i = 0; i < recognisedRenderersForMimeType.size(); i++) {
				mimeList[i] = recognisedRenderersForMimeType.get(i).getType();
				rendererList.add(recognisedRenderersForMimeType.get(i));
			}
			// Then add the other renderers just in case
			for (int i = 0; i < otherRenderers.size(); i++) {
				mimeList[recognisedRenderersForMimeType.size() + i] = otherRenderers.get(i)
						.getType();
				rendererList.add(otherRenderers.get(i));
			}

			renderersComboBox.setModel(new DefaultComboBoxModel<String>(mimeList));

			if (mimeList.length > 0) {
				int index = 0;

				// Find the index of the current MIME type for this output port.
				for (int i = 0; i < mimeList.length; i++)
					if (mimeList[i].equals(lastUsedMIMEtype)) {
						index = i;
						break;
					}

				int previousindex = renderersComboBox.getSelectedIndex();
				renderersComboBox.setSelectedIndex(index);
				/*
				 * force rendering as setSelectedIndex will not fire an
				 * itemstatechanged event if previousindex == index and we still
				 * need render the result as we may have switched from a
				 * different result item in a result list but the renderer index
				 * stayed the same
				 */
				if (previousindex == index)
					renderResult(); // draw the rendered result component
			}

		} else if (DataBundles.isError(path)) {
			// Disable refresh button
			refreshButton.setEnabled(false);

			// Hide wrap text check box - only works for actual data
			wrapTextCheckBox.setVisible(false);

			// Reset the renderers as we have an error item
			recognisedRenderersForMimeType = null;
			otherRenderers = null;

			DefaultMutableTreeNode root = new DefaultMutableTreeNode("Error Trace");

			try {
				ErrorDocument errorDocument = DataBundles.getError(path);
				try {
					buildErrorDocumentTree(root, errorDocument);
				} catch (IOException e) {
					logger.warn("Error building error document tree", e);
				}
			} catch (IOException e) {
				logger.warn("Error getting the error document", e);
			}

			JTree errorTree = new JTree(root);
			errorTree.setCellRenderer(new DefaultTreeCellRenderer() {
				@Override
				public Component getTreeCellRendererComponent(JTree tree,
						Object value, boolean selected, boolean expanded,
						boolean leaf, int row, boolean hasFocus) {
					Component renderer = null;
					if (value instanceof DefaultMutableTreeNode) {
						DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
						Object userObject = treeNode.getUserObject();
						if (userObject instanceof ErrorDocument)
							renderer = renderErrorDocument(tree, selected,
									expanded, leaf, row, hasFocus,
									(ErrorDocument) userObject);
					}
					if (renderer == null)
						renderer = super.getTreeCellRendererComponent(tree,
								value, selected, expanded, leaf, row, hasFocus);
					if (renderer instanceof JLabel) {
						JLabel label = (JLabel) renderer;
						label.setIcon(null);
					}
					return renderer;
				}

				private Component renderErrorDocument(JTree tree,
						boolean selected, boolean expanded, boolean leaf,
						int row, boolean hasFocus, ErrorDocument errorDocument) {
					return super.getTreeCellRendererComponent(tree, "<html>"
							+ escapeHtml(errorDocument.getMessage())
							+ "</html>", selected, expanded, leaf, row,
							hasFocus);
				}
			});

			renderersComboBox.setModel(new DefaultComboBoxModel<>(
					new String[] { ERROR_DOCUMENT }));
			renderedResultPanel.removeAll();
			renderedResultPanel.add(errorTree, CENTER);
			repaint();
		}
	}

	public void buildErrorDocumentTree(DefaultMutableTreeNode node,
			ErrorDocument errorDocument) throws IOException {
		DefaultMutableTreeNode child = new DefaultMutableTreeNode(errorDocument);
		String trace = errorDocument.getTrace();
		if (trace != null && !trace.isEmpty())
			for (String line : trace.split("\n"))
				child.add(new DefaultMutableTreeNode(line));
		node.add(child);

		List<Path> causes = errorDocument.getCausedBy();
		for (Path cause : causes)
			if (DataBundles.isError(cause)) {
				ErrorDocument causeErrorDocument = DataBundles.getError(cause);
				if (causes.size() == 1)
					buildErrorDocumentTree(node, causeErrorDocument);
				else
					buildErrorDocumentTree(child, causeErrorDocument);
			} else if (DataBundles.isList(cause)) {
				List<ErrorDocument> errorDocuments = getErrorDocuments(cause);
				if (errorDocuments.size() == 1)
					buildErrorDocumentTree(node, errorDocuments.get(0));
				else
					for (ErrorDocument errorDocument2 : errorDocuments)
						buildErrorDocumentTree(child, errorDocument2);
			}
	}

	public List<ErrorDocument> getErrorDocuments(Path reference)
			throws IOException {
		List<ErrorDocument> errorDocuments = new ArrayList<>();
		if (DataBundles.isError(reference))
			errorDocuments.add(DataBundles.getError(reference));
		else if (DataBundles.isList(reference))
			for (Path element : DataBundles.getList(reference))
				errorDocuments.addAll(getErrorDocuments(element));
		return errorDocuments;
	}

	/**
	 * Renders the result panel using the last used renderer.
	 */
	public void renderResult() {
		if (ERROR_DOCUMENT.equals(renderersComboBox.getSelectedItem()))
			// skip error documents - do not (re)render
			return;

		int selectedIndex = renderersComboBox.getSelectedIndex();
		if (mimeList != null && selectedIndex >= 0) {
			Renderer renderer = rendererList.get(selectedIndex);

			if (renderer.getType().equals("Text")) { // if the result is "text/plain"
				/*
				 * We use node's hash code as the key in the nodeToWrapCheckBox
				 * map as node's user object may be too large
				 */
				if (nodeToWrapSelection.get(path) == null)
					// initially not selected
					nodeToWrapSelection.put(path, false);
				wrapTextCheckBox.setSelected(nodeToWrapSelection.get(path));
				wrapTextCheckBox.setVisible(true);
			} else
				wrapTextCheckBox.setVisible(false);
			/*
			 * Remember the last used renderer - use it for all result items of
			 * this port
			 */
			// currentRendererIndex = selectedIndex;
			lastUsedMIMEtype = mimeList[selectedIndex];

			JComponent component = null;
			try {
				component = renderer.getComponent(path);
				if (component instanceof DialogTextArea)
					if (wrapTextCheckBox.isSelected())
						((JTextArea) component).setLineWrap(wrapTextCheckBox.isSelected());
				if (component instanceof JTextComponent)
					((JTextComponent) component).setEditable(false);
				else if (component instanceof JTree)
					((JTree) component).setEditable(false);
			} catch (RendererException e1) {
				// maybe this should be Exception
				/*
				 * show the user that something unexpected has happened but
				 * continue
				 */
				component = new DialogTextArea(
						"Could not render using renderer type "
								+ renderer.getClass()
								+ "\n"
								+ "Please try with a different renderer if available and consult log for details of problem");
				((DialogTextArea) component).setEditable(false);
				logger.warn("Couln not render using " + renderer.getClass(), e1);
			}
			renderedResultPanel.removeAll();
			renderedResultPanel.add(component, CENTER);
			repaint();
			revalidate();
		}
	}

	/**
	 * Clears the result panel.
	 */
	public void clearResult() {
		refreshButton.setEnabled(false);
		wrapTextCheckBox.setVisible(false);
		renderedResultPanel.removeAll();

		// Update the 'save result' buttons appropriately
		for (int i = 0; i < saveButtonsPanel.getComponents().length; i++) {
			JButton saveButton = (JButton) saveButtonsPanel.getComponent(i);
			SaveIndividualResultSPI action = (SaveIndividualResultSPI) saveButton
					.getAction();
			// Update the action
			action.setResultReference(null);
			saveButton.setEnabled(false);
		}

		renderersComboBox.setModel(new DefaultComboBoxModel<String>());
		renderersComboBox.setEnabled(false);

		revalidate();
		repaint();
	}

	class ColorCellRenderer implements ListCellRenderer<String> {
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

		@Override
		public Component getListCellRendererComponent(
				JList<? extends String> list, String value, int index,
				boolean isSelected, boolean cellHasFocus) {
			JComponent renderer = (JComponent) defaultRenderer
					.getListCellRendererComponent(list, value, index,
							isSelected, cellHasFocus);
			if (recognisedRenderersForMimeType == null) // error occurred
				return renderer;
			if (value != null && index >= recognisedRenderersForMimeType.size())
				// one of the non-preferred renderers - show it in grey
				renderer.setForeground(GRAY);
			return renderer;
		}
	}
}
