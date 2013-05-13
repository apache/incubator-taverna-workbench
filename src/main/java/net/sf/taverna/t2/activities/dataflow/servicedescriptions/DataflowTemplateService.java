package net.sf.taverna.t2.activities.dataflow.servicedescriptions;

import java.net.URI;

import javax.swing.Icon;

import net.sf.taverna.t2.servicedescriptions.AbstractTemplateService;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.Edits;
import uk.org.taverna.scufl2.api.configurations.Configuration;

public class DataflowTemplateService extends AbstractTemplateService {

	private static final String A_CONFIGURABLE_NESTED_WORKFLOW = "A service that allows you to have one workflow nested within another";
	private static final String DATAFLOW = "Nested workflow";

	private static final URI providerId = URI.create("http://taverna.sf.net/2010/service-provider/dataflow");
	private static final URI NESTED_ACTIVITY = URI.create("http://ns.taverna.org.uk/2010/activity/nested-workflow");

	private Edits edits;

	@Override
	public URI getActivityType() {
		return NESTED_ACTIVITY;
	}

	@Override
	public Configuration getActivityConfiguration() {
		Configuration configuration = new Configuration();
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

	@SuppressWarnings("unchecked")
	public static ServiceDescription getServiceDescription() {
		DataflowTemplateService dts = new DataflowTemplateService();
		return dts.templateService;
	}

	public String getId() {
		return providerId.toString();
	}

	public void setEdits(Edits edits) {
		this.edits = edits;
	}

}
