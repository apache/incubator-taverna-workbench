package net.sf.taverna.t2.workbench.activitytools;

import java.awt.Frame;
import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workbench.ui.Utils;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public abstract class AbstractConfigureActivityMenuAction<ActivityClass extends Activity<?>>
		extends AbstractContextualMenuAction {

	private static final URI configureSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/configureSection");

	protected final Class<ActivityClass> activityClass;

	public AbstractConfigureActivityMenuAction(
			Class<ActivityClass> activityClass) {
		super(configureSection, 10);
		this.activityClass = activityClass;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && findActivity() != null;
	}

	protected ActivityClass findActivity() {
		if (getContextualSelection() == null) {
			return null;
		}
		Object selection = getContextualSelection().getSelection();
		if (activityClass.isInstance(selection)) {
			return activityClass.cast(selection);
		}
		if (selection instanceof Processor) {
			Processor processor = (Processor) selection;
			for (Activity<?> activity : processor.getActivityList()) {
				if (activityClass.isInstance(activity)) {
					return activityClass.cast(activity);
				}
			}
		}
		return null;
	}

	protected Frame getParentFrame() {
		return Utils.getParentFrame(getContextualSelection()
				.getRelativeToComponent());
	}

	protected void addMenuDots(Action configAction) {
		String oldName = (String) configAction.getValue(Action.NAME);
		if (!oldName.endsWith("..")) {
			configAction.putValue(Action.NAME, oldName + "...");
		}
	}

}