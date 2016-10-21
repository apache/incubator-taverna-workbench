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
package org.apache.taverna.activities.stringconstant.servicedescriptions;

import java.net.URI;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.taverna.workbench.activityicons.ActivityIconSPI;

/**
 * @author Alex Nenadic
 */
public class StringConstantActivityIcon implements ActivityIconSPI {
	private static final URI ACTIVITY_TYPE = URI
			.create("http://ns.taverna.org.uk/2010/activity/constant");
	private static Icon icon = null;

	@Override
	public int canProvideIconScore(URI activityType) {
		if (activityType.equals(ACTIVITY_TYPE))
			return DEFAULT_ICON + 1;
		else
			return NO_ICON;
	}

	@Override
	public Icon getIcon(URI activityType) {
		return getStringConstantIcon();
	}

	public static Icon getStringConstantIcon() {
		if (icon == null)
			icon = new ImageIcon(
					StringConstantActivityIcon.class
							.getResource("/stringconstant.png"));
		return icon;
	}
}
