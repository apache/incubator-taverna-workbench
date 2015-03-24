package net.sf.taverna.t2.activities.dataflow.servicedescriptions;

import java.net.URI;

import javax.swing.Icon;

import net.sf.taverna.t2.servicedescriptions.AbstractTemplateService;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import org.apache.taverna.scufl2.api.configurations.Configuration;

public class DataflowTemplateService extends AbstractTemplateService {

	public static final URI ACTIVITY_TYPE = URI.create("http://ns.taverna.org.uk/2010/activity/nested-workflow");

	private static final String A_CONFIGURABLE_NESTED_WORKFLOW = "A service that allows you to have one workflow nested within another";
	private static final String DATAFLOW = "Nested workflow";

	private static final URI providerId = URI.create("http://taverna.sf.net/2010/service-provider/dataflow");

	@Override
	public URI getActivityType() {
		return ACTIVITY_TYPE;
	}

	@Override
	public Configuration getActivityConfiguration() {
		Configuration configuration = new Configuration();
		configuration.setType(ACTIVITY_TYPE.resolve("#Config"));
		return configuration;
	}

	@Override
	public Icon getIcon() {
		return DataflowActivityIcon.getDataflowIcon();
	}

	public String getName() {
		return DATAFLOW;
	}

	public String getDescription() {
		return A_CONFIGURABLE_NESTED_WORKFLOW;
	}

	public static ServiceDescription getServiceDescription() {
		DataflowTemplateService dts = new DataflowTemplateService();
		return dts.templateService;
	}

	public String getId() {
		return providerId.toString();
	}

}
