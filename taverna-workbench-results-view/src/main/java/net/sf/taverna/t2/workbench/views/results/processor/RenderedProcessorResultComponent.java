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
package net.sf.taverna.t2.workbench.views.results.processor;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.event.ItemEvent.SELECTED;
import static javax.swing.BoxLayout.LINE_AXIS;
import static javax.swing.SwingUtilities.invokeLater;
import static net.sf.taverna.t2.results.ResultsUtils.getMimeTypes;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.refreshIcon;
import static net.sf.taverna.t2.workbench.views.results.processor.ProcessorResultTreeNode.ProcessorResultTreeNodeState.RESULT_REFERENCE;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.renderers.Renderer;
import net.sf.taverna.t2.renderers.RendererException;
import net.sf.taverna.t2.renderers.RendererRegistry;
import net.sf.taverna.t2.renderers.RendererUtils;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI;

import org.apache.log4j.Logger;

import uk.org.taverna.databundle.DataBundles;
import uk.org.taverna.databundle.ErrorDocument;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;
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
public class RenderedProcessorResultComponent extends JPanel {
	private static final String WRAP_TEXT = "Wrap text";
	private static final String ERROR_DOCUMENT = "Error Document";
	private static Logger logger = Logger
			.getLogger(RenderedProcessorResultComponent.class);

	/** Panel containing rendered result */
	private JPanel renderedResultPanel;
	/** Combo box containing possible result types */
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
	 * handle the result's MIME type) In case user wants to use them.
	 */
	private List<Renderer> otherRenderers;
	/** Renderers' registry */
	private final RendererRegistry rendererRegistry;
	/**
	 * List of all MIME strings from all available renderers to be used for
	 * renderersComboBox. Those that come from recognisedRenderersForMimeType
	 * are the preferred ones. Those from otherRenderers will be greyed-out in
	 * the combobox list but could still be used.
	 */
	private String[] mimeList;
	/**
	 * List of all available renderers but ordered to match the corresponding
	 * MIME type strings in mimeList: first the preferred renderers from
	 * recognisedRenderersForMimeType then the ones from otherRenderers.
	 */
	private ArrayList<Renderer> rendererList;
	/**
	 * Remember the MIME type of the last used renderer; use "text/plain" by
	 * default until user changes it - then use that one for all result items of
	 * the port (in case result contains a list). "text/plain" will always be
	 * added to the mimeList.
	 */
	private String lastUsedMIMEtype = "text/plain";
	// text renderer will always be available
	/** If result is "text/plain" - provide possibility to wrap wide text */
	private JCheckBox wrapTextCheckBox;
	/** Reference to the object being displayed (contained in the tree node) */
	private Path path;
	/** Currently selected node from the ResultViewComponent, if any. */
	private ProcessorResultTreeNode node = null;
	/**
	 * In case the node can be rendered as "text/plain", map the hash code of
	 * the node to the wrap text check box selection value for that node (that
	 * remembers if user wanted the text wrapped or not). We are using hash code
	 * as using node's user object might be too large.
	 */
	private Map<Integer, Boolean> nodeToWrapSelection = new HashMap<>();
	/** List of all output ports - needs to be passed to 'save result' actions. */
	List<? extends OutputWorkflowPort> dataflowOutputPorts = null;
	/** Panel containing all 'save results' buttons */
	JPanel saveButtonsPanel = null;

	/**
	 * Creates the component.
	 */
	public RenderedProcessorResultComponent(RendererRegistry rendererRegistry,
			List<SaveIndividualResultSPI> saveActions) {
		this.rendererRegistry = rendererRegistry;
		setLayout(new BorderLayout());
		setBorder(new EtchedBorder());

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
				if (e.getStateChange() == SELECTED
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
				if (renderedResultPanel.getComponent(0) instanceof DialogTextArea) {
					nodeToWrapSelection.put(node.hashCode(),
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
		renderedResultPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		// Add all components
		add(topPanel, NORTH);
		add(new JScrollPane(renderedResultPanel), CENTER);
	}

	/**
	 * Sets the tree node this components renders the results for, and update
	 * the rendered results panel.
	 */
	public void setNode(final ProcessorResultTreeNode node) {
		this.node = node;
		invokeLater(new Runnable() {
			@Override
			public void run() {
				if (node.isState(RESULT_REFERENCE))
					updateResult();
				else
					clearResult();
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

		ProcessorResultTreeNode result = (ProcessorResultTreeNode) node;

		// Reference to the result data
		path = result.getReference();

		// Enable the combo box
		renderersComboBox.setEnabled(true);

		/*
		 * Update the 'save result' buttons appropriately as the result node had
		 * changed
		 */
		for (int i = 0; i < saveButtonsPanel.getComponents().length; i++) {
			JButton saveButton = (JButton) saveButtonsPanel.getComponent(i);
			SaveIndividualResultSPI action = (SaveIndividualResultSPI) (saveButton
					.getAction());
			// Update the action with the new result reference
			action.setResultReference(path);
			saveButton.setEnabled(true);
		}

		if (DataBundles.isValue(path) || DataBundles.isReference(path)) {
			// Enable refresh button
			refreshButton.setEnabled(true);

			List<MimeType> mimeTypes = new ArrayList<>();
			try (InputStream inputstream = RendererUtils.getInputStream(path)) {
				mimeTypes.addAll(getMimeTypes(inputstream));
			} catch (IOException e) {
				logger.warn("Error getting mimetype", e);
			}

			if (mimeTypes.isEmpty()) {
				// If MIME types is empty - add "plain/text" MIME type
				mimeTypes.add(new MimeType("text/plain"));
			} else if (mimeTypes.size() == 1
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

			for (MimeType mimeType : mimeTypes)
				for (Renderer renderer : rendererRegistry
						.getRenderersForMimeType(mimeType.toString()))
					if (!recognisedRenderersForMimeType.contains(renderer))
						recognisedRenderersForMimeType.add(renderer);
			// if there are no renderers then force text/plain
			if (recognisedRenderersForMimeType.isEmpty())
				recognisedRenderersForMimeType = rendererRegistry
						.getRenderersForMimeType("text/plain");

			/*
			 * Add all other available renderers that are not recognised to be
			 * able to handle the MIME type of the result
			 */
			otherRenderers = rendererRegistry.getRenderers();
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
				mimeList[recognisedRenderersForMimeType.size() + i] = otherRenderers
						.get(i).getType();
				rendererList.add(otherRenderers.get(i));
			}

			renderersComboBox.setModel(new DefaultComboBoxModel<>(mimeList));

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

			@SuppressWarnings("unused")
			ErrorDocument errorDocument;
			try {
				errorDocument = DataBundles.getError(path);
			} catch (IOException e) {
				logger.warn("Error getting the error document", e);
			}

			// Reset the renderers as we have an error item
			recognisedRenderersForMimeType = null;
			otherRenderers = null;

			DefaultMutableTreeNode root = new DefaultMutableTreeNode(
				"Error Trace");

			// TODO handle error documents
			// ResultsUtils.buildErrorDocumentTree(root, errorDocument, referenceService);

			JTree errorTree = new JTree(root);

			errorTree.setCellRenderer(new DefaultTreeCellRenderer() {
				@Override
				public Component getTreeCellRendererComponent(JTree tree,
						Object value, boolean selected, boolean expanded,
						boolean leaf, int row, boolean hasFocus) {
					Component renderer = null;
					if (value instanceof DefaultMutableTreeNode) {
						Object userObject = ((DefaultMutableTreeNode) value)
								.getUserObject();
						if (userObject instanceof ErrorDocument)
							renderer = getErrorDocumentRenderer(tree, selected,
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

				private Component getErrorDocumentRenderer(JTree tree,
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

	/**
	 * Renders the result panel using the last used renderer.
	 */
	public void renderResult() {
		if (ERROR_DOCUMENT.equals(renderersComboBox.getSelectedItem())) {
			// skip error documents - do not (re)render
			return;
		}

		int selectedIndex = renderersComboBox.getSelectedIndex();
		if (mimeList != null && selectedIndex >= 0) {
			Renderer renderer = rendererList.get(selectedIndex);

			if (renderer.getType().equals("Text")){ // if the result is "text/plain"
				/*
				 * We use node's hash code as the key in the nodeToWrapCheckBox
				 * map as node's user object may be too large
				 */
				if (nodeToWrapSelection.get(node.hashCode()) == null) {
					// initially not selected
					nodeToWrapSelection.put(node.hashCode(), false);
				}
				wrapTextCheckBox.setSelected(nodeToWrapSelection.get(node.hashCode()));
				wrapTextCheckBox.setVisible(true);
			} else {
				wrapTextCheckBox.setVisible(false);
			}

			// Remember the last used renderer - use it for all result items of this port
			//currentRendererIndex = selectedIndex;
			lastUsedMIMEtype = mimeList[selectedIndex];

			JComponent component;
			try {
				component = renderer.getComponent(path);
				if (component instanceof DialogTextArea
						&& wrapTextCheckBox.isSelected())
					((JTextArea) component).setLineWrap(wrapTextCheckBox
							.isSelected());
				if (component instanceof JTextComponent)
					((JTextComponent) component).setEditable(false);
				else if (component instanceof JTree)
					((JTree) component).setEditable(false);
			} catch (RendererException ex) {// maybe this should be Exception
				/*
				 * show the user that something unexpected has happened but
				 * continue
				 */
				component = new DialogTextArea(
						"Could not render using renderer type "
								+ renderer.getClass()
								+ "\nPlease try with a different renderer"
								+ " if available and consult log"
								+ " for details of problem");
				((DialogTextArea) component).setEditable(false);
				logger.warn("Couln not render using " + renderer.getClass(), ex);
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
		renderedResultPanel.removeAll();

		// Update the 'save result' buttons appropriately
		for (int i = 0; i < saveButtonsPanel.getComponents().length; i++) {
			JButton saveButton = (JButton) saveButtonsPanel.getComponent(i);
			SaveIndividualResultSPI action = (SaveIndividualResultSPI) (saveButton
					.getAction());
			// Update the action
			action.setResultReference(null);
			saveButton.setEnabled(false);
		}

		renderersComboBox.setModel(new DefaultComboBoxModel<String>());
		renderersComboBox.setEnabled(false);

		revalidate();
		repaint();
	}

	class ColorCellRenderer implements ListCellRenderer<Object> {
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel renderer = (JLabel) defaultRenderer
					.getListCellRendererComponent(list, value, index,
							isSelected, cellHasFocus);

			if (value instanceof Color)
				renderer.setBackground((Color) value);

			if (recognisedRenderersForMimeType == null) // error occurred
				return renderer;

			if (value != null && index >= recognisedRenderersForMimeType.size())
				// one of the non-preferred renderers - show it in grey
				renderer.setForeground(Color.GRAY);

			return renderer;
		}
	}
}
