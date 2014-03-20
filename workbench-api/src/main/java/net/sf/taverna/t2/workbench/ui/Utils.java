package net.sf.taverna.t2.workbench.ui;

import java.awt.Component;
import java.awt.Frame;

import net.sf.taverna.t2.workbench.MainWindow;

public class Utils {
	public static Frame getParentFrame(Component container) {
		while (container != null && !(container instanceof Frame)) {
			container = container.getParent();
		}
		if (container == null) {
			return MainWindow.getMainWindow();
		}
		// Must be a Frame
		return (Frame) container;
	}

}
