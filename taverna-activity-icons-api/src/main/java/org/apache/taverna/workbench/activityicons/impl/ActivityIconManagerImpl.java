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

package org.apache.taverna.workbench.activityicons.impl;

import static org.apache.taverna.workbench.activityicons.ActivityIconSPI.NO_ICON;

import java.net.URI;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.Icon;

import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.workbench.activityicons.ActivityIconSPI;

/**
 * Manager for activities' icons.
 *
 * @author Alex Nenadic
 * @author Alan R Williams
 */
public class ActivityIconManagerImpl implements ActivityIconManager {
	/** Cache of already obtained icons; maps activities to their icons*/
	private WeakHashMap<URI, Icon> iconsMap = new WeakHashMap<>();

	private List<ActivityIconSPI> activityIcons;

	/** Returns an icon for the Activity. */
	@Override
	public Icon iconForActivity(URI activityType) {
		Icon icon = iconsMap.get(activityType);
		if (icon != null)
			return icon;
		int bestScore = NO_ICON;
		ActivityIconSPI bestSPI = null;
		for (ActivityIconSPI spi : activityIcons) {
			int spiScore = spi.canProvideIconScore(activityType);
			if (spiScore > bestScore) {
				bestSPI = spi;
				bestScore = spiScore;
			}
		}
		if (bestSPI == null)
			return null;
		icon = bestSPI.getIcon(activityType);
		iconsMap.put(activityType, icon);
		return icon;
	}

	@Override
	public Icon iconForActivity(Activity activity) {
		return iconForActivity(activity.getType());
	}

	@Override
	public void resetIcon(URI activityType) {
		Icon icon = iconsMap.get(activityType);
		if (icon != null)
			iconsMap.remove(activityType);
		iconForActivity(activityType);
	}

	public void setActivityIcons(List<ActivityIconSPI> activityIcons) {
		this.activityIcons = activityIcons;
	}
}
