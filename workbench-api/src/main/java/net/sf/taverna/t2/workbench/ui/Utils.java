package net.sf.taverna.t2.workbench.ui;

import java.awt.Component;
import java.awt.Frame;

public class Utils {
	public static Frame getParentFrame(Component container) {
		while (container != null && !(container instanceof Frame)) {
			container = container.getParent();
		}
		// Must be null or a Frame
		return (Frame) container;
	}

}
