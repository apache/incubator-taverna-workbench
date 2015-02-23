/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import uk.org.taverna.scufl2.api.port.ActivityPort;

/**
 *
 *
 * @author David Withers
 */
public class ActivityPortConfiguration {

	private ActivityPort activityPort;

	private String name;

	private int depth;

	private int granularDepth;

	public ActivityPortConfiguration(ActivityPort activityPort) {
		this.activityPort = activityPort;
		name = activityPort.getName();
		depth = activityPort.getDepth();
	}

	public ActivityPortConfiguration(String name, int depth) {
		this(name, depth, depth);
	}

	public ActivityPortConfiguration(String name, int depth, int granularDepth) {
		this.name = name;
		this.depth = depth;
		this.granularDepth = granularDepth;
	}

	public ActivityPort getActivityPort() {
		return activityPort;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getGranularDepth() {
		return granularDepth;
	}

	public void setGranularDepth(int granularDepth) {
		this.granularDepth = granularDepth;
	}

}
