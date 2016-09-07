package org.apache.taverna.workbench.ui.impl;

import java.util.Arrays;

import org.apache.taverna.workbench.ui.Workbench;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Application {

		@Autowired
		Workbench workbench;

	
	    public static void main(String[] args) {
	    	ApplicationContext ctx = SpringApplication.run(Application.class, args);
	        System.out.println("Oooo");

	        System.out.println("Let's inspect the beans provided by Spring Boot:");

	        String[] beanNames = ctx.getBeanDefinitionNames();
	        Arrays.sort(beanNames);
	        for (String beanName : beanNames) {
	            System.out.println(beanName);
	        }	        
	    }
	
}
