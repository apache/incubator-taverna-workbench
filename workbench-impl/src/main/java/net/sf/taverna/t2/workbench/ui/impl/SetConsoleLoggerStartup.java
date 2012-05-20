package net.sf.taverna.t2.workbench.ui.impl;

import java.io.PrintStream;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import net.sf.taverna.t2.workbench.StartupSPI;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;

public class SetConsoleLoggerStartup implements StartupSPI {

	private static final PrintStream originalErr = System.err;
	private static final PrintStream originalOut = System.out;

	private final WorkbenchConfiguration workbenchConfiguration;

	public SetConsoleLoggerStartup(WorkbenchConfiguration workbenchConfiguration) {
		this.workbenchConfiguration = workbenchConfiguration;
	}

	public int positionHint() {
		// Must be <b>after</b> PrepareLoggerStarup in file-translator
		// -- otherwise Taverna 1 libraries will cause double logging
		return 10;
	}

	public boolean startup() {
		setSystemOutCapture();
		return true;
	}

	public void setSystemOutCapture() {
		if (! workbenchConfiguration.getCaptureConsole()) {
			System.setOut(originalOut);
			System.setErr(originalErr);
			return;
		}
		Logger systemOutLogger = Logger.getLogger("System.out");
		Logger systemErrLogger = Logger.getLogger("System.err");

		try {
			// This logger stream not loop with log4j > 1.2.13, which has getFollow method
			ConsoleAppender.class.getMethod("getFollow");
			System.setOut(new LoggerStream(systemOutLogger, Level.WARN, originalOut));
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
			System.err.println("Not capturing System.out, use log4j >= 1.2.13");
		}

		System.setErr(new LoggerStream(systemErrLogger, Level.ERROR, originalErr));
	}

}
