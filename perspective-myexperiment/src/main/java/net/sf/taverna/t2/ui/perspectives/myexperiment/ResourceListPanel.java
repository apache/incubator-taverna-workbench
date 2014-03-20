/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
 * 
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.ui.perspectives.myexperiment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URI;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import org.apache.log4j.Logger;

/**
 * @author Sergejs Aleksejevs, Emmanuel Tagarira, Jiten Bhagat
 */
public class ResourceListPanel extends JPanel implements HyperlinkListener {
  // CONSTANTS
  public final static int DESCRIPTION_TRUNCATE_LENGTH_FOR_SHORT_LIST_VIEW = 150;

  public final static int THUMBNAIL_WIDTH_FOR_SHORT_LIST_VIEW = 60;
  public final static int THUMBNAIL_HEIGHT_FOR_SHORT_LIST_VIEW = 45;

  public final static int THUMBNAIL_WIDTH_FOR_FULL_LIST_VIEW = 90;
  public final static int THUMBNAIL_HEIGHT_FOR_FULL_LIST_VIEW = 90;

  private MainComponent pluginMainComponent;
  private MyExperimentClient myExperimentClient;
  private Logger logger;

  private JPanel listPanel;
  private GridBagConstraints gbConstraints;

  private List<Resource> listItems;

  // some of the components will not be shown in the item list if
  // it's not of full size
  private boolean bFullSizeItemsList = true;

  public ResourceListPanel(MainComponent component, MyExperimentClient client, Logger logger) {
	super();

	// set main variables to ensure access to myExperiment, logger and the
	// parent component
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;

	this.initialiseUI();
  }

  public boolean isFullSizeItemsList() {
	return this.bFullSizeItemsList;
  }

  public void setFullSizeItemsList(boolean bFullSizeItemsList) {
	this.bFullSizeItemsList = bFullSizeItemsList;
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
	try {
	  if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		String strAction = e.getDescription().toString();

		if (strAction.startsWith("preview:")) {
		  this.pluginMainComponent.getPreviewBrowser().preview(strAction);
		} else {
		    Desktop.getDesktop().browse(new URI(strAction));
		}
	  }
	} catch (Exception ex) {
	  logger.error("Error occurred whilst clicking a hyperlink", ex);
	}
  }

  public void setListItems(List<Resource> items) {
	this.listItems = items;

	this.repopulate();
  }

  public void clear() {
	this.listPanel.removeAll();
	this.invalidate();
  }

  public void refresh() {
	if (this.listItems != null) {
	  this.repopulate();
	}
  }

  public void repopulate() {
	if (this.listItems != null) {
	  this.clear();

	  if (this.listItems.size() > 0) {
		Resource res = null;
		for (int i = 0; i < this.listItems.size(); i++) {
		  try {
			// this will make the layout manager to push all extra space in
			// Y-axis
			// to go to the last element in the panel; essentially, this will
			// push
			// all list items to the top of the list view panel
			if (i == listItems.size() - 1)
			  gbConstraints.weighty = 1.0;

			res = this.listItems.get(i);
			this.listPanel.add(res.createListViewPanel(bFullSizeItemsList, pluginMainComponent, this, logger), gbConstraints);
			logger.debug("Added entry in resource list panel for the resource (Type: "
				+ res.getItemTypeName() + ", URI: " + res.getURI() + ")");
		  } catch (Exception e) {
			logger.error("Failed to add item entry to ResourceListPanel (Item Type : "
				+ res.getItemTypeName() + ", URI: " + res.getURI() + ")", e);
		  }
		}
	  } else {
		// no items in the list
		JLabel lNone = new JLabel("None");
		lNone.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		lNone.setForeground(Color.GRAY);
		lNone.setFont(lNone.getFont().deriveFont(Font.ITALIC));

		gbConstraints.anchor = GridBagConstraints.WEST;
		this.listPanel.add(lNone, gbConstraints);

		this.listPanel.setPreferredSize(new Dimension(20, 40));
		this.listPanel.setBackground(Color.WHITE);
	  }
	}

	this.validate();
	this.repaint();
  }

  private void initialiseUI() {
	this.listPanel = new JPanel();
	this.listPanel.setBorder(BorderFactory.createEmptyBorder());

	this.listPanel.setLayout(new GridBagLayout());
	this.gbConstraints = new GridBagConstraints();
	this.gbConstraints.anchor = GridBagConstraints.NORTH;
	this.gbConstraints.fill = GridBagConstraints.HORIZONTAL;
	this.gbConstraints.gridx = GridBagConstraints.REMAINDER;
	this.gbConstraints.gridy = GridBagConstraints.RELATIVE;
	this.gbConstraints.weightx = 1.0;
	this.gbConstraints.weighty = 0;

	this.setLayout(new BorderLayout());
	this.add(this.listPanel, BorderLayout.CENTER);
  }
}
