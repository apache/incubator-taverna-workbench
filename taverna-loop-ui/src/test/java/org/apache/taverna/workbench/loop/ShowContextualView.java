package org.apache.taverna.workbench.loop;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Processor;

import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.edits.impl.EditManagerImpl;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.impl.FileManagerImpl;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.selection.impl.SelectionManagerImpl;
import org.apache.taverna.workbench.ui.views.contextualviews.impl.ContextualViewComponent;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.impl.ContextualViewFactoryRegistryImpl;

/**
 * A standalone application to show contextual views
 * <p>
 * The application shows a JFrame containing a contextual view, together with
 * buttons which will select items in the {@link SelectionManager} for a
 * (rather) empty current dataflow.
 *
 * @author Stian Soiland-Reyes.
 *
 */
public class ShowContextualView {

	public static void main(String[] args) throws Exception {
		EditManager editManager = new EditManagerImpl();
		FileManager fileManager = new FileManagerImpl(editManager);
		ContextualViewFactoryRegistry contextualViewFactoryRegistry = new ContextualViewFactoryRegistryImpl();
		SelectionManagerImpl selectionMan = new SelectionManagerImpl();
		selectionMan.setFileManager(fileManager);
		selectionMan.setEditManager(editManager);
		new ShowContextualView(editManager, fileManager,selectionMan, contextualViewFactoryRegistry).showFrame();
	}

	private SelectionManager selectionManager;
	private FileManager fileManager;
	private EditManager editManager;
	private ContextualViewFactoryRegistry contextualViewFactoryRegistry;

	private org.apache.taverna.scufl2.api.core.Processor processor;

	private WorkflowBundle currentDataflow;

	public ShowContextualView(EditManager editManager, FileManager fileManager, final SelectionManager selectionManager, ContextualViewFactoryRegistry contextualViewFactoryRegistry) {
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.selectionManager = selectionManager;
		this.contextualViewFactoryRegistry = contextualViewFactoryRegistry;
		currentDataflow = fileManager.newDataflow();
		makeProcessor();

	}

	private void makeProcessor() {
	    processor = new Processor(currentDataflow.getMainWorkflow(), "Hello");
	}

	private List getSelections() {
		return Arrays.asList(processor, currentDataflow);
	}

	private Component makeSelectionButtons() {
		JPanel buttons = new JPanel();
		for (final Object selection : getSelections()) {
			buttons.add(new JButton(new AbstractAction("" + selection) {
				public void actionPerformed(ActionEvent e) {
					selectionManager.getDataflowSelectionModel(
							currentDataflow).setSelection(
							Collections.<Object> singleton(selection));
				}
			}));
		}
		return buttons;
	}

	protected void showFrame() {
		JFrame frame = new JFrame(getClass().getName());
		ContextualViewComponent contextualViewComponent = new ContextualViewComponent(editManager, selectionManager, contextualViewFactoryRegistry);
		frame.add(contextualViewComponent, BorderLayout.CENTER);

		frame.add(makeSelectionButtons(), BorderLayout.NORTH);
		frame.setSize(400, 400);
		frame.setVisible(true);
	}

}
