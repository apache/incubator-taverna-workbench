package org.apache.taverna.ui.perspectives.myexperiment.model;
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
