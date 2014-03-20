package net.sf.taverna.t2.workbench.file.translator;

import net.sf.taverna.t2.workbench.StartupSPI;
import net.sf.taverna.utils.MyGridConfiguration;

public class PrepareLoggerStartupSpi implements StartupSPI {

	public int positionHint() {
		// Before SetConsoleLoggerStartup 
		// -- otherwise Taverna 1 libraries will cause double logging
		return 5;
	}

	public boolean startup() {
		// Causes Log4jConfiguration to be initialised
		System.setProperty("taverna.log4jInitialized", Boolean.toString(true));
		MyGridConfiguration.getInstance();
		return true;
	}

}
