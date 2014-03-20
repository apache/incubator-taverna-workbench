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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Tag;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.TagCloud;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

/**
 * @author Sergejs Aleksejevs, Jiten Bhagat
 */
public class TagCloudPanel extends JPanel implements ChangeListener, ItemListener, ActionListener, HyperlinkListener {
  // CONSTANTS
  private static final int TAGCLOUD_MAX_FONTSIZE = 36;
  private static final int TAGCLOUD_MIN_FONTSIZE = 12;
  private static final int TAGCLOUD_DEFAULT_MAX_SIZE = 350;
  private static final int TAGCLOUD_DEFAULT_DISPLAY_SIZE = 50;

  public static final int TAGCLOUD_TYPE_GENERAL = 0;
  public static final int TAGCLOUD_TYPE_USER = 1;
  public static final int TAGCLOUD_TYPE_RESOURCE_PREVIEW = 2;

  private MainComponent pluginMainComponent;
  private MyExperimentClient myExperimentClient;
  private Logger logger;

  // COMPONENTS
  private String strTitle;
  private int iType;
  private ShadedLabel lCloudTitle;
  private JSlider jsCloudSizeSlider;
  private JCheckBox cbShowAllTags;
  private JButton bRefresh;
  private JTextPane tpTagCloudBody;

  private ActionListener clickHandler;
  private TagCloud tcData = new TagCloud();
  private boolean bUserTagCloudSliderValueNeverSet = true;

  public TagCloudPanel(String title, int iTagCloudType, ActionListener clickHandler, MainComponent component, MyExperimentClient client, Logger logger) {
	super();

	// set parameters and the main variables to ensure access to myExperiment,
	// logger and the parent component
	this.strTitle = title;
	this.iType = iTagCloudType;
	this.clickHandler = clickHandler;
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;

	initialiseUI();
  }

  private void initialiseUI() {
	// set the title of the tag cloud
	lCloudTitle = new ShadedLabel(this.strTitle, ShadedLabel.BLUE);

	// create the tag cloud controls panel
	// (all controls will be created anyway, but if that's a resource
	// preview tag cloud, make sure that these controls are not displayed)
	JPanel jpCloudControlsPanel = new JPanel();
	jpCloudControlsPanel.setLayout(new BoxLayout(jpCloudControlsPanel, BoxLayout.LINE_AXIS));
	this.jsCloudSizeSlider = new JSlider(1, TAGCLOUD_DEFAULT_MAX_SIZE, TAGCLOUD_DEFAULT_DISPLAY_SIZE);
	this.cbShowAllTags = new JCheckBox("All tags", false);
	this.bRefresh = new JButton("Refresh", WorkbenchIcons.refreshIcon);

	if (this.iType != TagCloudPanel.TAGCLOUD_TYPE_RESOURCE_PREVIEW) {
	  this.jsCloudSizeSlider.addChangeListener(this);
	  this.jsCloudSizeSlider.setToolTipText("Drag the slider to select how big the tag cloud should be, or check the \"All tags\" box to get the full tag cloud.");
	  jpCloudControlsPanel.add(this.jsCloudSizeSlider);

	  this.cbShowAllTags.addItemListener(this);
	  jpCloudControlsPanel.add(this.cbShowAllTags);

	  this.bRefresh.addActionListener(this);
	  this.bRefresh.setToolTipText("Click this button to refresh the Tag Cloud");
	  jpCloudControlsPanel.add(this.bRefresh);
	}

	// tag cloud header panel which which contains controls
	JPanel jpCloudHeader = new JPanel(new BorderLayout());
	jpCloudHeader.add(jpCloudControlsPanel, BorderLayout.CENTER);
	jpCloudHeader.setBorder(BorderFactory.createEtchedBorder());

	// body of the tag cloud with the actual tags
	this.tpTagCloudBody = new JTextPane();
	this.tpTagCloudBody.setBorder(BorderFactory.createEmptyBorder());
	this.tpTagCloudBody.setEditable(false);
	this.tpTagCloudBody.setContentType("text/html");
	this.tpTagCloudBody.addHyperlinkListener(this);

	JScrollPane spTagCloudBody = new JScrollPane(this.tpTagCloudBody, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	spTagCloudBody.setBorder(BorderFactory.createEmptyBorder());
	spTagCloudBody.setOpaque(true);

	// PUT EVERYTHING TOGETHER
	JPanel jpTagCloudContentWithControls = new JPanel();
	jpTagCloudContentWithControls.setLayout(new BorderLayout());
	jpTagCloudContentWithControls.add(spTagCloudBody, BorderLayout.CENTER);
	if (this.iType != TagCloudPanel.TAGCLOUD_TYPE_RESOURCE_PREVIEW) {
	  jpTagCloudContentWithControls.add(jpCloudHeader, BorderLayout.NORTH);
	}

	this.setLayout(new BorderLayout());
	if (this.iType != TagCloudPanel.TAGCLOUD_TYPE_RESOURCE_PREVIEW) {
	  this.add(lCloudTitle, BorderLayout.NORTH);
	}
	this.add(jpTagCloudContentWithControls, BorderLayout.CENTER);
	this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), BorderFactory.createEtchedBorder()));
  }

  public void refresh() {
	this.lCloudTitle.setText(strTitle
		+ " <span style='color: gray;'>(Loading...)</span>");

	// Make call to myExperiment API in a different thread
	// (then use SwingUtilities.invokeLater to update the UI when ready).
	new Thread("Get '" + this.strTitle + "' tag cloud data") {
	  public void run() {
		logger.debug("Getting '" + strTitle + "' tag cloud data");

		try {
		  int size = -1;
		  if (!cbShowAllTags.isSelected()) {
			size = jsCloudSizeSlider.getValue();
		  }

		  // based on the type of the tag cloud, different data needs to be
		  // fetched
		  switch (iType) {
			case TagCloudPanel.TAGCLOUD_TYPE_GENERAL:
			  tcData = myExperimentClient.getGeneralTagCloud(size);
			  break;

			case TagCloudPanel.TAGCLOUD_TYPE_USER:
			  tcData = myExperimentClient.getUserTagCloud(myExperimentClient.getCurrentUser(), size);
			  break;

			case TagCloudPanel.TAGCLOUD_TYPE_RESOURCE_PREVIEW:
			  // fetch tag counts for tags that are already pre-set
			  myExperimentClient.convertTagListIntoTagCloudData(tcData.getTags());
			  break;

			default:
			  // unknown type of tag cloud; show no data
			  tcData = new TagCloud();
			  break;
		  }

		  SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			  repopulate();
			}
		  });
		} catch (Exception ex) {
		  logger.error("Failed to get tag cloud data from myExperiment", ex);
		}
	  }
	}.start();
  }

  public void repopulate() {
	logger.debug("Building '" + this.strTitle + "' tag cloud...");

	try {
	  this.jsCloudSizeSlider.removeChangeListener(this);
	  if (this.iType == TAGCLOUD_TYPE_USER) {
		jsCloudSizeSlider.setMinimum(1);
		jsCloudSizeSlider.setMaximum(myExperimentClient.getCurrentUser().getTags().size());
		if (bUserTagCloudSliderValueNeverSet) {
		  // this is the first load of the cloud, show all user tags
		  jsCloudSizeSlider.setValue(jsCloudSizeSlider.getMaximum());
		  bUserTagCloudSliderValueNeverSet = false;
		} else {
		  // not the first load, test if the position of the slider is still
		  // within the scope
		  // (put that back to maximum if exceeds)
		  if (jsCloudSizeSlider.getValue() > jsCloudSizeSlider.getMaximum()
			  || this.cbShowAllTags.isSelected())
			jsCloudSizeSlider.setValue(jsCloudSizeSlider.getMaximum());
		}
	  } else {
		// if "All tags" check box is ticked, set the slider max to the max no
		// of tags
		// (this will be the total number of tags available from myExperiment);
		// if "All tags" was never checked before, max position of the slider
		// will
		// stay at predefined default value (because the total number of tags is
		// not known yet)
		if (this.cbShowAllTags.isSelected()) {
		  int size = this.tcData.getTags().size();
		  this.jsCloudSizeSlider.setMaximum(size);
		  this.jsCloudSizeSlider.setValue(size);
		}
	  }
	  this.jsCloudSizeSlider.addChangeListener(this);

	  // For tag cloud font size calculations
	  int iMaxCount = this.tcData.getMaxTagCount();

	  StringBuffer content = new StringBuffer();

	  if (this.tcData.getTags().size() > 0) {
		content.append("<div class='outer'>");
		content.append("<br>");
		content.append("<div class='tag_cloud'>");

		for (Tag t : this.tcData.getTags()) {
		  // Normalise count and use it to obtain a font size value.
		  // Also chops off based on min and max.
		  int fontSize = (int) (((double) t.getCount() / ((double) iMaxCount / 3)) * TAGCLOUD_MAX_FONTSIZE);
		  if (fontSize < TAGCLOUD_MIN_FONTSIZE) {
			fontSize = TAGCLOUD_MIN_FONTSIZE;
		  }
		  if (fontSize > TAGCLOUD_MAX_FONTSIZE) {
			fontSize = TAGCLOUD_MAX_FONTSIZE;
		  }

		  content.append("<a style='font-size: " + fontSize + "pt;' href='tag:"
			  + t.getTagName() + "'>" + t.getTagName() + "</a>");
		  content.append("&nbsp;&nbsp;&nbsp;");
		}

		content.append("<br>");
		content.append("</div>");
		content.append("</div>");
	  } else {
		content.append("<br>");
		content.append("<span style='color: gray; font-weight: italic;'>&nbsp;&nbsp;No tags to display</span>");
	  }

	  HTMLEditorKit kit = new StyledHTMLEditorKit(pluginMainComponent.getStyleSheet());
	  HTMLDocument doc = (HTMLDocument) (kit.createDefaultDocument());
	  doc.insertAfterStart(doc.getRootElements()[0].getElement(0), content.toString());

	  this.tpTagCloudBody.setEditorKit(kit);
	  this.tpTagCloudBody.setDocument(doc);
	} catch (Exception e) {
	  logger.error("Failed to populate tag cloud", e);
	}

	SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
		lCloudTitle.setText(strTitle
			+ " <span style='color: gray;'>(currently showing "
			+ tcData.getTags().size() + ")</span>");
		revalidate();
	  }
	});
  }

  /**
   * Helper method to get hold of the tag cloud data. Needed to be able to set
   * tag cloud data when using this for resource preview (when tag fetching is
   * not required).
   */
  public TagCloud getTagCloudData() {
	return this.tcData;
  }

  public void stateChanged(ChangeEvent e) {
	if (e.getSource().equals(this.jsCloudSizeSlider)) {
	  // cloud size slider was dragged to a new place and the drag event
	  // has finished; refresh the tag cloud with the newly selected tag count
	  if (!this.jsCloudSizeSlider.getValueIsAdjusting()) {
		this.cbShowAllTags.removeItemListener(this);
		this.cbShowAllTags.setSelected(false);
		this.cbShowAllTags.addItemListener(this);

		this.refresh();
	  }
	}
  }

  public void itemStateChanged(ItemEvent e) {
	if (e.getItemSelectable().equals(this.cbShowAllTags)) {
	  // "Show all" clicked - need to refresh with all tags being displayed
	  this.refresh();
	}
  }

  public void actionPerformed(ActionEvent e) {
	if (e.getSource().equals(this.bRefresh)) {
	  // refresh button clicked on the cloud controls panel -
	  // simply refresh the cloud with the same parameters
	  this.refresh();
	}
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
	if (e.getSource().equals(this.tpTagCloudBody)
		&& e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
	  // one of the tags was clicked, but click processing is off-loaded to
	  // 'clickHandler'
	  this.clickHandler.actionPerformed(new ActionEvent(this, (this.getClass().getName() + e.getDescription()).hashCode(), e.getDescription()));
	}
  }
}
