/*******************************************************************************
 * Copyright (C) 2011 The University of Manchester
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

import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditException;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyStack;
import uk.org.taverna.scufl2.api.iterationstrategy.PortNode;

/**
 * Adds an iteration strategy input port node to an iteration strategy.
 *
 * @author David Withers
 */
public class AddIterationStrategyInputPortEdit implements Edit<IterationStrategyStack> {

	private final IterationStrategyStack iterationStrategy;
	private final PortNode portNode;
	private boolean applied;

	public AddIterationStrategyInputPortEdit(IterationStrategyStack iterationStrategy,
			PortNode portNode) {
		this.iterationStrategy = iterationStrategy;
		this.portNode = portNode;
	}

	@Override
	public IterationStrategyStack doEdit() throws EditException {
		if (applied) {
			throw new EditException("Edit has already been applied");
		}
		portNode.setParent(iterationStrategy.get(0));
		applied = true;
		return iterationStrategy;
	}

	@Override
	public void undo() {
		if (!applied) {
			throw new RuntimeException(
					"Attempt to undo edit that was never applied");
		}
		portNode.setParent(null);
		applied = false;
	}

	@Override
	public IterationStrategyStack getSubject() {
		return iterationStrategy;
	}

	@Override
	public boolean isApplied() {
		return applied;
	}

}
