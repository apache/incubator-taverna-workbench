package net.sf.taverna.t2.renderers;

import org.apache.batik.bridge.UserAgent;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.log4j.Logger;

public class ErrorCapturingSVGCanvas extends JSVGCanvas {
	
	private static Logger logger = Logger.getLogger(ErrorCapturingSVGCanvas.class);
	
	public ErrorCapturingSVGCanvas() {
		super();
	}
	
	protected UserAgent createUserAgent() {
		return new ErrorCapturingCanvasUserAgent();
	}
	
	protected class ErrorCapturingCanvasUserAgent extends CanvasUserAgent {
		public void displayError(String message) {
			logger.error(message);
			
		}
		
		public void displayError(Exception ex) {
			logger.error(ex);
		}
	}

}
