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
package org.apache.taverna.workbench;

import java.util.Comparator;

import org.apache.taverna.workbench.ui.zaria.PerspectiveSPI;

/**
 * SPI for components/plugins that want to be able to perform some configuration
 * or similar initialization on Workbench startup.
 *
 * @see ShutdownSPI
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 */
public interface StartupSPI {

	/**
	 * Called when the Workbench is starting up for implementations of this
	 * interface to perform any configuration on start up.
	 * <p>
	 * When the configuration process has finished this method should return
	 * <code>true</code>.
	 * <p>
	 * Return <code>false</code> if and only if failure in this method will
	 * cause Workbench not to function at all.
	 *
	 */
	public boolean startup();

	/**
	 * Provides a hint for the order in which the startup hooks (that implement
	 * this interface) will be called. The lower the number, the earlier will
	 * the startup hook be invoked.
	 * <p>
	 * Custom plugins are recommended to start with a value > 100.
	 */
	public int positionHint();

	public class StartupComparator implements Comparator<StartupSPI> {
		public int compare(StartupSPI o1, StartupSPI o2) {
			return o1.positionHint() - o2.positionHint();
		}
	}

}
