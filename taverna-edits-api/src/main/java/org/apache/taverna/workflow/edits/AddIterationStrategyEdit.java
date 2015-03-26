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
import org.apache.taverna.scufl2.api.iterationstrategy.IterationStrategyTopNode;

/**
 * Adds an IterationStrategyTopNode to an IterationStrategyStack.
 *
 * @author David Withers
 */
public class AddIterationStrategyEdit extends AbstractEdit<IterationStrategyStack> {
	private final IterationStrategyTopNode iterationStrategyTopNode;

	public AddIterationStrategyEdit(IterationStrategyStack iterationStrategyStack,
			IterationStrategyTopNode iterationStrategyTopNode) {
		super(iterationStrategyStack);
		this.iterationStrategyTopNode = iterationStrategyTopNode;
	}

	@Override
	public void doEditAction(IterationStrategyStack iterationStrategyStack) {
		iterationStrategyStack.add(iterationStrategyTopNode);
	}

	@Override
	public void undoEditAction(IterationStrategyStack iterationStrategyStack) {
		iterationStrategyStack.remove(iterationStrategyTopNode);
	}
}
