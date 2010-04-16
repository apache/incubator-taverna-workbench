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
package net.sf.taverna.t2.workbench.parallelize;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.ParallelizeConfig;

import org.apache.log4j.Logger;

/**
 * View of a processor, including it's iteration stack, activities, etc.
 * 
 * @author Alan R Williams
 * 
 */
public class ParallelizeContextualView extends ContextualView {

	private static Logger logger = Logger.getLogger(ParallelizeContextualView.class);

	private EditManager editManager = EditManager.getInstance();

	private Parallelize parallelizeLayer;

	private JPanel panel;

    //	private Processor processor;

	public ParallelizeContextualView(Parallelize parallelizeLayer) {
		super();
		this.parallelizeLayer = parallelizeLayer;
		//		processor = parallelizeLayer.getProcessor();
		initialise();
		initView();
	}

    /*
	@Override
	public Action getConfigureAction(Frame owner) {
		return new ConfigureAction(owner);
	}
    */
	@Override
	public void refreshView() {
		initialise();
	}

	private void initialise() {
		if (panel == null) {
			panel = createPanel();
		} else {
			panel.removeAll();
		}
		
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		String text = "";
		ParallelizeConfig config = parallelizeLayer.getConfiguration();
		int maxJobs = config.getMaximumJobs();
			text += "The maximum number of jobs is " + maxJobs;
		textArea.setText(text);
		textArea.setBackground(panel.getBackground());
		panel.add(textArea, BorderLayout.CENTER);
		revalidate();
	}

	
	@Override
	public JComponent getMainFrame() {
		return panel;
	}

	@Override
	public String getViewTitle() {
	    return "Parallelize of " + parallelizeLayer.getProcessor().getLocalName();
	}

	protected JPanel createPanel() {
		JPanel result = new JPanel();
		result.setLayout(new BorderLayout());

		
		return result;
	}
	
	@Override
	public int getPreferredPosition() {
		return 400;
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new ParallelizeConfigureAction(owner, this, this.parallelizeLayer);
	}


}
