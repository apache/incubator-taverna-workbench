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
package net.sf.taverna.t2.workbench.retry;

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
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Retry;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.RetryConfig;

import org.apache.log4j.Logger;

/**
 * View of a processor, including it's iteration stack, activities, etc.
 * 
 * @author Alan R Williams
 * 
 */
public class RetryContextualView extends ContextualView {

	private static Logger logger = Logger.getLogger(RetryContextualView.class);

	private EditManager editManager = EditManager.getInstance();

	private Retry retryLayer;

	private JPanel panel;

    //	private Processor processor;

	public RetryContextualView(Retry retryLayer) {
		super();
		this.retryLayer = retryLayer;
		//		processor = retryLayer.getProcessor();
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
		RetryConfig config = retryLayer.getConfiguration();
		int maxRetries = config.getMaxRetries();
		if (maxRetries < 1) {
			text += "The service is not re-tried";
		} else if (maxRetries == 1) {
			text += "The service is re-tried once";
			text += " after " + config.getInitialDelay() + "ms";
		} else {
			text += "The service is re-tried " + maxRetries + " times.  ";
			float backoffFactor = config.getBackoffFactor();
			int initialDelay = config.getInitialDelay();
			if (backoffFactor == 1.0) {
				text += "Each time after a delay of " + initialDelay + "ms.";
			} else {
				int maxDelay = config.getMaxDelay();
				text += "The first delay is " + initialDelay + "ms";
				int noMaxDelay = (int) (initialDelay * Math.pow(backoffFactor,maxRetries - 1));
				if (noMaxDelay < maxDelay) {
					maxDelay = noMaxDelay;
				}
				text += " with a maximum delay of " + maxDelay + "ms.";
				text += "  Each delay is increased by a factor of " + backoffFactor;
			}
		}
		textArea.setText(text);
		panel.add(textArea, BorderLayout.CENTER);
		revalidate();
	}

	
	@Override
	public JComponent getMainFrame() {
		return panel;
	}

	@Override
	public String getViewTitle() {
	    return "Retry of fred";
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
		return new RetryConfigureAction(owner, this, this.retryLayer);
	}


}
