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

import java.util.List;

import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.scufl2.api.core.DataLink;
import org.apache.taverna.scufl2.api.port.ReceiverPort;

/**
 * Change datalink merge positions based on ordered list of data links.
 * 
 * @author David Withers
 * @author Stian Soiland-Reyes
 */
public class ReorderMergePositionsEdit extends AbstractEdit<ReceiverPort> {
	private List<DataLink> newMergePositions;
	private final List<DataLink> oldMergePositions;

	public ReorderMergePositionsEdit(List<DataLink> dataLinks,
			List<DataLink> newMergePositions) {
		super(dataLinks.get(0).getSendsTo());
		this.oldMergePositions = dataLinks;
		this.newMergePositions = newMergePositions;
	}

	@Override
	protected void doEditAction(ReceiverPort subject) throws EditException {
		for (int i = 0; i < newMergePositions.size(); i++)
			newMergePositions.get(i).setMergePosition(i);
	}

	@Override
	protected void undoEditAction(ReceiverPort subject) {
		for (int i = 0; i < oldMergePositions.size(); i++)
			oldMergePositions.get(i).setMergePosition(i);
	}
}
