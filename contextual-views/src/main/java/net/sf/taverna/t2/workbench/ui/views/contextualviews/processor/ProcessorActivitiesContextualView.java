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

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.Utils;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.profiles.ProcessorBinding;

/**
 * View of a processor, including it's iteration stack, activities, etc.
 *
 * @author Stian Soiland-Reyes
 * @author Alan R Williams
 */
@SuppressWarnings("serial")
public class ProcessorActivitiesContextualView extends ContextualView {

	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	protected JPanel mainPanel = new JPanel();

	protected Processor processor;

	private final ContextualViewFactoryRegistry contextualViewFactoryRegistry;

	private final SelectionManager selectionManager;

	public ProcessorActivitiesContextualView(Processor processor,
			ContextualViewFactoryRegistry contextualViewFactoryRegistry, SelectionManager selectionManager) {
		super();
		this.processor = processor;
		this.contextualViewFactoryRegistry = contextualViewFactoryRegistry;
		this.selectionManager = selectionManager;
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

		List<ProcessorBinding> processorBindings = scufl2Tools.processorBindingsForProcessor(
				processor, selectionManager.getSelectedProfile());
		if (processorBindings.isEmpty()) {
			JLabel noActivitiesLabel = new JLabel("<html><strong>Abstract processor</strong>"
					+ "<br><i>No services.  This will not execute./i></html>");
			constraints.fill = GridBagConstraints.NONE;
			constraints.anchor = GridBagConstraints.LINE_START;
			mainPanel.add(noActivitiesLabel, constraints);
		} else {
			for (ProcessorBinding processorBinding : processorBindings) {
				Activity activity = processorBinding.getBoundActivity();
				List<ContextualViewFactory> viewFactoryForBeanType = (List<ContextualViewFactory>) contextualViewFactoryRegistry
						.getViewFactoriesForObject(activity);
				if (!viewFactoryForBeanType.isEmpty()) {
					ContextualView view = (ContextualView) viewFactoryForBeanType.get(0)
							.getViews(activity).get(0);
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
