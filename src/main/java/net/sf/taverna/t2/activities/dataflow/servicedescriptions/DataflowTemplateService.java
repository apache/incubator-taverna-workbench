package net.sf.taverna.t2.activities.dataflow.servicedescriptions;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.dataflow.query.DataflowActivityItem;
import net.sf.taverna.t2.servicedescriptions.AbstractTemplateService;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;

public class DataflowTemplateService extends AbstractTemplateService<Dataflow>{
	
	private static final String DATAFLOW = "Nested workflow";

	@Override
	public Class<DataflowActivity> getActivityClass() {
		return DataflowActivity.class;
	}

	@Override
	public Dataflow getActivityConfiguration() {
		return EditsRegistry.getEdits().createDataflow();
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(DataflowActivityItem.class.getResource("/dataflow.png"));
	}

	public String getName() {
		return DATAFLOW;
	}

}
