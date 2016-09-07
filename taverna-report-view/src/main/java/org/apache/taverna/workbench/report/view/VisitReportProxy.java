package org.apache.taverna.workbench.report.view;
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

import org.apache.taverna.visit.VisitKind;
import org.apache.taverna.visit.VisitReport;
import org.apache.taverna.visit.VisitReport.Status;

public class VisitReportProxy {

	Status status;
	int subjectHashCode;
	VisitKind kind;
	String message;

	public VisitReportProxy(VisitReport vr) {
	    this.status = vr.getStatus();
	    this.subjectHashCode = vr.getSubject().hashCode();
	    this.kind = vr.getKind();
	    this.message = vr.getMessage();
	}

	public boolean equals(Object o) {
	    if ((o == null) || !((o instanceof VisitReportProxy) || (o instanceof VisitReport))) {
		return false;
	    }
	    if (o instanceof VisitReport) {
		return this.equals(new VisitReportProxy((VisitReport) o));
	    }
	    VisitReportProxy vrp = (VisitReportProxy) o;
	    return (vrp.status.equals(this.status) &&
		    (vrp.subjectHashCode == this.subjectHashCode) &&
		    (vrp.kind.equals(this.kind)) &&
		    (vrp.message.equals(this.message)));
	}

	public int hashCode() {
	    return ((status.hashCode() >> 2) +
		    (subjectHashCode >> 2) +
		    (kind.hashCode() >> 2) +
		    (message.hashCode() >> 2));
	}
    }

