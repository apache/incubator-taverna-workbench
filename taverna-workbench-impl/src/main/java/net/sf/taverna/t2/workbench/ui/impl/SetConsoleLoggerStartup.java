package net.sf.taverna.t2.workbench.ui.impl;

import static org.apache.log4j.Level.ERROR;
import static org.apache.log4j.Level.WARN;

import java.io.PrintStream;

import net.sf.taverna.t2.workbench.StartupSPI;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;

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
