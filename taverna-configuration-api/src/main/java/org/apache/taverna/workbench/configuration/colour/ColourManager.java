/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.configuration.colour;

import java.awt.Color;

import uk.org.taverna.configuration.Configurable;

/**
 * @author David Withers
 */
public interface ColourManager extends Configurable {
	/**
	 * Builds a Color that has been configured and associated with the given
	 * String (usually an object type).
	 * 
	 * @return the associated Color, or if nothing is associated returns
	 *         {@link Color#WHITE}.
	 */
	Color getPreferredColour(String itemKey);

	void setPreferredColour(String itemKey, Color colour);
}