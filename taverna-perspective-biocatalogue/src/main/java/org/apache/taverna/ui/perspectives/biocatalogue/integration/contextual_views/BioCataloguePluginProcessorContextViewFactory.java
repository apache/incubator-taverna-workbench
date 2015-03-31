package org.apache.taverna.ui.perspectives.biocatalogue.integration.contextual_views;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import org.apache.taverna.biocatalogue.model.SoapOperationIdentity;
import org.apache.taverna.ui.perspectives.biocatalogue.integration.Integration;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import org.apache.taverna.workflowmodel.Processor;

public class BioCataloguePluginProcessorContextViewFactory implements
		ContextualViewFactory<Processor> {
  
	public boolean canHandle(Object selection)
	{
		// TODO - HACK: this would stop showing the contextual view in case of any error,
	  //        not just in case of unsupported contextual selection; this needs to be
	  //        changed, so that useful error messages are still displayed in the
	  //        contextual view
	  if (selection instanceof Processor)
	  {
	    SoapOperationIdentity opId = Integration.extractSoapOperationDetailsFromProcessor((Processor) selection);
	    boolean canHandleSelection = !opId.hasError();
		  if (!canHandleSelection) {
	      Logger.getLogger(BioCataloguePluginProcessorContextViewFactory.class).debug(
	          "Service's contextual view not shown due to some condition: " + opId.getErrorDetails());
	    }
		  
		  return (canHandleSelection);
	  }
	  else {
	    return (false);
	  }
	}
	
	public List<ContextualView> getViews(Processor selection) {
		return Arrays.<ContextualView>asList(new ProcessorView(selection));
	}

}
