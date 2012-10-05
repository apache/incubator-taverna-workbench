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

import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyStack;

/**
 * Removes all the iteration strategies from an iteration strategy stack.
 *
 * @author David Withers
 */
public class ClearIterationStrategyStackEdit extends AbstractEdit<IterationStrategyStack> {

	private IterationStrategyStack oldIterationStrategyStack;

	public ClearIterationStrategyStackEdit(IterationStrategyStack iterationStrategyStack) {
		super(iterationStrategyStack);
	}

	@Override
	protected void doEditAction(IterationStrategyStack iterationStrategyStack) {
		oldIterationStrategyStack = new IterationStrategyStack();
		oldIterationStrategyStack.addAll(iterationStrategyStack);
		iterationStrategyStack.clear();
	}

	@Override
	public void undoEditAction(IterationStrategyStack iterationStrategyStack) {
		iterationStrategyStack.addAll(oldIterationStrategyStack);
	}

}
