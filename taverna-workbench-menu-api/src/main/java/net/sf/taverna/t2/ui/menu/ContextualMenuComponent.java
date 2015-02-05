package net.sf.taverna.t2.ui.menu;

import java.awt.Component;

/**
 * A contextual menu component.
 * <p>
 * A {@link MenuComponent} that also implements ContextualMenuComponent, when
 * included in a menu tree rooted in the {@link DefaultContextualMenu} and
 * retrieved using
 * {@link MenuManager#createContextMenu(Object, Object, Component)}, will be
 * {@linkplain #setContextualSelection(ContextualSelection) informed} before
 * calls to {@link #isEnabled()} or {@link #getAction()}.
 * <p>
 * In this way the contextual menu item can be visible for only certain
 * selections, and its action can be bound to the current selection.
 * <p>
 * Contextual menu components can be grouped by {@linkplain AbstractMenuSection
 * sections} and {@linkplain AbstractMenu sub-menus}, or directly have the
 * {@link DefaultContextualMenu} as the parent.
 * 
 * @see ContextualSelection
 * @see DefaultContextualMenu
 * @author Stian Soiland-Reyes
 */
public interface ContextualMenuComponent extends MenuComponent {
	/**
	 * Set the contextual selection, or <code>null</code> if there is no current
	 * selection (if the menu item was not included in a contextual menu).
	 * 
	 * @param contextualSelection
	 *            The contextual selection
	 */
	void setContextualSelection(ContextualSelection contextualSelection);
}
