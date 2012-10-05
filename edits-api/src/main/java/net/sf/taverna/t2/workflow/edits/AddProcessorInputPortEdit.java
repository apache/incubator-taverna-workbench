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

import net.sf.taverna.t2.workbench.edits.EditException;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.iterationstrategy.CrossProduct;
import uk.org.taverna.scufl2.api.iterationstrategy.DotProduct;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyParent;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyStack;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyTopNode;
import uk.org.taverna.scufl2.api.iterationstrategy.PortNode;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;

/**
 * Build a new input port on a processor, also modifies the processor's
 * iteration strategy or strategies to ensure the new port is bound into them.
 *
 * @author Tom Oinn
 *
 */
public class AddProcessorInputPortEdit extends AbstractProcessorEdit {

	private final InputProcessorPort port;
	private PortNode portNode;

	public AddProcessorInputPortEdit(Processor p, InputProcessorPort port) {
		super(p);
		this.port = port;

	}

	@Override
	protected void doEditAction(Processor processor) throws EditException {
		// Add a new InputPort object to the processor and also create an
		// appropriate PortNode in any iteration strategies. By
		// default set the desired drill depth on each iteration strategy node
		// to the same as the input port, so this won't automatically trigger
		// iteration staging unless the depth is altered on the iteration
		// strategy itself.)
		processor.getInputPorts().addWithUniqueName(port);
		port.setParent(processor);
		IterationStrategyStack iterationStrategyStack = processor.getIterationStrategyStack();
		for (IterationStrategyTopNode iterationStrategyTopNode : iterationStrategyStack) {
			portNode = new PortNode(iterationStrategyTopNode, port);
			portNode.setDesiredDepth(port.getDepth());
			break;
		}

	}

	@Override
	protected void undoEditAction(Processor processor) {
		if (portNode != null) {
			IterationStrategyParent parent = portNode.getParent();
			if (parent instanceof DotProduct) {
				((DotProduct) parent).remove(portNode);
			} else if (parent instanceof CrossProduct) {
				((CrossProduct) parent).remove(portNode);
			}
		}
		processor.getInputPorts().remove(port);
	}

}
