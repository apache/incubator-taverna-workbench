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

package org.apache.taverna.workbench.selection.events;

import org.apache.taverna.scufl2.api.profiles.Profile;

/**
 * {@link SelectionManagerEvent} for changes to the selected {@link Profile}.
 * 
 * @author David Withers
 */
public class ProfileSelectionEvent implements SelectionManagerEvent {
	private Profile previouslySelectedProfile;
	private Profile selectedProfile;

	public ProfileSelectionEvent(Profile previouslySelectedProfile,
			Profile selectedProfile) {
		this.previouslySelectedProfile = previouslySelectedProfile;
		this.selectedProfile = selectedProfile;
	}

	/**
	 * Returns the previously selected Profile.
	 * 
	 * @return the previously selected Profile
	 */
	public Profile getPreviouslySelectedProfile() {
		return previouslySelectedProfile;
	}

	/**
	 * Returns the currently selected Profile.
	 * 
	 * @return the currently selected Profile
	 */
	public Profile getSelectedProfile() {
		return selectedProfile;
	}
}
