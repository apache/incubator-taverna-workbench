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

package org.apache.taverna.workbench.report.config;

import org.apache.taverna.configuration.Configurable;

/**
 * @author David Withers
 */
public interface ReportManagerConfiguration extends Configurable {
	String TIMEOUT = "TIMEOUT";
	String ON_EDIT = "ON_EDIT";
	String ON_OPEN = "ON_OPEN";
	String BEFORE_RUN = "BEFORE_RUN";
	String NO_CHECK = "NoCheck";
	String QUICK_CHECK = "QuickCheck";
	String FULL_CHECK = "FullCheck";
	String NONE = "Do not care";
	String ERRORS_OR_WARNINGS = "Errors or warnings";
	String ERRORS = "Errors";
	String QUERY_BEFORE_RUN = "QUERY_BEFORE_RUN";
	int DEFAULT_REPORT_EXPIRATION = 0;
	String REPORT_EXPIRATION = "REPORT_EXPIRATION";

	public void applySettings();
}
