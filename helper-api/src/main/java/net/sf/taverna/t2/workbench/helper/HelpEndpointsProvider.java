/**
 * 
 */
package net.sf.taverna.t2.workbench.helper;

import java.net.URL;
import java.util.Map;

/**
 * @author alanrw
 *
 */
public interface HelpEndpointsProvider {
	
	Map<String,URL> getExamples();
	String getHelpId();
	URL getHelpURL();

}
