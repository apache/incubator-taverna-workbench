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
/*

package org.apache.taverna.workbench.design.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workflow.edits.AddChildEdit;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.core.BlockingControlLink;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.profiles.ProcessorBinding;

/**
 * Action for adding a condition to the dataflow.
 * 
 * @author David Withers
 */
@SuppressWarnings("serial")
public class AddConditionAction extends DataflowEditAction {
	private static final Logger logger = Logger.getLogger(AddConditionAction.class);
	private static final Scufl2Tools scufl2Tools = new Scufl2Tools();

	private Processor control;
	private Processor target;

	public AddConditionAction(Workflow dataflow, Processor control,
			Processor target, Component component, EditManager editManager,
			SelectionManager selectionManager,
			ActivityIconManager activityIconManager) {
		super(dataflow, component, editManager, selectionManager);
		this.control = control;
		this.target = target;
		ProcessorBinding processorBinding = scufl2Tools
				.processorBindingForProcessor(control, dataflow.getParent()
						.getMainProfile());
		putValue(SMALL_ICON,
				activityIconManager.iconForActivity(processorBinding
						.getBoundActivity().getType()));
		putValue(NAME, control.getName());
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		try {
			BlockingControlLink controlLink = new BlockingControlLink();
			controlLink.setUntilFinished(control);
			controlLink.setBlock(target);
			editManager.doDataflowEdit(dataflow.getParent(),
					new AddChildEdit<>(dataflow, controlLink));
		} catch (EditException e) {
			logger.debug("Create control link between '" + control.getName()
					+ "' and '" + target.getName() + "' failed");
		}
	}
}
