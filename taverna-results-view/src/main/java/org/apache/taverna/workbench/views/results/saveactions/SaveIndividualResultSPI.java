package org.apache.taverna.workbench.views.results.saveactions;

import java.nio.file.Path;

import javax.swing.AbstractAction;

/**
 * Defines an interface for various actions for saving results of a workflow
 * run. Path to a single result data is contained inside a MutableTreeNode,
 * which can be used by actions that only want to save the current result. The
 * interface also contains a list of output ports that can be used to
 * dereference all outputs, for actions wishing to save a all results (e.g. in
 * different formats).
 * 
 * @author Alex Nenadic
 * @author David Withers
 */
public interface SaveIndividualResultSPI {
	/**
	 * Sets the Path pointing to the result to be saved.
	 */
	void setResultReference(Path reference);

	/**
	 * Returns the save result action implementing this interface.
	 */
	AbstractAction getAction();
}
