package net.sf.taverna.t2.reference.ui;

import static org.junit.Assert.assertEquals;
import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class TestReferenceGeneration {

	@Ignore("Spring settings are wrong") @Test
	public void testGenerator() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"inMemoryReferenceServiceTestContext.xml");
		ReferenceService rs = (ReferenceService) context
				.getBean("t2reference.service.referenceService");
		String o = "I am a string";
		T2Reference register = rs.register(o, 0, true, null);
		assertEquals(register.getNamespacePart(), "taverna");
	}
}
