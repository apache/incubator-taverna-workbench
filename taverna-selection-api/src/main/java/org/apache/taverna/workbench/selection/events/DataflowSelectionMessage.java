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

package org.apache.taverna.workbench.selection.events;

import org.apache.taverna.scufl2.api.core.Workflow;

/**
 * A message about the selection of a {@linkplain Workflow dataflow} object.
 * 
 * @author David Withers
 */
public class DataflowSelectionMessage {
	public enum Type {
		ADDED, REMOVED
	}

	private Type type;
	private Object element;

	/**
	 * Constructs a new instance of DataflowSelectionMessage.
	 * 
	 * @param type
	 * @param element
	 */
	public DataflowSelectionMessage(Type type, Object element) {
		this.type = type;
		this.element = element;
	}

	/**
	 * Returns the type of the message.
	 * 
	 * @return the type of the message
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns the subject of the message.
	 * 
	 * @return the of the message
	 */
	public Object getElement() {
		return element;
	}
}
