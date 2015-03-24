package net.sf.taverna.t2.ui.menu;

import java.awt.Component;

import javax.swing.JPopupMenu;

import org.apache.taverna.scufl2.api.core.Workflow;

/**
 * A contextual selection as passed to a {@link ContextualMenuComponent}.
 * 
 * @author Stian Soiland-Reyes
 */
public class ContextualSelection {
	private final Object parent;
	private final Object selection;
	private final Component relativeToComponent;

	public ContextualSelection(Object parent, Object selection,
			Component relativeToComponent) {
		this.parent = parent;
		this.selection = selection;
		this.relativeToComponent = relativeToComponent;
	}

	/**
	 * The parent object of the selected object, for instance a {@link Workflow}.
	 */
	public Object getParent() {
		return parent;
	}

	/**
	 * The selected object which actions in the contextual menu relate to, for
	 * instance a Processor.
	 */
	public Object getSelection() {
		return selection;
	}

	/**
	 * A UI component which the returned {@link JPopupMenu} (and it's actions)
	 * is to be relative to, for instance as a parent of pop-up dialogues.
	 */
	public Component getRelativeToComponent() {
		return relativeToComponent;
	}
}
