/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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
package net.sf.taverna.t2.workbench.selection.events;

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
