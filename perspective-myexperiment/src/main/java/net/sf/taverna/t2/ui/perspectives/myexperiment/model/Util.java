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
package net.sf.taverna.t2.ui.perspectives.myexperiment.model;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.ui.perspectives.myexperiment.JClickableLabel;
import net.sf.taverna.t2.ui.perspectives.myexperiment.MyExperimentPerspective;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.SearchEngine.QuerySearchInstance;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Sergejs Aleksejevs
 */
public class Util {
  private static Logger logger = Logger.getLogger(Util.class);

  // ******** DATA ENCRYPTION ********

  private static final String PBE_PASSWORD = System.getProperty("user.home");
  private static final String PBE_SALT;

  static {
	String host_name = "";
	try {
	  host_name = InetAddress.getLocalHost().toString();
	} catch (UnknownHostException e) {
	  host_name = "unknown_localhost";
	}
	PBE_SALT = host_name;
  }

  /**
   * The following section (encrypt(), decrypt() and doEncryption() methods) is
   * used to store user passwords in an encrypted way within the settings file.
   */
  public static byte[] encrypt(String str) {
	return (doEncryption(str, Cipher.ENCRYPT_MODE));
  }

  public static byte[] decrypt(String str) {
	return (doEncryption(str, Cipher.DECRYPT_MODE));
  }

  private static byte[] doEncryption(String str, int mode) {
	// password-based encryption uses 2 parameters for processing:
	// a *password*, which is then hashed with a *salt* to generate
	// a strong key - these 2 are defined as class constants
	try {
	  SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
	  PBEKeySpec keySpec = new PBEKeySpec(Util.PBE_PASSWORD.toCharArray());
	  SecretKey key = keyFactory.generateSecret(keySpec);
	  MessageDigest md = MessageDigest.getInstance("MD5");
	  md.update(Util.PBE_SALT.getBytes());
	  byte[] digest = md.digest();
	  byte[] salt = new byte[8];
	  for (int i = 0; i < 8; ++i)
		salt[i] = digest[i];
	  PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 20);

	  Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
	  cipher.init(mode, key, paramSpec);

	  byte[] encrypted = cipher.doFinal(str.getBytes());
	  return (encrypted);
	} catch (Exception e) {
	  logger.error("Could not encrypt and store password");
	  logger.error(e.getCause() + "\n" + e.getMessage());
	  return (new byte[1]);
	}

  }

  // ******** RESIZING OF IMAGES ********

  /**
   * The method will scale down the <b>sourceImageIcon</b> in a way that it will
   * fit into the rectangle with dimensions <b>(iRequiredWidth,
   * iRequiredHeight)</b> with the original aspect ratio being preserved.
   * 
   * @param iRequiredWidth
   *          Maximum desired width of the resized image.
   * @param iRequiredHeight
   *          Maximum desired height of the resized image.
   */
  public static ImageIcon getResizedImageIcon(ImageIcon sourceImageIcon, int iRequiredWidth, int iRequiredHeight) {
	// *** calculate the desired width and height of the resized image ***
	int iWidth = sourceImageIcon.getIconWidth();
	int iHeight = sourceImageIcon.getIconHeight();

	float fWidthResizeRatio = iWidth / (float) iRequiredWidth;
	float fHeightResizeRatio = iHeight / (float) iRequiredHeight;

	// the chosen resize ratio will be the greatest of the two -
	// this way we will preserve aspect ratio of the sides of the
	// original image
	float fResizeRatio = Math.max(fWidthResizeRatio, fHeightResizeRatio);

	// obtain the width and height of the new image
	int iNewWidth = Math.round(iWidth / fResizeRatio);
	int iNewHeight = Math.round(iHeight / fResizeRatio);

	// *** make the actual resizing work ***
	BufferedImage resizedImage = new BufferedImage(iNewWidth, iNewHeight, BufferedImage.TYPE_INT_RGB);
	Graphics2D g2 = resizedImage.createGraphics();
	g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	g2.drawImage(sourceImageIcon.getImage(), 0, 0, iNewWidth, iNewHeight, null);
	g2.dispose();
	return (new ImageIcon(resizedImage, ""));
  }

  /**
   * Loads an image from a remote URL, scales it down to the required size and
   * stores in a temporary file for the duration of the current session (e.g.
   * the temporary file will be destroyed on the application termination).
   * 
   * @param sourceImageURL
   *          URL to fetch the source image from.
   * @param iRequiredWidth
   *          Maximum desired width of the resized image.
   * @param iRequiredHeight
   *          Maximum desired height of the resized image.
   * 
   * @return Local file URL which can be directly used to fetch the resized
   *         image.
   */
  public static String getResizedImageIconTempFileURL(URL sourceImageURL, int iRequiredWidth, int iRequiredHeight) {
	java.io.File fDestinationTempFile = null;

	try {
	  // all resized images will be store in temporary files - these will be deleted on program termination
	  fDestinationTempFile = java.io.File.createTempFile("resized_image", "jpg");
	  fDestinationTempFile.deleteOnExit();

	  // resize the image icon
	  ImageIcon sourceImageIcon = new ImageIcon(sourceImageURL);
	  ImageIcon resizedImageIcon = Util.getResizedImageIcon(sourceImageIcon, iRequiredWidth, iRequiredHeight);

	  Image img = resizedImageIcon.getImage();
	  BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
	  Graphics2D g2 = bi.createGraphics();
	  // Draw img into bi so we can write it to file.
	  g2.drawImage(img, 0, 0, null);
	  g2.dispose();
	  // Now bi contains the img.
	  // Note: img may have transparent pixels in it; if so, okay.
	  // If not and you can use TYPE_INT_RGB you will get better
	  // performance with it in the jvm.
	  ImageIO.write(bi, "jpg", fDestinationTempFile);
	} catch (Exception e) {

	}

	return ("file:///" + fDestinationTempFile.getAbsolutePath());
  }

  // ******** DATA RETRIEVAL FROM XML DOCUMENT FRAGMENTS ********

  /**
   * Instantiates primitive Resource object from XML element. This is very
   * lightweight and completely generic - it only sets resource's type, title
   * and URL on myExperiment / URI in the API.
   */
  public static Resource instantiatePrimitiveResourceFromElement(Element e) {
	if (e != null) {
	  Resource r = new Resource();
	  r.setItemType(e.getName());
	  r.setTitle(e.getText());
	  r.setURI(e.getAttributeValue("uri"));
	  r.setResource(e.getAttributeValue("resource"));
	  return (r);
	} else {
	  return (null);
	}
  }

  /**
   * Instantiates primitive User object from XML element. This is much more
   * lightweight than User.buildFromXML() where full details of the user can be
   * obtained from XML.
   */
  public static User instantiatePrimitiveUserFromElement(Element e) {
	if (e != null) {
	  User u = new User();
	  u.setName(e.getText());
	  u.setTitle(e.getText());
	  u.setURI(e.getAttributeValue("uri"));
	  u.setResource(e.getAttributeValue("resource"));
	  return (u);
	} else {
	  return (null);
	}
  }

  /**
   * Instantiates primitive Tag object from XML element. Very lightweight
   * method.
   */
  public static Tag instantiatePrimitiveTagFromElement(Element e) {
	if (e != null) {
	  Tag t = new Tag();
	  t.setTitle(e.getText());
	  t.setTagName(e.getText());
	  t.setResource(e.getAttributeValue("resource"));
	  t.setURI(e.getAttributeValue("uri"));
	  return (t);
	} else {
	  return (null);
	}
  }

  /**
   * Instantiates primitive Comment object from XML element. Very lightweight
   * method - faster and requires less data than Comment.buildFromXML().
   * 
   * Can only be used if known which resource is being commented.
   */
  public static Comment instantiatePrimitiveCommentFromElement(Element e, Resource r) {
	if (e != null) {
	  Comment c = new Comment();
	  c.setTitle(e.getText());
	  c.setComment(e.getText());
	  c.setTypeOfCommentedResource(r.getItemType());
	  c.setURIOfCommentedResource(r.getURI());
	  c.setResource(e.getAttributeValue("resource"));
	  c.setURI(e.getAttributeValue("uri"));
	  return (c);
	} else {
	  return (null);
	}
  }

  /**
   * Generic method that accepts the iterator over a collection of resources in
   * XML format (obtained from myExperiment API) and an ArrayList to store the
   * processed results in.
   * 
   * The method will extract 3 attributes for every item in the collection: 1)
   * name of the item; 2) URI of the item (to access this item via the API); 3)
   * URI of the item (to access via WEB);
   * 
   * @return Returns the number of processed elements in the collection.
   */
  public static int getResourceCollectionFromXMLIterator(Iterator<Element> iterator, ArrayList<HashMap<String, String>> collection) {
	int iCount = 0;

	while (iterator.hasNext()) {
	  // get XML element for the next element in the collection
	  Element element = iterator.next();

	  // store all details of current group into a hash map
	  HashMap<String, String> itemDetails = new HashMap<String, String>();
	  itemDetails.put("name", element.getText());
	  itemDetails.put("uri", element.getAttributeValue("uri"));
	  itemDetails.put("resource", element.getAttributeValue("resource"));

	  // add current item to the complete list of items
	  collection.add(itemDetails);
	  iCount++;
	}

	return (iCount);
  }

  /**
   * Takes XML Element instance with privilege listing for an item and returns
   * an integer value for that access type.
   */
  public static int getAccessTypeFromXMLElement(Element privileges) {
	// if the item for which the privileges are processed got received,
	// there definitely is viewing access to it
	int iAccessType = Resource.ACCESS_VIEWING;

	// pick the highest allowed access type
	Iterator<Element> iPrivileges = privileges.getChildren("privilege").iterator();
	while (iPrivileges.hasNext()) {
	  Element privilege = iPrivileges.next();
	  String strValue = privilege.getAttributeValue("type");

	  int iCurrentPrivilege = Resource.ACCESS_VIEWING;
	  if (strValue.equals("download"))
		iCurrentPrivilege = Resource.ACCESS_DOWNLOADING;
	  else if (strValue.equals("edit"))
		iCurrentPrivilege = Resource.ACCESS_EDITING;

	  if (iCurrentPrivilege > iAccessType)
		iAccessType = iCurrentPrivilege;
	}

	return (iAccessType);
  }

  /**
   * Returns contents of the "reason" field of the error message.
   */
  public static String retrieveReasonFromErrorXMLDocument(Document doc) {
	if (doc != null) {
	  Element root = doc.getRootElement();
	  return (root.getChildText("reason"));
	} else {
	  return ("unknown reason");
	}
  }

  /**
   * Parses an XML document containing resources favourited by a User. These
   * resources are instantiated with the basic data available in the XML
   * document and collected into a single List object.
   */
  @SuppressWarnings("unchecked")
  public static List<Resource> retrieveUserFavourites(Element docRootElement) {
	List<Resource> favouritedItemList = new ArrayList<Resource>();

	Element favouritesElement = docRootElement.getChild("favourited");
	if (favouritesElement != null) {
	  Iterator<Element> iFavourites = favouritesElement.getChildren().iterator();
	  while (iFavourites.hasNext()) {
		favouritedItemList.add(Util.instantiatePrimitiveResourceFromElement(iFavourites.next()));
	  }
	}

	return (favouritedItemList);
  }

  /**
   * Returns a list of credits - can be applied to any XML document which
   * contains "credits" element.
   */
  @SuppressWarnings("unchecked")
  public static List<Resource> retrieveCredits(Element docRootElement) {
	List<Resource> credits = new ArrayList<Resource>();

	Element creditsElement = docRootElement.getChild("credits");
	if (creditsElement != null) {
	  List<Element> creditsNodes = creditsElement.getChildren();
	  for (Element e : creditsNodes) {
		credits.add(Util.instantiatePrimitiveResourceFromElement(e));
	  }
	}

	return (credits);
  }

  /**
   * Returns a list of attributions - can be applied to any XML document which
   * contains "attributions" element.
   */
  @SuppressWarnings("unchecked")
  public static List<Resource> retrieveAttributions(Element docRootElement) {
	List<Resource> attributions = new ArrayList<Resource>();

	Element attributionsElement = docRootElement.getChild("attributions");
	if (attributionsElement != null) {
	  List<Element> attributionsNodes = attributionsElement.getChildren();
	  for (Element e : attributionsNodes) {
		attributions.add(Util.instantiatePrimitiveResourceFromElement(e));
	  }
	}

	return (attributions);
  }

  /**
   * Returns a list of tags - can be applied to any XML document which contains
   * "tags" element.
   */
  @SuppressWarnings("unchecked")
  public static List<Tag> retrieveTags(Element docRootElement) {
	List<Tag> tags = new ArrayList<Tag>();

	Element tagsElement = docRootElement.getChild("tags");
	if (tagsElement != null) {
	  List<Element> tagsNodes = tagsElement.getChildren();
	  for (Element e : tagsNodes) {
		tags.add(Util.instantiatePrimitiveTagFromElement(e));
	  }
	}

	return (tags);
  }

  /**
   * Returns a list of comments - can be applied to any XML document which
   * contains "comments" element. This should be called when the Resource object
   * (for which the list of comments is being obtained) is known.
   */
  @SuppressWarnings("unchecked")
  public static List<Comment> retrieveComments(Element docRootElement, Resource r) {
	List<Comment> comments = new ArrayList<Comment>();

	Element tagsElement = docRootElement.getChild("comments");
	if (tagsElement != null) {
	  List<Element> tagsNodes = tagsElement.getChildren();
	  for (Element e : tagsNodes) {
		comments.add(Util.instantiatePrimitiveCommentFromElement(e, r));
	  }
	}

	return (comments);
  }

  // ******** STRIPPING OUT HTML FROM STRINGS ********

  /**
   * Tiny helper to strip out HTML tags. Basic HTML tags like &nbsp; and <br>
   * are left in place, because these can be rendered by JLabel. This helps to
   * present HTML content inside JAVA easier.
   */
  public static String stripHTML(String source) {
	// don't do anything if not string is provided
	if (source == null)
	  return ("");

	// need to preserve at least all line breaks
	// (ending and starting paragraph also make a line break)
	source = source.replaceAll("</p>[\r\n]*<p>", "<br>");
	source = source.replaceAll("\\<br/?\\>", "[-=BR=-]");

	// strip all HTML
	source = source.replaceAll("\\<.*?\\>", "");

	// put the line breaks back
	source = source.replaceAll("\\[-=BR=-\\]", "<br><br>");

	return (source);
  }

  /**
   * Tiny helper to strip out all HTML tags. This will not leave any HTML tags
   * at all (so that the content can be displayed in DialogTextArea - and the
   * like - components. This helps to present HTML content inside JAVA easier.
   */
  public static String stripAllHTML(String source) {
	// don't do anything if not string is provided
	if (source == null)
	  return ("");

	// need to preserve at least all line breaks
	// (ending and starting paragraph also make a line break)
	source = source.replaceAll("</p>[\r\n]*<p>", "<br>");
	source = source.replaceAll("\\<br/?\\>", "\n\n");

	// strip all HTML
	source = source.replaceAll("\\<.*?\\>", ""); // any HTML tags
	source = source.replaceAll("&\\w{1,4};", ""); // this is for things like "&nbsp;", "&gt;", etc

	return (source);
  }

  // ******** WINDOW OPERATIONS ********

  /**
   * Makes sure that one component (for example, a window) is centered
   * horizontally and vertically relatively to the other component.
   * 
   * This method is applicable when 'dependentComponent' fully fits within the
   * 'mainComponent'. Otherwise behaviour is unpredictable.
   */
  public static void centerComponentWithinAnother(Component mainComponent, Component dependentComponent) {
	int iMainComponentCenterX = (int) Math.round(mainComponent.getLocationOnScreen().getX()
		+ (mainComponent.getWidth() / 2));
	int iPosX = iMainComponentCenterX - (dependentComponent.getWidth() / 2);
	int iMainComponentCenterY = (int) Math.round(mainComponent.getLocationOnScreen().getY()
		+ (mainComponent.getHeight() / 2));
	int iPosY = iMainComponentCenterY - (dependentComponent.getHeight() / 2);

	dependentComponent.setLocation(iPosX, iPosY);
  }

  // ******** VARIOUS HELPERS ********

  /**
   * Generates a JClickableLabel instance for a supplied Resource object with a
   * specified click handler.
   */
  public static JClickableLabel generateClickableLabelFor(Resource r, ActionListener clickHandler) {
	return (new JClickableLabel(r.getTitle(), "preview:" + r.getItemType()
		+ ":" + r.getURI(), clickHandler, new ImageIcon(MyExperimentPerspective.getLocalIconURL(r.getItemType())), SwingConstants.LEFT, r.getItemTypeName()
		+ ": " + r.getTitle()));
  }

  /**
   * Generates a JLabel with a "none-text" style.
   */
  public static JLabel generateNoneTextLabel(String strLabel) {
	JLabel lNoneText = new JLabel(strLabel);
	lNoneText.setFont(lNoneText.getFont().deriveFont(Font.ITALIC));
	lNoneText.setForeground(Color.GRAY);
	lNoneText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	return (lNoneText);
  }

  /**
   * A helper method to assemble part of the URL query for myExperiment API.
   * This will produce a string in the form of
   * "&elements=<element1>[,<element2>,...]
   * 
   * @param queryInstance
   *          Instance of {@link QuerySearchInstance} that contains details on
   *          the current search by query or null value if search by tag is
   *          being made.
   */
  public static String composeAPIQueryElements(QuerySearchInstance queryInstance) {
	// this will create one large comma-separated string of elements that
	// are required for generating a FULL LISTING (which is used in search
	// result presentation) for all various types
	String strElements = "";
	if (queryInstance == null || queryInstance.getSearchWorkflows()) {
	  strElements += Workflow.getRequiredAPIElements(Resource.REQUEST_FULL_LISTING)
		  + ",";
	}
	if (queryInstance == null || queryInstance.getSearchFiles()) {
	  strElements += File.getRequiredAPIElements(Resource.REQUEST_FULL_LISTING)
		  + ",";
	}
	if (queryInstance == null || queryInstance.getSearchPacks()) {
	  strElements += Pack.getRequiredAPIElements(Resource.REQUEST_FULL_LISTING)
		  + ",";
	}
	if (queryInstance == null || queryInstance.getSearchUsers()) {
	  strElements += User.getRequiredAPIElements(Resource.REQUEST_FULL_LISTING)
		  + ",";
	}
	if (queryInstance == null || queryInstance.getSearchGroups()) {
	  strElements += Group.getRequiredAPIElements(Resource.REQUEST_FULL_LISTING)
		  + ",";
	}

	// now need to make this list of elements to contain only unique tokens
	TreeSet<String> elementSet = new TreeSet<String>(Arrays.asList(strElements.split(",")));
	Iterator<String> elementSetIterator = elementSet.iterator();
	strElements = "&elements=";
	while (elementSetIterator.hasNext()) {
	  strElements += elementSetIterator.next();
	  if (elementSetIterator.hasNext())
		strElements += ",";
	}

	return (strElements);
  }

  /**
   * The parameter is the class name to be processed; class name is likely to be
   * in the form <class_name>$<integer_value>, where the trailing part starting
   * with the $ sign indicates the anonymous inner class within the base class.
   * This will strip out that part of the class name to get the base class name.
   */
  public static String getBaseClassName(String strClassName) {
	// strip out the class name part after the $ sign; return
	// the original value if the dollar sign wasn't found
	String strResult = strClassName;

	int iDollarIdx = strResult.indexOf("$");
	if (iDollarIdx != -1)
	  strResult = strResult.substring(0, iDollarIdx);

	return (strResult);
  }

  /**
   * Determines whether the plugin is running as a standalone JFrame or inside
   * Taverna Workbench.
   */
  public static boolean isRunningInTaverna() {
	try {
	  // ApplicationRuntime class is defined within Taverna API. If this is available,
	  // it should mean that the plugin runs within Taverna.
	  ApplicationRuntime.getInstance();
	  return true;
	} catch (NoClassDefFoundError e) {
	  return false;
	}
  }
}
