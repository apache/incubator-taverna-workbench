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

import org.apache.taverna.scufl2.api.port.GranularDepthPort;

/**
 * Changes the granular depth of a port.
 * 
 * @author David Withers
 */
public class ChangeGranularDepthEdit<T extends GranularDepthPort> extends
		AbstractEdit<T> {
	private Integer newGranularDepth, oldGranularDepth;

	public ChangeGranularDepthEdit(T granularDepthPort, Integer newGranularDepth) {
		super(granularDepthPort);
		this.newGranularDepth = newGranularDepth;
		oldGranularDepth = granularDepthPort.getGranularDepth();
	}

	@Override
	protected void doEditAction(T granularDepthPort) {
		granularDepthPort.setGranularDepth(newGranularDepth);
	}

	@Override
	protected void undoEditAction(T granularDepthPort) {
		granularDepthPort.setGranularDepth(oldGranularDepth);
	}
}
