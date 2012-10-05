package net.sf.taverna.t2.servicedescriptions;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import uk.org.taverna.scufl2.api.configurations.Configuration;

public abstract class AbstractTemplateService implements ServiceDescriptionProvider {

	protected TemplateServiceDescription templateService = new TemplateServiceDescription();

	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		callBack.partialResults(Collections.singleton(templateService));
		callBack.finished();
	}

	public abstract Icon getIcon();

	public URI getActivityURI() {
		return null;
	}

	public abstract Configuration getActivityConfiguration();

	public class TemplateServiceDescription extends ServiceDescription {

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
		public URI getActivityURI() {
			return AbstractTemplateService.this.getActivityURI();
		}

		@Override
		public Configuration getActivityConfiguration() {
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
