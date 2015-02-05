package net.sf.taverna.t2.servicedescriptions;

import static java.util.Collections.singleton;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import uk.org.taverna.scufl2.api.configurations.Configuration;

public abstract class AbstractTemplateService implements
		ServiceDescriptionProvider {
	protected TemplateServiceDescription templateService = new TemplateServiceDescription();

	@Override
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		callBack.partialResults(singleton(templateService));
		callBack.finished();
	}

	@Override
	public abstract Icon getIcon();

	public URI getActivityType() {
		return null;
	}

	public abstract Configuration getActivityConfiguration();

	public class TemplateServiceDescription extends ServiceDescription {
		@Override
		public Icon getIcon() {
			return AbstractTemplateService.this.getIcon();
		}

		@Override
		public String getName() {
			return AbstractTemplateService.this.getName();
		}

		@Override
		public List<String> getPath() {
			return Arrays.asList(SERVICE_TEMPLATES);
		}

		@Override
		public boolean isTemplateService() {
			return true;
		}

		@Override
		protected List<Object> getIdentifyingData() {
			// Do it by object identity
			return null;
		}

		@Override
		public URI getActivityType() {
			return AbstractTemplateService.this.getActivityType();
		}

		@Override
		public Configuration getActivityConfiguration() {
			return AbstractTemplateService.this.getActivityConfiguration();
		}

		@Override
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
