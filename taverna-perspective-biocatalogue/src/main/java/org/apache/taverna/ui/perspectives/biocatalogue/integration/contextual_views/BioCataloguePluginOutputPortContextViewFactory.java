package org.apache.taverna.ui.perspectives.biocatalogue.integration.contextual_views;

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
