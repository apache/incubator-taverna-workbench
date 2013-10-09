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
package net.sf.taverna.t2.workbench.views.results.workflow;

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
import javax.swing.SwingUtilities;
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
import net.sf.taverna.t2.results.ResultsUtils;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI;

import org.apache.commons.lang.StringEscapeUtils;
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
public class RenderedResultComponent extends JPanel {

	private static final String WRAP_TEXT = "Wrap text";

	final String ERROR_DOCUMENT = "Error Document";

	private static Logger logger = Logger.getLogger(RenderedResultComponent.class);

	// Panel containing rendered result
	private JPanel renderedResultPanel;

	// Combo box containing possible result types
	private JComboBox<String> renderersComboBox;

	// Button to refresh (re-render) the result, especially needed
	// for large results that are not rendered or are
	// partially rendered and the user wished to re-render them
	private JButton refreshButton;

	// Preferred result type renderers (the ones recognised to be able to handle the result's MIME
	// type)
	private List<Renderer> recognisedRenderersForMimeType;

	// All other result type renderers (the ones not recognised to be able to handle the result's
	// MIME type)
	// In case user wants to use them.
	private List<Renderer> otherRenderers;

	// Renderers' registry
	private final RendererRegistry rendererRegistry;

	// List of all MIME strings from all available renderers to be used for renderersComboBox.
	// Those that come from recognisedRenderersForMimeType are the preferred ones.
	// Those from otherRenderers will be greyed-out in the combobox list but could still be used.
	private String[] mimeList;

	// List of all available renderers but ordered to match the corresponding MIME type strings in
	// mimeList:
	// first the preferred renderers from recognisedRenderersForMimeType then the ones from
	// otherRenderers.
	private ArrayList<Renderer> rendererList;

	// Remember the MIME type of the last used renderer; use "text/plain" by default until
	// user changes it - then use that one for all result items of the port (in case result
	// contains a list). "text/plain" will always be added to the mimeList.
	private String lastUsedMIMEtype = "text/plain"; // text renderer will always be available

	// If result is "text/plain" - provide possibility to wrap wide text
	private JCheckBox wrapTextCheckBox;

	// Reference to the object being displayed (contained in the tree node)
	private Path path;

	// In case the node can be rendered as "text/plain", map the hash code of the node
	// to the wrap text check box selection value for that node (that remembers if user wanted the
	// text wrapped or not).
	private Map<Path, Boolean> nodeToWrapSelection = new HashMap<Path, Boolean>();

	// List of all output ports - needs to be passed to 'save result' actions.
	List<OutputWorkflowPort> dataflowOutputPorts = null;

	// Panel containing all 'save results' buttons
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
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED
						&& !((String) e.getItem()).equals(ERROR_DOCUMENT)) {
					// render the result using the newly selected renderer
					renderResult();
				}
			}
		});

		JPanel resultsTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		resultsTypePanel.add(new JLabel("Value type"));
		resultsTypePanel.add(renderersComboBox);

		// Refresh (re-render) button
		refreshButton = new JButton("Refresh", WorkbenchIcons.refreshIcon);
		refreshButton.setEnabled(false);
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				renderResult();
				refreshButton.getParent().requestFocusInWindow();
				// so that the button does not stay focused after it is clicked on and did its
				// action
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
				if (renderedResultPanel.getComponents().length == 0) {
					return;
				}
				Component component = renderedResultPanel.getComponent(0);
				if (component instanceof DialogTextArea) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						nodeToWrapSelection.put(path, Boolean.TRUE);
						renderResult();
					} else {
						nodeToWrapSelection.put(path, Boolean.FALSE);
						renderResult();
					}
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
				public void actionPerformed(ActionEvent e) {
					saveButton.getParent().requestFocusInWindow();
					// so that the button does not stay focused after it is clicked on and did its
					// action
				}
			});
			saveButtonsPanel.add(saveButton);
		}

		// Top panel contains result type combobox and various save buttons
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
		topPanel.add(resultsTypePanel);
		topPanel.add(saveButtonsPanel);

		// Rendered results panel - initially empty
		renderedResultPanel = new JPanel(new BorderLayout());

		// Add all components
		add(topPanel, BorderLayout.NORTH);
		add(new JScrollPane(renderedResultPanel), BorderLayout.CENTER);
	}

	/**
	 * Sets the path this components renders the results for, and update
	 * the rendered results panel.
	 */
	public void setPath(Path path) {
		this.path = path;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (RenderedResultComponent.this.path == null) {
					clearResult();
				} else if (DataBundles.isList(RenderedResultComponent.this.path)) {
					clearResult();
				} else {
					updateResult();
				}
			}
		});
	}

	/**
	 * Update the component based on the node selected from the
	 * ResultViewComponent tree.
	 */
	public void updateResult() {
		if (recognisedRenderersForMimeType == null) {
			recognisedRenderersForMimeType = new ArrayList<Renderer>();
		}
		if (otherRenderers == null) {
			otherRenderers = new ArrayList<Renderer>();
		}

		// Enable the combo box
		renderersComboBox.setEnabled(true);

		// Update the 'save result' buttons appropriately as the result node had
		// changed
		for (int i = 0; i < saveButtonsPanel.getComponents().length; i++) {
			JButton saveButton = (JButton) saveButtonsPanel.getComponent(i);
			SaveIndividualResultSPI action = (SaveIndividualResultSPI) (saveButton.getAction());
			// Update the action with the new result reference
			action.setResultReference(path);
			saveButton.setEnabled(true);
		}

		if (DataBundles.isValue(path) || DataBundles.isReference(path)) {
			// Enable refresh button
			refreshButton.setEnabled(true);

			List<MimeType> mimeTypes = new ArrayList<MimeType>();
			try (InputStream inputstream = RendererUtils.getInputStream(path)) {
				mimeTypes.addAll(ResultsUtils.getMimeTypes(inputstream));
			} catch (IOException e) {
				logger.warn("Error getting mimetype", e);
			}

			if (mimeTypes.isEmpty()) {
				// If MIME types is empty - add "plain/text" MIME type
				mimeTypes.add(new MimeType("text/plain"));
			} else if (mimeTypes.size() == 1
					&& mimeTypes.get(0).toString().equals("chemical/x-fasta")) {
				// If MIME type is recognised as "chemical/x-fasta" only then
				// this might be an error
				// from MIME magic (i.e. sometimes it recognises stuff that is
				// not "chemical/x-fasta" as
				// "chemical/x-fasta" and then Seq Vista renderer is used that
				// causes errors) - make sure
				// we also add the renderers for "text/plain" and "text/xml" as
				// it is most probably just
				// normal xml text and push the "chemical/x-fasta" to the bottom
				// of the list.
				mimeTypes.add(0, new MimeType("text/plain"));
				mimeTypes.add(1, new MimeType("text/xml"));
			}

			for (MimeType mimeType : mimeTypes) {
				List<Renderer> renderersList = rendererRegistry.getRenderersForMimeType(mimeType
						.toString());
				for (Renderer renderer : renderersList) {
					if (!recognisedRenderersForMimeType.contains(renderer)) {
						recognisedRenderersForMimeType.add(renderer);
					}
				}
			}
			// if there are no renderers then force text/plain
			if (recognisedRenderersForMimeType.isEmpty()) {
				recognisedRenderersForMimeType = rendererRegistry
						.getRenderersForMimeType("text/plain");
			}

			// Add all other available renderers that are not recognised to be
			// able to handle the
			// MIME type of the result
			otherRenderers = new ArrayList<>(rendererRegistry.getRenderers());
			otherRenderers.removeAll(recognisedRenderersForMimeType);

			mimeList = new String[recognisedRenderersForMimeType.size() + otherRenderers.size()];
			rendererList = new ArrayList<Renderer>();

			// First add the ones that can handle the MIME type of the result
			// item
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
				for (int i = 0; i < mimeList.length; i++) {
					if (mimeList[i].equals(lastUsedMIMEtype)) {
						index = i;
						break;
					}
				}

				int previousindex = renderersComboBox.getSelectedIndex();
				renderersComboBox.setSelectedIndex(index);
				// force rendering as setSelectedIndex will not fire an
				// itemstatechanged event if previousindex == index and we still need
				// render the result as we may have switched from a different result item in
				// a result list but the renderer index stayed the same
				if (previousindex == index) {
					renderResult(); // draw the rendered result component
				}
			}

		} else if (DataBundles.isError(path)) {
			// Disable refresh button
			refreshButton.setEnabled(false);

			// Hide wrap text check box - only works for actual data
			wrapTextCheckBox.setVisible(false);

			ErrorDocument errorDocument;
			try {
				errorDocument = DataBundles.getError(path);
			} catch (IOException e) {
				logger.warn("Error getting the error document", e);
			}

			// Reset the renderers as we have an error item
			recognisedRenderersForMimeType = null;
			otherRenderers = null;

			// If there is no exception message, e.g. service returned error as HTML document but no
			// actual exception occurred - do not build the error tree just show the message text
			// This is because multiline text is not being showed properly in the JTree
			JTextArea errorTextArea = null;
			JTree errorTree = null;
			DefaultMutableTreeNode root = new DefaultMutableTreeNode("Error Trace");
			// TODO handle error documents
			// ResultsUtils.buildErrorDocumentTree(root, errorDocument, referenceService);

			errorTree = new JTree(root);
			errorTree.setCellRenderer(new DefaultTreeCellRenderer() {
				public Component getTreeCellRendererComponent(JTree tree, Object value,
						boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
					Component renderer = null;
					if (value instanceof DefaultMutableTreeNode) {
						DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
						Object userObject = treeNode.getUserObject();
						if (userObject instanceof ErrorDocument) {
							ErrorDocument errorDocument = (ErrorDocument) userObject;
							renderer = super.getTreeCellRendererComponent(
									tree,
									"<html>"
											+ StringEscapeUtils
													.escapeHtml(((ErrorDocument) userObject)
															.getMessage()) + "</html>", selected,
									expanded, leaf, row, hasFocus);
						}
					}
					if (renderer == null) {
						renderer = super.getTreeCellRendererComponent(tree, value, selected,
								expanded, leaf, row, hasFocus);
					}
					if (renderer instanceof JLabel) {
						JLabel label = (JLabel) renderer;
						label.setIcon(null);
					}
					return renderer;
				}
			});

			renderersComboBox.setModel(new DefaultComboBoxModel<String>(
					new String[] { ERROR_DOCUMENT }));
			renderedResultPanel.removeAll();
			renderedResultPanel.add(errorTextArea != null ? errorTextArea : errorTree,
					BorderLayout.CENTER);
			repaint();
		}

	}

	/**
	 * Renders the result panel using the last used renderer.
	 */
	public void renderResult() {

		if (((String) renderersComboBox.getSelectedItem()).equals(ERROR_DOCUMENT)) {
			// skip error documents - do not (re)render
			return;
		}

		int selectedIndex = renderersComboBox.getSelectedIndex();
		if (mimeList != null && selectedIndex >= 0) {

			Renderer renderer = rendererList.get(selectedIndex);

			if (renderer.getType().equals("Text")) { // if the result is "text/plain"
				// We use node's hash code as the key in the nodeToWrapCheckBox
				// map as node's user object may be too large
				if (nodeToWrapSelection.get(path) == null) {
					// initially not selected
					nodeToWrapSelection.put(path, Boolean.FALSE);
				}
				wrapTextCheckBox.setSelected(nodeToWrapSelection.get(path));
				wrapTextCheckBox.setVisible(true);
			} else {
				wrapTextCheckBox.setVisible(false);
			}
			// Remember the last used renderer - use it for all result items of this port
			// currentRendererIndex = selectedIndex;
			lastUsedMIMEtype = mimeList[selectedIndex];

			JComponent component = null;
			try {
				component = renderer.getComponent(path);
				if (component instanceof DialogTextArea) {
					if (wrapTextCheckBox.isSelected()) {
						((JTextArea) component).setLineWrap(wrapTextCheckBox.isSelected());
					}
				}
				if (component instanceof JTextComponent) {
					((JTextComponent) component).setEditable(false);
				} else if (component instanceof JTree) {
					((JTree) component).setEditable(false);
				}
			} catch (RendererException e1) {
				// maybe this should be Exception
				// show the user that something unexpected has
				// happened but continue
				component = new DialogTextArea(
						"Could not render using renderer type "
								+ renderer.getClass().getName()
								+ "\n"
								+ "Please try with a different renderer if available and consult log for details of problem");
				((DialogTextArea) component).setEditable(false);
				logger.warn("Couln not render using " + renderer.getClass().getName(), e1);
			}
			renderedResultPanel.removeAll();
			renderedResultPanel.add(component, BorderLayout.CENTER);
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
			SaveIndividualResultSPI action = (SaveIndividualResultSPI) (saveButton.getAction());
			// Update the action
			action.setResultReference(null);
			saveButton.setEnabled(false);
		}

		renderersComboBox.setModel(new DefaultComboBoxModel<String>());
		renderersComboBox.setEnabled(false);

		revalidate();
		repaint();
	}

	class ColorCellRenderer implements ListCellRenderer {
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value,
					index, isSelected, cellHasFocus);

			if (value instanceof Color) {
				renderer.setBackground((Color) value);
			}

			if (recognisedRenderersForMimeType == null) { // error occurred
				return renderer;
			}

			if (value != null && index >= recognisedRenderersForMimeType.size()) {
				// one of the non-preferred renderers - show it in grey
				renderer.setForeground(Color.GRAY);
			}

			return renderer;
		}
	}

}
