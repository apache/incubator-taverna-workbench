package net.sf.taverna.t2.workbench.ui.impl.menu;

import static java.lang.Runtime.getRuntime;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;
import static net.sf.taverna.t2.workbench.MainWindow.getMainWindow;
import static net.sf.taverna.t2.workbench.ui.impl.menu.AdvancedMenu.ADVANCED_URI;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

import org.apache.log4j.Logger;

import uk.org.taverna.configuration.app.ApplicationConfiguration;

public class ShowLogsAndDataMenuAction extends AbstractMenuAction {
	private static final String OPEN = "open";
	private static final String EXPLORER = "explorer";
	// TODO Consider using xdg-open instead of gnome-open
	private static final String GNOME_OPEN = "gnome-open";
	private static final String WINDOWS = "Windows";
	private static final String MAC_OS_X = "Mac OS X";

	private ApplicationConfiguration applicationConfiguration;

	public ShowLogsAndDataMenuAction() {
		super(ADVANCED_URI, 200);
	}

	private static Logger logger = Logger.getLogger(ShowLogsAndDataMenuAction.class);

	@Override
	protected Action createAction() {
		return new AbstractAction("Show logs and data folder") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				File logsAndDataDir = applicationConfiguration.getApplicationHomeDir();
				showDirectory(logsAndDataDir, "Taverna logs and data folder");
			}
		};
	}

	public static void showDirectory(File dir, String title) {
		String path = dir.getAbsolutePath();
		String os = System.getProperty("os.name");
		String cmd;
		boolean isWindows = false;
		if (os.equals(MAC_OS_X))
			cmd = OPEN;
		else if (os.startsWith(WINDOWS)) {
			cmd = EXPLORER;
			isWindows = true;
		} else
			// Assume Unix - best option is gnome-open
			cmd = GNOME_OPEN;

		String[] cmdArray = new String[2];
		cmdArray[0] = cmd;
		cmdArray[1] = path;
		try {
			Process exec = getRuntime().exec(cmdArray);
			Thread.sleep(300);
			exec.getErrorStream().close();
			exec.getInputStream().close();
			exec.getOutputStream().close();
			exec.waitFor();
			if (exec.exitValue() == 0 || isWindows && exec.exitValue() == 1)
				// explorer.exe thinks 1 means success
				return;
			logger.warn("Exit value from " + cmd + " " + path + ": " + exec.exitValue());
		} catch (Exception ex) {
			logger.warn("Could not call " + cmd + " " + path, ex);
		}
		// Fall-back - just show a dialogue with the path
		showInputDialog(getMainWindow(), "Copy path from below:", title,
				INFORMATION_MESSAGE, null, null, path);
	}

	public void setApplicationConfiguration(ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}
}
