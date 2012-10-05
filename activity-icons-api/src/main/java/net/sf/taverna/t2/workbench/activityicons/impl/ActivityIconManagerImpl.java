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
package net.sf.taverna.t2.workbench.activityicons.impl;

import java.net.URI;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.Icon;

import uk.org.taverna.scufl2.api.activity.Activity;

import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconSPI;

/**
 * Manager for activities' icons.
 *
 * @author Alex Nenadic
 * @author Alan R Williams
 *
 */
public class ActivityIconManagerImpl implements ActivityIconManager{

	// Cache of already obtained icons; maps activities to their icons
	private WeakHashMap<URI, Icon> iconsMap = new WeakHashMap<URI, Icon>();

	private List<ActivityIconSPI> activityIcons;

	/** Returns an icon for the Activity. */
	public Icon iconForActivity(URI activityType) {
		Icon icon = iconsMap.get(activityType);
		if (icon == null) {
			int bestScore = ActivityIconSPI.NO_ICON;
			ActivityIconSPI bestSPI = null;
			for (ActivityIconSPI spi : activityIcons) {
				int spiScore = spi.canProvideIconScore(activityType);
				if (spiScore > bestScore) {
					bestSPI = spi;
					bestScore = spiScore;
				}
			}
			if (bestSPI != null) {
				icon = bestSPI.getIcon(activityType);
				iconsMap.put(activityType, icon);
				return icon;
			}
			else{
				return null;
			}
		} else {
			return icon;
		}
	}

	public Icon iconForActivity(Activity activity) {
		return iconForActivity(activity.getConfigurableType());
	}

	public void resetIcon(URI activityType) {
		Icon icon = iconsMap.get(activityType);
		if (icon != null) {
			iconsMap.remove(activityType);
		}
		iconForActivity(activityType);
	}

	public void setActivityIcons(List<ActivityIconSPI> activityIcons) {
		this.activityIcons = activityIcons;
	}

}
