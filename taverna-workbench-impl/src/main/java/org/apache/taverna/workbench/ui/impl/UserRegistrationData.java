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

public class UserRegistrationData {
	private String tavernaVersion = "";
	private String firstName = "";
	private String lastName = "";
	private String emailAddress = "";
	private String institutionOrCompanyName = "";
	private String industry = "";
	private String field = "";
	private String purposeOfUsingTaverna = "";
	private boolean keepMeInformed = false;

	public void setTavernaVersion(String tavernaVersion) {
		this.tavernaVersion = tavernaVersion;
	}

	public String getTavernaVersion() {
		return tavernaVersion;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setInstitutionOrCompanyName(String institutionOrCompanyName) {
		this.institutionOrCompanyName = institutionOrCompanyName;
	}

	public String getInstitutionOrCompanyName() {
		return institutionOrCompanyName;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public String getIndustry() {
		return industry;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getField() {
		return field;
	}

	public void setPurposeOfUsingTaverna(String purposeOfUsingTaverna) {
		this.purposeOfUsingTaverna = purposeOfUsingTaverna;
	}

	public String getPurposeOfUsingTaverna() {
		return purposeOfUsingTaverna;
	}

	public void setKeepMeInformed(boolean keepMeInformed) {
		this.keepMeInformed = keepMeInformed;
	}

	public boolean getKeepMeInformed() {
		return keepMeInformed;
	}
}
