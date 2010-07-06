/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.ui.impl;

public class UserRegistrationData {
	
	private String tavernaVersion="";
	
	private String firstName="";
	
	private String lastName="";
	
	private String emailAddress="";
	
	private String institutionOrCompanyName="";
	
	private String industry="";
	
	private String field="";
	
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
