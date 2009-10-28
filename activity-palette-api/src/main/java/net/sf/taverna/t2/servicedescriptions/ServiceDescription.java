package net.sf.taverna.t2.servicedescriptions;

import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.lang.beans.PropertyAnnotation;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.apache.log4j.Logger;

public abstract class ServiceDescription<ConfigType> extends IdentifiedObject {

	public static final String SERVICE_TEMPLATES = "Service templates";
	private static final String NAME = "Name";
	private static final String SERVICE_CONFIGURATION = "Service configuration";
	private static final String SERVICE_IMPLEMENTATION_CLASS = "Service implementation class";
	private static final String DESCRIPTION = "Description";

	public static final String LOCAL_SERVICES = "Local services";

	private String description = "";

	private static Logger logger = Logger.getLogger(ServiceDescription.class);

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

	@PropertyAnnotation(hidden = true)
	public boolean isTemplateService() {
		return false;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public String toString() {
		return "Service description " + getName();
	}

	/**
	 * Any addition edit that needs to be performed upon insertion of an
	 * instance of the ServiceDescription into the Dataflow withi nthe specified
	 * Processor
	 * 
	 * @param dataflow
	 * @param p
	 * @param a
	 * @return
	 */
	public Edit getInsertionEdit(Dataflow dataflow, Processor p, Activity a) {
		return null;
	}

}
