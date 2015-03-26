/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workflow.edits;

import org.apache.taverna.scufl2.api.iterationstrategy.IterationStrategyStack;

/**
 * Removes all the iteration strategies from an iteration strategy stack.
 * 
 * @author David Withers
 */
public class ClearIterationStrategyStackEdit extends
		AbstractEdit<IterationStrategyStack> {
	private IterationStrategyStack oldIterationStrategyStack;

	public ClearIterationStrategyStackEdit(
			IterationStrategyStack iterationStrategyStack) {
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
