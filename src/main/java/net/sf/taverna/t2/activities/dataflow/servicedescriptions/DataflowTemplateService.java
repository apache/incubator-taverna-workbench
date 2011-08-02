package net.sf.taverna.t2.activities.dataflow.servicedescriptions;

import java.net.URI;

import javax.swing.Icon;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.servicedescriptions.AbstractTemplateService;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edits;

public class DataflowTemplateService extends AbstractTemplateService<Dataflow>{

	private static final String A_CONFIGURABLE_NESTED_WORKFLOW = "A service that allows you to have one workflow nested within another";
	private static final String DATAFLOW = "Nested workflow";

	private static final URI providerId = URI.create("http://taverna.sf.net/2010/service-provider/dataflow");
	private Edits edits;

	@Override
	public Class<DataflowActivity> getActivityClass() {
		return DataflowActivity.class;
	}

	@Override
	public Dataflow getActivityConfiguration() {
		return edits.createDataflow();
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
