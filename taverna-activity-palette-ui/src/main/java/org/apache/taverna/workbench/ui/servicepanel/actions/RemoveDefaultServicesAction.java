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

package org.apache.taverna.workbench.ui.servicepanel.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.taverna.servicedescriptions.ConfigurableServiceProvider;
import org.apache.taverna.servicedescriptions.ServiceDescriptionProvider;
import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;

@SuppressWarnings("serial")
public class RemoveDefaultServicesAction extends AbstractAction {
	private final ServiceDescriptionRegistry serviceDescriptionRegistry;

	public RemoveDefaultServicesAction(
			ServiceDescriptionRegistry serviceDescriptionRegistry) {
		super("Remove default service providers");
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		for (ServiceDescriptionProvider provider : serviceDescriptionRegistry
				.getDefaultServiceDescriptionProviders()) {
			if (!(provider instanceof ConfigurableServiceProvider))
				continue;
			serviceDescriptionRegistry
					.removeServiceDescriptionProvider(provider);
		}
	}
}
