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

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.profiles.ProcessorBinding;


/**
 * Add a new Activity to a Processor, adding the new Activity at the end of the
 * current activity list for that processor.
 *
 * @author Tom Oinn
 *
 */
public class AddActivityEdit extends AbstractProcessorEdit {

	private Activity activityToAdd;
	private ProcessorBinding addedProcessorBinding;

	public AddActivityEdit(Processor processor, Activity activity) {
		super(processor);
		this.activityToAdd = activity;
	}

	@Override
	protected void doEditAction(Processor processor) {
		ProcessorBinding processorBinding = new ProcessorBinding();
		processorBinding.setBoundProcessor(processor);
		processorBinding.setBoundActivity(activityToAdd);
		processorBinding.setParent(activityToAdd.getParent());
		addedProcessorBinding = processorBinding;
	}

	@Override
	protected void undoEditAction(Processor processor) {
		addedProcessorBinding.setParent(null);
	}

}
