/**
 * 
 */
package net.sf.taverna.t2.workbench.helper;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.help.JHelp;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;

import org.apache.log4j.Logger;

/**
 * This class creates the dialogs for the presentation of the HelpSet held by
 * the HelpCollator.
 * 
 * @author alanrw
 * 
 */
public final class Helper {
	private static Helper instance;
	private static JDialog dialog;
	private static JHelp jhelp;
	private static Map<Container, JDialog> dialogMap;
	private static Dimension oldSize = new Dimension(1000, 500);

	private static Logger logger = Logger.getLogger(Helper.class);

	/**
	 * Create a Helper and initialize the static variables.
	 */
	private Helper() {
		jhelp = HelpCollator.getJHelp();
		dialogMap = new HashMap<Container, JDialog>();
		dialog = createDialog(null);
	}

	/**
	 * Get the singleton instance of Helper. In theory there could be more than
	 * one.
	 * 
	 * @return
	 */
	private static Helper getInstance() {
		if (instance == null) {
			instance = new Helper();
		}
		return instance;
	}

	/**
	 * Initialize the current JDialog with the current JHelp. Attempt to set its
	 * size.
	 * 
	 * @param d
	 */
	private static void initializeDialog(JDialog d) {
		d.add(jhelp);
		d.setPreferredSize(oldSize);
		d.setSize(oldSize);
	}

	/**
	 * Create a JDialog belonging to the specified container and showing the
	 * current JHelp presentation. Remember the JDialog in the dialogMap.
	 * 
	 * @param rootpanecontainer
	 * @return
	 */
	private static JDialog createDialog(RootPaneContainer rootpanecontainer) {
		JDialog result = null;
		if (rootpanecontainer instanceof JFrame) {
			result = new JDialog((JFrame) rootpanecontainer);
			dialogMap.put((Container) rootpanecontainer, result);
		} else if (rootpanecontainer instanceof JDialog) {
			result = new JDialog((JDialog) rootpanecontainer);
			dialogMap.put((Container) rootpanecontainer, result);
		} else {
			result = new JDialog();
		}
		initializeDialog(result);

		return result;
	}

	/**
	 * Show in the current dialog the entry (if any) corresponding to the
	 * specified id.
	 * 
	 * @param id
	 */
	private static void showID(String id) {
		getInstance();
		if (dialog == null) {
			dialog = createDialog(null);
		}
		dialog.setVisible(true);
		if (!HelpCollator.isEmptyHelp()) {
			jhelp.setCurrentID(id);
		}
	}

	/**
	 * Show the most suitable help for the specified component.
	 * 
	 * @param c
	 */
	public static void showHelp(Component c) {
		showID(HelpCollator.getHelpID(c));
	}

	/**
	 * Display the default home page help.
	 * 
	 * @param e
	 */
	public static void displayDefaultHelp(AWTEvent e) {
		showID("home");
	}

	public static void displayFieldLevelHelp(ActionEvent e) {
		//
	}

	/**
	 * Associated the specified acion with key presses in the specified
	 * compoent.
	 * 
	 * @param component
	 * @param theAction
	 */
	public static void setKeyCatcher(final JComponent component,
			final AbstractAction theAction) {
		InputMap oldInputMap = component
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		InputMap newInputMap = new InputMap();
		newInputMap.setParent(oldInputMap);
		newInputMap.put(KeyStroke.getKeyStroke("F1"), "doSomething");
		component.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
				newInputMap);
		ActionMap oldActionMap = component.getActionMap();
		ActionMap newActionMap = new ActionMap();
		newActionMap.setParent(oldActionMap);
		newActionMap.put("doSomething", theAction);
		component.setActionMap(newActionMap);
	}

	/**
	 * Set up a key-press catcher for the specified component such that when F1
	 * is pressed it should help for the component where the cursor is.
	 * 
	 * @param rootpanecontainer
	 */
	public static void setKeyCatcher(final RootPaneContainer rootpanecontainer) {
		AbstractAction theAction = new AbstractAction() {

			public void actionPerformed(ActionEvent arg0) {
				Component component = (Component) rootpanecontainer;
				Container container = (Container) rootpanecontainer;
				logger.info("frame action F1 pressed with source "
						+ arg0.getSource().getClass().getName());
				Point mousePosition = MouseInfo.getPointerInfo().getLocation();
				Point framePosition = component.getLocation();
				Point relativePosition = (Point) mousePosition.clone();
				relativePosition.translate(-framePosition.x, -framePosition.y);
				Component c = container.findComponentAt(relativePosition);
				if (c != null) {
					logger.info("F1 pressed in a " + c.getClass().getName());
				}
				showHelpWithinContainer(rootpanecontainer, c);
			}

		};

		JRootPane pane = rootpanecontainer.getRootPane();
		setKeyCatcher(pane, theAction);
	}

	/**
	 * Show the help most associated with the specific component within the container.
	 * 
	 * @param rootpanecontainer
	 * @param c
	 */
	static void showHelpWithinContainer(
			final RootPaneContainer rootpanecontainer, final Component c) {
		getInstance();
		if ((dialog == null) || !dialog.getOwner().equals(rootpanecontainer)) {
			if (dialog != null) {
				dialog.setVisible(false);
				oldSize = dialog.getSize();
				logger.info("Size of dialog was x=" + oldSize.width + " y="
						+ oldSize.height);
			}
			if (dialogMap.containsKey(rootpanecontainer)) {
				dialog = dialogMap.get(rootpanecontainer);
				initializeDialog(dialog);
			} else {
				dialog = createDialog(rootpanecontainer);
			}
		}
		showHelp(c);

	}

	/**
	 * Register a component with the HelpCollator under the specified id.
	 * 
	 * @param component
	 * @param id
	 */
	public static void registerComponent(Component component, final String id) {
		HelpCollator.registerComponent(component, id);
	}

	/**
	 * Register a component with the HelpCollator.
	 * 
	 * @param component
	 * @param parent
	 * @param suffix
	 */
	public static void registerComponent(Component component, Object parent,
			String suffix) {
		HelpCollator.registerComponent(component, parent, suffix);

	}

}
