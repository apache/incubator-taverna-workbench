package net.sf.taverna.t2.ui.perspectives.myexperiment;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class TestJFrameForLocalLaunch {

	/**
	 * This is a simple test class for launching myExperiment perspective
	 * from outside Taverna. At some point it will be not usable anymore,
	 * when proper integration of myExperiment plugin is made.
	 * 
	 * @author Sergejs Aleksejevs
	 */
	public static void main(String[] args)
	{
	  JFrame frame = new JFrame("myExperiment Perspective Test");
	  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	  frame.setMinimumSize(new Dimension(1000, 700));
	  frame.setLocation(300, 150);
	  frame.getContentPane().add(new net.sf.taverna.t2.ui.perspectives.myexperiment.MainComponent());
	  
	  frame.pack();
	  frame.setVisible(true);
	}

}
