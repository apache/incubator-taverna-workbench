/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.ui.views.contextualviews.activity;

import org.apache.taverna.scufl2.api.port.ActivityPort;

/**
 *
 *
 * @author David Withers
 */
public class ActivityPortConfiguration {

	private ActivityPort activityPort;

	private String name;

	private int depth;

	private int granularDepth;

	public ActivityPortConfiguration(ActivityPort activityPort) {
		this.activityPort = activityPort;
		name = activityPort.getName();
		depth = activityPort.getDepth();
	}

	public ActivityPortConfiguration(String name, int depth) {
		this(name, depth, depth);
	}

	public ActivityPortConfiguration(String name, int depth, int granularDepth) {
		this.name = name;
		this.depth = depth;
		this.granularDepth = granularDepth;
	}

	public ActivityPort getActivityPort() {
		return activityPort;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getGranularDepth() {
		return granularDepth;
	}

	public void setGranularDepth(int granularDepth) {
		this.granularDepth = granularDepth;
	}

}
