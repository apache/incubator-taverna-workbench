package net.sf.taverna.t2.workbench.activitytools;

import java.awt.Frame;
import java.net.URI;

import javax.swing.Action;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.profiles.ProcessorBinding;
import uk.org.taverna.scufl2.api.profiles.Profile;

import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workbench.ui.Utils;

public abstract class AbstractConfigureActivityMenuAction extends AbstractContextualMenuAction {

	private static final URI configureSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/configure");

	protected Scufl2Tools scufl2Tools = new Scufl2Tools();

	protected final URI activityType;

	public AbstractConfigureActivityMenuAction(URI activityType) {
		super(configureSection, 50);
		this.activityType = activityType;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && findActivity() != null;
	}

	protected Activity findActivity() {
		if (getContextualSelection() == null) {
			return null;
		}
		Object selection = getContextualSelection().getSelection();
		if (selection instanceof Activity) {
			Activity activity = (Activity) selection;
			if (activity.getConfigurableType().equals(activityType)) {
				return activity;
			}
		}
		if (selection instanceof Processor) {
			Processor processor = (Processor) selection;
			Profile profile = processor.getParent().getParent().getMainProfile();
			for (ProcessorBinding processorBinding : scufl2Tools.processorBindingsForProcessor(processor, profile)) {
				if (processorBinding.getBoundActivity().getConfigurableType().equals(activityType)) {
					return processorBinding.getBoundActivity();
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