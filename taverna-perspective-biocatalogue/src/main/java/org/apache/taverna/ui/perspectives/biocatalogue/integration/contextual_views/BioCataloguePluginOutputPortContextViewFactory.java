package org.apache.taverna.ui.perspectives.biocatalogue.integration.contextual_views;
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

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import org.apache.taverna.biocatalogue.model.SoapOperationPortIdentity;
import org.apache.taverna.ui.perspectives.biocatalogue.integration.Integration;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import org.apache.taverna.workflowmodel.processor.activity.ActivityOutputPort;


public class BioCataloguePluginOutputPortContextViewFactory implements
		ContextualViewFactory<ActivityOutputPort> {
  
	public boolean canHandle(Object selection)
	{
		// TODO - HACK: this would stop showing the contextual view in case of any error,
    //        not just in case of unsupported contextual selection; this needs to be
    //        changed, so that useful error messages are still displayed in the
    //        contextual view
    if (selection instanceof ActivityOutputPort)
    {
      SoapOperationPortIdentity portDetails = Integration.
          extractSoapOperationPortDetailsFromActivityInputOutputPort((ActivityOutputPort)selection);
      boolean canHandleSelection = !portDetails.hasError();
      if (!canHandleSelection) {
        Logger.getLogger(BioCataloguePluginProcessorContextViewFactory.class).debug(
            "Output port contextual view not shown due to some condition: " + portDetails.getErrorDetails());
      }
      
      return (canHandleSelection);
    }
    else {
      return (false);
    }
	}
	
	public List<ContextualView> getViews(ActivityOutputPort selection) {
		return Arrays.<ContextualView>asList(new ProcessorOutputPortView(selection));
	}
	
}
