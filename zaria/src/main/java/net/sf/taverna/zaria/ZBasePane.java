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
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;
import net.sf.taverna.raven.spi.SpiRegistry;
import net.sf.taverna.zaria.progress.BlurredGlassPane;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * A base ZPane implementation, this is always the root of the ZTreeNode
 * heirarchy (or should be for sane uses). We need an additional layer here as
 * the swap component method relies on having a parent, without the extra
 * 'invisible' parent here we couldn't swap out the user visible top level UI
 * component.
 * 
 * @author Tom Oinn
 */
public abstract class ZBasePane extends ZPane {

	private static Logger logger = Logger.getLogger(ZBasePane.class);

	private static void addChildText(Element parent, String elementName,
			String text) {
		Element e = new Element(elementName);
		e.setText(text);
		parent.addContent(e);
	}

	private ZTreeNode child = null;

	private Repository repository = null;

	private Map<String, SpiRegistry> registries = new HashMap<String, SpiRegistry>();

	private String[] knownSpiNames = new String[0];

	private Action toggleEditAction;

	private BlurredGlassPane blur = null;

	Map<String, NamedRavenComponentSpecifier> namedComponentDefinitions = new HashMap<String, NamedRavenComponentSpecifier>();

	Map<String, JComponent> namedComponents = new HashMap<String, JComponent>();

	/**
	 * Construct a new ZBasePane, inserting a default ZBlankComponent as the
	 * solitary child
	 */
	@SuppressWarnings("serial")
	public ZBasePane() {
		super();
		child = new ZBlankComponent();
		add((Component) child, BorderLayout.CENTER);
		toggleEditAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				boolean edit = ZBasePane.this.editable;
				ZBasePane.this.setEditable(!edit);
			}
		};
		setEditActionState();
	}

	/**
	 * Automatically intializes the repository with any named components that
	 * aren't already there.
	 */
	public void configure(Element e) {

		// Initialize any named component definitions we may have
		// lying around. This needs doing before the children
		Element namedComponentSetElement = e.getChild("namedcomponents");
		if (namedComponentSetElement != null) {
			boolean needUpdate = false;
			for (Object childObj : namedComponentSetElement
					.getChildren("namedcomponent")) {
				Element compElement = (Element) childObj;
				String groupId = compElement.getChildTextTrim("groupid");
				String artifactId = compElement.getChildTextTrim("artifact");
				String version = null;
				if (compElement.getChildTextTrim("version") != null) {
					version = compElement.getChildTextTrim("version");
				}
				String className = compElement.getChildTextTrim("classname");
				String name = compElement.getChildTextTrim("name");

				Artifact a = null;
				if (version != null) {
					a = new BasicArtifact(groupId, artifactId, version);
				} else {
					Profile prof = ProfileFactory.getInstance().getProfile();
					if (prof != null) {
						a = prof.discoverArtifact(groupId, artifactId, repository);
						if (a == null) {
							logger.warn("No artifact found in profile for:"
									+ groupId + ":" + artifactId);
						}
					}
				}
				if (a != null) {
					NamedRavenComponentSpecifier nrcs = new NamedRavenComponentSpecifier(
							a, className, name);
					namedComponentDefinitions.put(name, nrcs);
					repository.addArtifact(a);
					if (repository.getStatus(a).equals(ArtifactStatus.Ready) == false) {
						needUpdate = true;
					}
				} else {
					logger.warn("Cannot determine version for artifact "
							+ groupId + ":" + artifactId);
				}
			}
			if (needUpdate) {
				lockFrame();
				repository.update();
				unlockFrame();
			}
		}

		Element childElement = e.getChild("child");
		if (childElement != null) {
			childElement = childElement.getChild("znode");
			if (childElement != null) {
				ZTreeNode node = componentFor(childElement);
				swap(child, node);
				node.configure(childElement);
			}
		}

		// ensure editable status is correct on this and all children
		setEditable(this.editable);
	}

	/**
	 * No actions, the ZBasePane is effectively invisible
	 */
	public List<Action> getActions() {
		return new ArrayList<Action>();
	}

	/**
	 * Given a Class object from an SPI construct a JComponent. Typically the
	 * SPI itself will point to a factory pattern although it may be directly
	 * linked to the implementation classes (which is why we're using the
	 * SpiRegistry rather than the more elegant InstanceRegistry). This method
	 * will always be extended, the only reason to avoid making this entire
	 * class abstract is for testing purposes (not a very good reason really)
	 */
	@SuppressWarnings("unchecked")
	public abstract JComponent getComponent(Class theClass);

	public Element getElement() {

		Element baseElement = new Element("basepane");
		Element childElement = new Element("child");
		childElement.addContent(elementFor(child));
		baseElement.addContent(childElement);
		Element namedComponentsElement = new Element("namedcomponents");
		baseElement.addContent(namedComponentsElement);
		for (String name : namedComponentDefinitions.keySet()) {
			NamedRavenComponentSpecifier nrcs = namedComponentDefinitions
					.get(name);
			Element namedComponentElement = new Element("namedcomponent");
			namedComponentsElement.addContent(namedComponentElement);
			if (nrcs.artifact == null) {
				logger.warn("Unknown Raven component for serialising perspective " + name);
			} else {
				addChildText(namedComponentElement, "groupid", nrcs.artifact
						.getGroupId());
				addChildText(namedComponentElement, "artifact", nrcs.artifact
						.getArtifactId());
				if (!artifactExistsInProfile(nrcs.artifact)) {
					addChildText(namedComponentElement, "version",
							nrcs.artifact.getVersion());
				}
			}
			addChildText(namedComponentElement, "classname", nrcs.className);
			addChildText(namedComponentElement, "name", name);
		}
		return baseElement;
	}

	/**
	 * Get known SPIs to be used with the ZRavenComponent
	 */
	public String[] getKnownSPINames() {
		return knownSpiNames;
	}

	/**
	 * Given a Class object from an SPI produce an appropriate JMenuItem. By
	 * default this method doesn't do much, it just returns a textual menu item
	 * with the classname in but it's expected that this will be extended by any
	 * implementing class to produce something sensible. I don't want to make
	 * this method any more specialized to avoid dependencies on the potential
	 * range of SPI interfaces.
	 */
	@SuppressWarnings("unchecked")
	public abstract JMenuItem getMenuItem(Class theClass);

	/**
	 * Enumerate all visible ZRavenComponent panes within the current layout
	 */
	public List<ZRavenComponent> getRavenComponents() {
		List<ZRavenComponent> result = new ArrayList<ZRavenComponent>();
		enumerateRavenComponents(result, this);
		return result;
	}

	/**
	 * Create or return a cached reference to an SpiRegistry for the specified
	 * SPI name
	 */
	public synchronized SpiRegistry getRegistryFor(String spiName) {
		if (registries.containsKey(spiName)) {
			return registries.get(spiName);
		} else {
			SpiRegistry sr = new SpiRegistry(repository, spiName, null);
			sr.addFilter(ProfileFactory.getInstance().getProfile());
			registries.put(spiName, sr);
			return sr;
		}
	}

	/**
	 * Get the Raven repository associated with this ZBasePane
	 */
	public Repository getRepository() {
		return this.repository;
	}

	/**
	 * Get an Action object which can toggle the editable state of the ZBasePane
	 */
	public Action getToggleEditAction() {
		return toggleEditAction;
	}

	/**
	 * Single element list consiting only of the child item
	 */
	public List<ZTreeNode> getZChildren() {
		List<ZTreeNode> children = new ArrayList<ZTreeNode>();
		children.add(child);
		return children;
	}

	/**
	 * Lock the parent frame, showing an infinite progress display message
	 */
	public void lockFrame() {
		if (blur == null) {
			blur = new BlurredGlassPane(getFrame());
			getFrame().setGlassPane(blur);
		}
		blur.setActive(true);

		/**
		 * JFrame jf = getFrame(); if (jf != null) { Component c =
		 * jf.getGlassPane(); if (c != glassPane) { oldGlassPane = c;
		 * jf.setGlassPane(glassPane); } glassPane.setText("Locked...");
		 * glassPane.start(); }
		 */
	}

	/**
	 * Call setEditable on the single child
	 */
	@Override
	public void setEditable(boolean b) {
		super.setEditable(b);
		if (child != null) {
			child.setEditable(b);
		}
		setEditActionState();
		revalidate();
		repaint();
	}

	/**
	 * Set the array of known SPIs
	 */
	public void setKnownSPINames(String[] spis) {
		this.knownSpiNames = spis;
	}

	/**
	 * Set the Raven Repository object used to discover SPI implementations for
	 * the ZRavenComponent instances within the layout
	 * 
	 * @param r
	 */
	public void setRepository(Repository r) {
		this.repository = r;
	}

	/**
	 * Only a single child so always swap it out and replace with the new
	 * component
	 */
	public void swap(ZTreeNode oldComponent, ZTreeNode newComponent) {
		if (oldComponent == child || child == null) {
			if (child != null) {
				child.discard();
				remove((Component) child);
			}
			child = newComponent;
			add((Component) newComponent, BorderLayout.CENTER);
			newComponent.setEditable(this.editable);
			revalidate();
		}
	}

	/**
	 * Unlock the parent frame
	 */
	public void unlockFrame() {
		blur.setActive(false);
		/**
		 * JFrame jf = getFrame(); if (jf != null) { glassPane.stop(); if
		 * (oldGlassPane != null) { jf.setGlassPane(oldGlassPane); } }
		 */
	}

	private void enumerateRavenComponents(List<ZRavenComponent> results,
			ZTreeNode node) {
		if (node instanceof ZRavenComponent) {
			results.add((ZRavenComponent) node);
		} else {
			for (ZTreeNode child : node.getZChildren()) {
				enumerateRavenComponents(results, child);
			}
		}
	}

	/**
	 * Configure the edit action based on the current editable state.
	 */
	private void setEditActionState() {
		if (editable) {
			toggleEditAction.putValue(Action.NAME, "Stop editing");
		} else {
			toggleEditAction.putValue(Action.NAME, "Edit");
		}
	}

	/**
	 * Called when a component is removed from a ZRavenComponent pane, only
	 * called if the component is not named (and therefore will not be shared by
	 * other panes)
	 */
	protected void deregisterComponent(JComponent comp) {
		// Do nothing by default
	}

	/**
	 * Called when a new component is added to a ZRavenComponent pane, can be
	 * overridden to perform implementation specific initialization of the
	 * component
	 */
	protected void registerComponent(JComponent comp) {
		// Do nothing by default
	}

	/**
	 * Returns or creates and returns the given named component assuming the
	 * definition for that component exists within this base pane.
	 * 
	 * @param componentName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	JComponent getNamedComponent(String componentName) {
		synchronized (namedComponents) {
			if (namedComponents.containsKey(componentName)) {
				return namedComponents.get(componentName);
			} else {

				if (namedComponentDefinitions.containsKey(componentName)) {
					NamedRavenComponentSpecifier spec = namedComponentDefinitions
							.get(componentName);
					try {
						Class theClass = spec.getComponentClass();
						JComponent theComponent = getComponent(theClass);
						namedComponents.put(componentName, theComponent);
						return theComponent;
					} catch (ArtifactNotFoundException e) {
						logger.error("ArtifactNotFoundException", e);
					} catch (ArtifactStateException e) {
						logger.error("ArtifactStateException", e);
					} catch (ClassNotFoundException e) {
						logger.error("ClassNotFoundException", e);
					}
				} else {
					return null;
				}
			}
		}
		return new JLabel("Error, see console: can't create '" + componentName
				+ "'");
	}

	/**
	 * A bean containing information about a named Raven component allowing its
	 * reuse amongst various different layouts.
	 * 
	 * @author Tom Oinn
	 */
	class NamedRavenComponentSpecifier {

		private Artifact artifact;
		private String className;

		public NamedRavenComponentSpecifier(Artifact artifact,
				String className, String componentName) {
			this.artifact = artifact;
			this.className = className;
		}

		@SuppressWarnings("unchecked")
		public Class getComponentClass() throws ArtifactNotFoundException,
				ArtifactStateException, ClassNotFoundException {
			ClassLoader acl = repository.getLoader(artifact, null);
			return acl.loadClass(className);
		}

	}

}
