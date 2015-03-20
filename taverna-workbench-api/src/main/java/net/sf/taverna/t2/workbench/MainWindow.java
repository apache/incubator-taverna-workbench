package net.sf.taverna.t2.workbench;

import javax.swing.JFrame;

public class MainWindow {
	
	private static JFrame mainWindow;
	
	public static JFrame getMainWindow() {
		return mainWindow;
	}
	
	public static void setMainWindow(JFrame window) {
		mainWindow = window;
	}
}
