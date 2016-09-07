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

package org.apache.taverna.workbench.report;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.taverna.scufl2.api.common.WorkflowBean;
import org.apache.taverna.scufl2.api.profiles.Profile;
import org.apache.taverna.scufl2.validation.WorkflowBeanReport;
import org.apache.taverna.scufl2.validation.Status;

import org.apache.taverna.lang.observer.Observer;

public interface ReportManager {
	void updateReport(Profile p, boolean includeTimeConsuming, boolean remember);

	void updateObjectSetReport(Profile p, Set<WorkflowBean> objects);

	void updateObjectReport(Profile p, WorkflowBean o);

	Set<WorkflowBeanReport> getReports(Profile p, WorkflowBean object);

	Map<WorkflowBean, Set<WorkflowBeanReport>> getReports(Profile p);

	boolean isStructurallySound(Profile p);

	Status getStatus(Profile p);

	Status getStatus(Profile p, WorkflowBean object);

	String getSummaryMessage(Profile p, WorkflowBean object);

	long getLastCheckedTime(Profile p);

	long getLastFullCheckedTime(Profile p);

	void addObserver(Observer<ReportManagerEvent> observer);

	List<Observer<ReportManagerEvent>> getObservers();

	void removeObserver(Observer<ReportManagerEvent> observer);
}
