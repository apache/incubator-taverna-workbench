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

import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;

/**
 * An Edit class responsible for add a Processor to the dataflow.
 *
 * @author Stuart Owen
 *
 */
public class AddProcessorEdit extends AbstractDataflowEdit {

	private Processor processor;

	public Processor getProcessor() {
		return processor;
	}

	public AddProcessorEdit(Workflow dataflow, Processor processor) {
		super(dataflow);
		this.processor=processor;
	}

	/**
	 * Adds the Processor instance to the Dataflow
	 *
	 */
	@Override
	protected void doEditAction(Workflow workflow) {
		workflow.getProcessors().addWithUniqueName(processor);
		processor.setParent(workflow);
	}

	@Override
	protected void undoEditAction(Workflow workflow) {
		processor.setParent(null);
	}
}
