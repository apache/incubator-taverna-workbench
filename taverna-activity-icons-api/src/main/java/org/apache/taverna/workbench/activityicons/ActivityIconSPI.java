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
package org.apache.taverna.workbench.activityicons;

import java.net.URI;

import javax.swing.Icon;

/**
 * Defines an interface for getting an icon for an Activity.
 * 
 * @author Alex Nenadic
 */
public interface ActivityIconSPI {
	/**
	 * A return value for {@link canProvideIconScore()} indicating an SPI cannot
	 * provide an icon for a given activity.
	 */
	int NO_ICON = 0;

	/**
	 * {@link DefaultActivityIcon} returns this value that will be used when an
	 * activity that has no other SPI providing an icon for. Any SPI shour
	 * return value of at least DEFAULT_ICON + 1 if they want to 'override' the
	 * default icon.
	 */
	int DEFAULT_ICON = 10;

	/**
	 * Returns a positive number if the class can provide an icon for the given
	 * activity or {@link NO_ICON} otherwise. Out of two SPIs capable of
	 * providing an icon for the same activity, the one returning a higher score
	 * will be used.
	 */
	int canProvideIconScore(URI activityType);

	/** Returns an icon for the Activity. */
	Icon getIcon(URI activityType);
}
