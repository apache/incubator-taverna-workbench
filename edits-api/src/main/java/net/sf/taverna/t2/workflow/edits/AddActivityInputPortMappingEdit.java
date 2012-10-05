/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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
package net.sf.taverna.t2.workflow.edits;

import java.util.ArrayList;
import java.util.List;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.port.InputActivityPort;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.profiles.ProcessorBinding;
import uk.org.taverna.scufl2.api.profiles.ProcessorInputPortBinding;

public class AddActivityInputPortMappingEdit extends AbstractActivityEdit {

	private final InputProcessorPort inputProcessorPort;
	private final InputActivityPort inputActivityPort;
	private List<ProcessorInputPortBinding> portBindings;

	public AddActivityInputPortMappingEdit(Activity activity, InputProcessorPort inputProcessorPort,
			InputActivityPort inputActivityPort) {
		super(activity);
		this.inputProcessorPort = inputProcessorPort;
		this.inputActivityPort = inputActivityPort;
	}

	@Override
	protected void doEditAction(Activity activity) {
		portBindings = new ArrayList<ProcessorInputPortBinding>();
		List<ProcessorBinding> bindingsToActivity = scufl2Tools.processorBindingsToActivity(activity);
		for (ProcessorBinding processorBinding : bindingsToActivity) {
			portBindings.add(new ProcessorInputPortBinding(processorBinding, inputProcessorPort, inputActivityPort));
		}
	}

	@Override
	protected void undoEditAction(Activity activity) {
		for (ProcessorInputPortBinding portBinding : portBindings) {
			portBinding.setParent(null);
		}
	}

}
