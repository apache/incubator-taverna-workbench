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
package net.sf.taverna.zaria;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;
import net.sf.taverna.raven.spi.SpiRegistry;
import net.sf.taverna.zaria.ZBasePane.NamedRavenComponentSpecifier;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * A non-Zaria JComponent selected from a Raven-described SPI or list of named
 * instances.
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 */
@SuppressWarnings("serial")
public class ZRavenComponent extends ZPane {

	private static Log logger = Log.getLogger(ZRavenComponent.class);
	private String spiName = null;
	private Artifact artifact = null;
	private String className = null;
	private boolean hasScrollPane = true;
	private JPanel contentArea = new JPanel();
	private JComponent contents = null;
	private List<Action> actions = new ArrayList<Action>();
	private SpiRegistry registry = null;
	private String sharedName = "";

	private ToggleScrollPaneAction toggleScroll = new ToggleScrollPaneAction();
	private Action selectSPI = new SelectSPIAction();
	private Action selectInstance = new SelectInstanceAction();
	private Action nameComponent = new NameComponentAction();
	private Action selectNamedInstance = new SelectNamedInstanceAction();

	public ZRavenComponent() {
		super();
		actions.add(selectSPI);
		actions.add(selectInstance);
		actions.add(toggleScroll);
		actions.add(nameComponent);
		actions.add(selectNamedInstance);
		nameComponent.setEnabled(false);
		actions.add(new ReplaceWithBlankAction());
		setLayout(new BorderLayout());
		contentArea.setLayout(new BorderLayout());
		add(contentArea, BorderLayout.CENTER);
	}

	@SuppressWarnings("unchecked")
	public void configure(Element confElement) {
		Element e = confElement.getChild("component");
		if (e == null) {
			// Try as named component instead
			e = confElement.getChild("namedcomponent");
			if (e == null) {
				logger
						.warn("Could not find either <component> or <namedcomponent> in "
								+ confElement);
				return;
			}
		}
		Attribute scroll = e.getAttribute("scroll");
		if (scroll == null) {
			setScroll(true); // default behaviour in Taverna 1.5.0
		} else {
			setScroll(scroll.getValue().equals("true"));
		}

		if (e.getName().equals("namedcomponent")) {
			Element componentNameElement = e.getChild("name");
			String componentName = componentNameElement.getTextTrim();
			JComponent jc = getRoot().getNamedComponent(componentName);
			if (jc == null) {
				logger
						.error("Could not find named component: "
								+ componentName);
				return;
			}
			if (jc.getParent() != null) {
				jc.getParent().remove(jc);
			}
			setComponent(jc);
			sharedName = componentName;
			return;
		}
		Element spiNameElement = e.getChild("interface");
		if (spiNameElement != null) {
			this.setSPI(spiNameElement.getTextTrim());
		}
		Element ravenElement = e.getChild("raven");
		Repository repository = getRoot().getRepository();
		if (ravenElement != null) {
			String groupId = ravenElement.getChild("group").getTextTrim();
			String artifactId = ravenElement.getChild("artifact").getTextTrim();
			String version = null;
			if (ravenElement.getChild("version") != null) {
				version = ravenElement.getChild("version").getTextTrim();
			}

			// if no version defined, use the version defined in the profile
			if (version == null) {
				Profile profile = ProfileFactory.getInstance().getProfile();
				artifact = profile.discoverArtifact(groupId, artifactId,
						repository);
			} else {
				artifact = new BasicArtifact(groupId, artifactId, version);
			}
		}
		Element classNameElement = e.getChild("classname");
		if (classNameElement != null) {
			className = classNameElement.getTextTrim();
		}
		if (className != null) {
			ClassLoader acl;
			try {
				try {
					acl = repository.getLoader(artifact, null);
				} catch (ArtifactNotFoundException ex) {
					// add to repository if it doesn't exist
					logger.info("Fetching artifact for ZRavenComponent:"
							+ artifact.getGroupId() + ":"
							+ artifact.getArtifactId());
					repository.addArtifact(artifact);
					repository.update();
					acl = repository.getLoader(artifact, null);
				}
				Class theClass = acl.loadClass(className);
				setComponent(getRoot().getComponent(theClass));
			} catch (ArtifactNotFoundException ex) {
				logger.warn("Could not find artifact " + artifact, ex);
			} catch (ArtifactStateException ex) {
				logger.warn("Invalid state for artifact " + artifact, ex);
			} catch (ClassNotFoundException ex) {
				logger.warn("Class not found: " + className, ex);
			}
		}
	}

	public void discard() {
		if (this.contents != null) {
			getRoot().deregisterComponent(this.contents);
		}
	}

	public List<Action> getActions() {
		return actions;
	}

	/**
	 * Get the current component
	 */
	public JComponent getComponent() {
		return this.contents;
	}

	public Element getElement() {
		Element e;
		if (sharedName.equals("")) {
			e = new Element("component");
		} else {
			e = new Element("namedcomponent");
		}
		e.setAttribute("scroll", hasScrollPane ? "true" : "false");

		if (e.getName().equals("namedcomponent")) {
			Element nameElement = new Element("name");
			nameElement.setText(sharedName);
			e.addContent(nameElement);
			return e;
		}
		if (artifact == null || className == null) {
			logger.warn("Can't serialize null-valued artifact or className");
			return e;
		}
		Element ravenElement = new Element("raven");
		Element groupElement = new Element("group");
		groupElement.setText(artifact.getGroupId());
		Element artifactElement = new Element("artifact");
		artifactElement.setText(artifact.getArtifactId());

		ravenElement.addContent(groupElement);
		ravenElement.addContent(artifactElement);

		if (!artifactExistsInProfile(artifact)) {
			Element versionElement = new Element("version");
			versionElement.setText(artifact.getVersion());
			ravenElement.addContent(versionElement);
		}

		Element classNameElement = new Element("classname");
		classNameElement.setText(className);

		Element spiNameElement = new Element("interface");
		spiNameElement.setText(spiName);

		e.addContent(ravenElement);
		e.addContent(classNameElement);
		e.addContent(spiNameElement);

		return e;
	}

	/**
	 * Component has no children, it's always a leaf
	 */
	public List<ZTreeNode> getZChildren() {
		return new ArrayList<ZTreeNode>();
	}

	/**
	 * Enable or disable scrollpane. By default, a scroll panel is enabled.
	 * 
	 * @param scroll
	 *            true if scrollpane is to be used
	 */
	public void setScroll(boolean scroll) {
		hasScrollPane = scroll;
		contentArea.removeAll();
		toggleScroll.updateState();
		if (contents == null) {
			// Too early to set the contentArea
			return;
		}
		if (hasScrollPane) {
			JScrollPane sp = new JScrollPane(contents,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			sp.setPreferredSize(new Dimension(0, 0));
			contentArea.add(sp, BorderLayout.CENTER);
		} else {
			contentArea.add(contents, BorderLayout.CENTER);
		}
		repaint();
		revalidate();
	}

	public void setSharedName(String name) {
		sharedName = name;
		// Either fetch a component from the base pane with this name
		// or we're creating a new one.
		JComponent jc = getRoot().getNamedComponent(name);
		if (jc != null) {
			if (jc.getParent() != null) {
				JComponent parent = (JComponent) jc.getParent();
				// Remove from any existing parent just in case
				jc.getParent().remove(jc);
				parent.revalidate();
				parent.repaint();

			}
			setComponent(jc);
		} else {
			// Create a new shared component entry in the basepane
			NamedRavenComponentSpecifier nrcs = getRoot().new NamedRavenComponentSpecifier(
					artifact, className, name);
			getRoot().namedComponentDefinitions.put(name, nrcs);
			if (contents != null) {
				getRoot().namedComponents.put(name, this.contents);
			}
		}
		nameComponent.setEnabled(false);
	}

	public void setSPI(String newSPIName) {
		if (spiName == null || newSPIName.equals(spiName) == false) {
			this.spiName = newSPIName;
			registry = getRoot().getRegistryFor(spiName);
			selectInstance.setEnabled(true);
		}
	}

	/**
	 * Component has no children so the swap method is never used
	 */
	public void swap(ZTreeNode oldComponent, ZTreeNode newComponent) {
		// Do nothing, will never be called
	}

	public void unsetSharedName() {
		this.sharedName = "";
		nameComponent.setEnabled(true);
	}

	/**
	 * Set the current component, whether within a scrollpane or not based on
	 * the boolean hasScrollPane property
	 * 
	 * @param theComponent
	 */
	private synchronized void setComponent(JComponent theComponent) {
		if (this.contents != null) {
			getRoot().deregisterComponent(this.contents);
		}
		contentArea.removeAll();
		if (hasScrollPane) {
			contentArea.add(new JScrollPane(theComponent), BorderLayout.CENTER);
		} else {
			contentArea.add(theComponent, BorderLayout.CENTER);
		}
		this.contents = theComponent;
		getRoot().registerComponent(this.contents);
		toggleScroll.setEnabled(true);
		getRoot().revalidate();
	}

	@Override
	public boolean makeVisible(JComponent component) {
		// TODO: Should the scroll pane be scrolled to the top?
		return super.makeVisible(component) || this.contents == component;
	}

	/**
	 * Assign a new name to the current (unnamed) component
	 */
	public class NameComponentAction extends AbstractAction {

		public NameComponentAction() {
			super();
			putValue(Action.SHORT_DESCRIPTION, "Assign name to component");
			putValue(Action.SMALL_ICON, ZIcons.iconFor("setname"));
			// putValue(Action.NAME,"addName");
		}

		public void actionPerformed(ActionEvent arg0) {
			ImageIcon icon = null;
			String newName = (String) JOptionPane.showInputDialog(getFrame(),
					"Assign name to component", "Name",
					JOptionPane.QUESTION_MESSAGE, icon, null, "new name");
			if (newName != null) {
				setSharedName(newName);
			}
		}

	}

	/**
	 * Action to select an instance of the current SPI
	 */
	public class SelectInstanceAction extends AbstractAction {

		public SelectInstanceAction() {
			super();
			putValue(Action.SHORT_DESCRIPTION, "Select component");
			putValue(Action.SMALL_ICON, ZIcons.iconFor("selectraven"));
			setEnabled(false);
		}

		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			JPopupMenu menu = new JPopupMenu("Available Components");
			for (final Class theClass : registry.getClasses()) {
				JMenuItem item = getRoot().getMenuItem(theClass);
				item.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						try {
							className = theClass.getName();
							setComponent(getRoot().getComponent(theClass));
							artifact = getRoot().getRepository()
									.artifactForClass(theClass);
							unsetSharedName();
							nameComponent.setEnabled(true);
						} catch (ArtifactNotFoundException e) {
							// Should never happen as these things can only be
							// loaded
							// from within a raven classloader and so should by
							// definition
							// have an artifact associated with them. You never
							// know though.
							logger.error("Could not find artifact " + artifact,
									e);
						} catch (NoClassDefFoundError ncdfe) {
							JOptionPane
									.showMessageDialog(
											null,
											"Transitive dependency failure - this is normally\n"
													+ "caused by an invalid POM file with missing dependencies.",
											"Error!", JOptionPane.ERROR_MESSAGE);
						}

					}
				});
				menu.add(item);
			}
			Component sourceComponent = (Component) e.getSource();
			menu.show(sourceComponent, 0, sourceComponent.getHeight());
		}
	}

	/**
	 * Pick the component from a list of known named base scoped components
	 */
	public class SelectNamedInstanceAction extends AbstractAction {

		public SelectNamedInstanceAction() {
			super();
			putValue(Action.SHORT_DESCRIPTION, "Select named component");
			putValue(Action.SMALL_ICON, ZIcons.iconFor("selectnamed"));
			// putValue(Action.NAME, "fromName");
		}

		public void actionPerformed(ActionEvent arg0) {
			JPopupMenu menu = new JPopupMenu("Named Components");
			for (final String name : getRoot().namedComponentDefinitions
					.keySet()) {
				NamedRavenComponentSpecifier nrcs = getRoot().namedComponentDefinitions
						.get(name);
				JMenuItem prototype;
				try {
					prototype = getRoot().getMenuItem(nrcs.getComponentClass());
					JMenuItem item = new JMenuItem(name, prototype.getIcon());
					item.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							setSharedName(name);
						}
					});
					menu.add(item);
				} catch (ArtifactNotFoundException ex) {
					logger.warn("Could not find artifact " + artifact, ex);
				} catch (ArtifactStateException ex) {
					logger.warn("Invalid state for artifact " + artifact, ex);
				} catch (ClassNotFoundException ex) {
					logger.warn("Class not found: " + className, ex);
				}
			}
			Component sourceComponent = (Component) arg0.getSource();
			menu.show(sourceComponent, 0, sourceComponent.getHeight());
		}
	}

	/**
	 * Action to select the current SPI from which components can be selected
	 */
	public class SelectSPIAction extends AbstractAction {

		public SelectSPIAction() {
			super();
			putValue(Action.SHORT_DESCRIPTION, "Select SPI");
			putValue(Action.SMALL_ICON, ZIcons.iconFor("selectspi"));
		}

		public void actionPerformed(ActionEvent arg0) {
			ImageIcon icon = null;
			String[] options = getRoot().getKnownSPINames();
			String newSPI = (String) JOptionPane.showInputDialog(getFrame(),
					"Select Service Provider Interface (SPI)", "SPI Chooser",
					JOptionPane.QUESTION_MESSAGE, icon, options, spiName);
			if (newSPI != null) {
				setSPI(newSPI);
			}
		}

	}

	/**
	 * Action to toggle whether the contents are shown within a scroll pane or
	 * directly within the panel
	 * 
	 * @author Tom Oinn
	 */
	public class ToggleScrollPaneAction extends AbstractAction {

		public ToggleScrollPaneAction() {
			super();
			setEnabled(false);
			updateState();
		}

		public void actionPerformed(ActionEvent ev) {
			// Flip
			setScroll(!hasScrollPane);
		}

		public void updateState() {
			if (hasScrollPane) {
				// Set state for button to remove pane
				putValue(Action.SHORT_DESCRIPTION, "Disable scroll pane");
				putValue(Action.SMALL_ICON, ZIcons.iconFor("disablescroll"));
			} else {
				// Set state for button to enable pane
				putValue(Action.SHORT_DESCRIPTION, "Enable scroll pane");
				putValue(Action.SMALL_ICON, ZIcons.iconFor("enablescroll"));
			}
			if (contents == null) {
				setEnabled(false);
			} else {
				setEnabled(true);
			}
		}

	}

}
