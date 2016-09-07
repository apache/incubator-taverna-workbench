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

package org.apache.taverna.workbench.ui.actions.activity;

import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.configurations.Configuration;

/**
 * A contextual view specific to an Activity. Concrete subclasses must
 * initialise the view by calling {@link #initView()}.
 * <p>
 * The implementation provides a view based upon the properties set in the
 * Configuration
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 * 
 * @see Activity
 * @see ContextualView
 */
@SuppressWarnings("serial")
public abstract class ActivityContextualView extends ContextualView {
	private Activity activity;
	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	/**
	 * Constructs an instance of the view.
	 * <p>
	 * The constructor parameter for the implementation of this class should
	 * define the specific Activity type itself.
	 * 
	 * @param activity
	 */
	protected ActivityContextualView(Activity activity) {
		super();
		this.activity = activity;
	}

	public Activity getActivity() {
		return activity;
	}

	public Configuration getConfigBean() {
		return scufl2Tools.configurationFor(activity, activity.getParent());
	}

	@Override
	public abstract void refreshView();
}
