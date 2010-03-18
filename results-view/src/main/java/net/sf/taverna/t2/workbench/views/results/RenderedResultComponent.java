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
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.text.JTextComponent;
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
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
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
@SuppressWarnings("serial")
public class RenderedResultComponent extends JPanel {

	final String ERROR_DOCUMENT = "Error Document";
	
	private static Logger logger = Logger
			.getLogger(RenderedResultComponent.class);

	// Panel containing rendered result
	private JPanel renderedResultPanel;

	// Combo box containing possible result types
	private JComboBox renderersComboBox;
	
	// Button to refresh (re-render) the result, especially needed 
	// for large results that are not rendered or are
	// partially rendered and the user wished to re-render them
	private JButton refreshButton;

	// Preferred result type renderers (the ones recognised to be able to handle the result's MIME type)
	private List<Renderer> recognisedRenderersForMimeType;
	
	// All other result type renderers (the ones not recognised to be able to handle the result's MIME type)
	// In case user wants to use them.
	private List<Renderer> otherRenderers;

	// Renderers' registry
	static RendererRegistry rendererRegistry = new RendererRegistry();
	
	// List of all MIME strings from all available renderers to be used for renderersComboBox. 
	// Those that come from recognisedRenderersForMimeType are the preferred ones. 
	// Those from otherRenderers will be greyed-out in the combobox list but could still be used.
	private String[] mimeList;
	
	// List of all available renderers but ordered to match the corresponding MIME type strings in mimeList:
	// first the preferred renderers from recognisedRenderersForMimeType then the ones from otherRenderers. 
	private ArrayList<Renderer> rendererList;
	
	// Remember the MIME type of the last used renderer; use "text/plain" by default until 
	// user changes it - then use that one for all result items of the port (in case result
	// contains a list). "text/plain" will always be added to the mimeList.
	private String lastUsedMIMEtype = "text/plain"; // text renderer always will be available

	// Reference to the object being displayed (contained in the tree node)
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
		renderersComboBox.setModel(new DefaultComboBoxModel()); // initially empty

		renderersComboBox.setRenderer(new ColorCellRenderer());
		renderersComboBox.setEditable(false);
		renderersComboBox.setEnabled(false); // initially disabled

		JPanel resultsTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		resultsTypePanel.add(new JLabel("Value type"));
		resultsTypePanel.add(renderersComboBox);
		
		// Refresh (re-render) button
		refreshButton = new JButton("Refresh", WorkbenchIcons.refreshIcon);
		refreshButton.setEnabled(false);
		refreshButton.addActionListener(new ActionListener() {		
			public void actionPerformed(ActionEvent e) {
				refreshResult();				
			}
		});
		resultsTypePanel.add(refreshButton);

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

		// Rendered results panel - initially empty
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
		if (this.node.isState(ResultTreeNodeState.RESULT_REFERENCE)){
			updateResult();
		}
		else{
			clearResult();
		}
	}

	/**
	 * Update the component based on the node selected from the
	 * ResultViewComponent tree.
	 * 
	 */
	public void updateResult() {
		
		if (recognisedRenderersForMimeType == null){
			recognisedRenderersForMimeType = new ArrayList<Renderer>();
		}
		if (otherRenderers == null){
			otherRenderers = new ArrayList<Renderer>();
		}

		ResultTreeNode result = (ResultTreeNode) node;

		// Reference to the result data
		t2Reference = result.getReference();
		context = result.getContext();

		// Enable the combo box
		renderersComboBox.setEnabled(true);
		
		renderersComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					int selectedIndex = renderersComboBox.getSelectedIndex();
					if (mimeList != null && selectedIndex >= 0) {

						Renderer renderer = rendererList.get(selectedIndex);
						
						// Remember the last used renderer - use it for all result items of this port
						//currentRendererIndex = selectedIndex;
						lastUsedMIMEtype = mimeList[selectedIndex];
						
						JComponent component = null;
						try {
							component = renderer.getComponent(context
									.getReferenceService(), t2Reference);
							if (component instanceof JTextComponent){
								((JTextComponent)component).setEditable(false);
							}
							else if (component instanceof JTree){
								((JTree)component).setEditable(false);
							}
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
							((DialogTextArea)component).setEditable(false);
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

		Identified identified = context.getReferenceService()
				.resolveIdentifier(t2Reference, null, context);
		List<MimeType> mimeTypes = new ArrayList<MimeType>();
		if (identified instanceof ReferenceSet) {
			
			// Enable refresh button
			refreshButton.setEnabled(true);
			
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
				mimeTypes.addAll(ResultsUtils.getMimeTypes(externalReference, context));
				if (!mimeTypes.isEmpty()) {
					break;
				}
			}
			
			if (mimeTypes.isEmpty()) { // If MIME types is empty - add "plain/text" MIME type
				mimeTypes.add(new MimeType("text/plain"));
			}
			else if (mimeTypes.size() == 1 && mimeTypes.get(0).toString().equals("chemical/x-fasta")){
				// If MIME type is recognised as "chemical/x-fasta" only then this might be an error  
				// from MIME magic (i.e. sometimes it recognises stuff that is not "chemical/x-fasta" as 
				// "chemical/x-fasta" and then Seq Vista renderer is used that causes errors) - make sure 
				// we also add the renderers for "text/plain" and "text/xml" as it is most probably just
				// normal xml text and push the "chemical/x-fasta" to the bottom of the list.
				mimeTypes.add(0, new MimeType("text/plain"));
				mimeTypes.add(1, new MimeType("text/xml"));
			}

			for (MimeType mimeType:mimeTypes) {
				List<Renderer> renderersList = rendererRegistry.getRenderersForMimeType(
						context, t2Reference, mimeType.toString());
				for (Renderer renderer:renderersList) {
					if (!recognisedRenderersForMimeType.contains(renderer)) {
						recognisedRenderersForMimeType.add(renderer);	
					}
				}
			}
			//if there are no renderers then try text/plain
			if (recognisedRenderersForMimeType.isEmpty()) {
				recognisedRenderersForMimeType = rendererRegistry.getRenderersForMimeType(
						context, t2Reference, "text/plain");
			}
			
			// Add all other available renderers that are not recognised to be able to handle the 
			// MIME type of the result
			otherRenderers = rendererRegistry.getInstances();
			otherRenderers.removeAll(recognisedRenderersForMimeType);
						
			mimeList = new String[recognisedRenderersForMimeType.size()
					+ otherRenderers.size()];
			rendererList = new ArrayList<Renderer>();

			// First add the ones that can handle the MIME type of the result item
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

			renderersComboBox.setModel(new DefaultComboBoxModel(mimeList));
			if (mimeList.length > 0) {
				int index = 0;
				
				// Find the index of the current MIME type
				// (this index could change as we modify the renderers list but
				// we should always use the renderer for the current MIME type)
				for (int i = 0; i< mimeList.length; i++){
					if (mimeList[i].equals(lastUsedMIMEtype)){
						index = i;
						break;
					}
				}
				renderersComboBox.setSelectedIndex(-1);// this will force the itemStateChanged event, which will set the last used renderer index to -1
				renderersComboBox.setSelectedIndex(index);
				
			}
		} else if (identified instanceof ErrorDocument) {
			
			// Disable refresh button
			refreshButton.setEnabled(false);
			
			ErrorDocument errorDocument = (ErrorDocument) identified;
			
			// Reset the renderers as we have an error item
			recognisedRenderersForMimeType = null;
			otherRenderers = null;
			
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
					new String[] { ERROR_DOCUMENT }));
			renderedResultPanel.removeAll();
			renderedResultPanel.add(errorTree, BorderLayout.CENTER);
			repaint();
		}
		
	}
	
	/**
	 * Refreshes the result panel using the last used renderer.
	 */
	public void refreshResult() {
		
		if (((String)renderersComboBox.getSelectedItem()).equals(ERROR_DOCUMENT)){ // skip error document - do not re-render
			// Refresh button is not enabled when result item is an error but nevertheless check it here
			return;
		}
		
		int index = renderersComboBox.getSelectedIndex();
		renderersComboBox.setSelectedIndex(-1);// this will force the itemStateChanged event, which will set the last used renderer index to -1
		renderersComboBox.setSelectedIndex(index);	// this will refresh the rendered component
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
			action.setInvocationContext(null);
			saveButton.setEnabled(false);
		}

		renderersComboBox.setModel(new DefaultComboBoxModel());
		renderersComboBox.setEnabled(false);

		revalidate();
		repaint();
	}
	
	
	class ColorCellRenderer implements ListCellRenderer {
		  protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

		  public Component getListCellRendererComponent(JList list, Object value, int index,
		      boolean isSelected, boolean cellHasFocus) {
		    JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
		        isSelected, cellHasFocus);
		    		    
		    if (value instanceof Color) {
		      renderer.setBackground((Color) value);
		    }
		    
		    if (recognisedRenderersForMimeType == null){ // error occured
			    return renderer;
		    }

			if (value != null && index >= recognisedRenderersForMimeType.size()){ // one of the non-preferred renderers - show it in red
			    renderer.setForeground(Color.GRAY);
			}

		    return renderer;
		  }
		}

}
