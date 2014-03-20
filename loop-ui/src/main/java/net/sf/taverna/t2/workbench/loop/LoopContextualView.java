/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
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
package net.sf.taverna.t2.workbench.loop;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.loop.comparisons.Comparison;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Loop;

import org.apache.log4j.Logger;

/**
 * View of a processor, including it's iteration stack, activities, etc.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class LoopContextualView extends ContextualView {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(LoopContextualView.class);

	private EditManager editManager = EditManager.getInstance();

	private FileManager fileManager = FileManager.getInstance();

	private Edits edits = EditManager.getInstance().getEdits();

	private Loop loopLayer;

	private JPanel panel;

	private Processor processor;

	public LoopContextualView(Loop loopLayer) {
		super();
		this.loopLayer = loopLayer;
		processor = loopLayer.getProcessor();
		initialise();
		initView();
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new LoopConfigureAction(owner, this, loopLayer);
	}

	@Override
	public void refreshView() {
		initialise();
	}

	private void initialise() {
		if (panel == null) {
			panel = new JPanel();
		} else {
			panel.removeAll();
		}
		panel.setLayout(new GridBagLayout());
		updateUIByConfig();
	}

	@Override
	public JComponent getMainFrame() {
		return panel;
	}

	@Override
	public String getViewTitle() {
		return "Loop of " + processor.getLocalName();
	}

	protected void updateUIByConfig() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		StringBuilder description = new StringBuilder("<html><body>");
		Properties properties = loopLayer.getConfiguration().getProperties();
		if (properties.getProperty(ActivityGenerator.COMPARISON,
				ActivityGenerator.CUSTOM_COMPARISON).equals(
				ActivityGenerator.CUSTOM_COMPARISON)) {
			Activity<?> condition = loopLayer.getConfiguration().getCondition();
			if (condition != null) {
				description.append("Looping using custom conditional ");
				if (condition instanceof BeanshellActivity) {
					String script = ((BeanshellActivity)condition).getConfiguration().getScript();
					if (script != null) {
						if (script.length() <= 100) {
							description.append("<pre>\n");
							description.append(script);
							description.append("</pre>\n");
						}
					}
				}
			} else {
				description.append("<i>Unconfigured, will not loop</i>");
			}
		} else {
			description.append("The service will be invoked repeatedly ");
			description.append("until<br> its output <strong>");
			description.append(properties
					.getProperty(ActivityGenerator.COMPARE_PORT));
			description.append("</strong> ");

			Comparison comparison = ActivityGenerator
					.getComparisonById(properties
							.getProperty(ActivityGenerator.COMPARISON));
			description.append(comparison.getName());
			
			description.append(" the " + comparison.getValueType() + ": <pre>");
			description.append(properties
					.getProperty(ActivityGenerator.COMPARE_VALUE));
			description.append("</pre>");
			
			String delay = properties.getProperty(ActivityGenerator.DELAY, "");
			try {
				if (Double.parseDouble(delay) > 0) {
					description.append("adding a delay of " + delay
							+ " seconds between loops.");
				}
			} catch (NumberFormatException ex) {
			}
		}
		description.append("</body></html>");

		panel.add(new JLabel(description.toString()), gbc);
		gbc.gridy++;

		revalidate();
	}



	@Override
	public int getPreferredPosition() {
		return 400;
	}

}
