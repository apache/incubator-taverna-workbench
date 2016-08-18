package org.apache.taverna.workbench.ui.impl;

import org.apache.taverna.workbench.ui.Workbench;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class WorkbenchLauncher {

	@Autowired
	Workbench workbench;

	@Test
	public void testName() throws Exception {
		Thread.sleep(1000);
		System.out.println(workbench.toString());
	}
	
}
