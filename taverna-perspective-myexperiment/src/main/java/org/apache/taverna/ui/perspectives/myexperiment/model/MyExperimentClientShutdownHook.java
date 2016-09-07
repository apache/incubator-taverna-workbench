package org.apache.taverna.ui.perspectives.myexperiment.model;

import org.apache.log4j.Logger;
import org.apache.taverna.ui.perspectives.myexperiment.model.MyExperimentClient;
import org.apache.taverna.workbench.ShutdownSPI;

/**
 * @author Sergejs Aleksejevs, Jiten Bhagat
 */

public class MyExperimentClientShutdownHook implements ShutdownSPI {

  
  private MyExperimentClient myExperimentClient;
  private Logger logger = Logger.getLogger(MyExperimentClientShutdownHook.class);

  public int positionHint() {
	// all custom plugins are suggested to return a value of > 100;
	// this affects when in the termination process will this plugin
	// be shutdown;
	return 100;
  }

  public boolean shutdown() {
	  if (myExperimentClient == null) {
		  // no myExperimentClient yet, all done
		  return true;
	  }
	// find instance of main component of the running myExperiment perspective
	  logger.debug("Starting shutdown operations for myExperiment plugin");

	  try {
		  myExperimentClient.storeHistoryAndSettings();
	  } catch (Exception e) {
		logger.error("Failed while serializing myExperiment plugin settings:\n"
			+ e);
	  }

	  logger.debug("myExperiment plugin shutdown is completed; terminated...");

	// "true" means that shutdown operations are complete and Taverna can terminate
	return true;
  }


	public void setMyExperimentClient(MyExperimentClient myExperimentClient) {
		this.myExperimentClient = myExperimentClient;
	}

	public MyExperimentClient getMyExperimentClient() {
		return myExperimentClient;
	}


}
