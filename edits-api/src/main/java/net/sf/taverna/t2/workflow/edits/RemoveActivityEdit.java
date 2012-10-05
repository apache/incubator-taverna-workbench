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

import java.util.List;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.profiles.ProcessorBinding;

/**
 * Remove an Activity from a Processor.
 *
 * @author alanrw
 *
 */
public class RemoveActivityEdit extends AbstractProcessorEdit {

	private Activity activityToRemove;
	private ProcessorBinding removedProcessorBinding;

	public RemoveActivityEdit(Processor processor, Activity activity) {
		super(processor);
		this.activityToRemove = activity;
	}

	@Override
	protected void doEditAction(Processor processor) {
		List<ProcessorBinding> bindingsToActivity = scufl2Tools.processorBindingsToActivity(activityToRemove);
		for (ProcessorBinding processorBinding : bindingsToActivity) {
			if (processorBinding.getBoundProcessor().equals(processor)) {
				removedProcessorBinding = processorBinding;
				removedProcessorBinding.setParent(null);
			}
		}
	}

	@Override
	protected void undoEditAction(Processor processor) {
		removedProcessorBinding.setParent(activityToRemove.getParent());
	}

}
