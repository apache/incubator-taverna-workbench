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

package org.apache.taverna.activities.unrecognized.views;

import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.port.InputActivityPort;
import org.apache.taverna.scufl2.api.port.OutputActivityPort;

/**
 * A UnrecognizedContextualView displays information about a UnrecognizedActivity
 *
 * @author alanrw
 * @author David Withers
 */
@SuppressWarnings("serial")
public class UnrecognizedContextualView extends HTMLBasedActivityContextualView {

	public UnrecognizedContextualView(Activity activity, ColourManager colourManager) {
		super(activity, colourManager);
	}

	/**
	 * The table for the UnrecognizedActivity shows its ports.
	 *
	 * @return
	 */
	@Override
	protected String getRawTableRowsHtml() {
		StringBuilder html = new StringBuilder();
		html.append("<tr><th>Input Port Name</th><th>Depth</th></tr>");
		for (InputActivityPort inputActivityPort : getActivity().getInputPorts()) {
			html.append("<tr><td>" + inputActivityPort.getName() + "</td><td>");
			html.append(inputActivityPort.getDepth() + "</td></tr>");
		}
		html.append("<tr><th>Output Port Name</th><th>Depth</th></tr>");
		for (OutputActivityPort outputActivityPort : getActivity().getOutputPorts()) {
			html.append("<tr><td>" + outputActivityPort.getName() + "</td><td>");
			html.append(outputActivityPort.getDepth() + "</td></tr>");
		}
		return html.toString();
	}

	@Override
	public String getViewTitle() {
		return "Unrecognized service";
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

}
