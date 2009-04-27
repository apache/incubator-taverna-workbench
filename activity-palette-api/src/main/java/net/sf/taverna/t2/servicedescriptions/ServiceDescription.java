package net.sf.taverna.t2.servicedescriptions;

import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.lang.beans.PropertyAnnotated;
import net.sf.taverna.t2.lang.beans.PropertyAnnotation;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public abstract class ServiceDescription<ConfigType> extends PropertyAnnotated {

	public static final String SERVICE_TEMPLATES = "Service templates";
	private static final String NAME = "Name";
	private static final String SERVICE_CONFIGURATION = "Service configuration";
	private static final String SERVICE_IMPLEMENTATION_CLASS = "Service implementation class";
	private static final String DESCRIPTION = "Description";

	private String description = "";

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof ServiceDescription)) {
			return false;
		}
		List<Object> myIdentifyingData = getIdentifyingData();
		if (myIdentifyingData == null) {
			return super.equals(obj);
		}
		if (! getClass().isInstance(obj) && obj.getClass().isInstance(this)) {
			return false;
		}
		ServiceDescription serviceDescription = (ServiceDescription) obj;
		return myIdentifyingData.equals(serviceDescription.getIdentifyingData());
	}

	@PropertyAnnotation(expert = true, displayName = SERVICE_IMPLEMENTATION_CLASS)
	public abstract Class<? extends Activity<ConfigType>> getActivityClass();

	@PropertyAnnotation(expert = true, displayName = SERVICE_CONFIGURATION)
	public abstract ConfigType getActivityConfiguration();

	@PropertyAnnotation(displayName = DESCRIPTION)
	public String getDescription() {
		return this.description;
	}

	@PropertyAnnotation(expert = true)
	public abstract Icon getIcon();

	@PropertyAnnotation(displayName = NAME)
	public abstract String getName();
	

	@PropertyAnnotation(expert = true)
	@SuppressWarnings("unchecked")
	public abstract List<? extends Comparable> getPath();

	@Override
	public int hashCode() {
		List<Object> identifyingData = getIdentifyingData();
		if (identifyingData == null) {
			return super.hashCode();
		}
		return identifyingData.hashCode();
	}

	@PropertyAnnotation(hidden = true)
	public boolean isTemplateService() {
		return false;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public String toString() {
		return "Service description " +  getName();
	}
	
	protected abstract List<Object> getIdentifyingData();
	
}
