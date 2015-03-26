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

package org.apache.taverna.workbench.ui.impl;

import static org.apache.log4j.Level.ERROR;
import static org.apache.log4j.Level.WARN;

import java.io.PrintStream;

import org.apache.taverna.workbench.StartupSPI;
import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;

public class SetConsoleLoggerStartup implements StartupSPI {
	private static final PrintStream originalErr = System.err;
	private static final PrintStream originalOut = System.out;

	private final WorkbenchConfiguration workbenchConfiguration;

	public SetConsoleLoggerStartup(WorkbenchConfiguration workbenchConfiguration) {
		this.workbenchConfiguration = workbenchConfiguration;
	}

	@Override
	public int positionHint() {
		/*
		 * Must be <b>after</b> PrepareLoggerStarup in file-translator --
		 * otherwise Taverna 1 libraries will cause double logging
		 */
		return 10;
	}

	@Override
	public boolean startup() {
		setSystemOutCapture();
		return true;
	}

	public void setSystemOutCapture() {
		if (!workbenchConfiguration.getCaptureConsole()) {
			System.setOut(originalOut);
			System.setErr(originalErr);
			return;
		}
		Logger systemOutLogger = Logger.getLogger("System.out");
		Logger systemErrLogger = Logger.getLogger("System.err");

		try {
			/*
			 * This logger stream not loop with log4j > 1.2.13, which has
			 * getFollow method
			 */
			ConsoleAppender.class.getMethod("getFollow");
			System.setOut(new LoggerStream(systemOutLogger, WARN, originalOut));
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
			System.err.println("Not capturing System.out, use log4j >= 1.2.13");
		}

		System.setErr(new LoggerStream(systemErrLogger, ERROR, originalErr));
	}
}
