/**
 * 
 */
package net.sf.taverna.t2.workbench.views.graph.menu;

import static java.awt.event.KeyEvent.VK_I;
import static javax.swing.Action.MNEMONIC_KEY;
import static net.sf.taverna.t2.ui.menu.DefaultMenuBar.DEFAULT_MENU_BAR;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenu;

/**
 * @author alanrw
 */
public class InsertMenu extends AbstractMenu {
	public static final URI INSERT = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#insert");

	public InsertMenu() {
		super(DEFAULT_MENU_BAR, 64, INSERT, makeAction());
	}

	public static DummyAction makeAction() {
		DummyAction action = new DummyAction("Insert");
		action.putValue(MNEMONIC_KEY, VK_I);
		return action;
	}
}
