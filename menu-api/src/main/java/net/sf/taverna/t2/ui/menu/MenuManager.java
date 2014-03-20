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
package net.sf.taverna.t2.ui.menu;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.ui.menu.MenuComponent.MenuType;
import net.sf.taverna.t2.ui.menu.MenuManager.MenuManagerEvent;

/**
 * Create {@link JMenuBar}s and {@link JToolBar}s based on SPI instances of
 * {@link MenuComponent}.
 * <p>
 * Elements of menus are discovered automatically using an {@link SPIRegistry}.
 * The elements specify their internal relationship through
 * {@link MenuComponent#getParentId()} and
 * {@link MenuComponent#getPositionHint()}. {@link MenuComponent#getType()}
 * specifies how the component is to be rendered or grouped.
 * </p>
 * <p>
 * The menu manager is {@link Observable}, you can
 * {@link #addObserver(Observer) add an observer} to be notified when the menus
 * have changed, i.e. when {@link #update()} has been called, for instance when
 * the {@link SPIRegistry} (which the menu manager observes) has been updated
 * due to a plugin installation.
 * </p>
 * <p>
 * {@link #createMenuBar()} creates the default menu bar, ie. the menu bar
 * containing all the items with {@link DefaultMenuBar#DEFAULT_MENU_BAR} as
 * their parent. Alternate menu bars can be created using
 * {@link #createMenuBar(URI)}.
 * </p>
 * <p>
 * Similary {@link #createToolBar()} creates the default tool bar, containing
 * the items that has {@link DefaultToolBar#DEFAULT_TOOL_BAR} as their parent.
 * Alternate toolbars can be created using {@link #createToolBar(URI)}.
 * </p>
 * <p>
 * The menu manager keeps weak references to the created (published) menu bars
 * and tool bars, and will attempt to update them when {@link #update()} is
 * called.
 * </p>
 * <p>
 * See the package level documentation for more information about how to specify
 * menu elements.
 * </p>
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class MenuManager implements Observable<MenuManagerEvent> {

	private static MenuManager instance;

	/**
	 * Get the {@link MenuManager} implementation singleton as discovered
	 * through an {@link SPIRegistry}.
	 * 
	 * @throws IllegalStateException
	 *             If no implementation was found.
	 * @return Discovered {@link MenuManager} implementation singleton.
	 */
	public static synchronized MenuManager getInstance()
			throws IllegalStateException {
		if (instance == null) {
			SPIRegistry<MenuManager> registry = new SPIRegistry<MenuManager>(
					MenuManager.class);
			try {
				instance = registry.getInstances().get(0);
			} catch (IndexOutOfBoundsException ex) {
				throw new IllegalStateException(
						"Could not find implementation of " + MenuManager.class);
			}
		}
		return instance;
	}

	/**
	 * Add the items from the list of menu items to the parent menu with
	 * expansion sub-menus if needed.
	 * <p>
	 * If the list contains more than maxItemsInMenu items, a series of
	 * sub-menus will be created and added to the parentMenu instead, each
	 * containing a maximum of maxItemsInMenu items. (Note that if menuItems
	 * contains more than maxItemsInMenu*maxItemsInMenu items, there might be
	 * more than maxItemsInMenu sub-menus added to the parent).
	 * <p>
	 * The sub-menus are titled according to the {@link JMenuItem#getText()} of
	 * the first and last menu item it contains - assuming that they are already
	 * sorted.
	 * <p>
	 * The optional {@link ComponentFactory} headerItemFactory, if not
	 * <code>null</code>, will be invoked to create a header item that will be
	 * inserted on top of the sub-menus. This item does not count towards
	 * maxItemsInMenu.
	 * <p>
	 * Note that this is a utility method that does not mandate the use of the
	 * {@link MenuManager} structure for the menu.
	 * 
	 * @param menuItems
	 *            {@link JMenuItem}s to be inserted
	 * @param parentMenu
	 *            Menu to insert items to
	 * @param maxItemsInMenu
	 *            Maximum number of items in parent menu or created sub-menus
	 * @param headerItemFactory
	 *            If not <code>null</code>, a {@link ComponentFactory} to create
	 *            a header item to insert at top of created sub-menus
	 */
	public abstract void addMenuItemsWithExpansion(List<JMenuItem> menuItems,
			JMenu parentMenu, int maxItemsInMenu,
			ComponentFactory headerItemFactory);

	/**
	 * Create a contextual menu for a selected object.
	 * <p>
	 * Items for the contextual menues are discovered in a similar to fashion as
	 * with {@link #createMenuBar()}, but using {@link DefaultContextualMenu} as
	 * the root.
	 * <p>
	 * Additionally, items implementing {@link ContextualMenuComponent} will be
	 * {@link ContextualMenuComponent#setContextualSelection(Object, Object, Component)
	 * informed} about what is the current selection, as passed to this method.
	 * <p>
	 * Thus, the items can choose if they want to be
	 * {@link MenuComponent#isEnabled() visible} or not for a given selection,
	 * and return an action that is bound it to the selection.
	 * 
	 * @param parent
	 *            The parent object of the selected object, for instance a
	 *            {@link net.sf.taverna.t2.workflowmodel.Dataflow Dataflow}.
	 * @param selection
	 *            The selected object which actions in the contextual menu
	 *            relate to, for instance a Processor
	 * @param relativeToComponent
	 *            A UI component which the returned JPopupMenu (and it's
	 *            actions) is to be relative to, for instance as a parent of
	 *            pop-up dialogues.
	 * @return An empty or populated JPopupMenu depending on the selected
	 *         objects.
	 */
	public abstract JPopupMenu createContextMenu(Object parent,
			Object selection, Component relativeToComponent);

	/**
	 * Create the {@link JMenuBar} containing menu elements defining
	 * {@link DefaultMenuBar#DEFAULT_MENU_BAR} as their
	 * {@link MenuComponent#getParentId() parent}.
	 * <p>
	 * A {@link WeakReference weak reference} is kept in the menu manager to
	 * update the menubar if {@link #update()} is called (manually or
	 * automatically when the SPI is updated).
	 * </p>
	 * 
	 * @return A {@link JMenuBar} populated with the items belonging to the
	 *         default menu bar
	 */
	public abstract JMenuBar createMenuBar();

	/**
	 * Create the {@link JMenuBar} containing menu elements defining the given
	 * <code>id</code> as their {@link MenuComponent#getParentId() parent}.
	 * <p>
	 * Note that the parent itself also needs to exist as a registered SPI
	 * instance og {@link MenuComponent#getType()} equal to
	 * {@link MenuType#menu}, for instance by subclassing {@link AbstractMenu}.
	 * </p>
	 * <p>
	 * A {@link WeakReference weak reference} is kept in the menu manager to
	 * update the menubar if {@link #update()} is called (manually or
	 * automatically when the SPI is updated).
	 * </p>
	 * 
	 * @param id
	 *            The {@link URI} identifying the menu bar
	 * @return A {@link JMenuBar} populated with the items belonging to the
	 *         given parent id.
	 */
	public abstract JMenuBar createMenuBar(URI id);

	/**
	 * Create the {@link JToolBar} containing elements defining
	 * {@link DefaultToolBar#DEFAULT_TOOL_BAR} as their
	 * {@link MenuComponent#getParentId() parent}.
	 * <p>
	 * A {@link WeakReference weak reference} is kept in the menu manager to
	 * update the toolbar if {@link #update()} is called (manually or
	 * automatically when the SPI is updated).
	 * </p>
	 * 
	 * @return A {@link JToolBar} populated with the items belonging to the
	 *         default tool bar
	 */
	public abstract JToolBar createToolBar();

	/**
	 * Create the {@link JToolBar} containing menu elements defining the given
	 * <code>id</code> as their {@link MenuComponent#getParentId() parent}.
	 * <p>
	 * Note that the parent itself also needs to exist as a registered SPI
	 * instance og {@link MenuComponent#getType()} equal to
	 * {@link MenuType#toolBar}, for instance by subclassing
	 * {@link AbstractToolBar}.
	 * </p>
	 * <p>
	 * A {@link WeakReference weak reference} is kept in the menu manager to
	 * update the toolbar if {@link #update()} is called (manually or
	 * automatically when the SPI is updated).
	 * </p>
	 * 
	 * @param id
	 *            The {@link URI} identifying the tool bar
	 * @return A {@link JToolBar} populated with the items belonging to the
	 *         given parent id.
	 */
	public abstract JToolBar createToolBar(URI id);

	/**
	 * Get a menu item identified by the given URI.
	 * <p>
	 * Return the UI {@link Component} last created for a {@link MenuComponent},
	 * through {@link #createMenuBar()}, {@link #createMenuBar(URI)},
	 * {@link #createToolBar()} or {@link #createToolBar(URI)}.
	 * </p>
	 * <p>
	 * For instance, if {@link #createMenuBar()} created a menu bar containing a
	 * "File" menu with {@link MenuComponent#getId()} ==
	 * <code>http://example.com/menu#file</code>, calling:
	 * </p>
	 * 
	 * <pre>
	 * Component fileMenu = getComponentByURI(URI
	 * 		.create(&quot;http://example.com/menu#file&quot;));
	 * </pre>
	 * 
	 * <p>
	 * would return the {@link JMenu} last created for "File". Note that "last
	 * created" could mean both the last call to {@link #createMenuBar()} and
	 * last call to {@link #update()} - which could have happened because the
	 * SPI registry was updated. To be notified when
	 * {@link #getComponentByURI(URI)} might return a new Component because the
	 * menues have been reconstructed, {@link #addObserver(Observer) add an
	 * observer} to the MenuManager.
	 * </p>
	 * <p>
	 * If the URI is unknown, has not yet been rendered as a {@link Component},
	 * or the Component is no longer in use outside the menu manager's
	 * {@link WeakReference weak references}, <code>null</code> is returned
	 * instead.
	 * </p>
	 * 
	 * @see #getURIByComponent(Component)
	 * @param id
	 *            {@link URI} of menu item as returned by
	 *            {@link MenuComponent#getId()}
	 * @return {@link Component} as previously generated by
	 *         {@link #createMenuBar()}/{@link #createToolBar()}, or
	 *         <code>null</code> if the URI is unknown, or if the
	 *         {@link Component} no longer exists.
	 */
	public abstract Component getComponentByURI(URI id);

	/**
	 * Get the URI of the {@link MenuComponent} this menu/toolbar
	 * {@link Component} was created from.
	 * <p>
	 * If the component was created by the MenuManager, through
	 * {@link #createMenuBar()}, {@link #createMenuBar(URI)},
	 * {@link #createToolBar()} or {@link #createToolBar(URI)}, the URI
	 * identifying the defining {@link MenuComponent} is returned. This will be
	 * the same URI as returned by {@link MenuComponent#getId()}.
	 * </p>
	 * <p>
	 * Note that if {@link #update()} has been invoked, the {@link MenuManager}
	 * might have rebuilt the menu structure and replaced the components since
	 * the given <code>component</code> was created. The newest
	 * {@link Component} for the given URI can be retrieved using
	 * {@link #getComponentByURI(URI)}.
	 * </p>
	 * <p>
	 * If the component is unknown, <code>null</code> is returned instead.
	 * </p>
	 * 
	 * @see #getComponentByURI(URI)
	 * @param component
	 *            {@link Component} that was previously created by the
	 *            {@link MenuManager}
	 * @return {@link URI} identifying the menu component, as returned by
	 *         {@link MenuComponent#getId()}, or <code>null</code> if the
	 *         component is unknown.
	 */
	public abstract URI getURIByComponent(Component component);

	/**
	 * Update and rebuild the menu structure.
	 * <p>
	 * Rebuild menu structure as defined by the {@link MenuComponent}s retrieved
	 * from the MenuComponent {@link SPIRegistry}.
	 * </p>
	 * <p>
	 * Rebuilds previously published menubars and toolbars created with
	 * {@link #createMenuBar()}, {@link #createMenuBar(URI)},
	 * {@link #createToolBar()} and {@link #createToolBar(URI)}. Note that the
	 * rebuild will do a removeAll() on the menubar/toolbar, so all components
	 * will be reconstructed. You can use {@link #getComponentByURI(URI)} to
	 * look up individual components within the menu and toolbars.
	 * </p>
	 * <p>
	 * Note that the menu manager is observing the {@link SPIRegistry}, so if a
	 * plugin gets installed and the SPI registry is updated, this update method
	 * will be called by the SPI registry observer.
	 * </p>
	 * <p>
	 * If there are several concurrent calls to {@link #update()}, the calls
	 * from the other thread will return immediately, while the first thread to
	 * get the synchronization lock on the menu manager will do the actual
	 * update. If you want to ensure that {@link #update()} does not return
	 * before the update has been performed fully, synchronize on the menu
	 * manager:
	 * </p>
	 * 
	 * <pre>
	 * MenuManager menuManager = MenuManager.getInstance();
	 * synchronized (menuManager) {
	 * 	menuManager.update();
	 * }
	 * doSomethingAfterUpdateFinished();
	 * </pre>
	 */
	public abstract void update();

	/**
	 * Abstract class for events sent to {@link Observer observers} of the menu
	 * manager.
	 * 
	 * @see UpdatedMenuManagerEvent
	 * @author Stian Soiland-Reyes
	 * 
	 */
	public static abstract class MenuManagerEvent {
	}

	/**
	 * Event sent to observers registered by
	 * {@link net.sf.taverna.t2.ui.menu.MenuManager#addObserver(Observer)} when
	 * the menus have been updated, i.e. when {@link MenuManager#update()} has
	 * been called.
	 */
	public static class UpdatedMenuManagerEvent extends MenuManagerEvent {
	}

	/**
	 * A factory for making {@link Component}s, in particular for making headers
	 * (like JLabels) for
	 * {@link MenuManager#addMenuItemsWithExpansion(List, JMenu, int, ComponentFactory)}
	 */
	public interface ComponentFactory {
		public Component makeComponent();
	}

	

}
