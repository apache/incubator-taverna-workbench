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

package org.apache.taverna.workbench.file.impl.menu;

import static org.apache.taverna.workbench.file.impl.menu.FileSaveMenuSection.FILE_SAVE_SECTION_URI;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuAction;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.impl.actions.SaveAllWorkflowsAction;

public class FileSaveAllMenuAction extends AbstractMenuAction {
	private final EditManager editManager;
	private final FileManager fileManager;

	public FileSaveAllMenuAction(EditManager editManager,
			FileManager fileManager) {
		super(FILE_SAVE_SECTION_URI, 30);
		this.editManager = editManager;
		this.fileManager = fileManager;
	}

	@Override
	protected Action createAction() {
		return new SaveAllWorkflowsAction(editManager, fileManager);
	}
}
