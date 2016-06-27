/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
 * 
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
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
