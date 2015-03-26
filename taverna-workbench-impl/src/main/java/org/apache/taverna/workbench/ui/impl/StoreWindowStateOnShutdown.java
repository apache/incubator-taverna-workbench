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

package org.apache.taverna.workbench.ui.impl;

import org.apache.log4j.Logger;

import org.apache.taverna.workbench.ShutdownSPI;
import org.apache.taverna.workbench.ui.Workbench;

/**
 * Store Workbench window size and perspectives, so that settings can be used on
 * next startup.
 * 
 * @author Stian Soiland-Reyes
 */
public class StoreWindowStateOnShutdown implements ShutdownSPI {
	private static Logger logger = Logger
			.getLogger(StoreWindowStateOnShutdown.class);

	private Workbench workbench;

	@Override
	public int positionHint() {
		return 1000;
	}

	@Override
	public boolean shutdown() {
		try {
			workbench.storeSizeAndLocationPrefs();
		} catch (Exception ex) {
			logger.error("Error saving the Workbench size and position", ex);
		}
		return true;
	}

	public void setWorkbench(Workbench workbench) {
		this.workbench = workbench;
	}
}
