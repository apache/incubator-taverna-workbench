/**
 * 
 */
package net.sf.taverna.t2.workbench.helper;

import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.JHelp;
import javax.help.TryMap;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

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
	 * The PropertyResourceBundle that holds the settings
	 */
	private static PropertyResourceBundle prb = null;

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

	/**
	 * Attempt to read the up-to-date HelpSet from the web
	 */
	private static void readExternalHelpSet() {
		try {
			String externalHelpSetURL = prb.getString("externalhelpseturl");
			hs = new HelpSet(null, new URL(externalHelpSetURL));
			logger.info("Read external help set from " + externalHelpSetURL);
		} catch (MissingResourceException e) {
			logger.info("No external HelpSet URL specified");
		} catch (MalformedURLException e) {
			logger.info("External HelpSet URL is malformed");
		} catch (HelpSetException e) {
			logger.info("External HelpSet could not be read");
		}
	}

	/**
	 * Attempt to read the backup HelpSet included in Taverna
	 */
	private static void readBackupHelpSet() {
		try {
			URL backupURL = HelpCollator.class.getResource("backupHelpSet.hs");
			if (backupURL == null) {
				logger.info("could not find backupHelpSet resource");
			}
			hs = new HelpSet(null, backupURL);
			logger.info("Read backup help set");
		} catch (HelpSetException e) {
			logger.info("Backup HelpSet could not be read");
		}

	}

	/**
	 * This methods creates a HelpSet based upon, in priority, the external
	 * HelpSet, the backup HelpSet a newly created empty HelpSet.
	 */
	public static void initialize() {
		if (!initialized) {
			prb = (PropertyResourceBundle) ResourceBundle
					.getBundle("helpcollator");
			readExternalHelpSet();
			if (hs == null) {
				readBackupHelpSet();
			}
			if (hs == null) {
				hs = new HelpSet();
				hs.setLocalMap(new TryMap());
			} else {
				logger.info("EmptyHelp set to false");
				emptyHelp = false;
			}
			idMap = new HashMap<Component, String>();
			nonAlphanumeric = Pattern.compile("[^a-z0-9\\.]");
			initialized = true;
		}
	}

	/**
	 * Indicates if an empty HelpSet is being used
	 * 
	 * @return
	 */
	public static boolean isEmptyHelp() {
		return emptyHelp;
	}

	/**
	 * Create the JHelp for the HelpSet that is found
	 * 
	 * @return
	 */
	public static JHelp getJHelp() {
		initialize();
		return new JHelp(hs);
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
			if (hs.getLocalMap().isValidID(normalizedId, hs)) {
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
		String result = "home";
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
	private static String getHelpIDInTree(JTree c) {
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
