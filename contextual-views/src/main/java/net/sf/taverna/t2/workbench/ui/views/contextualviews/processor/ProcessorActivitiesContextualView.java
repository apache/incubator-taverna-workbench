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
public class ProcessorActivitiesContextualView extends ContextualView {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7675243000874724197L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(ProcessorActivitiesContextualView.class);

	protected JPanel mainPanel = new JPanel();

	protected Processor processor;
	
	public ProcessorActivitiesContextualView(Processor processor) {
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
	

	private synchronized void initialise() {
		mainPanel.removeAll();
		mainPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0.1;
		constraints.weighty = 0;

		List<? extends Activity<?>> activityList = processor.getActivityList();
		for (Activity<?> activity : activityList) {

			List<ContextualViewFactory> viewFactoryForBeanType = (List<ContextualViewFactory>)
			ContextualViewFactoryRegistry
			.getInstance()
			.getViewFactoriesForObject(activity);
			if (!viewFactoryForBeanType.isEmpty()) {
				ContextualView view = (ContextualView) viewFactoryForBeanType.get(0).getViews(
						activity).get(0);
				constraints.anchor = GridBagConstraints.CENTER;
				constraints.fill = GridBagConstraints.HORIZONTAL;
				mainPanel.add(view, constraints);
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
					mainPanel.add(configureButton, constraints);
				}
				constraints.gridy++;
			}
		}
		if (activityList.isEmpty()) {
			JLabel noActivitiesLabel = new JLabel(
					"<html><strong>Abstract processor</strong>"
							+ "<br><i>No services.  This will not execute./i></html>");
			constraints.fill = GridBagConstraints.NONE;
			constraints.anchor = GridBagConstraints.LINE_START;
			mainPanel.add(noActivitiesLabel, constraints);
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
		return "Service";
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}
}
