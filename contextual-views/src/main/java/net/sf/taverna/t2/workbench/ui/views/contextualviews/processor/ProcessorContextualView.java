/*******************************************************************************
 * Copyright (C) 2007-2008 The University of Manchester   
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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.processor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.iterationstrategy.contextview.IterationStrategyContextualView;
import net.sf.taverna.t2.workbench.ui.Utils;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.AddLayerFactorySPI;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;

import org.apache.log4j.Logger;

/**
 * View of a processor, including it's iteration stack, activities, etc.
 * 
 * @author Stian Soiland-Reyes
 * @author Alan R Williams
 * 
 */
public class ProcessorContextualView extends ContextualView {

	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(ProcessorContextualView.class);

	private static final long serialVersionUID = -5916300049123653585L;

	private EditManager editManager = EditManager.getInstance();

	private Edits edits = editManager.getEdits();

	private FileManager fileManager = FileManager.getInstance();
	private JPanel openSection;

	private ContextualViewFactoryRegistry viewFactoryRegistry = ContextualViewFactoryRegistry
			.getInstance();
	protected SectionLabel actitiviesLabel = new SectionLabel("Activities",
			ShadedLabel.BLUE);
	protected JPanel activitiesPanel = new JPanel();
	protected SPIRegistry<AddLayerFactorySPI> addLayerFactories = new SPIRegistry<AddLayerFactorySPI>(
			AddLayerFactorySPI.class);
	
//	protected SectionLabel advancedLabel = new SectionLabel("Advanced </b><i>(dispatch stack)</i></html>", ShadedLabel.ORANGE);
	protected SectionLabel advancedLabel = new SectionLabel("Advanced", ShadedLabel.ORANGE);
	protected JPanel advancedPanel = new JPanel();
	protected Map<JPanel, SectionLabel> closeables = new HashMap<JPanel, SectionLabel>();
	protected SectionLabel iterationStrategyLabel = new SectionLabel(
			"List handling", ShadedLabel.GREEN);
	protected JPanel iterationStrategyPanel = new JPanel();
	protected SectionLabel predictedBehaviourLabel = new SectionLabel("Predicted behavior", ShadedLabel.BLUE);
	protected JPanel predictedBehaviourPanel = new JPanel();
	protected JPanel mainPanel = new JPanel();

	protected Processor processor;
	public ProcessorContextualView(Processor processor) {
		super();
		this.processor = processor;
		initialise();
		initView();
	}
	public synchronized void openSection(JPanel sectionToOpen) {
		openSection = null;
		for (Entry<JPanel, SectionLabel> entry : closeables.entrySet()) {
			JPanel section = entry.getKey();
			SectionLabel sectionLabel = entry.getValue();
			
			if (section != sectionToOpen) {
				section.setVisible(false);
			} else {
				section.setVisible(! section.isVisible());
				if (section.isVisible()) {
					openSection = section;
				}
			}
			sectionLabel.setExpanded(section.isVisible());
		}
	}
	@Override
	public void refreshView() {
		initialise();
		mainPanel.revalidate();
	}
	private JPanel getLayerView(DispatchLayer<?> layer) {

		ContextualViewFactory<DispatchLayer<?>> viewFactory;
		try {
			viewFactory = (ContextualViewFactory<DispatchLayer<?>>) viewFactoryRegistry
					.getViewFactoryForObject(layer);
		} catch (IllegalArgumentException ex) {
			viewFactory = null;
		}

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridx = 0;
		gbc.gridy = 0;

		JPanel view;
		view = new JPanel(new GridBagLayout());
		view.setBorder(BorderFactory.createEtchedBorder());
		view.add(new JLabel("<html><strong>" + layer.getClass().getSimpleName()
				+ "</strong></html>"), gbc);

		JButton removeButton = new JButton(new RemoveAction(layer));
		gbc.gridx = 1;
		gbc.weightx = 0.1;
		view.add(new JPanel(), gbc); // filler
		gbc.gridx = 2;
		gbc.weightx = 0.0;
		view.add(removeButton, gbc);

		gbc.gridx = 0;
		gbc.gridy++;

		if (viewFactory != null) {
			ContextualView contextualView = viewFactory.getView(layer);
			view.add(contextualView, gbc);
			gbc.gridy++;

			Frame frame = Utils.getParentFrame(this);
			Action configureAction = contextualView.getConfigureAction(frame);
			if (configureAction != null) {
				JButton configureButton = new JButton(configureAction);
				if (configureButton.getText() == null
						|| configureButton.getText().equals("")) {
					configureButton.setText("Configure");
				}
				gbc.fill = GridBagConstraints.NONE;
				view.add(configureButton, gbc);
				gbc.gridy++;
				gbc.fill = GridBagConstraints.HORIZONTAL;
			}
		} else {
			// Not currently exposing dispatch stack
			view.setVisible(false);
		}
		return view;
	}

	private synchronized void initialise() {
		mainPanel.removeAll();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		if (processor.getActivityList().size() == 1) {
			actitiviesLabel.setText("Activity");
		} else {
			actitiviesLabel.setText("Activities");
		}
		makeActivitiesPanel();
		makePredictedBehaviourPanel();
		makeIterationStrategyPanel();
		makeAdvancedPanel();
		
		mainPanel.add(actitiviesLabel, gbc);
		mainPanel.add(activitiesPanel, gbc);
		mainPanel.add(iterationStrategyLabel, gbc);
		mainPanel.add(iterationStrategyPanel, gbc);
		mainPanel.add(predictedBehaviourLabel, gbc);
		mainPanel.add(predictedBehaviourPanel, gbc);
		predictedBehaviourLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				fileManager.getCurrentDataflow().checkValidity();
				populatePredictedBehaviourPanel();
			}
		});
		mainPanel.add(advancedLabel, gbc);
		mainPanel.add(advancedPanel, gbc);
		
		// Filler
		gbc.weighty = 0.1;
		gbc.fill = GridBagConstraints.BOTH;
		mainPanel.add(new JPanel(), gbc);
		if (openSection == null) {
			// Default is activities panel
			openSection(activitiesPanel);
		} else {
			// But if we're refreshing we remember the last
			// opened one
			openSection(openSection);
		}
		
	}

	private void makeActivitiesPanel() {
		activitiesPanel.removeAll();
		activitiesPanel.setLayout(new GridBagLayout());
		makeCloseable(activitiesPanel, actitiviesLabel);
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0.1;
		constraints.weighty = 0;

		List<? extends Activity<?>> activityList = processor.getActivityList();
		for (Activity<?> activity : activityList) {

			ContextualViewFactory<Activity<?>> viewFactoryForBeanType = (ContextualViewFactory<Activity<?>>) viewFactoryRegistry
					.getViewFactoryForObject(activity);
			ContextualView view = viewFactoryForBeanType.getView(activity);
			constraints.anchor = GridBagConstraints.CENTER;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			activitiesPanel.add(view, constraints);
			Frame frame = Utils.getParentFrame(this);
			Action configureAction = view.getConfigureAction(frame);
			if (configureAction != null) {
				constraints.gridy++;
				constraints.fill = GridBagConstraints.NONE;
				constraints.anchor = GridBagConstraints.LINE_START;
				JButton configureButton = new JButton(configureAction);
				if (configureButton.getText() == null
						|| configureButton.getText().equals("")) {
					configureButton.setText("Configure");
				}
				activitiesPanel.add(configureButton, constraints);
			}
			constraints.gridy++;
		}
		if (activityList.isEmpty()) {
			JLabel noActivitiesLabel = new JLabel(
					"<html><strong>Abstract processor</strong>"
							+ "<br><i>No activities, this processor will not execute</i></html>");
			constraints.fill = GridBagConstraints.NONE;
			constraints.anchor = GridBagConstraints.LINE_START;
			activitiesPanel.add(noActivitiesLabel, constraints);
		}
	}
	
	private void makePredictedBehaviourPanel() {
		makeCloseable(predictedBehaviourPanel, predictedBehaviourLabel);		
	}
	
	private void populatePredictedBehaviourPanel() {
		predictedBehaviourPanel.removeAll();
		predictedBehaviourPanel.setLayout(new BorderLayout());
		
		String html ="<html><head>" + getStyle() + "</head><body>";
		
		List<? extends ProcessorInputPort> inputs = processor.getInputPorts();
		if (!inputs.isEmpty()) {
			html += "<table border=1><tr><th>Input Port Name</th>"
				+ "<th>Size of data</th>" + "</tr>";
			for (ProcessorInputPort ip : inputs) {
				html += "<tr><td>" + ip.getName() + "</td><td>";
				Datalink incoming = ip.getIncomingLink();
				if (incoming == null) {
					html += "No value";
				} else {
					int depth = incoming.getResolvedDepth();
					if (depth == -1) {
						html += "Invalid";
					} else if (depth == 0) {
						html += "Single value";
					} else {
						html += "List of depth " + depth;
					}
				}
				html += "</td></tr>";
			}
			html += "</table>";
		}
		List<? extends ProcessorOutputPort> outputs = processor.getOutputPorts();
		if (!outputs.isEmpty()) {
			html += "<table border=1><tr><th>Output Port Name</th>"
				+ "<th>Size of data</th>" + "</tr>";
			for (ProcessorOutputPort op : outputs) {
				html += "<tr><td>" + op.getName() + "</td><td>";
				Set<? extends Datalink> outgoingSet = op.getOutgoingLinks();
				if (outgoingSet.isEmpty()) {
					html += "No value";
				} else {
					Datalink outgoing = outgoingSet.iterator().next();
					int depth = outgoing.getResolvedDepth();
					if (depth == -1) {
						html += "Invalid";
					} else if (depth == 0) {
						html += "Single value";
					} else {
						html += "List of depth " + depth;
					}
				}
				html += "</td></tr>";
			}
			html += "</table>";
		}
		html +="</body></html>";
		JEditorPane editorPane = new JEditorPane("text/html", html);
		editorPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(editorPane);
		predictedBehaviourPanel.add(scrollPane, BorderLayout.CENTER);
	}

	protected String getStyle() {
		String style = "<style type='text/css'>";
		style += "table {align:center; border:solid black 1px;"
				+ "width:100%; height:100%; overflow:auto;}";
		style += "</style>";
		return style;
	}
	
	private void makeAdvancedPanel() {
		advancedPanel.removeAll();
		advancedPanel.setLayout(new GridBagLayout());

		makeCloseable(advancedPanel, advancedLabel);
		
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.weightx = 0.1;

		List<DispatchLayer<?>> layers = processor.getDispatchStack()
				.getLayers();
		for (DispatchLayer<?> layer : layers) {
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			JPanel view = getLayerView(layer);
			advancedPanel.add(view, gbc);
		}

		if (layers.isEmpty()) {
			advancedPanel.add(new JLabel(
					"<html><i>Warning: No dispatch stack</i></html>"), gbc);
			advancedPanel.add(new JButton(new AddDefaultStackAction()), gbc);
		}

		gbc.fill = GridBagConstraints.NONE;
		// Buttons for adding new layers
		for (AddLayerFactorySPI addLayerFactory : addLayerFactories
				.getInstances()) {
			if (addLayerFactory.canAddLayerFor(processor)) {
				JButton addButton = new JButton(addLayerFactory
						.getAddLayerActionFor(processor));
				advancedPanel.add(addButton, gbc);
			}
		}
	}

	private void makeCloseable(JPanel panel, SectionLabel label) {
		panel.setVisible(false);
		if (closeables.get(panel) != label) {
			closeables.put(panel, label);
			// Only add mouse listener once
			label.addMouseListener(new SectionOpener(panel));
		}
	}

	private void makeIterationStrategyPanel() {
		iterationStrategyPanel.removeAll();
		iterationStrategyPanel.setLayout(new GridBagLayout());
		
		makeCloseable(iterationStrategyPanel, iterationStrategyLabel);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		
		IterationStrategyContextualView view = new IterationStrategyContextualView(processor);
		iterationStrategyPanel.add(view, gbc);
		gbc.gridy++;

		Frame frame = Utils.getParentFrame(this);
		Action configureAction = view.getConfigureAction(frame);
		if (configureAction != null) {
			JButton configureButton = new JButton(configureAction);
			if (configureButton.getText() == null
					|| configureButton.getText().equals("")) {
				configureButton.setText("Configure");
			}
			gbc.fill = GridBagConstraints.NONE;
			iterationStrategyPanel.add(configureButton, gbc);
			gbc.gridy++;
		}
 
	}

	@Override
	public JComponent getMainFrame() {
		return new JScrollPane(mainPanel);
	}

	@Override
	public String getViewTitle() {
		return "Service: " + processor.getLocalName();
	}

	private final class AddDefaultStackAction extends AbstractAction {

		public AddDefaultStackAction() {
			super("Add default stack");
		}

		public void actionPerformed(ActionEvent e) {
			Edit<Processor> edit = edits.getDefaultDispatchStackEdit(processor);
			try {
				editManager.doDataflowEdit(fileManager.getCurrentDataflow(),
						edit);
			} catch (EditException ex) {
				logger.warn("Could not create default stack", ex);
			}
		}
	}
	
	private final class SectionLabel extends ShadedLabel {
		private JLabel expand;

		private SectionLabel(String text, Color colour) {
			super(text, colour);
			expand = new JLabel(WorkbenchIcons.minusIcon);
			add(expand, 0);
			setExpanded(true);
		}
		public void setExpanded(boolean expanded) {
			if (expanded) {
				expand.setIcon(WorkbenchIcons.minusIcon);
			} else {
				expand.setIcon(WorkbenchIcons.plusIcon);
			}
		}
	}

	protected class RemoveAction extends AbstractAction {

		private final DispatchLayer<?> layer;

		public RemoveAction(DispatchLayer<?> layer) {
			this.layer = layer;
			putValue(SMALL_ICON, WorkbenchIcons.deleteIcon);
			putValue(SHORT_DESCRIPTION, "Remove layer");
		}

		public void actionPerformed(ActionEvent e) {
			Edit<DispatchStack> deleteEdit = edits.getDeleteDispatchLayerEdit(
					processor.getDispatchStack(), layer);
			// TODO: Should warn before removing "essential" layers
			try {
				editManager.doDataflowEdit(fileManager.getCurrentDataflow(),
						deleteEdit);
			} catch (EditException ex) {
				logger.warn("Could not remove layer " + layer, ex);
			}
		}
	}

	protected class SectionOpener extends MouseAdapter {

		private final JPanel sectionToOpen;

		public SectionOpener(JPanel sectionToOpen) {
			this.sectionToOpen = sectionToOpen;
		}

		public void mouseClicked(MouseEvent e) {
			openSection(sectionToOpen);
		}
	}

}
