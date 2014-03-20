/**
 * 
 */
package net.sf.taverna.t2.workbench.helper;

import java.awt.Component;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.help.BadIDException;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.JHelp;
import javax.help.TryMap;
import javax.help.Map.ID;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;

import org.apache.log4j.Logger;

/**
 * 
 * This class loads the HelpSet and also deals with the registration of ids and
 * the decoding from a Component to the corresponding id. These two sets of
 * functionality should possibly be separated.
 * 
 * @author alanrw
 * 
 */
public final class HelpCollator {

	private static Logger logger = Logger.getLogger(HelpCollator.class);

	/**
	 * The singleton instance of the HelpCollator. In theory there could be more
	 * than one.
	 */
	private static HelpCollator instance = null;

	/**
	 * The HelpSet that is being used.
	 */
	private static HelpSet hs = null;

	/**
	 * The mapping from components to ids. This is used because of problems with
	 * CSH throwing exceptions because it tried to use ids that were not in the
	 * map.
	 */
	private static Map<Component, String> idMap;

	/**
	 * Indicates whether the HelpCollator has been initialized.
	 */
	private static boolean initialized = false;

	/**
	 * A Pattern for normalizing the ids.
	 */
	private static Pattern nonAlphanumeric;

	/**
	 * The emptyHelp is set if the HelpCollator was unable to read the
	 */
	private static boolean emptyHelp = true;

	private static URLConnection connection;
	
	private static int TIMEOUT = 5000;
	
	private static Profile profile = ProfileFactory.getInstance().getProfile();
	private static String version = profile.getVersion();
	private static String externalHelpSetURL = "http://www.mygrid.org.uk/taverna/helpset/" + version + "/helpset.hs";
	
	private static Map<String, Map<String, URL>> examplesMap = new TreeMap<String,Map<String, URL>>();

	/**
	 * Attempt to read the up-to-date HelpSet from the web
	 */
	private static void readExternalHelpSet() {
		try {
			URL url = new URL(externalHelpSetURL);
		
			connection = url.openConnection();
			connection.setReadTimeout(TIMEOUT);
			connection.setConnectTimeout(TIMEOUT);
			connection.connect();
			hs = new HelpSet(null, new URL(externalHelpSetURL));
			if (hs.getLocalMap() == null) {
			    hs = null;
			    logger.error("Helpset from " + externalHelpSetURL + " local map was null");
			}
			logger.info("Read external help set from " + externalHelpSetURL);
		} catch (MissingResourceException e) {
		    logger.error("No external HelpSet URL specified", e);
		} catch (MalformedURLException e) {
		    logger.error("External HelpSet URL is malformed", e);
		} catch (HelpSetException e) {
		    logger.error("External HelpSet could not be read", e);
		} catch (IOException e) {
			logger.error("IOException reading External HelpSet", e);
		}
		finally {
			try {
				if ((connection != null) && (connection.getInputStream() != null)) {
					connection.getInputStream().close();
				}
			} catch (IOException e) {
				logger.error("Unable to close connection", e);
			}
		}
	}

	/**
	 * This methods creates a HelpSet based upon, in priority, the external
	 * HelpSet, then a newly created empty HelpSet.
	 */
	public static void initialize() {
		if (!initialized) {
			readExternalHelpSet();
			if (hs == null) {
				hs = new HelpSet();
				hs.setLocalMap(new TryMap());
			} else {
				logger.info("EmptyHelp set to false");
				emptyHelp = false;
			}
			idMap = new HashMap<Component, String>();
			nonAlphanumeric = Pattern.compile("[^a-z0-9\\.]");
			
			if (hs != null) {
				for (Object idAsObject : Collections.list(hs.getLocalMap().getAllIDs())) {
					final ID id = (ID) idAsObject;
					String stringId = id.getIDString();
					if (stringId.contains("$")) {
						String[] parts = stringId.split("[$]");
						if (parts.length != 2) {
							continue;
						}
						String baseId = parts[0];
						String exampleTitle = parts[1];
						try {
							URL targetURL = id.getURL();
							Map<String, URL> examples = examplesMap.get(baseId);
							if (examples == null) {
								examples = new HashMap<String,URL>();
								examplesMap.put(baseId, examples);
							}
							examples.put(exampleTitle, targetURL);
						} catch (MalformedURLException e) {
							logger.error(e);
						}
						
					}
				}
			}
			initialized = true;
		}
	}
	
	public static Map<String, URL> getExamples(String baseId) {
		Map<String, URL> result = examplesMap.get(baseId);
		if (result == null) {
			return Collections.emptyMap();
		}
		return result;
	}

	/**
	 * Indicates if an empty HelpSet is being used
	 * 
	 * @return
	 */
	public static boolean isEmptyHelp() {
		return emptyHelp;
	}
	
	public static URL getURLFromID(String id) throws BadIDException, MalformedURLException {
		initialize();
		logger.info("Looking for id: " + id);
		ID theId = ID.create(id, hs);
		if (theId == null) {
			return null;
		}
		return (hs.getCombinedMap().getURLFromID(theId));	
	}

	/**
	 * Register a component under the specified id. The method checks that the
	 * id is known to the HelpSet's map.
	 * 
	 * @param component
	 * @param id
	 */
	public static void registerComponent(Component component, final String id) {
		logger.info("Attempting to register " + id);
		initialize();
		String normalizedId = normalizeString(id.toLowerCase());
		if (idMap.containsKey(component)) {
			logger.info("Registered " + normalizedId);
		} else {
			
			// If Workbench is started up while there is no network connection - 
			// hs.getLocalMap() is null for some reason
			if (hs != null && hs.getLocalMap()!= null && hs.getLocalMap().isValidID(normalizedId, hs)) {
				idMap.put(component, normalizedId);
				logger.info("Registered " + normalizedId);
			} else {
				logger.info("Refused to register component as " + normalizedId
						+ " - not in map");
			}
		}
	}

	/**
	 * Register a component. Since no id is specified, the HelpCollator takes
	 * the canonical name of the component's class. This is useful when an
	 * explicit hierarchy-based approach has been taken.
	 * 
	 * @param component
	 */
	public static void registerComponent(Component component) {
		String canonicalName = component.getClass().getCanonicalName();
		if (canonicalName != null) {
			registerComponent(component, canonicalName);
		}
	}

	/**
	 * Register a component based upon its parent's class and a suffix
	 * indicating the component's purpose in the parent.
	 * 
	 * @param component
	 * @param parent
	 * @param suffix
	 */
	public static void registerComponent(Component component, Object parent,
			String suffix) {
		String canonicalName = parent.getClass().getCanonicalName();
		if (canonicalName != null) {
			registerComponent(component, canonicalName + "-" + suffix);
		}

	}

	/**
	 * Try to find an id for the Component. This code should be re-written when
	 * we have more experience in how to couple the UI and HelpSets.
	 * 
	 * @param c
	 * @return
	 */
	static String getHelpID(Component c) {
		initialize();
		boolean found = false;
		String result = null;
		if (c instanceof JTree) {
			String idInTree = getHelpIDInTree((JTree) c);
			if (idInTree != null) {
				found = true;
				result = idInTree;
			}
		}
		Component working = c;
		if (c != null) {
			logger.info("Starting at a "
					+ working.getClass().getCanonicalName());
		}
		while (!found && (working != null)) {
			if (idMap.containsKey(working)) {
				result = idMap.get(working);
				found = true;
				logger.info("Found component id " + result);
			} else {
				String className = working.getClass().getCanonicalName();
				if (hs.getLocalMap().isValidID(className, hs)) {
					result = className;
					found = true;
					logger.info("Found class name " + result);
				}
			}
			if (!found) {
				working = working.getParent();
				if (working != null) {
					logger.info("Moved up to a "
							+ working.getClass().getCanonicalName());
				}
			}
		}
		return result;
	}

	/**
	 * Change the input String into an id that contains only alphanumeric
	 * characters or hyphens.
	 * 
	 * @param input
	 * @return
	 */
	static String normalizeString(String input) {
		Matcher m = nonAlphanumeric.matcher(input);
		return m.replaceAll("-");
	}

	/**
	 * If help is sought on part of a JTree, then this method attempts to find a
	 * node of the tree that can be mapped to an id. The possibilities are ad
	 * hoc and should be re-examined when more experience is gained.
	 * 
	 * @param c
	 * @return
	 */
	public static String getHelpIDInTree(JTree c) {
		initialize();
		String result = null;

		TreePath tp = c.getSelectionPath();
		if (tp != null) {
			Object o = tp.getLastPathComponent();
			if (o != null) {
				if (o instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) o;
					if (dmtn.getUserObject() != null) {
						o = dmtn.getUserObject();
					}
				}
				if (o instanceof HelpEndpointsProvider) {
					return ((HelpEndpointsProvider) o).getHelpId();
				}
				String possibility = o.toString();
				String className = o.getClass().getCanonicalName();
				logger.info("Tree node as a string is " + possibility);
				possibility = normalizeString(possibility.toLowerCase());
				logger.info("Normalized is " + possibility);
				logger.info("Tree node class name is " + className);
				possibility = className + "-" + possibility;
				logger.info("Possibility is " + possibility);
				if (hs.getLocalMap().isValidID(possibility, hs)) {
					result = possibility;
					logger.info("Accepted tree node " + result);
				} else {
					if (hs.getLocalMap().isValidID(className, hs)) {
						result = className;
						logger.info("Found tree node class name " + result);
					}
				}
				logger
						.info("Tree node is a "
								+ o.getClass().getCanonicalName());
			}
		}
		return result;
	}
}
