package net.sf.taverna.t2.servicedescriptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public abstract class AbstractTemplateService<ConfigType> implements
		ServiceDescriptionProvider {

	protected TemplateServiceDescription templateService = new TemplateServiceDescription();

	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		callBack.partialResults(Collections.singleton(templateService));
		callBack.finished();
	}

	public abstract Icon getIcon();

	public abstract Class<? extends Activity<ConfigType>> getActivityClass();

	public abstract ConfigType getActivityConfiguration();

	public class TemplateServiceDescription extends
			ServiceDescription<ConfigType> {
		public Icon getIcon() {
			return AbstractTemplateService.this.getIcon();
		}

		public String getName() {
			return AbstractTemplateService.this.getName();
		}

		public List<String> getPath() {
			return Arrays.asList(SERVICE_TEMPLATES);
		}

		public boolean isTemplateService() {
			return true;
		}

		@Override
		protected List<Object> getIdentifyingData() {
			// Do it by object identity
			return null;
		}

		@Override
		public Class<? extends Activity<ConfigType>> getActivityClass() {
			return AbstractTemplateService.this.getActivityClass();
		}

		@Override
		public ConfigType getActivityConfiguration() {
			return AbstractTemplateService.this.getActivityConfiguration();
		}
		
		public String getDescription() {
			return AbstractTemplateService.this.getDescription();
		}
	}

	@Override
	public String toString() {
		return "Template service " + getName();
	}

	public String getDescription() {
		// Default to an empty string
		return "";
	}
	
}
