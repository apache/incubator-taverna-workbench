/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.reference.ui;

import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

import org.springframework.context.ApplicationContext;

public class WorkflowLaunchTestApp {

	private static WorkflowLaunchPanel wlp;
	

	private static ImageIcon workflowThumbnail = new ImageIcon(
			WorkflowLaunchTestApp.class.getResource("/workflow.png"));
	


	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
				"inMemoryReferenceServiceTestContext.xml");
		final ReferenceService referenceService = (ReferenceService) appContext
				.getBean("t2reference.service.referenceService");
		final ReferenceContext refContext = null;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI(referenceService, refContext);
			}
		});
	}

	@SuppressWarnings("serial")
	private static void createAndShowGUI(ReferenceService referenceService,
			ReferenceContext referenceContext) {
		// Create and set up the window.
		JFrame frame = new JFrame("Workflow input builder");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//		wlp = new WorkflowLaunchPanel(referenceService, referenceContext) {
//			@Override
//			public void handleLaunch(Map<String, T2Reference> workflowInputs) {
//				System.out.println("Launch...");
//				for (String inputName : workflowInputs.keySet()) {
//					System.out.println(inputName + " = "
//							+ workflowInputs.get(inputName).toString());
//				}
//			}
//		};
//		wlp.setOpaque(true); // content panes must be opaque
//
//		wlp.setWorkflowDescription("It is very good to be able to put a description of the workflow" +
//				" right here in the code. We'll put quite a long description so we can " +
//				"check that line wrapping actually works as we expect. Note that in some cases" +
//				" the initial window will be very wide because of frame.pack() being" +
//				"called.");
		
		// Should be a passive SVG graph of the dataflow
		wlp.setWorkflowImageComponent(new JLabel(workflowThumbnail));
		
		// Add some inputs
		wlp.addInput("Single item", 0, "Make the inputs", null);
		wlp.addInput("List", 1, "Add a list here, because that's what I mean", "Example value");
		wlp.addInput("List of lists", 2);
		wlp.addInput("List of lists of lists", 3);
		wlp.addInput("Really deep list", 6, 
				"And another really long description that is to " +
				"wrap onto multiple lines once it appears in the tiny little window. " +
				"This can be done by using HTML tags, for instance.", "And an example\nOf a\nlong\nexample");
		
		frame.setContentPane(wlp);

		// Display the window.
		frame.pack();
		frame.setSize(630, 450);
		frame.setVisible(true);
	}
}
