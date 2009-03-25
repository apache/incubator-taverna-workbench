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
package net.sf.taverna.t2.ui.menu.impl;

import java.awt.Color;
import java.awt.Component;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;

import javax.help.CSH;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.spi.SPIRegistry.SPIRegistryEvent;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.ui.menu.AbstractMenuOptionGroup;
import net.sf.taverna.t2.ui.menu.AbstractMenuSection;
import net.sf.taverna.t2.ui.menu.ContextualMenuComponent;
import net.sf.taverna.t2.ui.menu.ContextualSelection;
import net.sf.taverna.t2.ui.menu.DefaultContextualMenu;
import net.sf.taverna.t2.ui.menu.DefaultMenuBar;
import net.sf.taverna.t2.ui.menu.DefaultToolBar;
import net.sf.taverna.t2.ui.menu.MenuComponent;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.ui.menu.MenuComponent.MenuType;

import org.apache.log4j.Logger;

/**
 * Implementation of {@link MenuManager}.
 * 
 * {@inheritDoc}
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class MenuManagerImpl extends MenuManager {

	private static Logger logger = Logger.getLogger(MenuManagerImpl.class);

	private boolean needsUpdate;
	
	/**
	 * Cache used by {@link #getURIByComponent(Component)}
	 */
	private WeakHashMap<Component, URI> componentToUri;

	/**
	 * {@link MenuElementComparator} used for sorting menu components from the
	 * SPI registry.
	 */
	private MenuElementComparator menuElementComparator = new MenuElementComparator();

	/**
	 * Map of {@link URI} to it's discovered children. Populated by
	 * {@link #findChildren()}.
	 */
	private HashMap<URI, List<MenuComponent>> menuElementTree;

	/**
	 * SPI registry of found {@link MenuComponent} implementations.
	 */
	private SPIRegistry<MenuComponent> menuRegistry = new SPIRegistry<MenuComponent>(
			MenuComponent.class);

	/**
	 * Multicaster to distribute messages to {@link Observer}s of this menu
	 * manager.
	 */
	private MultiCaster<MenuManagerEvent> multiCaster;

	/**
	 * Lock for {@link #update()}
	 */
	private final Object updateLock = new Object();

	/**
	 * True if {@link #doUpdate()} is running, subsequents call to
	 * {@link #update()} will return immediately.
	 */
	private boolean updating;

	/**
	 * Cache used by {@link #getComponentByURI(URI)}
	 */
	private Map<URI, WeakReference<Component>> uriToComponent;

	/**
	 * Map from {@link URI} to defining {@link MenuComponent}. Children are in
	 * {@link #menuElementTree}.
	 */
	private Map<URI, MenuComponent> uriToMenuElement;

	// Note: Not reset by #resetCollections()
	private Map<URI, List<WeakReference<Component>>> uriToPublishedComponents = new HashMap<URI, List<WeakReference<Component>>>();

	private MenuRegistryObserver menuRegistryObserver = new MenuRegistryObserver();

	/**
	 * Construct the MenuManagerImpl. Observes the SPI registry and does an
	 * initial {@link #update()}.
	 */
	public MenuManagerImpl() {
		menuRegistry.addObserver(menuRegistryObserver);
		multiCaster = new MultiCaster<MenuManagerEvent>(this);
		needsUpdate = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addMenuItemsWithExpansion(List<JMenuItem> menuItems,
			JMenu parentMenu, int maxItemsInMenu,
			ComponentFactory headerItemFactory) {
		if (menuItems.size() <= maxItemsInMenu) {
			// Just add them directly
			for (JMenuItem menuItem : menuItems) {
				parentMenu.add(menuItem);
			}
			return;
		}
		int index = 0;
		while (index < menuItems.size()) {
			int toIndex = Math.min(menuItems.size(), index
					+ maxItemsInMenu);
			if (toIndex == menuItems.size() - 1) {
				// Don't leave a single item left for the last subMenu
				toIndex--;
			}
			List<JMenuItem> subList = menuItems.subList(index, toIndex);
			JMenuItem firstItem = subList.get(0);
			JMenuItem lastItem = subList.get(subList.size() - 1);
			JMenu subMenu = new JMenu(firstItem.getText() + " ... "
					+ lastItem.getText());
			if (headerItemFactory != null) {
				subMenu.add(headerItemFactory.makeComponent());
			}
			for (JMenuItem menuItem : subList) {
				subMenu.add(menuItem);
			}
			parentMenu.add(subMenu);
			index = toIndex;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addObserver(Observer<MenuManagerEvent> observer) {
		multiCaster.addObserver(observer);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public JPopupMenu createContextMenu(Object parent, Object selection,
			Component relativeToComponent) {
		ContextualSelection contextualSelection = new ContextualSelection(
				parent, selection, relativeToComponent);
		JPopupMenu popupMenu = new JPopupMenu();
		populateContextMenu(popupMenu,
				DefaultContextualMenu.DEFAULT_CONTEXT_MENU, contextualSelection);
		registerComponent(DefaultContextualMenu.DEFAULT_CONTEXT_MENU,
				popupMenu, true);
		return popupMenu;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JMenuBar createMenuBar() {
		return createMenuBar(DefaultMenuBar.DEFAULT_MENU_BAR);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JMenuBar createMenuBar(URI id) {
		JMenuBar menuBar = new JMenuBar();
		if (needsUpdate) {
			update();
		}
		populateMenuBar(menuBar, id);
		registerComponent(id, menuBar, true);
		return menuBar;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JToolBar createToolBar() {
		return createToolBar(DefaultToolBar.DEFAULT_TOOL_BAR);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JToolBar createToolBar(URI id) {
		JToolBar toolbar = new JToolBar();
		if (needsUpdate) {
			update();
		}
		populateToolBar(toolbar, id);
		registerComponent(id, toolbar, true);
		return toolbar;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Component getComponentByURI(URI id) {
		WeakReference<Component> componentRef = uriToComponent.get(id);
		if (componentRef == null) {
			return null;
		}
		// Might also be null it reference has gone dead
		return componentRef.get();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Observer<MenuManagerEvent>> getObservers() {
		return multiCaster.getObservers();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized URI getURIByComponent(Component component) {
		return componentToUri.get(component);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeObserver(Observer<MenuManagerEvent> observer) {
		multiCaster.removeObserver(observer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update() {
		synchronized (updateLock) {
			if (updating && !needsUpdate) {
				return;
			}
			updating = true;
		}
		try {
			doUpdate();
		} finally {
			synchronized (updateLock) {
				updating = false;
				needsUpdate = false;
			}
		}
	}

	/**
	 * Add a {@link JMenu} to the list of components as described by the menu
	 * component. If there are no children, the menu is not added.
	 * 
	 * @param components
	 *            List of components where to add the created {@link JMenu}
	 * @param menuComponent
	 *            The {@link MenuComponent} definition for this menu
	 * @param isToolbar
	 *            True if the list of components is to be added to a toolbar
	 */
	private void addMenu(List<Component> components,
			MenuComponent menuComponent, MenuOptions menuOptions) {
		URI menuId = menuComponent.getId();
		if (menuOptions.isToolbar()) {
			logger.warn("Can't have menu " + menuComponent
					+ " within toolBar element");
			return;
		}
		MenuOptions childOptions = new MenuOptions(menuOptions);
		List<Component> subComponents = makeComponents(menuId, childOptions);
		if (subComponents.isEmpty()) {
			logger.warn("No sub components found for menu " + menuId);
			return;
		}

		JMenu menu = new JMenu(menuComponent.getAction());
		for (Component menuItem : subComponents) {
			if (menuItem == null) {
				menu.addSeparator();
			} else {
				menu.add(menuItem);
			}
		}
		registerComponent(menuId, menu);
		components.add(menu);
	}

	/**
	 * Add <code>null</code> to the list of components, meaning that a separator
	 * is to be created. Subsequent separators are ignored, and if there are no
	 * components on the list already no separator will be added.
	 * 
	 * @param components
	 *            List of components
	 */
	private void addNullSeparator(List<Component> components) {
		if (components.isEmpty()) {
			// Don't start with a separator
			return;
		}
		if (components.get(components.size() - 1) == null) {
			// Already a separator in last position
			return;
		}
		components.add(null);
	}

	/**
	 * Add an {@link AbstractMenuOptionGroup option group} to the list of
	 * components
	 * 
	 * @param components
	 *            List of components where to add the created {@link JMenu}
	 * @param optionGroupId
	 *            The {@link URI} identifying the option group
	 * @param isToolbar
	 *            True if the option group is to be added to a toolbar
	 */
	private void addOptionGroup(List<Component> components, URI optionGroupId,
			MenuOptions menuOptions) {
		MenuOptions childOptions = new MenuOptions(menuOptions);
		childOptions.setOptionGroup(true);

		List<Component> buttons = makeComponents(optionGroupId, childOptions);
		addNullSeparator(components);
		if (buttons.isEmpty()) {
			logger.warn("No sub components found for option group "
					+ optionGroupId);
			return;
		}
		ButtonGroup buttonGroup = new ButtonGroup();

		for (Component button : buttons) {
			if (button instanceof AbstractButton) {
				buttonGroup.add((AbstractButton) button);
			} else {
				logger.warn("Component of button group " + optionGroupId
						+ " is not an AbstractButton: " + button);
			}
			if (button == null) {
				logger.warn("Separator found within button group");
				addNullSeparator(components);
			} else {
				components.add(button);
			}
		}
		addNullSeparator(components);
	}

	/**
	 * Add a section to a list of components.
	 * 
	 * @param components
	 *            List of components
	 * @param sectionId
	 *            The {@link URI} identifying the section
	 * @param menuOptions
	 *            {@link MenuOptions options} for creating the menu
	 */
	private void addSection(List<Component> components, URI sectionId,
			MenuOptions menuOptions) {
		List<Component> childComponents = makeComponents(sectionId, menuOptions);
		

		MenuComponent sectionDef = uriToMenuElement.get(sectionId);
		Action sectionAction = sectionDef.getAction();
		if (sectionAction != null) {
			String sectionLabel = (String) sectionAction.getValue(Action.NAME);
			if (sectionLabel != null) {
				Color labelColor = (Color) sectionAction.getValue(AbstractMenuSection.SECTION_COLOR);
				if (labelColor == null) {
					labelColor = ShadedLabel.GREEN;
				}
				ShadedLabel label = new ShadedLabel(sectionLabel, labelColor);
				components.add(label);
			} else {
				addNullSeparator(components);
			}
		} else {
			addNullSeparator(components);
		}

		if (childComponents.isEmpty()) {
			logger.warn("No sub components found for section " + sectionId);
			return;
		}
		for (Component childComponent : childComponents) {
			if (childComponent == null) {
				logger.warn("Separator found within section");
				addNullSeparator(components);
			} else {
				components.add(childComponent);
			}
		}
		addNullSeparator(components);
	}

	/**
	 * Remove the last <code>null</code> separator from the list of components
	 * if it's present.
	 * 
	 * @param components
	 *            List of components
	 */
	private void stripTrailingNullSeparator(List<Component> components) {
		if (!components.isEmpty()) {
			int lastIndex = components.size() - 1;
			if (components.get(lastIndex) == null) {
				components.remove(lastIndex);
			}
		}
	}

	/**
	 * Perform the actual update, called by {@link #update()}. Reset all the
	 * collections, refresh from SPI, modify any previously published components
	 * and notify any observers.
	 * 
	 */
	protected synchronized void doUpdate() {
		resetCollections();
		findChildren();
		updatePublishedComponents();
		multiCaster.notify(new UpdatedMenuManagerEvent());
	}

	/**
	 * Find all children for all known menu components. Populates
	 * {@link #uriToMenuElement}.
	 * 
	 */
	protected void findChildren() {
		for (MenuComponent menuElement : menuRegistry.getInstances()) {
			uriToMenuElement.put(menuElement.getId(), menuElement);
			logger.debug("Found menu element " + menuElement.getId() + " "
					+ menuElement);
			if (menuElement.getParentId() == null) {
				continue;
			}
			List<MenuComponent> siblings = menuElementTree.get(menuElement
					.getParentId());
			if (siblings == null) {
				siblings = new ArrayList<MenuComponent>();
				menuElementTree.put(menuElement.getParentId(), siblings);
			}
			siblings.add(menuElement);
		}
		if (uriToMenuElement.isEmpty()) {
			logger.error("No menu elements found, check classpath/Raven/SPI");
		}
	}

	/**
	 * Get the children which have the given URI specified as their parent, or
	 * an empty list if no children exist.
	 * 
	 * @param id
	 *            The {@link URI} of the parent
	 * @return The {@link List} of {@link MenuComponent} which have the given
	 *         parent
	 */
	protected List<MenuComponent> getChildren(URI id) {
		List<MenuComponent> children = menuElementTree.get(id);
		if (children == null) {
			return Collections.<MenuComponent> emptyList();
		}
		Collections.sort(children, menuElementComparator);
		return children;
	}

	/**
	 * Make the list of Swing {@link Component}s that are the children of the
	 * given {@link URI}.
	 * 
	 * @param id
	 *            The {@link URI} of the parent which children are to be made
	 * @param menuOptions
	 *            Options of the created menu, for instance
	 *            {@link MenuOptions#isToolbar()}.
	 * @return A {@link List} of {@link Component}s that can be added to a
	 *         {@link JMenuBar}, {@link JMenu} or {@link JToolBar}.
	 */
	protected List<Component> makeComponents(URI id, MenuOptions menuOptions) {
		List<Component> components = new ArrayList<Component>();
		for (MenuComponent childElement : getChildren(id)) {
			if (childElement instanceof ContextualMenuComponent) {
				((ContextualMenuComponent) childElement)
						.setContextualSelection(menuOptions
								.getContextualSelection());
			}
			// Important - check this AFTER setContextualSelection so the item
			// can change it's enabled-state if needed.
			if (!childElement.isEnabled()) {
				continue;
			}
			MenuType type = childElement.getType();
			Action action = childElement.getAction();
			URI childId = childElement.getId();
			if (type.equals(MenuType.action)) {
				if (action == null) {
					logger.warn("Skipping invalid action " + childId + " for "
							+ id);
					continue;
				}

				Component actionComponent;
				if (menuOptions.isOptionGroup()) {
					if (menuOptions.isToolbar()) {
						actionComponent = new JToggleButton(action);
						toolbarizeButton((AbstractButton) actionComponent);
					} else {
						actionComponent = new JRadioButtonMenuItem(action);
					}
				} else {
					if (menuOptions.isToolbar()) {
						actionComponent = new JButton(action);
						toolbarizeButton((AbstractButton) actionComponent);

					} else {
						actionComponent = new JMenuItem(action);
					}
				}
				registerComponent(childId, actionComponent);
				components.add(actionComponent);
			} else if (type.equals(MenuType.toggle)) {
				if (action == null) {
					logger.warn("Skipping invalid toggle " + childId + " for "
							+ id);
					continue;
				}
				Component toggleComponent;
				if (menuOptions.isToolbar()) {
					toggleComponent = new JToggleButton(action);
				} else {
					toggleComponent = new JCheckBoxMenuItem(action);
				}
				registerComponent(childId, toggleComponent);
				components.add(toggleComponent);
			} else if (type.equals(MenuType.custom)) {
				Component customComponent = childElement.getCustomComponent();
				if (customComponent == null) {
					logger.warn("Skipping null custom component " + childId
							+ " for " + id);
					continue;
				}
				registerComponent(childId, customComponent);
				components.add(customComponent);
			} else if (type.equals(MenuType.optionGroup)) {
				addOptionGroup(components, childId, menuOptions);
			} else if (type.equals(MenuType.section)) {
				addSection(components, childId, menuOptions);
			} else if (type.equals(MenuType.menu)) {
				addMenu(components, childElement, menuOptions);
			} else {
				logger.warn("Skipping invalid/unknown type " + type + " for "
						+ id);
				continue;
			}
		}
		stripTrailingNullSeparator(components);
		return components;
	}

	/**
	 * Fill the specified menu bar with the menu elements that have the given
	 * URI as their parent.
	 * <p>
	 * Existing elements on the menu bar will be removed.
	 * </p>
	 * 
	 * @param menuBar
	 *            The {@link JMenuBar} to update
	 * @param id
	 *            The {@link URI} of the menu bar
	 */
	protected void populateMenuBar(JMenuBar menuBar, URI id) {
		menuBar.removeAll();
		MenuComponent menuDef = uriToMenuElement.get(id);
		if (menuDef == null) {
			throw new IllegalArgumentException("Unknown menuBar " + id);
		}
		if (!menuDef.getType().equals(MenuType.menu)) {
			throw new IllegalArgumentException("Element " + id
					+ " is not a menu, but a " + menuDef.getType());
		}
		MenuOptions menuOptions = new MenuOptions();
		for (Component component : makeComponents(id, menuOptions)) {
			if (component == null) {
				logger.warn("Ignoring separator in menu bar " + id);
			} else {
				menuBar.add(component);
			}
		}
	}

	/**
	 * Fill the specified menu bar with the menu elements that have the given
	 * URI as their parent.
	 * <p>
	 * Existing elements on the menu bar will be removed.
	 * </p>
	 * 
	 * @param popupMenu
	 *            The {@link JPopupMenu} to update
	 * @param id
	 *            The {@link URI} of the menu bar
	 * @param contextualSelection
	 *            The current selection for the context menu
	 */
	protected void populateContextMenu(JPopupMenu popupMenu, URI id,
			ContextualSelection contextualSelection) {
		popupMenu.removeAll();
		MenuComponent menuDef = uriToMenuElement.get(id);
		if (menuDef == null) {
			throw new IllegalArgumentException("Unknown menuBar " + id);
		}
		if (!menuDef.getType().equals(MenuType.menu)) {
			throw new IllegalArgumentException("Element " + id
					+ " is not a menu, but a " + menuDef.getType());
		}
		MenuOptions menuOptions = new MenuOptions();
		menuOptions.setContextualSelection(contextualSelection);
		for (Component component : makeComponents(id, menuOptions)) {
			if (component == null) {
				logger.warn("Ignoring separator in menu bar " + id);
			} else {
				popupMenu.add(component);
			}
		}
	}

	/**
	 * Fill the specified tool bar with the elements that have the given URI as
	 * their parent.
	 * <p>
	 * Existing elements on the tool bar will be removed.
	 * </p>
	 * 
	 * @param toolbar
	 *            The {@link JToolBar} to update
	 * @param id
	 *            The {@link URI} of the tool bar
	 */
	protected void populateToolBar(JToolBar toolbar, URI id) {
		toolbar.removeAll();
		MenuComponent toolbarDef = uriToMenuElement.get(id);
		if (toolbarDef == null) {
			throw new IllegalArgumentException("Unknown toolBar " + id);
		}
		if (!toolbarDef.getType().equals(MenuType.toolBar)) {
			throw new IllegalArgumentException("Element " + id
					+ " is not a toolBar, but a " + toolbarDef.getType());
		}
		if (toolbarDef.getAction() != null) {
			String name = (String) toolbarDef.getAction().getValue(Action.NAME);
			toolbar.setName(name);
		} else {
			toolbar.setName("");
		}
		MenuOptions menuOptions = new MenuOptions();
		menuOptions.setToolbar(true);
		for (Component component : makeComponents(id, menuOptions)) {
			if (component == null) {
				toolbar.addSeparator();
			} else {
				if (component instanceof JButton) {
					JButton toolbarButton = (JButton) component;
					toolbarButton.putClientProperty("hideActionText", true);
				}
				toolbar.add(component);
			}
		}
	}

	/**
	 * Register a component that has been created. Such a component can be
	 * resolved through {@link #getComponentByURI(URI)}.
	 * 
	 * @param id
	 *            The {@link URI} that defined the component
	 * @param component
	 *            The {@link Component} that was created.
	 */
	protected synchronized void registerComponent(URI id, Component component) {
		registerComponent(id, component, false);
	}

	/**
	 * Register a component that has been created. Such a component can be
	 * resolved through {@link #getComponentByURI(URI)}.
	 * 
	 * @param id
	 *            The {@link URI} that defined the component
	 * @param component
	 *            The {@link Component} that was created.
	 * @param published
	 *            <code>true</code> if the component has been published through
	 *            {@link #createMenuBar()} or similar, and is to be
	 *            automatically updated by later calls to {@link #update()}.
	 */
	protected synchronized void registerComponent(URI id, Component component,
			boolean published) {
		uriToComponent.put(id, new WeakReference<Component>(component));
		componentToUri.put(component, id);
		if (published) {
			List<WeakReference<Component>> publishedComponents = uriToPublishedComponents
					.get(id);
			if (publishedComponents == null) {
				publishedComponents = new ArrayList<WeakReference<Component>>();
				uriToPublishedComponents.put(id, publishedComponents);
			}
			publishedComponents.add(new WeakReference<Component>(component));
		}
		setHelpStringForComponent(component, id);
	}

	/**
	 * Reset all collections
	 * 
	 */
	protected synchronized void resetCollections() {
		menuElementTree = new HashMap<URI, List<MenuComponent>>();
		componentToUri = new WeakHashMap<Component, URI>();
		uriToMenuElement = new HashMap<URI, MenuComponent>();
		uriToComponent = new HashMap<URI, WeakReference<Component>>();
	}

	/**
	 * Set javax.help string to identify the component for later references to
	 * the help document. Note that the component (ie. the
	 * {@link AbstractMenuAction} must have an ID for an registration to take
	 * place.
	 * 
	 * @param component
	 *            The {@link Component} to set help string for
	 * @param componentId
	 *            The {@link URI} to be used as identifier
	 */
	protected void setHelpStringForComponent(Component component,
			URI componentId) {
		if (componentId != null) {
			String helpId = componentId.toASCIIString();
			CSH.setHelpIDString(component, helpId);
		}
	}

	/**
	 * Make an {@link AbstractButton} be configured in a "toolbar-like" way, for
	 * instance showing only the icon.
	 * 
	 * @param actionButton
	 *            Button to toolbarise
	 */
	protected void toolbarizeButton(AbstractButton actionButton) {
		Action action = actionButton.getAction();
		if (action.getValue(Action.SHORT_DESCRIPTION) == null) {
			action.putValue(Action.SHORT_DESCRIPTION, action
					.getValue(Action.NAME));
		}
		actionButton.setBorder(new EmptyBorder(0, 2, 0, 2));
		// actionButton.setHorizontalTextPosition(JButton.CENTER);
		// actionButton.setVerticalTextPosition(JButton.BOTTOM);
		if (action.getValue(Action.SMALL_ICON) != null) {
			// Don't show the text
			actionButton.putClientProperty("hideActionText", Boolean.TRUE);
			// Since hideActionText seems to be broken in Java 5
			// and/or OS X
			actionButton.setText(null);
		}
	}

	/**
	 * Update all components that have been published using
	 * {@link #createMenuBar()} and similar. Content of such componenents will
	 * be removed and replaced by fresh components.
	 * 
	 */
	protected void updatePublishedComponents() {
		for (Entry<URI, List<WeakReference<Component>>> entry : uriToPublishedComponents
				.entrySet()) {
			URI id = entry.getKey();
			for (WeakReference<Component> reference : entry.getValue()) {
				Component component = reference.get();
				if (component == null) {
					continue;
				}
				if (component instanceof JToolBar) {
					populateToolBar((JToolBar) component, id);
				} else if (component instanceof JMenuBar) {
					populateMenuBar((JMenuBar) component, id);
				} else {
					logger.warn("Could not update published component " + id
							+ ": " + component.getClass());
				}
			}
		}
	}

	/**
	 * {@link Comparator} that can order {@link MenuComponent}s by their
	 * {@link MenuComponent#getPositionHint()}.
	 * 
	 */
	protected static class MenuElementComparator implements
			Comparator<MenuComponent> {
		public int compare(MenuComponent a, MenuComponent b) {
			return a.getPositionHint() - b.getPositionHint();
		}
	}

	/**
	 * Update menus when {@link SPIRegistry} is updated.
	 * 
	 */
	protected class MenuRegistryObserver implements Observer<SPIRegistryEvent> {
		public void notify(Observable<SPIRegistryEvent> sender,
				SPIRegistryEvent message) throws Exception {
			if (message.equals(SPIRegistry.UPDATED)) {
				// Note: If needsUpdate is true we don't need to do update() as it will
				// be called on the first create*() call anyway
				if (! needsUpdate) {
					update();
				}
			}
		}
	}

	/**
	 * Various options for
	 * {@link MenuManagerImpl#makeComponents(URI, MenuOptions)} and friends.
	 * 
	 * @author Stian Soiland-Reyes
	 * 
	 */
	public static class MenuOptions {
		private boolean isToolbar = false;
		private boolean isOptionGroup = false;
		private ContextualSelection contextualSelection = null;

		public ContextualSelection getContextualSelection() {
			return contextualSelection;
		}

		public void setContextualSelection(
				ContextualSelection contextualSelection) {
			this.contextualSelection = contextualSelection;
		}

		public MenuOptions(MenuOptions original) {
			this.isOptionGroup = original.isOptionGroup();
			this.isToolbar = original.isToolbar();
			this.contextualSelection = original.getContextualSelection();
		}

		public MenuOptions() {
		}

		@Override
		protected MenuOptions clone() {
			return new MenuOptions(this);
		}

		public boolean isToolbar() {
			return isToolbar;
		}

		public void setToolbar(boolean isToolbar) {
			this.isToolbar = isToolbar;
		}

		public boolean isOptionGroup() {
			return isOptionGroup;
		}

		public void setOptionGroup(boolean isOptionGroup) {
			this.isOptionGroup = isOptionGroup;
		}
	}

}
