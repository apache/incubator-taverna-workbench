package net.sf.taverna.t2.workbench.ui.impl.menu;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.raven.appconfig.config.Log4JConfiguration;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.MainWindow;

import org.apache.log4j.Logger;

public class ShowLogsMenuAction extends AbstractMenuAction {
	
	private static final String OPEN = "open";
	private static final String EXPLORER = "explorer";
	private static final String GNOME_OPEN = "gnome-open";
	private static final String WINDOWS = "Windows";
	private static final String MAC_OS_X = "Mac OS X";

	public ShowLogsMenuAction() {
		super(AdvancedMenu.ADVANCED_URI, 200);
	}
	
	private static Logger logger = Logger.getLogger(ShowLogsMenuAction.class);
	
	@Override
	protected Action createAction() {
		return new AbstractAction("Show log folder") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				File logDir = Log4JConfiguration.getInstance().getLogDir();
				showDirectory(logDir, "Taverna log folder");
			}
		};
	}

	public static void showDirectory(File dir, String title) {
		String path = dir.getAbsolutePath();
		String os = System.getProperty("os.name");
		String cmd;
		boolean isWindows = false;
		if (os.equals(MAC_OS_X)) {
			cmd = OPEN;
		} else if (os.startsWith(WINDOWS)) {
			cmd = EXPLORER;		
			isWindows = true;
		} else {
			// Assume Unix - best option is gnome-open
			cmd = GNOME_OPEN;
		}
		String[] cmdArray = new String[2];
		cmdArray[0] = cmd;
		cmdArray[1] = path;
		try {
			Process exec = Runtime.getRuntime().exec(cmdArray);
			Thread.sleep(300);
			exec.getErrorStream().close();
			exec.getInputStream().close();
			exec.getOutputStream().close();
			exec.waitFor();
			if (exec.exitValue() == 0 || isWindows && exec.exitValue() == 1) {
				// explorer.exe thinks 1 means success
				return;
			}
			logger.warn("Exit value from " + cmd + " " + path + ": " + exec.exitValue());
		} catch (Exception ex) {
			logger.warn("Could not call " + cmd + " " + path, ex);
		}
		// Fall-back - just show a dialogue with the path
		JOptionPane.showInputDialog(MainWindow.getMainWindow(), "Copy path from below:", title,
				JOptionPane.INFORMATION_MESSAGE, null, null,
                path);
	}

}
