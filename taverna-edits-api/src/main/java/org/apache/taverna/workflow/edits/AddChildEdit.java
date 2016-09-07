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
/*

package org.apache.taverna.workflow.edits;

import org.apache.taverna.scufl2.api.common.Child;
import org.apache.taverna.scufl2.api.common.WorkflowBean;

/**
 * Adds a child to a parent.
 *
 * @author David Withers
 */
public class AddChildEdit<T extends WorkflowBean> extends AbstractEdit<T> {
	private Child<T> child;

	public AddChildEdit(T parent, Child<T> child) {
		super(parent);
		this.child = child;
	}

	@Override
	protected void doEditAction(T parent) {
		child.setParent(parent);
	}

	@Override
	protected void undoEditAction(T parent) {
		child.setParent(null);
	}

	public Child<T> getChild() {
		return child;
	}
}
