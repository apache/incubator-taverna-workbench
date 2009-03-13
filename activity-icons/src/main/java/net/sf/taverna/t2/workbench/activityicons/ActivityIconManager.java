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
package net.sf.taverna.t2.workbench.activityicons;

import java.util.WeakHashMap;

import javax.swing.Icon;

import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Manager for activities' icons.
 * 
 * @author Alex Nenadic
 * 
 */
public class ActivityIconManager extends SPIRegistry<ActivityIconSPI> {

	// Cache of already obtained icons; maps activities to their icons
	private WeakHashMap<Activity<?>, Icon> iconsMap = new WeakHashMap<Activity<?>, Icon>();

	protected ActivityIconManager() {
		super(ActivityIconSPI.class);
	}

	/**
	 * Get the singleton instance of this registry.
	 */
	public static synchronized ActivityIconManager getInstance() {
		return Singleton.instance;
	}

	private static class Singleton {
		private static final ActivityIconManager instance = new ActivityIconManager();
	}

	/** Returns an icon for the Activity. */
	public Icon iconForActivity(Activity<?> activity) {
		Icon icon = iconsMap.get(activity);
		if (icon == null) {
			int bestScore = ActivityIconSPI.NO_ICON;
			ActivityIconSPI bestSPI = null;
			for (ActivityIconSPI spi : getInstances()) {
				int spiScore = spi.canProvideIconScore(activity);
				if (spiScore > bestScore) {
					bestSPI = spi;
					bestScore = spiScore;
				}
			}
			if (bestSPI != null) {
				icon = bestSPI.getIcon(activity);
				iconsMap.put(activity, icon);
				return icon;
			}
			else{
				return null;
			}
		} else {
			return icon;
		}
	}

}
