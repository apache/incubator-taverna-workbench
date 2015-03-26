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
import org.apache.taverna.scufl2.api.iterationstrategy.PortNode;

/**
 * Adds an iteration strategy input port node to an iteration strategy.
 * 
 * @author David Withers
 */
public class AddIterationStrategyInputPortEdit extends
		AbstractEdit<IterationStrategyStack> {
	private final PortNode portNode;

	public AddIterationStrategyInputPortEdit(
			IterationStrategyStack iterationStrategy, PortNode portNode) {
		super(iterationStrategy);
		this.portNode = portNode;
	}

	@Override
	public void doEditAction(IterationStrategyStack iterationStrategy) {
		portNode.setParent(iterationStrategy.get(0));
	}

	@Override
	public void undoEditAction(IterationStrategyStack iterationStrategy) {
		portNode.setParent(null);
	}
}
