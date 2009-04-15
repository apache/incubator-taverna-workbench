package net.sf.taverna.t2.servicedescriptions;

import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public interface ServiceDescription<ConfigType> {
	
	public static final String SERVICE_TEMPLATES = "Service templates";
	
	public Class<? extends Activity<ConfigType>> getActivityClass();

	public ConfigType getActivityConfiguration();

	public String getName();
	
	public Icon getIcon();
	
	@SuppressWarnings("unchecked")
	public List<? extends Comparable> getPath();
	
	
	public boolean isTemplateService();
	
	@Override
	public boolean equals(Object obj);
	
	@Override
	public int hashCode();
	
}
