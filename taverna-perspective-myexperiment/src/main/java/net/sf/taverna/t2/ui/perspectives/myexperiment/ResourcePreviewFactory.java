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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Comment;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.File;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Group;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Pack;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.PackItem;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Tag;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.User;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Util;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Workflow;

import org.apache.log4j.Logger;
import org.jdom.Document;

/**
 * @author Sergejs Aleksejevs
 */
public class ResourcePreviewFactory {
  // CONSTANTS
  private static final int PREFERRED_LOWER_TABBED_PANE_HEIGHT = 250; // used for
  // all
  // tabbed
  // views
  // inside
  // preview
  // for
  // every
  // resource
  // type

  private final MainComponent pluginMainComponent;
  private final MyExperimentClient myExperimentClient;
  private final Logger logger;

  // icons which are used in several places in the preview factory
  private final ImageIcon iconWorkflow;
  private final ImageIcon iconFile;
  private final ImageIcon iconPack;
  private final ImageIcon iconUser;
  private final ImageIcon iconGroup;

  public ResourcePreviewFactory(MainComponent component, MyExperimentClient client, Logger logger) {
	super();

	// set main variables to ensure access to myExperiment, logger and the
	// parent component
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;

	// set up the icons
	iconWorkflow = new ImageIcon(MyExperimentPerspective.getLocalIconURL(Resource.WORKFLOW));
	iconFile = new ImageIcon(MyExperimentPerspective.getLocalIconURL(Resource.FILE));
	iconPack = new ImageIcon(MyExperimentPerspective.getLocalIconURL(Resource.PACK));
	iconUser = new ImageIcon(MyExperimentPerspective.getLocalIconURL(Resource.USER));
	iconGroup = new ImageIcon(MyExperimentPerspective.getLocalIconURL(Resource.GROUP));
  }

  // main worker method - generates the content to be shown in the preview;
  // responsible for parsing the preview action request, fetching data and
  // generating all the content (via helpers)
  public ResourcePreviewContent createPreview(String action, EventListener eventHandler) {
	JPanel panelToPopulate = new JPanel();

	// === PREPROCESSING ===

	// return error message if the action string isn't actually a request for
	// preview
	if (!action.startsWith("preview:")) {
	  this.logger.error("Bad preview request: \"" + action + "\"");
	  panelToPopulate.add(new JLabel("An error has occurred."));
	  Resource r = new Resource();
	  r.setItemType(Resource.UNEXPECTED_TYPE);
	  r.setTitle("Bad preview request");
	  r.setURI(action);
	  return (new ResourcePreviewContent(r, panelToPopulate));
	}

	// parse the action string - we are now sure that it starts with a
	// 'preview:'
	action = action.substring(action.indexOf(":") + 1); // remove "preview:"
	int iType = Integer.parseInt(action.substring(0, action.indexOf(":"))); // get
	// type
	action = action.substring(action.indexOf(":") + 1); // remove type
	String strURI = action; // get URI

	// === FETCHING RESOURCE DATA ===
	Document doc = null;
	try {
	  // the resource type is known at this point, hence can use specialist
	  // method
	  // that only fetches required metadata for (individual for each resource
	  // type)
	  doc = this.myExperimentClient.getResource(iType, strURI, Resource.REQUEST_FULL_PREVIEW);
	} catch (Exception e) {
	  logger.error("Error while fetching resource data from myExperiment to generate a preview.\nResource type: "
		  + Resource.getResourceTypeName(iType)
		  + "\nResource URI: "
		  + strURI
		  + "\nException: " + e);
	}

	// === GENERATING PREVIEW ===
	Resource resource = null;
	switch (iType) {
	  case Resource.WORKFLOW:
		Workflow w = Workflow.buildFromXML(doc, this.logger);
		resource = w;
		this.generateWorkflowPreviewContent(w, panelToPopulate, eventHandler);
		break;

	  case Resource.FILE:
		File f = File.buildFromXML(doc, this.logger);
		resource = f;
		this.generateFilePreviewContent(f, panelToPopulate, eventHandler);
		break;

	  case Resource.PACK:
		Pack p = Pack.buildFromXML(doc, this.myExperimentClient, this.logger);
		resource = p;
		this.generatePackPreviewContent(p, panelToPopulate, eventHandler);
		break;

	  case Resource.USER:
		User u = User.buildFromXML(doc, logger);
		resource = u;
		this.generateUserPreviewContent(u, panelToPopulate, eventHandler);
		break;

	  case Resource.GROUP:
		Group g = Group.buildFromXML(doc, logger);
		resource = g;
		this.generateGroupPreviewContent(g, panelToPopulate, eventHandler);
		break;

	  default:
		// unexpected resource type - can't generate preview
		this.logger.error("Failed generating preview. Reason: unknown resource type - \""
			+ Resource.getResourceTypeName(iType) + "\"");
		panelToPopulate.add(new JLabel("Cannot generate preview for unknown resource types."));
		Resource r = new Resource();
		r.setItemType(iType);
		r.setTitle("Error: unknown resource type");
		r.setURI(strURI);
		return (new ResourcePreviewContent(r, panelToPopulate));
	}

	// format output data
	return (new ResourcePreviewContent(resource, panelToPopulate));
  }

  private void generateWorkflowPreviewContent(Workflow w, JPanel panelToPopulate, EventListener eventHandler) {
	if (w != null) {
	  try {
		StringBuffer content = new StringBuffer();
		content.append("<div class='outer'>");
		content.append("<div class='workflow'>");

		content.append("<br>");

		content.append("<p class='title'>");
		content.append("Workflow Entry: <a href='preview:" + Resource.WORKFLOW
			+ ":" + w.getURI() + "'>" + w.getTitle() + "</a> (version "
			+ w.getVersion() + ")");
		content.append("</p>");

		content.append("<br>");

		content.append("<p class='info'>");
		content.append("<b>Type:</b> " + w.getVisibleType() + "<br><br>");
		content.append("<b>Uploader:</b> <a href='preview:" + Resource.USER
			+ ":" + w.getUploader().getURI() + "'>" + w.getUploader().getName()
			+ "</a><br>");
		content.append("<b>Created at: </b> " + w.getCreatedAt() + "<br>");
		content.append("<b>License: </b> <a href='"
			+ w.getLicense().getLink()
			+ "'>"
			+ w.getLicense().getText()
			+ "</a>"
			+ "&nbsp;<img src='"
			+ MyExperimentPerspective.getLocalResourceURL("external_link_small_icon")
			+ "' />");
		content.append("</p>");

		content.append("<br>");

		content.append("<a href='" + w.getPreview() + "'>");
		content.append("<img class='preview' src='" + w.getThumbnailBig()
			+ "'></img>");
		content.append("</a>");

		content.append("<br>");
		content.append("<br>");

		if (!w.getDescription().equals("")) {
		  content.append("<p class='desc'>");
		  content.append("<br>");
		  content.append(Util.stripHTML(w.getDescription()));
		  content.append("<br>");
		  content.append("</p>");
		} else {
		  content.append("<span class='none_text'>No description</span>");
		}

		content.append("<br>");
		content.append("</div>");
		content.append("</div>");

		HTMLEditorKit kit = new StyledHTMLEditorKit(pluginMainComponent.getStyleSheet());
		HTMLDocument doc = (HTMLDocument) (kit.createDefaultDocument());
		doc.insertAfterStart(doc.getRootElements()[0].getElement(0), content.toString());

		// === Now render user's items as Swing components ===
		// .. TABS for components, tags, comments, credits, attributions ..
		JScrollPane spComponentsTab = createWorkflowComponentPreviewTab(w);
		JScrollPane spTagsTab = createTagPreviewTab(w.getTags());
		JScrollPane spCommentsTab = createCommentsPreviewTab(w.getComments());
		JScrollPane spCreditsTab = createCreditsPreviewTab(w.getCredits());
		JScrollPane spAttributionsTab = createAttributionsPreviewTab(w.getAttributions());

		// .. ASSEMBLE ALL TABS together
		JTabbedPane tpTabbedView = new JTabbedPane();
		tpTabbedView.add("Components", spComponentsTab);
		tpTabbedView.add("Tags (" + w.getTags().size() + ")", spTagsTab);
		tpTabbedView.add("Comments (" + w.getComments().size() + ")", spCommentsTab);
		tpTabbedView.addTab("Credits (" + w.getCredits().size() + ")", spCreditsTab);
		tpTabbedView.addTab("Attributions (" + w.getAttributions().size() + ")", spAttributionsTab);

		// PUT EVERYTHING TOGETHER
		JTextPane tpWorkflowPreview = new JTextPane();
		tpWorkflowPreview.setEditable(false);
		tpWorkflowPreview.setEditorKit(kit);
		tpWorkflowPreview.setDocument(doc);
		tpWorkflowPreview.addHyperlinkListener((HyperlinkListener) eventHandler);

		JPanel jpFullWorkflowPreview = wrapTextPaneAndTabbedViewIntoFullPreview(tpWorkflowPreview, tpTabbedView);

		// POPULATE THE GIVEN PANEL
		panelToPopulate.setLayout(new BorderLayout());
		panelToPopulate.add(jpFullWorkflowPreview, BorderLayout.CENTER);

		// this.statusLabel.setText("Workflow information found. Last fetched: "
		// + new Date().toString());

		// this.clearButton.setEnabled(true);
		// this.refreshButton.setEnabled(true);
		// this.loadButton.setEnabled(true);
		// this.importButton.setEnabled(true);
	  } catch (Exception e) {
		logger.error("Failed to populate Workflow Preview pane", e);
	  }
	} else {
	  // statusLabel.setText("Could not find information for workflow ID: " +
	  // currentWorkflowId);
	  // clearContentTextPane();
	  // disableButtons();
	}
  }

  private void generateFilePreviewContent(File f, JPanel panelToPopulate, EventListener eventHandler) {
	if (f != null) {
	  try {
		StringBuffer content = new StringBuffer();
		content.append("<div class='outer'>");
		content.append("<div class='file'>");

		content.append("<br>");

		content.append("<p class='title'>");
		content.append("File: <a href='preview:" + Resource.FILE + ":"
			+ f.getURI() + "'>" + f.getTitle() + "</a>");
		content.append("</p>");

		content.append("<br>");

		content.append("<p class='info'>");
		content.append("<b>Type:</b> " + f.getVisibleType() + "<br>");
		content.append("<b>Filename:</b> " + f.getFilename() + "<br><br>");
		content.append("<b>Uploader:</b> <a href='preview:" + Resource.USER
			+ ":" + f.getUploader().getURI() + "'>" + f.getUploader().getName()
			+ "</a><br>");
		content.append("<b>Created at: </b> " + f.getCreatedAt() + "<br>");
		content.append("<b>Last updated at: </b> " + f.getUpdatedAt() + "<br>");
		content.append("<b>License: </b> <a href='"
			+ f.getLicense().getLink()
			+ "'>"
			+ f.getLicense().getText()
			+ "</a>"
			+ "&nbsp;<img src='"
			+ MyExperimentPerspective.getLocalResourceURL("external_link_small_icon")
			+ "' />");
		content.append("</p>");

		content.append("<br>");

		if (!f.getDescription().equals("")) {
		  content.append("<p class='desc'>");
		  content.append("<br>");
		  content.append(Util.stripHTML(f.getDescription()));
		  content.append("<br>");
		  content.append("</p>");
		} else {
		  content.append("<span class='none_text'>No description</span>");
		}

		content.append("<br>");
		content.append("</div>");
		content.append("</div>");

		HTMLEditorKit kit = new StyledHTMLEditorKit(pluginMainComponent.getStyleSheet());
		HTMLDocument doc = (HTMLDocument) (kit.createDefaultDocument());
		doc.insertAfterStart(doc.getRootElements()[0].getElement(0), content.toString());

		// === Now render group's items as Swing components ===
		// TABS FOR file's tags, credits, etc
		JScrollPane spTagsTab = createTagPreviewTab(f.getTags());
		JScrollPane spCommentsTab = createCommentsPreviewTab(f.getComments());
		JScrollPane spCreditsTab = createCreditsPreviewTab(f.getCredits());
		JScrollPane spAttributionsTab = createAttributionsPreviewTab(f.getAttributions());

		// ASSEMBLE tabs into tabbed view
		JTabbedPane tpTabbedView = new JTabbedPane();
		tpTabbedView.add("Tags (" + f.getTags().size() + ")", spTagsTab);
		tpTabbedView.add("Comments (" + f.getComments().size() + ")", spCommentsTab);
		tpTabbedView.add("Credits (" + f.getCredits().size() + ")", spCreditsTab);
		tpTabbedView.add("Attributions (" + f.getAttributions().size() + ")", spAttributionsTab);

		// PUT EVERYTHING TOGETHER
		JTextPane tpFilePreview = new JTextPane();
		tpFilePreview.setEditable(false);
		tpFilePreview.setEditorKit(kit);
		tpFilePreview.setDocument(doc);
		tpFilePreview.addHyperlinkListener((HyperlinkListener) eventHandler);

		JPanel jpFullFilePreview = new JPanel();
		jpFullFilePreview.setBackground(Color.WHITE); // white background for
		// the whole file preview
		// panel
		jpFullFilePreview.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = GridBagConstraints.REMAINDER;
		c.gridy = 0;
		c.weighty = 0; // will not change size when the window is resized
		jpFullFilePreview.add(tpFilePreview, c);

		c.gridx = GridBagConstraints.REMAINDER;
		c.gridy = 1;
		c.weighty = 1; // will grow in size when the window is resized..
		c.fill = GridBagConstraints.VERTICAL; // ..and fill all available space
		// vertically
		c.insets = new Insets(20, 0, 5, 0); // a bit of margin at the top &
		// bottom
		jpFullFilePreview.add(tpTabbedView, c);

		// POPULATE THE GIVEN PANEL
		panelToPopulate.setLayout(new BorderLayout());
		panelToPopulate.add(jpFullFilePreview, BorderLayout.CENTER);

		// this.statusLabel.setText("File information found. Last fetched: " +
		// new Date().toString());

		// this.clearButton.setEnabled(true);
		// this.refreshButton.setEnabled(true);
		// this.loadButton.setEnabled(true);
		// this.importButton.setEnabled(true);
	  } catch (Exception e) {
		logger.error("Failed to populate File Preview pane", e);
	  }
	} else {
	  // statusLabel.setText("Could not find information for file ID: " +
	  // currentFileId);
	  // clearContentTextPane();
	  // disableButtons();
	}
  }

  private void generatePackPreviewContent(Pack p, JPanel panelToPopulate, EventListener eventHandler) {
	if (p != null) {
	  try {
		// === Render pack details in HTML format ===
		StringBuffer content = new StringBuffer();
		content.append("<div class='outer'>");
		content.append("<div class='pack'>");

		content.append("<br>");

		content.append("<p class='title'>");
		content.append("Pack: <a href='preview:" + Resource.PACK + ":"
			+ p.getURI() + "'>" + p.getTitle() + "</a>");
		content.append("</p>");

		content.append("<br>");

		content.append("<p class='info'>");
		content.append("<b>Creator:</b> <a href='preview:" + Resource.USER
			+ ":" + p.getCreator().getURI() + "'>" + p.getCreator().getName()
			+ "</a><br>");
		content.append("<b>Created at: </b> " + p.getCreatedAt() + "<br>");
		content.append("<b>Last updated at: </b> " + p.getUpdatedAt() + "<br>");
		content.append("</p>");

		content.append("<br>");

		if (!p.getDescription().equals("")) {
		  content.append("<p class='desc'>");
		  content.append("<br>");
		  content.append(Util.stripHTML(p.getDescription()));
		  content.append("<br>");
		  content.append("<br>");
		  content.append("</p>");
		} else {
		  content.append("<span class='none_text'>No description</span>");
		}

		content.append("<br>");
		content.append("</div>");
		content.append("</div>");

		HTMLEditorKit kit = new StyledHTMLEditorKit(pluginMainComponent.getStyleSheet());
		HTMLDocument doc = (HTMLDocument) (kit.createDefaultDocument());
		doc.insertAfterStart(doc.getRootElements()[0].getElement(0), content.toString());

		// === Now render group's items as Swing components ===
		// TABS FOR pack items, tags, etc
		JScrollPane spPackItemsTab = createPackItemPreviewTab(p);
		JScrollPane spTagsTab = createTagPreviewTab(p.getTags());
		JScrollPane spCommentsTab = createCommentsPreviewTab(p.getComments());

		// ASSEMBLE tabs into tabbed view
		JTabbedPane tpTabbedView = new JTabbedPane();
		tpTabbedView.addTab("Pack Items (" + p.getItemCount() + ")", spPackItemsTab);
		tpTabbedView.add("Tags (" + p.getTags().size() + ")", spTagsTab);
		tpTabbedView.add("Comments (" + p.getComments().size() + ")", spCommentsTab);

		// PUT EVERYTHING TOGETHER
		JTextPane tpPackPreview = new JTextPane();
		tpPackPreview.setEditable(false);
		tpPackPreview.setEditorKit(kit);
		tpPackPreview.setDocument(doc);
		tpPackPreview.addHyperlinkListener((HyperlinkListener) eventHandler);

		JPanel jpFullPackPreview = new JPanel();
		jpFullPackPreview.setBackground(Color.WHITE); // white background for
		// the whole pack preview
		// panel
		jpFullPackPreview.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = GridBagConstraints.REMAINDER;
		c.gridy = 0;
		c.weighty = 0; // will not change size when the window is resized
		jpFullPackPreview.add(tpPackPreview, c);

		c.gridx = GridBagConstraints.REMAINDER;
		c.gridy = 1;
		c.weighty = 1; // will grow in size when the window is resized..
		c.fill = GridBagConstraints.VERTICAL; // ..and fill all available space
		// vertically
		c.insets = new Insets(20, 0, 5, 0); // a bit of margin at the top &
		// bottom
		jpFullPackPreview.add(tpTabbedView, c);

		// POPULATE THE GIVEN PANEL
		panelToPopulate.setLayout(new BorderLayout());
		panelToPopulate.add(jpFullPackPreview, BorderLayout.CENTER);

		// this.statusLabel.setText("Pack information found. Last fetched: " +
		// new Date().toString());

		// this.clearButton.setEnabled(true);
		// this.refreshButton.setEnabled(true);
		// this.loadButton.setEnabled(true);
		// this.importButton.setEnabled(true);
	  } catch (Exception e) {
		logger.error("Failed to populate Pack Preview pane", e);
	  }
	} else {
	  // statusLabel.setText("Could not find information for pack ID: " +
	  // currentPackId);
	  // clearContentTextPane();
	  // disableButtons();
	}
  }

  private void generateUserPreviewContent(User u, JPanel panelToPopulate, EventListener eventHandler) {
	if (u != null) {
	  try {
		// === Render user details in HTML format ===
		StringBuffer content = new StringBuffer();
		content.append("<div class='outer'>");
		content.append("<div class='user'>");

		content.append("<br>");

		content.append("<p class='name'>");
		content.append("User: <a href=preview:" + Resource.USER + ":"
			+ u.getURI() + "'>" + u.getName() + "</a>");
		content.append("</p>");

		content.append("<br>");

		content.append("<p class='info'>");
		String strLocation;
		if (u.getCity().length() == 0 && u.getCountry().length() == 0)
		  strLocation = "<span class='none_text'>Not specified</span>";
		else
		  strLocation = u.getCity()
			  + (u.getCity().length() == 0 || u.getCountry().length() == 0 ? "" : ", ")
			  + u.getCountry();
		content.append("<b>Location:</b> " + strLocation + "<br>");
		content.append("<b>Joined at: </b> " + u.getCreatedAt() + "<br>");
		content.append("<b>Last seen at: </b> " + u.getUpdatedAt() + "<br>");
		content.append("</p>");

		content.append("<br>");

		content.append("<a href='" + u.getAvatarResource() + "'>");
		content.append("<img class='avatar' src='" + u.getAvatarResource()
			+ "'></img>");
		content.append("</a>");

		content.append("<br>");
		content.append("<br>");

		if (!u.getDescription().equals("")) {
		  // HACK: the way JAVA renders html causes styling not to be inherited;
		  // hence need to
		  // remove any nested <p> or <div> tags to get a proper layout
		  content.append("<p class='desc'>"
			  + Util.stripHTML(u.getDescription()) + "<br><br></p>");
		} else {
		  content.append("<span class='none_text'>No description</span>");
		}

		content.append("<p class='contact_details_header'>Contact Details</p>");
		content.append("<p class='contact_details'>");
		content.append("<b>Email: </b>"
			+ (u.getEmail().length() == 0 ? "<span class='none_text'>Not specified</span>" : u.getEmail())
			+ "<br>");
		content.append("<b>Website: </b>"
			+ (u.getWebsite().length() == 0 ? "<span class='none_text'>Not specified</span>" : u.getWebsite()));
		content.append("</p>");

		content.append("</div>");
		content.append("</div>");

		HTMLEditorKit kit = new StyledHTMLEditorKit(pluginMainComponent.getStyleSheet());
		HTMLDocument doc = (HTMLDocument) (kit.createDefaultDocument());
		doc.insertAfterStart(doc.getRootElements()[0].getElement(0), content.toString());

		// === Now render user's items as Swing components ===
		// .. WORKFLOWS ..
		JPanel jpWorkflowsTabContent = new JPanel();
		jpWorkflowsTabContent.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		jpWorkflowsTabContent.setLayout(new BoxLayout(jpWorkflowsTabContent, BoxLayout.Y_AXIS));

		// iterate through all workflows and add all to the panel
		Iterator<HashMap<String, String>> iWorkflows = u.getWorkflows().iterator();
		while (iWorkflows.hasNext()) {
		  HashMap<String, String> hmCurWF = iWorkflows.next();
		  jpWorkflowsTabContent.add(new JClickableLabel(hmCurWF.get("name"), "preview:"
			  + Resource.WORKFLOW + ":" + hmCurWF.get("uri"), pluginMainComponent.getPreviewBrowser(), this.iconWorkflow));
		}

		// .. FILES ..
		JPanel jpFilesTabContent = new JPanel();
		jpFilesTabContent.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		jpFilesTabContent.setLayout(new BoxLayout(jpFilesTabContent, BoxLayout.Y_AXIS));

		// iterate through all files and add all to the panel
		Iterator<HashMap<String, String>> iFiles = u.getFiles().iterator();
		while (iFiles.hasNext()) {
		  HashMap<String, String> hmCurFile = iFiles.next();
		  jpFilesTabContent.add(new JClickableLabel(hmCurFile.get("name"), "preview:"
			  + Resource.FILE + ":" + hmCurFile.get("uri"), pluginMainComponent.getPreviewBrowser(), this.iconFile));
		}

		// .. PACKS ..
		JPanel jpPacksTabContent = new JPanel();
		jpPacksTabContent.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		jpPacksTabContent.setLayout(new BoxLayout(jpPacksTabContent, BoxLayout.Y_AXIS));

		// iterate through all packs and add all to the panel
		Iterator<HashMap<String, String>> iPacks = u.getPacks().iterator();
		while (iPacks.hasNext()) {
		  HashMap<String, String> hmCurPack = iPacks.next();
		  jpPacksTabContent.add(new JClickableLabel(hmCurPack.get("name"), "preview:"
			  + Resource.PACK + ":" + hmCurPack.get("uri"), pluginMainComponent.getPreviewBrowser(), this.iconPack));
		}

		// .. FRIENDS ..
		JPanel jpFriendsTabContent = new JPanel();
		jpFriendsTabContent.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		jpFriendsTabContent.setLayout(new BoxLayout(jpFriendsTabContent, BoxLayout.Y_AXIS));

		// iterate through all friends and add all to the panel
		Iterator<HashMap<String, String>> iFriends = u.getFriends().iterator();
		while (iFriends.hasNext()) {
		  HashMap<String, String> hmCurFriend = iFriends.next();
		  jpFriendsTabContent.add(new JClickableLabel(hmCurFriend.get("name"), "preview:"
			  + Resource.USER + ":" + hmCurFriend.get("uri"), pluginMainComponent.getPreviewBrowser(), this.iconUser));
		}

		// .. GROUPS ..
		JPanel jpGroupsTabContent = new JPanel();
		jpGroupsTabContent.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		jpGroupsTabContent.setLayout(new BoxLayout(jpGroupsTabContent, BoxLayout.Y_AXIS));

		// iterate through all groups and add all to the panel
		Iterator<HashMap<String, String>> iGroups = u.getGroups().iterator();
		while (iGroups.hasNext()) {
		  HashMap<String, String> hmCurGroup = iGroups.next();
		  jpGroupsTabContent.add(new JClickableLabel(hmCurGroup.get("name"), "preview:"
			  + Resource.GROUP + ":" + hmCurGroup.get("uri"), pluginMainComponent.getPreviewBrowser(), this.iconGroup));
		}

		// .. WRAP EVERY TAB content into it's own scroll pane ..
		Dimension dPreferredTabSize = new Dimension(ResourcePreviewBrowser.PREFERRED_WIDTH - 50, PREFERRED_LOWER_TABBED_PANE_HEIGHT);

		JScrollPane spWorkflowsTab = new JScrollPane(jpWorkflowsTabContent);
		spWorkflowsTab.setBorder(BorderFactory.createEmptyBorder());
		spWorkflowsTab.setPreferredSize(dPreferredTabSize);
		spWorkflowsTab.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);

		JScrollPane spFilesTab = new JScrollPane(jpFilesTabContent);
		spFilesTab.setBorder(BorderFactory.createEmptyBorder());
		spFilesTab.setPreferredSize(dPreferredTabSize);
		spFilesTab.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);

		JScrollPane spPacksTab = new JScrollPane(jpPacksTabContent);
		spPacksTab.setBorder(BorderFactory.createEmptyBorder());
		spPacksTab.setPreferredSize(dPreferredTabSize);
		spPacksTab.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);

		JScrollPane spFriendsTab = new JScrollPane(jpFriendsTabContent);
		spFriendsTab.setBorder(BorderFactory.createEmptyBorder());
		spFriendsTab.setPreferredSize(dPreferredTabSize);
		spFriendsTab.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);

		JScrollPane spGroupsTab = new JScrollPane(jpGroupsTabContent);
		spGroupsTab.setBorder(BorderFactory.createEmptyBorder());
		spGroupsTab.setPreferredSize(dPreferredTabSize);
		spGroupsTab.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);

		// .. ASSEMBLE ALL TABS together
		JTabbedPane tpTabbedItems = new JTabbedPane();
		tpTabbedItems.addTab("Workflows (" + u.getWorkflows().size() + ")", spWorkflowsTab);
		tpTabbedItems.addTab("Files (" + u.getFiles().size() + ")", spFilesTab);
		tpTabbedItems.addTab("Packs (" + u.getPacks().size() + ")", spPacksTab);
		tpTabbedItems.addTab("Friends (" + u.getFriends().size() + ")", spFriendsTab);
		tpTabbedItems.addTab("Groups (" + u.getGroups().size() + ")", spGroupsTab);

		// === PUT EVERYTHING TOGETHER ===
		JTextPane tpUserPreview = new JTextPane();
		tpUserPreview.setEditable(false);
		tpUserPreview.setEditorKit(kit);
		tpUserPreview.setDocument(doc);
		tpUserPreview.addHyperlinkListener((HyperlinkListener) eventHandler);

		JPanel jpFullUserPreview = new JPanel();
		jpFullUserPreview.setBackground(Color.WHITE); // white background for
		// the whole user preview
		// panel
		jpFullUserPreview.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = GridBagConstraints.REMAINDER;
		c.gridy = 0;
		c.weighty = 0; // will not change size when the window is resized
		jpFullUserPreview.add(tpUserPreview, c);

		c.gridx = GridBagConstraints.REMAINDER;
		c.gridy = 1;
		c.weighty = 1; // will grow in size when the window is resized..
		c.fill = GridBagConstraints.VERTICAL; // ..and fill all available space
		// vertically
		c.insets = new Insets(20, 0, 5, 0); // a bit of margin at the top &
		// bottom
		jpFullUserPreview.add(tpTabbedItems, c);

		// POPULATE THE GIVEN PANEL
		panelToPopulate.setLayout(new BorderLayout());
		panelToPopulate.add(jpFullUserPreview, BorderLayout.CENTER);

		// this.statusLabel.setText("User information found. Last fetched: " +
		// new Date().toString());

		// this.clearButton.setEnabled(true);
		// this.refreshButton.setEnabled(true);
		// this.loadButton.setEnabled(true);
		// this.importButton.setEnabled(true);
	  } catch (Exception e) {
		logger.error("Failed to populate Workflow Preview pane", e);
	  }
	} else {
	  // statusLabel.setText("Could not find information for workflow ID: " +
	  // currentWorkflowId);
	  // clearContentTextPane();
	  // disableButtons();
	}
  }

  private void generateGroupPreviewContent(Group g, JPanel panelToPopulate, EventListener eventHandler) {
	if (g != null) {
	  try {
		// === Render group details in HTML format ===
		StringBuffer content = new StringBuffer();
		content.append("<div class='outer'>");
		content.append("<div class='group'>");

		content.append("<br>");

		content.append("<p class='title'>");
		content.append("Group: <a href='preview:" + Resource.GROUP + ":"
			+ g.getURI() + "'>" + g.getTitle() + "</a>");
		content.append("</p>");

		content.append("<br>");

		content.append("<p class='info'>");
		content.append("<b>Administrator:</b> <a href='preview:"
			+ Resource.USER + ":" + g.getAdmin().getURI() + "'>"
			+ g.getAdmin().getName() + "</a><br>");
		content.append("<b>Created at: </b> " + g.getCreatedAt() + "<br>");
		content.append("</p>");

		content.append("<br>");

		if (!g.getDescription().equals("")) {
		  content.append("<p class='desc'>");
		  content.append("<br>");
		  content.append(Util.stripHTML(g.getDescription()));
		  content.append("<br>");
		  content.append("</p>");
		} else {
		  content.append("<span class='none_text'>No description</span>");
		}

		content.append("<br>");
		content.append("</div>");
		content.append("</div>");

		HTMLEditorKit kit = new StyledHTMLEditorKit(pluginMainComponent.getStyleSheet());
		HTMLDocument doc = (HTMLDocument) (kit.createDefaultDocument());
		doc.insertAfterStart(doc.getRootElements()[0].getElement(0), content.toString());

		// === Now render group's items as Swing components ===

		// .. MEMBERS ..
		JPanel jpMembersTabContent = createStandardTabContentPanel();

		// iterate through all shared items and add all to the panel
		Iterator<User> iMembers = g.getMembers().iterator();
		while (iMembers.hasNext()) {
		  User uCurMember = iMembers.next();
		  jpMembersTabContent.add(new JClickableLabel(uCurMember.getName(), "preview:"
			  + uCurMember.getItemType() + ":" + uCurMember.getURI(), pluginMainComponent.getPreviewBrowser(), new ImageIcon(MyExperimentPerspective.getLocalIconURL(uCurMember.getItemType()))));
		}

		// wrap into a standard scroll pane
		JScrollPane spMembersTabContent = wrapPreviewTabContentIntoScrollPane(jpMembersTabContent);

		// .. SHARED ITEMS ..
		JPanel jpSharedItemsTabContent = createStandardTabContentPanel();

		// iterate through all shared items and add all to the panel
		Iterator<Resource> iSharedItems = g.getSharedItems().iterator();
		while (iSharedItems.hasNext()) {
		  Resource rCurItem = iSharedItems.next();
		  jpSharedItemsTabContent.add(new JClickableLabel(rCurItem.getTitle(), "preview:"
			  + rCurItem.getItemType() + ":" + rCurItem.getURI(), pluginMainComponent.getPreviewBrowser(), new ImageIcon(MyExperimentPerspective.getLocalIconURL(rCurItem.getItemType()))));
		}

		// wrap into a standard scroll pane
		JScrollPane spSharedItemsTabContent = wrapPreviewTabContentIntoScrollPane(jpSharedItemsTabContent);

		// .. TAGS, COMMENTS ..
		JScrollPane spTagsTabContent = createTagPreviewTab(g.getTags());
		JScrollPane spCommentsTab = createCommentsPreviewTab(g.getComments());

		// ASSEMBLE tabs together
		JTabbedPane tpTabbedItems = new JTabbedPane();
		tpTabbedItems.addTab("Members (" + g.getMemberCount() + ")", spMembersTabContent);
		tpTabbedItems.addTab("Shared Items (" + g.getSharedItemCount() + ")", spSharedItemsTabContent);
		tpTabbedItems.addTab("Tags (" + g.getTags().size() + ")", spTagsTabContent);
		tpTabbedItems.addTab("Comments (" + g.getComments().size() + ")", spCommentsTab);

		// PUT EVERYTHING TOGETHER
		JTextPane tpGroupPreview = new JTextPane();
		tpGroupPreview.setEditable(false);
		tpGroupPreview.setEditorKit(kit);
		tpGroupPreview.setDocument(doc);
		tpGroupPreview.addHyperlinkListener((HyperlinkListener) eventHandler);

		JPanel jpFullGroupPreview = new JPanel();
		jpFullGroupPreview.setBackground(Color.WHITE); // white background for
		// the whole group
		// preview panel
		jpFullGroupPreview.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = GridBagConstraints.REMAINDER;
		c.gridy = 0;
		c.weighty = 0; // will not change size when the window is resized
		jpFullGroupPreview.add(tpGroupPreview, c);

		c.gridx = GridBagConstraints.REMAINDER;
		c.gridy = 1;
		c.weighty = 1; // will grow in size when the window is resized..
		c.fill = GridBagConstraints.VERTICAL; // ..and fill all available space
		// vertically
		c.insets = new Insets(20, 0, 5, 0); // a bit of margin at the top &
		// bottom
		jpFullGroupPreview.add(tpTabbedItems, c);

		// POPULATE THE GIVEN PANEL
		panelToPopulate.setLayout(new BorderLayout());
		panelToPopulate.add(jpFullGroupPreview, BorderLayout.CENTER);

		// this.statusLabel.setText("Group information found. Last fetched: " +
		// new Date().toString());

		// this.clearButton.setEnabled(true);
		// this.refreshButton.setEnabled(true);
		// this.loadButton.setEnabled(true);
		// this.importButton.setEnabled(true);
	  } catch (Exception e) {
		logger.error("Failed to populate Group Preview pane", e);
	  }
	} else {
	  // statusLabel.setText("Could not find information for group ID: " +
	  // currentGroupId);
	  // clearContentTextPane();
	  // disableButtons();
	}
  }

  // *** Helper methods follow that generate particular reusable pieces of the
  // previews ***

  private JScrollPane createWorkflowComponentPreviewTab(Workflow w) {
	final JPanel jpWorkflowComponentsTabContent = createStandardTabContentPanel();

	if (!w.isTavernaWorkflow()) {
	  // can only display components for Taverna 1 workflows at the moment
	  JLabel lNotSupported = new JLabel("<html>This is a "
		  + w.getVisibleType()
		  + " workflow;<br>myExperiment can only display Taverna workflow components at the moment.</html>");
	  lNotSupported.setFont(lNotSupported.getFont().deriveFont(Font.ITALIC));
	  lNotSupported.setForeground(Color.GRAY);
	  jpWorkflowComponentsTabContent.add(lNotSupported);
	} else if (!w.isDownloadAllowed()) {
	  // can display components for workflow of this type, but current user is
	  // not
	  // allowed to download this workflow - and, hence, to view its components
	  JLabel lNotAuthorized = new JLabel("You are not authorised to download this workflow, "
		  + "and hence component preview is not available.");
	  lNotAuthorized.setFont(lNotAuthorized.getFont().deriveFont(Font.ITALIC));
	  lNotAuthorized.setForeground(Color.GRAY);
	  jpWorkflowComponentsTabContent.add(lNotAuthorized);
	} else {
	  // can display the components

	  // storage for table column names
	  Vector<String> vColumnNames = new Vector<String>();

	  // ** inputs **
	  vColumnNames.clear();
	  vColumnNames.addAll(Arrays.asList(new String[] { "Name", "Description" }));

	  Vector<Vector<String>> vInputsData = new Vector<Vector<String>>();
	  ArrayList<HashMap<String, String>> inputs = w.getComponents().get("inputs");
	  if (inputs != null) {
		for (HashMap<String, String> curInput : inputs) {
		  Vector<String> vCurData = new Vector<String>();
		  vCurData.add(curInput.get("name"));
		  vCurData.add(curInput.get("description"));

		  vInputsData.add(vCurData);
		}
	  }

	  JTable jtInputs = new JTable(vInputsData, vColumnNames);
	  jtInputs.getColumnModel().getColumn(0).setPreferredWidth(100);
	  jtInputs.getTableHeader().setFont(jtInputs.getTableHeader().getFont().deriveFont(Font.BOLD));
	  JPanel jpInputs = new JPanel();
	  jpInputs.setLayout(new BorderLayout());
	  jpInputs.add(jtInputs.getTableHeader(), BorderLayout.NORTH);
	  jpInputs.add(jtInputs, BorderLayout.CENTER);

	  JPanel jpInputsWithTitle = new JPanel();
	  jpInputsWithTitle.setBorder(BorderFactory.createEtchedBorder());
	  jpInputsWithTitle.setLayout(new BorderLayout());
	  jpInputsWithTitle.add(new ShadedLabel("Workflow input ports ("
		  + vInputsData.size() + ")", ShadedLabel.BLUE, true), BorderLayout.NORTH);
	  if (vInputsData.size() > 0) {
		jpInputsWithTitle.add(jpInputs, BorderLayout.CENTER);
	  }

	  // ** processors **
	  vColumnNames.clear();
	  vColumnNames.addAll(Arrays.asList(new String[] { "Name", "Type", "Description" }));

	  Vector<Vector<String>> vProcessorsData = new Vector<Vector<String>>();
	  ArrayList<HashMap<String, String>> processors = w.getComponents().get("processors");
	  if (processors != null) {
		for (HashMap<String, String> curProcessor : processors) {
		  Vector<String> vCurData = new Vector<String>();
		  vCurData.add(curProcessor.get("name"));
		  vCurData.add(curProcessor.get("type"));
		  vCurData.add(curProcessor.get("description"));

		  vProcessorsData.add(vCurData);
		}
	  }

	  JTable jtProcessors = new JTable(vProcessorsData, vColumnNames);
	  jtProcessors.getTableHeader().setFont(jtProcessors.getTableHeader().getFont().deriveFont(Font.BOLD));
	  JPanel jpProcessors = new JPanel();
	  jpProcessors.setLayout(new BorderLayout());
	  jpProcessors.add(jtProcessors.getTableHeader(), BorderLayout.NORTH);
	  jpProcessors.add(jtProcessors, BorderLayout.CENTER);

	  JPanel jpProcessorsWithTitle = new JPanel();
	  jpProcessorsWithTitle.setBorder(BorderFactory.createEtchedBorder());
	  jpProcessorsWithTitle.setLayout(new BorderLayout());
	  jpProcessorsWithTitle.add(new ShadedLabel("Services ("
		  + vProcessorsData.size() + ")", ShadedLabel.BLUE, true), BorderLayout.NORTH);
	  if (vProcessorsData.size() > 0) {
		jpProcessorsWithTitle.add(jpProcessors, BorderLayout.CENTER);
	  }

	  // ** links **
	  vColumnNames.clear();
	  vColumnNames.addAll(Arrays.asList(new String[] { "Source", "Sink" }));

	  Vector<Vector<String>> vLinksData = new Vector<Vector<String>>();
	  ArrayList<HashMap<String, String>> links = w.getComponents().get("links");
	  if (links != null) {
		for (HashMap<String, String> curLink : links) {
		  Vector<String> vCurData = new Vector<String>();
		  vCurData.add(curLink.get("source"));
		  vCurData.add(curLink.get("sink"));

		  vLinksData.add(vCurData);
		}
	  }

	  JTable jtLinks = new JTable(vLinksData, vColumnNames);
	  jtLinks.getColumnModel().getColumn(0).setPreferredWidth(100);
	  jtLinks.getTableHeader().setFont(jtLinks.getTableHeader().getFont().deriveFont(Font.BOLD));
	  JPanel jpLinks = new JPanel();
	  jpLinks.setLayout(new BorderLayout());
	  jpLinks.add(jtLinks.getTableHeader(), BorderLayout.NORTH);
	  jpLinks.add(jtLinks, BorderLayout.CENTER);

	  JPanel jpLinksWithTitle = new JPanel();
	  jpLinksWithTitle.setBorder(BorderFactory.createEtchedBorder());
	  jpLinksWithTitle.setLayout(new BorderLayout());
	  jpLinksWithTitle.add(new ShadedLabel("Links (" + vLinksData.size() + ")", ShadedLabel.BLUE, true), BorderLayout.NORTH);
	  if (vLinksData.size() > 0) {
		jpLinksWithTitle.add(jpLinks, BorderLayout.CENTER);
	  }

	  // ** outputs **
	  vColumnNames.clear();
	  vColumnNames.addAll(Arrays.asList(new String[] { "Name", "Description" }));

	  Vector<Vector<String>> vOutputsData = new Vector<Vector<String>>();
	  ArrayList<HashMap<String, String>> outputs = w.getComponents().get("outputs");
	  if (outputs != null) {
		for (HashMap<String, String> curOutput : outputs) {
		  Vector<String> vCurData = new Vector<String>();
		  vCurData.add(curOutput.get("name"));
		  vCurData.add(curOutput.get("description"));

		  vOutputsData.add(vCurData);
		}
	  }

	  JTable jtOutputs = new JTable(vOutputsData, vColumnNames);
	  jtOutputs.getColumnModel().getColumn(0).setPreferredWidth(100);
	  jtOutputs.getTableHeader().setFont(jtOutputs.getTableHeader().getFont().deriveFont(Font.BOLD));
	  JPanel jpOutputs = new JPanel();
	  jpOutputs.setLayout(new BorderLayout());
	  jpOutputs.add(jtOutputs.getTableHeader(), BorderLayout.NORTH);
	  jpOutputs.add(jtOutputs, BorderLayout.CENTER);

	  JPanel jpOutputsWithTitle = new JPanel();
	  jpOutputsWithTitle.setBorder(BorderFactory.createEtchedBorder());
	  jpOutputsWithTitle.setLayout(new BorderLayout());
	  jpOutputsWithTitle.add(new ShadedLabel("Workflow output ports ("
		  + vOutputsData.size() + ")", ShadedLabel.BLUE, true), BorderLayout.NORTH);
	  if (vOutputsData.size() > 0) {
		jpOutputsWithTitle.add(jpOutputs, BorderLayout.CENTER);
	  }

	  // PUT EVERYTHING TOGETHER
	  jpWorkflowComponentsTabContent.setLayout(new GridBagLayout());
	  GridBagConstraints c = new GridBagConstraints();

	  c.gridx = 0;
	  c.gridy = GridBagConstraints.RELATIVE;
	  c.weightx = 1.0;
	  c.fill = GridBagConstraints.HORIZONTAL;
	  c.anchor = GridBagConstraints.NORTH;
	  jpWorkflowComponentsTabContent.add(jpInputsWithTitle, c);

	  c.insets = new Insets(10, 0, 0, 0);
	  jpWorkflowComponentsTabContent.add(jpProcessorsWithTitle, c);

	  jpWorkflowComponentsTabContent.add(jpLinksWithTitle, c);

	  c.weighty = 1.0; // ONLY FOR THE LAST ELEMENT
	  jpWorkflowComponentsTabContent.add(jpOutputsWithTitle, c);
	}

	return (wrapPreviewTabContentIntoScrollPane(jpWorkflowComponentsTabContent));
  }

  private JScrollPane createPackItemPreviewTab(Pack p) {
	JPanel jpPackItemsTabContent = createStandardTabContentPanel();
	GridBagConstraints c = new GridBagConstraints();
	jpPackItemsTabContent.setLayout(new GridBagLayout());
	c.anchor = GridBagConstraints.NORTHWEST;

	// iterate through all internal and external items and add all to the panel
	if (p.getItems().size() > 0) {
	  int iCnt = 0;
	  boolean bNoCommentForPrevItem = false;

	  for (PackItem piCurItem : p.getItems()) {
		c.gridx = 0;
		c.gridy = 3 * iCnt;
		c.weightx = 1.0;
		c.insets = (bNoCommentForPrevItem ? new Insets(7, 0, 0, 0) : new Insets(0, 0, 0, 0));
		c.fill = GridBagConstraints.NONE;
		// item data is stored differently whether the item is internal or
		// external
		if (piCurItem.isInternalItem()) {
		  jpPackItemsTabContent.add(new JClickableLabel(piCurItem.getItem().getTitle(), "preview:"
			  + piCurItem.getItem().getItemType()
			  + ":"
			  + piCurItem.getItem().getURI(), pluginMainComponent.getPreviewBrowser(), new ImageIcon(MyExperimentPerspective.getLocalIconURL(piCurItem.getItem().getItemType()))), c);
		} else {
		  jpPackItemsTabContent.add(new JClickableLabel(piCurItem.getTitle(), piCurItem.getLink(), // link should open up directly in the web
		  // browser
		  pluginMainComponent.getPreviewBrowser(), new ImageIcon(MyExperimentPerspective.getLocalIconURL(piCurItem.getItemType())), SwingConstants.LEFT, piCurItem.getTitle()
			  + " (link: " + piCurItem.getLink() + ")"), c);
		}

		// prepare comment before populating the metadata
		String strComment = Util.stripAllHTML(piCurItem.getComment());
		bNoCommentForPrevItem = (strComment == null || strComment.length() == 0);

		// add metadata to the item ..
		// .. who and when added the item ..
		JPanel jpWhoAddedTheItem = new JPanel();
		jpWhoAddedTheItem.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		c1.anchor = GridBagConstraints.NORTHWEST;
		jpWhoAddedTheItem.setBorder(BorderFactory.createEmptyBorder());
		jpWhoAddedTheItem.add(new JLabel("Added by "), c1);
		jpWhoAddedTheItem.add(new JClickableLabel(piCurItem.getUserWhoAddedTheItem().getName(), "preview:"
			+ Resource.USER + ":" + piCurItem.getUserWhoAddedTheItem().getURI(), pluginMainComponent.getPreviewBrowser()), c1);

		String strAddedOnDate = MyExperimentClient.formatDate(piCurItem.getCreatedAt());
		c1.weightx = 1.0;
		jpWhoAddedTheItem.add(new JLabel(" [" + strAddedOnDate + "]"), c1);

		c.gridx = 0;
		c.gridy = 3 * iCnt + 1;
		c.insets = new Insets(0, 25, 0, 0);
		c.weightx = 1.0;
		if (bNoCommentForPrevItem && (iCnt + 1 == p.getItems().size()))
		  c.weighty = 1.0;
		jpPackItemsTabContent.add(jpWhoAddedTheItem, c);

		// .. and the comment
		if (!bNoCommentForPrevItem) {
		  c.gridx = 0;
		  c.gridy = 3 * iCnt + 2;
		  c.fill = GridBagConstraints.HORIZONTAL;
		  c.insets = new Insets(0, 25, 7, 25);
		  c.weightx = 1.0;
		  if (iCnt + 1 == p.getItems().size())
			c.weighty = 1.0; // only if this is the comment for the last item,
		  // shift all items to the top of the panel

		  DialogTextArea taCommentText = new DialogTextArea("Comment: "
			  + strComment);
		  taCommentText.setOpaque(false);
		  taCommentText.setEditable(false);
		  taCommentText.setLineWrap(true);
		  taCommentText.setWrapStyleWord(true);
		  jpPackItemsTabContent.add(taCommentText, c);
		}

		// update the item counter
		iCnt++;
	  }
	} else {
	  c.weighty = 1.0;
	  c.weightx = 1.0;
	  jpPackItemsTabContent.add(Util.generateNoneTextLabel("None"), c);
	}

	return (wrapPreviewTabContentIntoScrollPane(jpPackItemsTabContent));
  }

  private JScrollPane createTagPreviewTab(List<Tag> lTags) {
	TagCloudPanel jpTagTabContent = new TagCloudPanel("Resource tag cloud", TagCloudPanel.TAGCLOUD_TYPE_RESOURCE_PREVIEW, pluginMainComponent.getPreviewBrowser(), pluginMainComponent, myExperimentClient, logger);
	jpTagTabContent.getTagCloudData().clear();
	jpTagTabContent.getTagCloudData().addAll(lTags);
	jpTagTabContent.refresh();

	// tag cloud panel itself already has a scroll pane in it; hence the outer
	// scroll pane
	// is only used for consistency across the user interface (contents of all
	// tabs in the
	// preview window are scroll panes); the preferred size of the tag cloud
	// panel should
	// still be adjusted accordingly to the preferred size of the outer scroll
	// pane
	JScrollPane spTagTabContent = wrapPreviewTabContentIntoScrollPane(jpTagTabContent);
	spTagTabContent.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	spTagTabContent.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

	jpTagTabContent.setPreferredSize(spTagTabContent.getPreferredSize());

	return (spTagTabContent);
  }

  private JScrollPane createCommentsPreviewTab(List<Comment> comments) {
	final List<Comment> lComments = comments;
	final JPanel jpCommentsTabContent = createStandardTabContentPanel();

	if (lComments.size() > 0) {
	  final GridBagConstraints c = new GridBagConstraints();
	  jpCommentsTabContent.setLayout(new GridBagLayout());
	  c.anchor = GridBagConstraints.NORTHWEST;

	  // a placeholder for comments while they are loading
	  JLabel lLoading = new JLabel("Loading comments...", new ImageIcon(MyExperimentPerspective.getLocalResourceURL("spinner")), SwingConstants.LEFT);
	  lLoading.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
	  c.weightx = 1.0;
	  c.weighty = 1.0;
	  jpCommentsTabContent.add(lLoading, c);

	  new Thread("Load comments for preview") {
		@Override
		public void run() {
		  myExperimentClient.updateCommentListWithExtraData(lComments);

		  SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			  // remove 'loading...' placeholder
			  jpCommentsTabContent.removeAll();

			  int iCnt = 0;
			  for (Comment comment : lComments) {
				c.gridx = 0;
				c.gridy = 2 * iCnt;
				c.weightx = 0;
				c.weighty = 0;
				c.gridwidth = 1;
				JClickableLabel lCommentAuthor = new JClickableLabel(comment.getUser().getName(), "preview:"
					+ comment.getUser().getItemType()
					+ ":"
					+ comment.getUser().getURI(), pluginMainComponent.getPreviewBrowser(), iconUser);
				jpCommentsTabContent.add(lCommentAuthor, c);

				c.gridx = 1;
				c.gridy = 2 * iCnt;
				c.weightx = 1.0;
				String strCommentDate = MyExperimentClient.formatDate(comment.getCreatedAt());
				JLabel lCommentDate = new JLabel(" - [" + strCommentDate + "]");
				lCommentDate.setBorder(lCommentAuthor.getBorder());
				jpCommentsTabContent.add(lCommentDate, c);

				c.gridx = 0;
				c.gridy = 2 * iCnt + 1;
				c.weightx = 1.0;
				c.gridwidth = 2;
				c.fill = GridBagConstraints.HORIZONTAL;
				if (iCnt + 1 == lComments.size())
				  c.weighty = 1.0;

				DialogTextArea taCommentText = new DialogTextArea(Util.stripAllHTML(comment.getComment()));
				taCommentText.setBorder(BorderFactory.createEmptyBorder(0, 25, 10, 25));
				taCommentText.setOpaque(false);
				taCommentText.setEditable(false);
				taCommentText.setLineWrap(true);
				taCommentText.setWrapStyleWord(true);
				jpCommentsTabContent.add(taCommentText, c);

				iCnt++;
			  }

			  jpCommentsTabContent.validate();
			  jpCommentsTabContent.repaint();

			  // this will ensure that even if there are many comments,
			  // the comment panel is still shown starting at the top comment
			  SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				  jpCommentsTabContent.scrollRectToVisible(new Rectangle());
				}
			  });
			}
		  });
		}
	  }.start();
	} else {
	  jpCommentsTabContent.add(Util.generateNoneTextLabel("None"));
	}

	JScrollPane spCommentsTabContent = wrapPreviewTabContentIntoScrollPane(jpCommentsTabContent);
	spCommentsTabContent.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	return (spCommentsTabContent);
  }

  private JScrollPane createCreditsPreviewTab(List<Resource> lCreditedUsersOrGroups) {
	JPanel jpCreditsTabContent = createStandardTabContentPanel();

	if (lCreditedUsersOrGroups.size() > 0) {
	  for (Resource r : lCreditedUsersOrGroups) {
		jpCreditsTabContent.add(new JClickableLabel(r.getTitle(), "preview:"
			+ r.getItemType() + ":" + r.getURI(), pluginMainComponent.getPreviewBrowser(), new ImageIcon(MyExperimentPerspective.getLocalIconURL(r.getItemType()))));
	  }
	} else {
	  jpCreditsTabContent.add(Util.generateNoneTextLabel("None"));
	}

	return (wrapPreviewTabContentIntoScrollPane(jpCreditsTabContent));
  }

  private JScrollPane createAttributionsPreviewTab(List<Resource> lAttributions) {
	JPanel jpAttributionsTabContent = createStandardTabContentPanel();

	if (lAttributions.size() > 0) {
	  for (Resource r : lAttributions) {
		jpAttributionsTabContent.add(new JClickableLabel(r.getTitle(), "preview:"
			+ r.getItemType() + ":" + r.getURI(), pluginMainComponent.getPreviewBrowser(), new ImageIcon(MyExperimentPerspective.getLocalIconURL(r.getItemType()))));
	  }
	} else {
	  jpAttributionsTabContent.add(Util.generateNoneTextLabel("None"));
	}

	return (wrapPreviewTabContentIntoScrollPane(jpAttributionsTabContent));
  }

  /**
   * A standard starting point for all preview window tabs.
   */
  private JPanel createStandardTabContentPanel() {
	JPanel jpTabContentPanel = new JPanel();
	jpTabContentPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
	jpTabContentPanel.setLayout(new BoxLayout(jpTabContentPanel, BoxLayout.Y_AXIS));

	return (jpTabContentPanel);
  }

  private JScrollPane wrapPreviewTabContentIntoScrollPane(JPanel jpTabContentPanel) {
	// WRAPS TAB CONTENT into it's own SCROLL PANE ..
	Dimension dPreferredTabSize = new Dimension(ResourcePreviewBrowser.PREFERRED_WIDTH - 50, PREFERRED_LOWER_TABBED_PANE_HEIGHT);

	JScrollPane spTabContent = new JScrollPane(jpTabContentPanel);
	spTabContent.setBorder(BorderFactory.createEmptyBorder());
	spTabContent.setPreferredSize(dPreferredTabSize);
	spTabContent.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);

	return (spTabContent);
  }

  private JPanel wrapTextPaneAndTabbedViewIntoFullPreview(JTextPane tpHTMLPreview, JTabbedPane tpTabbedView) {
	// WRAPS HTML JTextPane PREVIEW AND A JTabbedPane WITH DETAILS INTO A SINGLE
	// PREVIEW PANEL
	JPanel jpFullPreview = new JPanel();
	jpFullPreview.setBackground(Color.WHITE); // white background for the whole
	// preview panel
	jpFullPreview.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();

	c.gridx = GridBagConstraints.REMAINDER;
	c.gridy = 0;
	c.weighty = 0; // will not change size when the window is resized
	jpFullPreview.add(tpHTMLPreview, c);

	c.gridx = GridBagConstraints.REMAINDER;
	c.gridy = 1;
	c.weighty = 1; // will grow in size when the window is resized..
	c.fill = GridBagConstraints.VERTICAL; // ..and fill all available space
	// vertically
	c.insets = new Insets(20, 0, 5, 0); // a bit of margin at the top & bottom
	jpFullPreview.add(tpTabbedView, c);

	return (jpFullPreview);
  }

}
