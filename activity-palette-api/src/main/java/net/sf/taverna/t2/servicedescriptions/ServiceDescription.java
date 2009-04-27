package net.sf.taverna.t2.servicedescriptions;

import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.lang.beans.PropertyAnnotated;
import net.sf.taverna.t2.lang.beans.PropertyAnnotation;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public abstract class ServiceDescription<ConfigType> extends PropertyAnnotated {

	private static final String TEXTUAL_DESCRIPTION = "Textual description";
	private static final String SERVICE_IMPLEMENTATION_CLASS = "Service implementation class";
	private static final String SERVICE_CONFIGURATION = "Service configuration";
	private static final String NAME = "Name";
	public static final String SERVICE_TEMPLATES = "Service templates";

	@PropertyAnnotation(expert = true, displayName = SERVICE_IMPLEMENTATION_CLASS)
	public abstract Class<? extends Activity<ConfigType>> getActivityClass();

	@PropertyAnnotation(expert = true, displayName = SERVICE_CONFIGURATION)
	public abstract ConfigType getActivityConfiguration();

	@PropertyAnnotation(displayName = NAME)
	public abstract String getName();

	@PropertyAnnotation(expert = true)
	public abstract Icon getIcon();

	@PropertyAnnotation(expert = true)
	@SuppressWarnings("unchecked")
	public abstract List<? extends Comparable> getPath();

	@PropertyAnnotation(hidden = true)
	public abstract boolean isTemplateService();

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

	private String textualDescription = "";
	
	/**
	 * @param textualDescription the textualDescription to set
	 */
	public void setTextualDescription(String textualDescription) {
		this.textualDescription = textualDescription;
	}

	@PropertyAnnotation(displayName = TEXTUAL_DESCRIPTION)
	public String getTextualDescription() {
		return this.textualDescription;
	}
}
