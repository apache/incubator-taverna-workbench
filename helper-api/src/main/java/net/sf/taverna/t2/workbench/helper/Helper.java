/**
 *
 */
package net.sf.taverna.t2.workbench.helper;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.help.BadIDException;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
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

	private static Logger logger = Logger.getLogger(Helper.class);

	/**
	 * Create a Helper and initialize the static variables.
	 */
	private Helper() {
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
	 * Show in the current dialog the entry (if any) corresponding to the
	 * specified id.
	 *
	 * @param id
	 */
	private static void showID(String id) {
		getInstance();
		URL result;
		try {
			result = HelpCollator.getURLFromID(id);
			if (result == null) {
				result = HelpCollator.getURLFromID("home");
			}
			Desktop.getDesktop().browse(result.toURI());
		} catch (BadIDException e) {
			logger.error(e);
		} catch (MalformedURLException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (URISyntaxException e) {
			logger.error(e);
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
