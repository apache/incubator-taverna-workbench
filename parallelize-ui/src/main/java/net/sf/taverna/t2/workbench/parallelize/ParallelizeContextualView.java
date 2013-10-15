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

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.sf.taverna.t2.lang.ui.ReadOnlyTextArea;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.dispatchstack.DispatchStackLayer;

/**
 * View of a processor, including it's iteration stack, activities, etc.
 *
 * @author Alan R Williams
 *
 */
@SuppressWarnings("serial")
public class ParallelizeContextualView extends ContextualView {

	private final Scufl2Tools scufl2Tools = new Scufl2Tools();

	private DispatchStackLayer parallelizeLayer;

	private JPanel panel;

	private final EditManager editManager;

	private final SelectionManager selectionManager;

	public ParallelizeContextualView(DispatchStackLayer parallelizeLayer, EditManager editManager, SelectionManager selectionManager) {
		super();
		this.parallelizeLayer = parallelizeLayer;
		this.editManager = editManager;
		this.selectionManager = selectionManager;
		initialise();
		initView();
	}

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

		JTextArea textArea = new ReadOnlyTextArea();
		textArea.setEditable(false);
		String maxJobs = "1";
		Configuration config = scufl2Tools.configurationFor(parallelizeLayer, selectionManager.getSelectedProfile());
		if (config.getJson().has("maximumJobs")) {
			maxJobs = config.getJson().get("maximumJobs").asText();
		}
		textArea.setText("The maximum number of jobs is " + maxJobs);
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
	    return "Parallelize of " + parallelizeLayer.getParent().getParent().getName();
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
		return new ParallelizeConfigureAction(owner, this, this.parallelizeLayer, editManager, selectionManager);
	}


}
