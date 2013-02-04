package net.sf.taverna.t2.workbench.selection;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import net.sf.taverna.t2.workbench.selection.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.profiles.Profile;

/**
 * Manages workflowBundles, workflows, profiles and perspectives selected on the Workbench.
 *
 * @author David Withers
 */
public interface SelectionManager extends Observable<SelectionManagerEvent> {

	/**
	 * Returns the <code>DataflowSelectionModel</code> for the WorkflowBundle.
	 *
	 * @param workflowBundle
	 *            the WorkflowBundle to return the current selection model for
	 * @return the <code>DataflowSelectionModel</code> for the WorkflowBundle
	 */
	public DataflowSelectionModel getDataflowSelectionModel(WorkflowBundle workflowBundle);

	/**
	 * Removes the <code>DataflowSelectionModel</code> for the WorkflowBundle.
	 *
	 * @param workflowBundle
	 *            the WorkflowBundle to remove the current selection model for
	 */
	public void removeDataflowSelectionModel(WorkflowBundle workflowBundle);

	/**
	 * Returns the currently selected WorkflowBundle.
	 *
	 * @return the currently selected WorkflowBundle
	 */
	public WorkflowBundle getSelectedWorkflowBundle();

	/**
	 * Sets the currently selected WorkflowBundle.
	 *
	 * @param workflowBundle
	 *            the WorkflowBundle to set as currently selected
	 */
	public void setSelectedWorkflowBundle(WorkflowBundle workflowBundle);

	/**
	 * Returns the currently selected Workflow.
	 *
	 * @return the currently selected Workflow
	 */
	public Workflow getSelectedWorkflow();

	/**
	 * Sets the currently selected Workflow.
	 *
	 * @param workflow
	 *            the Workflow to set as currently selected
	 */
	public void setSelectedWorkflow(Workflow workflow);

	/**
	 * Returns the currently selected Profile.
	 *
	 * @return the currently selected Profile
	 */
	public Profile getSelectedProfile();

	/**
	 * Sets the currently selected Profile.
	 *
	 * @param profile
	 *            the Profile to set as currently selected
	 */
	public void setSelectedProfile(Profile profile);

	/**
	 * Returns the currently selected Perspective.
	 *
	 * @return the currently selected Perspective
	 */
	public PerspectiveSPI getSelectedPerspective();

	/**
	 * Sets the currently selected Perspective.
	 * @param perspective
	 *            the Perspective to set as currently selected
	 */
	public void setSelectedPerspective(PerspectiveSPI perspective);

}