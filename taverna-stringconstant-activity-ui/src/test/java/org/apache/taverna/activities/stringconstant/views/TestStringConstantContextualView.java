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

package org.apache.taverna.activities.stringconstant.views;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.apache.taverna.activities.stringconstant.actions.StringConstantActivityConfigurationAction;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.taverna.scufl2.api.activity.Activity;

public class TestStringConstantContextualView {
	Activity activity;

	@Before
	public void setup() {
		activity = new Activity();
	}

	@Test
	@Ignore
	public void testGetConfigureAction() throws Exception {
		ContextualView view = new StringConstantActivityContextualView(
				activity, null, null, null, null, null, null);
		assertNotNull("The action should not be null",
				view.getConfigureAction(null));
		assertTrue(
				"Should be a StringConstantActivityConfigurationAction",
				view.getConfigureAction(null) instanceof StringConstantActivityConfigurationAction);
	}
}
