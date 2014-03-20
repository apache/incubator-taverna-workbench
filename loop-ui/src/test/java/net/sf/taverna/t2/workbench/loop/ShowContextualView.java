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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualViewComponent;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * A standalone application to show contextual views
 * <p>
 * The application shows a JFrame containing a contextual view, together with
 * buttons which will select items in the {@link DataflowSelectionManager} for a
 * (rather) empty current dataflow.
 * 
 * @author Stian Soiland-Reyes.
 * 
 */
public class ShowContextualView {

	public static void main(String[] args) throws Exception {
		new ShowContextualView().showFrame();
	}

	private DataflowSelectionManager dataflowSelectionManager = DataflowSelectionManager
			.getInstance();

	private FileManager fileManager = FileManager.getInstance();

	private EditManager editManager = EditManager.getInstance();

	private Processor processor;

	private Edits edits = editManager.getEdits();

	private Dataflow currentDataflow;

	public ShowContextualView() throws EditException {
		currentDataflow = fileManager.newDataflow();
		makeProcessor();

	}

	private void makeProcessor() throws EditException {
		processor = edits.createProcessor("Hello");

		Edit<Dataflow> edit = edits.getAddProcessorEdit(currentDataflow,
				processor);
		editManager.doDataflowEdit(currentDataflow, edit);
		editManager.doDataflowEdit(currentDataflow, edits
				.getDefaultDispatchStackEdit(processor));

		BeanshellActivity beanshell = new BeanshellActivity();
		BeanshellActivityConfigurationBean beanshellConfig = new BeanshellActivityConfigurationBean();
		editManager.doDataflowEdit(currentDataflow, edits
				.getConfigureActivityEdit(beanshell, beanshellConfig));

		editManager.doDataflowEdit(currentDataflow, edits.getAddActivityEdit(
				processor, beanshell));
	}

	private List getSelections() {
		return Arrays.asList(processor, currentDataflow);
	}

	private Component makeSelectionButtons() {
		JPanel buttons = new JPanel();
		for (final Object selection : getSelections()) {
			buttons.add(new JButton(new AbstractAction("" + selection) {
				public void actionPerformed(ActionEvent e) {
					dataflowSelectionManager.getDataflowSelectionModel(
							currentDataflow).setSelection(
							Collections.<Object> singleton(selection));
				}
			}));
		}
		return buttons;
	}

	protected void showFrame() {
		JFrame frame = new JFrame(getClass().getName());
		ContextualViewComponent contextualViewComponent = new ContextualViewComponent();
		frame.add(contextualViewComponent, BorderLayout.CENTER);

		frame.add(makeSelectionButtons(), BorderLayout.NORTH);
		frame.setSize(400, 400);
		frame.setVisible(true);
	}

}
