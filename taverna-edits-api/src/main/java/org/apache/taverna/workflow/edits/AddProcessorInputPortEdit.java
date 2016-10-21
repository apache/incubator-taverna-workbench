/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.taverna.workflow.edits;

import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;

/**
 * Adds an input port to a processor.
 *
 * @author Tom Oinn
 * @author David Withers
 */
public class AddProcessorInputPortEdit extends AddChildEdit<Processor> {
	private final InputProcessorPort port;

	public AddProcessorInputPortEdit(Processor processor, InputProcessorPort port) {
		super(processor, port);
		this.port = port;
	}

	@Override
	protected void doEditAction(Processor processor) {
		processor.getInputPorts().addWithUniqueName(port);
		super.doEditAction(processor);
	}
}
