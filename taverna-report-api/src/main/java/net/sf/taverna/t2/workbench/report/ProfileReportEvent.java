/**
 * 
 */
package net.sf.taverna.t2.workbench.report;

import org.apache.taverna.scufl2.api.profiles.Profile;

/**
 * @author alanrw
 */
public class ProfileReportEvent implements ReportManagerEvent {
	private final Profile profile;

	public ProfileReportEvent(Profile d) {
		this.profile = d;
	}

	public Profile getProfile() {
		return profile;
	}
}
