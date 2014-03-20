/*******************************************************************************
 * Copyright (C) 2007-2010 The University of Manchester   
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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * ZPane manifesting a split panel design where each sub-panel is itself a ZPane
 * 
 * @author Tom Oinn
 */
@SuppressWarnings("serial")
public class ZSplitPane extends ZPane {

	private static Logger logger = Logger.getLogger(ZSplitPane.class);

	private JSplitPane splitPane = new DividerLocationSplitPane();

	private double dividerLocation = 0.5d;
	private List<Action> actions = new ArrayList<Action>();

	public ZSplitPane() {
		super();
		splitPane.setLeftComponent(new ZBlankComponent());
		splitPane.setRightComponent(new ZBlankComponent());
		splitPane.setDividerLocation(0.5d);
		splitPane.setResizeWeight(0.5d);
		actions.add(new SwitchOrientationAction());
		actions.add(new ReplaceWithBlankAction());
		add(splitPane, BorderLayout.CENTER);
	}

	// No longer needed due to DividerLocationSplitPane
	// @Override
	// public void repaint(long tm, int x, int y, int width, int height) {
	// super.repaint(tm, x, y, width, height);
	// if (splitPane.getWidth()!=0 && !dividerSet) {
	// splitPane.resetToPreferredSizes();
	// splitPane.setDividerLocation(dividerLocation);
	// splitPane.setResizeWeight(dividerLocation);
	// dividerSet=true;
	// }
	// }

	public void configure(Element e) {
		Element splitElement = e.getChild("split");
		if (splitElement != null) {
			String orientation = splitElement.getAttributeValue("orientation");
			if (orientation != null) {
				splitPane
						.setOrientation(orientation
								.equalsIgnoreCase("horizontal") ? JSplitPane.HORIZONTAL_SPLIT
								: JSplitPane.VERTICAL_SPLIT);
			}

			Element leftElement = splitElement.getChild("left");
			Element leftDefinition = leftElement.getChild("znode");
			ZTreeNode leftNode = componentFor(leftDefinition);
			splitPane.setLeftComponent((JComponent) leftNode);

			Element rightElement = splitElement.getChild("right");
			Element rightDefinition = rightElement.getChild("znode");
			ZTreeNode rightNode = componentFor(rightDefinition);
			splitPane.setRightComponent((JComponent) rightNode);

			leftNode.configure(leftDefinition);
			rightNode.configure(rightDefinition);

			String ratio = splitElement.getAttributeValue("ratio");
			if (ratio != null) {
				try {
					// defer setting the dividerlocation until the first repaint
					// that splitpane
					dividerLocation = Double.parseDouble(ratio);
				} catch (NumberFormatException ex) {
					logger.warn("Invalid divider ratio " + ratio, ex);
				}
			}
		}
	}

	public void discard() {
		for (ZTreeNode child : getZChildren()) {
			child.discard();
		}
	}

	public List<Action> getActions() {
		return this.actions;
	}

	public Element getElement() {
		double ratio = getDividerRatio();

		Element splitElement = new Element("split");
		splitElement.setAttribute("ratio", String.valueOf(ratio));
		splitElement
				.setAttribute(
						"orientation",
						splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT ? "horizontal"
								: "vertical");
		Element rightElement = new Element("right");
		splitElement.addContent(rightElement);
		Element leftElement = new Element("left");
		splitElement.addContent(leftElement);
		ZTreeNode rightNode = (ZTreeNode) splitPane.getRightComponent();
		ZTreeNode leftNode = (ZTreeNode) splitPane.getLeftComponent();
		rightElement.addContent(elementFor(rightNode));
		leftElement.addContent(elementFor(leftNode));
		return splitElement;
	}

	public List<ZTreeNode> getZChildren() {
		List<ZTreeNode> children = new ArrayList<ZTreeNode>();
		children.add(getLeftComponent());
		children.add(getRightComponent());
		return children;
	}

	/**
	 * Call superclass method to show or hide toolbar and recursively call on
	 * all child elements.
	 */
	@Override
	public void setEditable(boolean editable) {
		super.setEditable(editable);
		for (ZTreeNode child : getZChildren()) {
			child.setEditable(editable);
		}
	}

	public void swap(ZTreeNode oldComponent, ZTreeNode newComponent) {
		// Store the old divider location, we don't want this to change
		int location = splitPane.getDividerLocation();
		oldComponent.discard();
		if (getRightComponent().equals(oldComponent)) {
			// Swap the right component
			splitPane.remove((Component) oldComponent);
			splitPane.setRightComponent((Component) newComponent);
		} else if (getLeftComponent().equals(oldComponent)) {
			// Swap the left component
			splitPane.remove((Component) oldComponent);
			splitPane.setLeftComponent((Component) newComponent);
		}
		newComponent.setEditable(this.editable);
		splitPane.setDividerLocation(location);
		revalidate();
	}

	private double getDividerRatio() {

		// total size would be the height if oriented vertically, or the width
		// if horizontally
		double total = splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT ? (double) splitPane
				.getBounds().getWidth()
				: (double) splitPane.getBounds().getHeight();

		total = total - splitPane.getDividerSize();

		double ratio;

		if (getWidth() == 0) {
			ratio = this.dividerLocation;
		} else {
			double dividerLocation = splitPane.getDividerLocation();
			if (dividerLocation < 0)
				dividerLocation = 0; // when the divider is far to one side,
										// it
			// the dividerlocation results in being
			// negative (?!), setting to 0 prevents an
			// error on reload it gives the correct
			// approximate location
			if (total <= 0)
				ratio = 0;
			else
				ratio = dividerLocation / total;
		}
		return ratio;
	}

	private ZTreeNode getLeftComponent() {
		return (ZTreeNode) splitPane.getLeftComponent();
	}

	private ZTreeNode getRightComponent() {
		return (ZTreeNode) splitPane.getRightComponent();
	}

	/**
	 * Set the divider location on the first call to getDividerLocation() -
	 * which should be after (or while) we have been displayed.
	 * 
	 * @author Stian Soiland-Reyes
	 * 
	 */
	public class DividerLocationSplitPane extends JSplitPane {
		private boolean initialDividerSet = false;

		@Override
		public int getDividerLocation() {
			if (!initialDividerSet && getWidth() > 0) {
				setDividerLocation(dividerLocation);
				setResizeWeight(dividerLocation);
				initialDividerSet = true;
			}
			return super.getDividerLocation();
		}
	}

	@SuppressWarnings("serial")
	private class SwitchOrientationAction extends AbstractAction {

		public SwitchOrientationAction() {
			super();
			if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
				putValue(Action.SHORT_DESCRIPTION, "Switch to horizontal split");
				putValue(Action.SMALL_ICON, ZIcons
						.iconFor("converttohorizontalsplit"));
			} else {
				putValue(Action.SHORT_DESCRIPTION, "Switch to vertical split");
				putValue(Action.SMALL_ICON, ZIcons
						.iconFor("converttoverticalsplit"));
			}
		}

		public void actionPerformed(ActionEvent arg0) {
			if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
				splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
				putValue(Action.SHORT_DESCRIPTION, "Switch to vertical split");
				putValue(Action.SMALL_ICON, ZIcons
						.iconFor("converttoverticalsplit"));
			} else {
				splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
				putValue(Action.SHORT_DESCRIPTION, "Switch to horizontal split");
				putValue(Action.SMALL_ICON, ZIcons
						.iconFor("converttohorizontalsplit"));
			}
		}

	}

}
