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
package org.apache.taverna.workbench.file.impl.toolbar;

import static org.apache.taverna.workbench.file.impl.toolbar.FileToolbarMenuSection.FILE_TOOLBAR_SECTION;

import java.net.URI;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuAction;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.impl.actions.OpenWorkflowAction;

public class OpenToolbarAction extends AbstractMenuAction {
	private static final URI FILE_OPEN_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#fileToolbarOpen");
	private final FileManager fileManager;

	public OpenToolbarAction(FileManager fileManager) {
		super(FILE_TOOLBAR_SECTION, 20, FILE_OPEN_URI);
		this.fileManager = fileManager;
	}

	@Override
	protected Action createAction() {
		return new OpenWorkflowAction(fileManager);
	}
}
