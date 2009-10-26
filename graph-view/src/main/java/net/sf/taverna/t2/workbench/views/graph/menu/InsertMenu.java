/**
 * 
 */
package net.sf.taverna.t2.workbench.views.graph.menu;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenu;
import net.sf.taverna.t2.ui.menu.DefaultMenuBar;

/**
 * @author alanrw
 *
 */
public class InsertMenu extends AbstractMenu {
	
	public static final URI INSERT = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#insert");

	public InsertMenu() {
		super(DefaultMenuBar.DEFAULT_MENU_BAR, 64, INSERT, "Insert");
	}


}
