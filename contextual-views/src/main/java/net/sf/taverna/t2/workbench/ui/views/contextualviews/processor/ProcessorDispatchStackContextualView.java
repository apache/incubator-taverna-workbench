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
public class ProcessorDispatchStackContextualView extends ContextualView {

	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(ProcessorDispatchStackContextualView.class);

	private static final long serialVersionUID = -5916300049123653585L;

	private EditManager editManager = EditManager.getInstance();

	private Edits edits = editManager.getEdits();

	private FileManager fileManager = FileManager.getInstance();

	private ContextualViewFactoryRegistry viewFactoryRegistry = ContextualViewFactoryRegistry
			.getInstance();
	
	protected SPIRegistry<AddLayerFactorySPI> addLayerFactories = new SPIRegistry<AddLayerFactorySPI>(
			AddLayerFactorySPI.class);
	
	protected JPanel mainPanel = new JPanel();

	protected Processor processor;
	
	public ProcessorDispatchStackContextualView(Processor processor) {
		super();
		this.processor = processor;
		initialise();
		initView();
	}
	
	@Override
	public void refreshView() {
		initialise();
		this.revalidate();
	}
	
	private JPanel getLayerView(DispatchLayer<?> layer) {

		ContextualViewFactory<DispatchLayer<?>> viewFactory = null;

			List<ContextualViewFactory> factories = viewFactoryRegistry
			.getViewFactoriesForObject(layer);
			if (!factories.isEmpty()) {
				viewFactory = factories.get(0);
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
			ContextualView contextualView = viewFactory.getViews(layer).get(0);
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

		List<DispatchLayer<?>> layers = processor.getDispatchStack()
				.getLayers();
		for (DispatchLayer<?> layer : layers) {
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			JPanel view = getLayerView(layer);
			mainPanel.add(view, gbc);
		}

		if (layers.isEmpty()) {
			mainPanel.add(new JLabel(
					"<html><i>Warning: No dispatch stack</i></html>"), gbc);
			mainPanel.add(new JButton(new AddDefaultStackAction()), gbc);
		}

		gbc.fill = GridBagConstraints.NONE;
		// Buttons for adding new layers
		for (AddLayerFactorySPI addLayerFactory : addLayerFactories
				.getInstances()) {
			if (addLayerFactory.canAddLayerFor(processor)) {
				JButton addButton = new JButton(addLayerFactory
						.getAddLayerActionFor(processor));
				mainPanel.add(addButton, gbc);
			}
		}
		mainPanel.revalidate();
		mainPanel.repaint();
		this.revalidate();
		this.repaint();
	}

	@Override
	public JComponent getMainFrame() {
		return mainPanel;
	}

	@Override
	public String getViewTitle() {
		return "Advanced";
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
				refreshView();
			} catch (EditException ex) {
				logger.warn("Could not create default stack", ex);
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
				refreshView();
			} catch (EditException ex) {
				logger.warn("Could not remove layer " + layer, ex);
			}
		}
	}

	@Override
	public int getPreferredPosition() {
		return 400;
	}


}
