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
package org.apache.taverna.activities.stringconstant.views;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.apache.commons.lang.StringUtils.abbreviate;

import java.awt.Frame;

import javax.swing.Action;

import org.apache.taverna.activities.stringconstant.actions.StringConstantActivityConfigurationAction;
import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;
import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import org.apache.taverna.services.ServiceRegistry;
import org.apache.taverna.scufl2.api.activity.Activity;

import com.fasterxml.jackson.databind.JsonNode;

public class StringConstantActivityContextualView extends
		HTMLBasedActivityContextualView {
	private static final long serialVersionUID = -553974544001808511L;
	private static final int MAX_LENGTH = 100;

	private final EditManager editManager;
	private final FileManager fileManager;
	private final ActivityIconManager activityIconManager;
	private final ServiceDescriptionRegistry serviceDescriptionRegistry;
	private final ServiceRegistry serviceRegistry;

	public StringConstantActivityContextualView(Activity activity,
			EditManager editManager, FileManager fileManager,
			ActivityIconManager activityIconManager,
			ColourManager colourManager,
			ServiceDescriptionRegistry serviceDescriptionRegistry,
			ServiceRegistry serviceRegistry) {
		super(activity, colourManager);
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.activityIconManager = activityIconManager;
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
		this.serviceRegistry = serviceRegistry;
	}

	@Override
	public String getViewTitle() {
		return "Text constant";
	}

	@Override
	protected String getRawTableRowsHtml() {
		JsonNode json = getConfigBean().getJson();
		String value = json.get("string").textValue();
		value = abbreviate(value, MAX_LENGTH);
		value = escapeHtml(value);
		String html = "<tr><td>Value</td><td>" + value + "</td></tr>";
		return html;
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new StringConstantActivityConfigurationAction(getActivity(),
				owner, editManager, fileManager, activityIconManager,
				serviceDescriptionRegistry, serviceRegistry);
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}
}
