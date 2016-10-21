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
package org.apache.taverna.workbench.ui.views.contextualviews.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.profiles.ProcessorBinding;

/**
 * SPI factory for creating a {@link ProcessorContextualView}.
 * 
 */
public class ProcessorActivitiesContextualViewFactory implements
		ContextualViewFactory<Processor> {
	private Scufl2Tools scufl2Tools = new Scufl2Tools();
	private ContextualViewFactoryRegistry contextualViewFactoryRegistry;
	private SelectionManager selectionManager;

	@Override
	public boolean canHandle(Object selection) {
		return selection instanceof Processor;
	}

	@Override
	public List<ContextualView> getViews(Processor selection) {
		List<ContextualView> result = new ArrayList<>();
		List<ProcessorBinding> processorBindings = scufl2Tools
				.processorBindingsForProcessor(selection,
						selectionManager.getSelectedProfile());
		for (ProcessorBinding processorBinding : processorBindings) {
			Activity activity = processorBinding.getBoundActivity();
			for (ContextualViewFactory<? super Activity> cvf : contextualViewFactoryRegistry
					.getViewFactoriesForObject(activity))
				result.addAll(cvf.getViews(activity));
		}
		return result;
	}

	public void setContextualViewFactoryRegistry(
			ContextualViewFactoryRegistry contextualViewFactoryRegistry) {
		this.contextualViewFactoryRegistry = contextualViewFactoryRegistry;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}
}
