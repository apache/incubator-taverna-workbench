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
package org.apache.taverna.activities.unrecognized.views;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import org.apache.taverna.scufl2.api.activity.Activity;

/**
 * This class generates a contextual view for a UnrecognizedActivity
 *
 * @author alanrw
 */
public class UnrecognizedActivityViewFactory implements ContextualViewFactory<Activity> {

	public static final URI ACTIVITY_TYPE = URI.create("http://ns.taverna.org.uk/2010/activity/unrecognized");

	private ColourManager colourManager;

	/**
	 * The factory can handle a UnrecognizedActivity
	 *
	 * @param object
	 * @return
	 */
	public boolean canHandle(Object object) {
		return object instanceof Activity && ((Activity) object).getType().equals(ACTIVITY_TYPE);
	}

	/**
	 * Return a contextual view that can display information about a UnrecognizedActivity
	 *
	 * @param activity
	 * @return
	 */
	public List<ContextualView> getViews(Activity activity) {
		return Arrays.asList(new ContextualView[] { new UnrecognizedContextualView(activity,
				colourManager) });
	}

	public void setColourManager(ColourManager colourManager) {
		this.colourManager = colourManager;
	}

}
