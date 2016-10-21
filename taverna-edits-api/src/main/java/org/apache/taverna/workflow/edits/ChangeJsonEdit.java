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

import org.apache.taverna.scufl2.api.configurations.Configuration;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Changes the JSON of a configuration.
 * 
 * @author David Withers
 */
public class ChangeJsonEdit extends AbstractEdit<Configuration> {
	private JsonNode oldJson, newJson;

	public ChangeJsonEdit(Configuration configuration, JsonNode newJson) {
		super(configuration);
		this.newJson = newJson;
		newJson = configuration.getJson();
	}

	@Override
	protected void doEditAction(Configuration configuration) {
		configuration.setJson(newJson);
	}

	@Override
	protected void undoEditAction(Configuration configuration) {
		configuration.setJson(oldJson);
	}
}
