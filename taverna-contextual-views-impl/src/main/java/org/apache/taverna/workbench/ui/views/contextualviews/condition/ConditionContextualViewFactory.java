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
package org.apache.taverna.workbench.ui.views.contextualviews.condition;

import static java.util.Arrays.asList;

import java.util.List;

import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import org.apache.taverna.workflowmodel.Condition;
import org.apache.taverna.scufl2.api.core.BlockingControlLink;

/**
 * A factory of contextual views for dataflow's condition links.
 * 
 * @author David Withers
 * 
 */
public class ConditionContextualViewFactory implements
		ContextualViewFactory<BlockingControlLink> {
	@Override
	public boolean canHandle(Object object) {
		return object instanceof Condition;
	}

	@Override
	public List<ContextualView> getViews(BlockingControlLink condition) {
		return asList(new ContextualView[] { new ConditionContextualView(
				condition) });
	}

}
