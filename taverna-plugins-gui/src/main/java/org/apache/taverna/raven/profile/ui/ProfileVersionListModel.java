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
package org.apache.taverna.raven.profile.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import net.sf.taverna.raven.appconfig.bootstrap.RavenProperties;
import net.sf.taverna.raven.profile.ProfileVersion;
import net.sf.taverna.raven.profile.ProfileVersions;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class ProfileVersionListModel extends AbstractListModel {
	
	private static Logger logger = Logger
			.getLogger(ProfileVersionListModel.class);
	
	private List<ProfileVersion> versions;
	
	
	public ProfileVersionListModel() {		
		try {
			URL url = getProfileListLocation();
			versions = ProfileVersions.getProfileVersions(url);			
		}
		catch(MalformedURLException e) {
			logger.error("Error with profile list URL",e);
			versions = new ArrayList<ProfileVersion>();
		}
	}

	public Object getElementAt(int index) {
		return versions.get(index);
	}

	public int getSize() {
		return versions.size();
	}
	
	private URL getProfileListLocation() throws MalformedURLException{		
		return new URL(RavenProperties.getInstance().getRavenProfileListLocation());
	}

}
