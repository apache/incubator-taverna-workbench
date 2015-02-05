/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
/**
 * An {@link net.sf.taverna.t2.spi.SPIRegistry SPI} based system for creating
 * {@link javax.swing.JMenuBar menues} and {@link javax.swing.JToolBar toolbars}.
 * <p>
 * Each element of a menu and/or toolbar is created by making an SPI
 * implementation class of {@link net.sf.taverna.t2.ui.menu.MenuComponent} and listing the fully qualified
 * class name in the SPI description resource file
 * <code>/META-INF/services/net.sf.taverna.t2.ui.menu.MenuComponent</code>
 * </p>
 * <p>
 * The {@link net.sf.taverna.t2.ui.menu.MenuManager} discovers all menu components using an SPI registry,
 * and builds the {@link javax.swing.JMenuBar menu bar} using
 * {@link net.sf.taverna.t2.ui.menu.MenuManager#createMenuBar()} or the
 * {@link javax.swing.JToolBar toolbar} using
 * {@link net.sf.taverna.t2.ui.menu.MenuManager#createToolBar()}.
 * </p>
 * <p>
 * This allows plugins to provide actions (menu items) and submenues that can be
 * inserted at any points in the generated menu. All parts of the menues are
 * described through a parent/child relationship using
 * {@link net.sf.taverna.t2.ui.menu.MenuComponent#getId()} and {@link net.sf.taverna.t2.ui.menu.MenuComponent#getParentId()}. The
 * components are identified using {@link java.net.URI}s to avoid compile time
 * dependencies, so a plugin can for instance add something to the existing
 * "Edit" menu without depending on the actual implementation of the
 * {@link net.sf.taverna.t2.ui.menu.MenuComponent} describing "Edit", as long as it refers to the same
 * URI. The use of URIs instead of pure strings is to encourage the use of
 * unique identifiers, for instance plugins should use an URI base that is
 * derived from their package name to avoid collision with other plugins.
 * </p>
 * <p>
 * A set of abstract classes, with a common parent {@link net.sf.taverna.t2.ui.menu.AbstractMenuItem},
 * make it more convenient to create simple SPI implementations. Two default top
 * level implementations {@link net.sf.taverna.t2.ui.menu.DefaultMenuBar} and {@link net.sf.taverna.t2.ui.menu.DefaultToolBar} can
 * be used as parents for items that are to be included in
 * {@link net.sf.taverna.t2.ui.menu.MenuManager#createMenuBar()} and {@link net.sf.taverna.t2.ui.menu.MenuManager#createToolBar()},
 * but it's possible to have other parents - such menu trees would have to be
 * created by providing the URI of the top level parent to
 * {@link net.sf.taverna.t2.ui.menu.MenuManager#createMenuBar(URI)} or
 * {@link net.sf.taverna.t2.ui.menu.MenuManager#createToolBar(URI)}.
 * </p> 
 * <p>
 * In the simplest form a menu structure can be built by subclassing
 * {@link net.sf.taverna.t2.ui.menu.AbstractMenu} and {@link net.sf.taverna.t2.ui.menu.AbstractMenuAction}, but more complex menus
 * can be built by including submenus (AbstractMenu with an AbstractMenu as a
 * parent), grouping similar actions in a {@link net.sf.taverna.t2.ui.menu.AbstractMenuSection section},
 * or making {@link net.sf.taverna.t2.ui.menu.AbstractMenuToggle toggle actions} and
 * {@link net.sf.taverna.t2.ui.menu.AbstractMenuOptionGroup option groups}. You can add arbitrary "real"
 * {@link javax.swing.JMenuBar} / {@link javax.swing.JToolBar} compatible items
 * (such as {@link javax.swing.JMenu}s, {@link javax.swing.JMenuItem}s and
 * {@link javax.swing.JButton}s) using
 * {@link net.sf.taverna.t2.ui.menu.AbstractMenuCustom custom menu items}.
 * </p>
 * 
 * <p>
 * Example showing how <code>File-&gt;Open</code> could be implemented using
 * two SPI implementations net.sf.taverna.t2.ui.perspectives.hello.FileMenu and
 * net.sf.taverna.t2.ui.perspectives.hello.FileOpenAction:
 * </p>
 * 
 * <pre>
 * package net.sf.taverna.t2.ui.perspectives.hello;
 * 
 * import java.net.URI;
 * 
 * import net.sf.taverna.t2.ui.menu.AbstractMenu;
 * import net.sf.taverna.t2.ui.menu.DefaultMenuBar;
 * 
 * public class FileMenu extends AbstractMenu {
 * 
 * 	private static final URI FILE_URI = URI
 * 			.create(&quot;http://taverna.sf.net/2008/t2workbench/test#file&quot;);
 * 
 * 	public FileMenu() {
 * 		super(DefaultMenuBar.DEFAULT_MENU_BAR, 10, FILE_URI, &quot;File&quot;);
 * 	}
 * 
 * }
 * </pre>
 * <pre>
 * package net.sf.taverna.t2.ui.perspectives.hello;
 * 
 * import java.awt.event.ActionEvent;
 * import java.net.URI;
 * 
 * import javax.swing.AbstractAction;
 * import javax.swing.Action;
 * import javax.swing.JOptionPane;
 * 
 * import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
 * 
 * public class FileOpenAction extends AbstractMenuAction {
 *     public FileOpenAction() {
 *         super(URI.create(&quot;http://taverna.sf.net/2008/t2workbench/test#file&quot;),
 *                 20);
 *     }
 * 
 *     &#064;Override
 *     public Action createAction() {
 *         return new AbstractAction(&quot;Open&quot;) {
 *             public void actionPerformed(ActionEvent arg0) {
 *                 JOptionPane.showMessageDialog(null, &quot;Open&quot;);
 *             }
 *         };
 *     }
 * }
 * </pre>
 * 
 * <p>
 * The implementation of the {@link net.sf.taverna.t2.ui.menu.MenuManager} itself is discovered by an
 * internal SPI registry through {@link net.sf.taverna.t2.ui.menu.MenuManager#getInstance()}. The menu
 * manager is observing the SPI registry, so that any updates to the registry
 * from installing plugins etc. are reflected in an automatic rebuild of the
 * menus. This update can also be triggered manually by calling
 * {@link net.sf.taverna.t2.ui.menu.MenuManager#update()}.
 * </p>
 * 
 * @author Stian Soiland-Reyes
 * 
 */
package net.sf.taverna.t2.ui.menu;

