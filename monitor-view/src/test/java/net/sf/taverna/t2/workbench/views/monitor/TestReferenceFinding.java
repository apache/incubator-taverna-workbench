package net.sf.taverna.t2.workbench.views.monitor;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workbench.reference.config.ReferenceConfiguration;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class TestReferenceFinding {
	
	@Test
	public void findT2ReferenceImpl() {
		String context = ReferenceConfiguration.getInstance().getProperty(
				ReferenceConfiguration.REFERENCE_SERVICE_CONTEXT);
		
			ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
					context);
			ReferenceService referenceService = (ReferenceService) appContext
					.getBean("t2reference.service.referenceService");
			System.out.println(referenceService.getClass().getName());
	}

}
