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

package org.apache.taverna.workbench;

import java.util.Comparator;

/**
 * SPI for components that want to be notified when the workbench is about to be
 * shutdown.
 * <p>
 * Components should implement this if they need to delay the shutdown while
 * they finish a task, e.g. writing out cached data to disk.
 * <p>
 * <b>NB</b> There is no guarantee that the workbench will actually shut down as
 * the user may decide to abort the shutdown.
 *
 * @see ShutdownSPI
 * @author David Withers
 * @author Stian Soiland-Reyes
 */
public interface ShutdownSPI {

	/**
	 * Called when the workbench is about to shutdown. Implementations should
	 * block until they are ready for the shutdown process to proceed. If the
	 * shutdown for a component will take more than a few seconds a dialog
	 * should inform the user, possibly allowing the user to cancel the shutdown
	 * task for the component and/or the workbench shutdown process.
	 * <p>
	 * When the shutdown process has finished (or the user has canceled it) this
	 * method should return with a vale of <code>true</code>.
	 * <p>
	 * Only return <code>false</code> if the user has chosen to abort the
	 * workbench shutdown process.
	 *
	 * @return
	 */
	public boolean shutdown();

	/**
	 * Provides a hint for the position in the shutdown sequence that shutdown
	 * should be called. The lower the number the earlier shutdown will be
	 * called.
	 * <p>
	 * Custom plugins are recommended to start with a value < 100.
	 */
	public int positionHint();

	public class ShutdownComparator implements Comparator<ShutdownSPI> {
		public int compare(ShutdownSPI o1, ShutdownSPI o2) {
			return o1.positionHint() - o2.positionHint();
		}
	}
}
