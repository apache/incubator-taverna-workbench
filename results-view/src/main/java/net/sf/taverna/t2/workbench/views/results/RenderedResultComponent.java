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
package net.sf.taverna.t2.workbench.views.results;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.renderers.Renderer;
import net.sf.taverna.t2.renderers.RendererException;
import net.sf.taverna.t2.renderers.RendererRegistry;
import net.sf.taverna.t2.workbench.views.results.ResultTreeNode.ResultTreeNodeState;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPIRegistry;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;

import org.apache.log4j.Logger;

import eu.medsea.mimeutil.MimeType;

/**
 * Creates a component that renders an individual result from an output port.
 * The component can render the result according to the renderers existing for
 * the output port's MIME type or display an error document.
 * 
 * @author Ian Dunlop
 * @author Alex Nenadic
 * 
 */
public class RenderedResultComponent extends JPanel {

	private static final long serialVersionUID = -1958999599453285294L;

	private static Logger logger = Logger
			.getLogger(RenderedResultComponent.class);

	// Panel containing rendered result
	private JPanel renderedResultPanel;

	// Combo box containing possible result types
	private JComboBox renderersComboBox;

	// Result type renderers
	private List<Renderer> renderersForMimeType;

	// Renderers' registry
	private RendererRegistry rendererRegistry = new RendererRegistry();


	// Reference to the object being displayed (contained in the MutableTreeNode
	// node)
	private T2Reference t2Reference;

	private InvocationContext context;

	// Currently selected node from the ResultViewComponent, if any.
	private ResultTreeNode node = null;

	// List of all output ports - needs to be passed to 'save result' actions.
	List<? extends DataflowOutputPort> dataflowOutputPorts = null;

	// Registry of all existing 'save individual result' actions,
	// e.g. each action can save the result in a different format.
	private static SaveIndividualResultSPIRegistry saveActionsRegistry = SaveIndividualResultSPIRegistry
			.getInstance();

	// Panel containing all 'save results' buttons
	JPanel saveButtonsPanel = null;

	/**
	 * Creates the component.
	 */
	public RenderedResultComponent() {

		// this.dataflowOutputPorts = dataflowOutputPorts;
		setLayout(new BorderLayout());
		setBorder(new EtchedBorder());

		// Results type combo box
		renderersComboBox = new JComboBox();
		renderersComboBox.setModel(new DefaultComboBoxModel()); // initially
																// empty
		renderersComboBox.setEditable(false);
		renderersComboBox.setEnabled(false); // initially disabled

		JPanel resultsTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		resultsTypePanel.add(new JLabel("Result Type"));
		resultsTypePanel.add(renderersComboBox);

		// 'Save result' buttons panel
		saveButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		List<SaveIndividualResultSPI> saveActions = saveActionsRegistry
				.getSaveResultActions();
		for (SaveIndividualResultSPI action : saveActions) {
			action.setResultReference(null);
			action.setInvocationContext(null);
			JButton saveButton = new JButton(action.getAction());
			saveButton.setEnabled(false);
			saveButtonsPanel.add(saveButton);
		}

		// Top panel contains result type combobox and various save buttons
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
		topPanel.add(resultsTypePanel);
		topPanel.add(saveButtonsPanel);

		// Rendered results panel - intially empty
		renderedResultPanel = new JPanel(new BorderLayout());
		renderedResultPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		// Add all components
		add(topPanel, BorderLayout.NORTH);
		add(new JScrollPane(renderedResultPanel), BorderLayout.CENTER);
	}

	/**
	 * Sets the tree node this components renders the results for, and update
	 * the rendered results panel.
	 */
	public void setNode(ResultTreeNode node) {
		this.node = node;
		if (this.node.isState(ResultTreeNodeState.RESULT_REFERENCE))
			updateResult();
		else
			clearResult();
	}

	/**
	 * Update the component based on the node selected from the
	 * ResultViewComponent tree.
	 * 
	 */
	@SuppressWarnings( { "serial" })
	public void updateResult() {
		renderersForMimeType = new ArrayList<Renderer>();

		ResultTreeNode result = (ResultTreeNode) node;

		t2Reference = result.getReference();
		context = result.getContext();

		// Enable the combo box
		renderersComboBox.setEnabled(true);
		renderersComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					int selectedIndex = renderersComboBox.getSelectedIndex();
					if (renderersForMimeType != null
							&& renderersForMimeType.size() > selectedIndex) {
						Renderer renderer = renderersForMimeType
								.get(selectedIndex);
						JComponent component = null;
						try {
							component = renderer.getComponent(context
									.getReferenceService(), t2Reference);
						} catch (RendererException e1) {// maybe this should be
							// Exception
							// show the user that something unexpected has
							// happened but
							// continue
							component = new DialogTextArea(
									"Could not render using renderer type "
											+ renderer.getClass().getName()
											+ "\n"
											+ "Please try with a different renderer if available and consult log for details of problem");
							logger.warn("Couln not render using "
									+ renderer.getClass().getName(), e1);
						}
						renderedResultPanel.removeAll();
						renderedResultPanel.add(component, BorderLayout.CENTER);
						repaint();
						revalidate();
					}
				}
			}
		});

		// Update the 'save result' buttons appropriately as the result node had
		// changed
		for (int i = 0; i < saveButtonsPanel.getComponents().length; i++) {
			JButton saveButton = (JButton) saveButtonsPanel.getComponent(i);
			SaveIndividualResultSPI action = (SaveIndividualResultSPI) (saveButton
					.getAction());
			// Update the action with the new result reference
			action.setResultReference(t2Reference);
			action.setInvocationContext(context);
			saveButton.setEnabled(true);
		}

		// Reference to the result data
		t2Reference = result.getReference();
		context = result.getContext();
		Identified identified = context.getReferenceService()
				.resolveIdentifier(t2Reference, null, context);
		List<MimeType> mimeTypes = null;
		if (identified instanceof ReferenceSet) {
			ReferenceSet referenceSet = (ReferenceSet) identified;
			List<ExternalReferenceSPI> externalReferences = new ArrayList<ExternalReferenceSPI>(
					referenceSet.getExternalReferences());
			Collections.sort(externalReferences,
					new Comparator<ExternalReferenceSPI>() {
						public int compare(ExternalReferenceSPI o1,
								ExternalReferenceSPI o2) {
							return (int) (o1.getResolutionCost() - o2
									.getResolutionCost());
						}
					});
			for (ExternalReferenceSPI externalReference : externalReferences) {
				mimeTypes = ResultsUtils.getMimeTypes(externalReference, context);
				if (!mimeTypes.isEmpty()) {
					break;
				}
			}
			if (mimeTypes.isEmpty()) {
				mimeTypes.add(new MimeType("text/plain"));
			}
			for (MimeType mimeType:mimeTypes) {
				List<Renderer> renderersList = rendererRegistry.getRenderersForMimeType(
						context, t2Reference, mimeType.toString());
				for (Renderer renderer:renderersList) {
					if (!renderersForMimeType.contains(renderer)) {
						renderersForMimeType.add(renderer);	
					}
				}
			}
			//if there are no renderers then try text/plain
			if (renderersForMimeType.isEmpty()) {
				List<Renderer> renderersList = rendererRegistry.getRenderersForMimeType(
						context, t2Reference, "text/plain");
				for (Renderer renderer:renderersList) {
					if (!renderersForMimeType.contains(renderer)) {
						renderersForMimeType.add(renderer);	
					}
				}
			}
			Object[] rendererList = new Object[renderersForMimeType.size()];
			for (int i = 0; i < rendererList.length; i++) {
				rendererList[i] = renderersForMimeType.get(i).getType();
			}
			renderersComboBox.setModel(new DefaultComboBoxModel(rendererList));
			if (renderersForMimeType.size() > 0) {
				renderersComboBox.setSelectedIndex(-1);
				renderersComboBox.setSelectedIndex(0);
			}
		} else if (identified instanceof ErrorDocument) {
			ErrorDocument errorDocument = (ErrorDocument) identified;
			renderersForMimeType = null;

			DefaultMutableTreeNode root = new DefaultMutableTreeNode(
					"Error Trace");
			ResultsUtils.buildErrorDocumentTree(root, errorDocument, context);

			JTree errorTree = new JTree(root);
			errorTree.setCellRenderer(new DefaultTreeCellRenderer() {

				public Component getTreeCellRendererComponent(JTree tree,
						Object value, boolean selected, boolean expanded,
						boolean leaf, int row, boolean hasFocus) {
					Component renderer = null;
					if (value instanceof DefaultMutableTreeNode) {
						DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
						Object userObject = treeNode.getUserObject();
						if (userObject instanceof ErrorDocument) {
							ErrorDocument errorDocument = (ErrorDocument) userObject;
							renderer = super.getTreeCellRendererComponent(tree,
									errorDocument.getMessage(), selected,
									expanded, leaf, row, hasFocus);
						}
					}
					if (renderer == null) {
						renderer = super.getTreeCellRendererComponent(tree,
								value, selected, expanded, leaf, row, hasFocus);
					}
					if (renderer instanceof JLabel) {
						JLabel label = (JLabel) renderer;
						label.setIcon(null);
					}
					return renderer;
				}

			});

			renderersComboBox.setModel(new DefaultComboBoxModel(
					new String[] { "Error Document" }));
			renderedResultPanel.removeAll();
			renderedResultPanel.add(errorTree, BorderLayout.CENTER);
			repaint();
		}
	}

	/**
	 * Clears the result panel.
	 */
	public void clearResult() {
		renderedResultPanel.removeAll();

		// Update the 'save result' buttons appropriately
		for (int i = 0; i < saveButtonsPanel.getComponents().length; i++) {
			JButton saveButton = (JButton) saveButtonsPanel.getComponent(i);
			SaveIndividualResultSPI action = (SaveIndividualResultSPI) (saveButton
					.getAction());
			// Update the action
			action.setResultReference(null);
			action.setInvocationContext(null);
			saveButton.setEnabled(false);
		}

		renderersComboBox.setModel(new DefaultComboBoxModel());
		renderersComboBox.setEnabled(false);

		revalidate();
		repaint();
	}

}
