/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.views.graph.menu;

import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.taverna.workbench.views.graph.menu.DiagramSaveMenuSection.DIAGRAM_SAVE_MENU_SECTION;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.taverna.lang.io.StreamCopier;
import org.apache.taverna.lang.io.StreamDevourer;
import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.SwingAwareObserver;
import org.apache.taverna.lang.ui.ExtensionFileFilter;
import org.apache.taverna.ui.menu.AbstractMenuCustom;
import org.apache.taverna.ui.menu.DesignOnlyAction;
import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.icons.WorkbenchIcons;
import org.apache.taverna.workbench.models.graph.DotWriter;
import org.apache.taverna.workbench.models.graph.GraphController;
import org.apache.taverna.workbench.models.graph.svg.SVGUtil;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.selection.events.PerspectiveSelectionEvent;
import org.apache.taverna.workbench.selection.events.SelectionManagerEvent;
import org.apache.taverna.workbench.views.graph.GraphViewComponent;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.core.Workflow;

/**
 * An action that saves graph diagram image.
 *
 * @author Alex Nenadic
 * @author Tom Oinn
 */
public class SaveGraphImageSubMenu extends AbstractMenuCustom {
	private static final Logger logger = Logger
			.getLogger(SaveGraphImageSubMenu.class);
	private static final String[] saveTypes = { "dot", "png", "svg", "ps",
			"ps2" };
	private static final String[] saveExtensions = { "dot", "png", "svg", "ps",
			"ps" };
	private static final String[] saveTypeNames = { "dot text", "PNG bitmap",
			"scalable vector graphics", "postscript", "postscript for PDF" };	
	public static final URI SAVE_GRAPH_IMAGE_MENU_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#graphMenuSaveGraphImage");

	private JMenu saveDiagramMenu;
	private FileManager fileManager;
	private SelectionManager selectionManager;
	private WorkbenchConfiguration workbenchConfiguration;
	private GraphViewComponent graphViewComponent;

	public SaveGraphImageSubMenu() {
		super(DIAGRAM_SAVE_MENU_SECTION, 70, SAVE_GRAPH_IMAGE_MENU_URI);
	}

	@Override
	protected Component createCustomComponent() {
		saveDiagramMenu = new JMenu("Export diagram");
		saveDiagramMenu
				.setToolTipText("Open this menu to export the diagram in various formats");
		for (int i = 0; i < saveTypes.length; i++) {
			String type = saveTypes[i];
			String extension = saveExtensions[i];
			ImageIcon icon = new ImageIcon(
					WorkbenchIcons.class.getResource("graph/saveAs"
							+ type.toUpperCase() + ".png"));
			JMenuItem item = new JMenuItem(new DotInvoker("Export as "
					+ saveTypeNames[i], icon, type, extension));
			saveDiagramMenu.add(item);
		}
		return saveDiagramMenu;
	}

	@SuppressWarnings("serial")
	class DotInvoker extends AbstractAction implements DesignOnlyAction {
		String type = "dot";
		String extension = "dot";

		public DotInvoker(String name, ImageIcon icon, String type,
				String extension) {
			super(name, icon);
			this.type = type;
			this.extension = extension;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Workflow workflow = selectionManager.getSelectedWorkflow();
			if (workflow == null) {
				showMessageDialog(null, "Cannot export an empty diagram.",
						"Warning", WARNING_MESSAGE);
				return;
			}

			File file = saveDialogue(null, workflow, extension,
					"Export workflow diagram");
			if (file == null)
				// User cancelled
				return;

			try {
				GraphController graphController = graphViewComponent
						.getGraphController(workflow);

				if (type.equals("dot")) {
					// Just write out the dot text, no processing required
					PrintWriter out = new PrintWriter(new FileWriter(file));
					DotWriter dotWriter = new DotWriter(out);
					dotWriter.writeGraph(graphController.generateGraph());
					out.flush();
					out.close();
				} else {
					String dotLocation = (String) workbenchConfiguration
							.getProperty("taverna.dotlocation");
					if (dotLocation == null)
						dotLocation = "dot";
					logger.debug("GraphViewComponent: Invoking dot...");
					Process dotProcess = Runtime.getRuntime().exec(
							new String[] { dotLocation, "-T" + type });

					FileOutputStream fos = new FileOutputStream(file);

					StringWriter stringWriter = new StringWriter();
					DotWriter dotWriter = new DotWriter(stringWriter);
					dotWriter.writeGraph(graphController.generateGraph());

					OutputStream dotOut = dotProcess.getOutputStream();
					dotOut.write(SVGUtil.getDot(stringWriter.toString(),
							workbenchConfiguration).getBytes());
					dotOut.flush();
					dotOut.close();
					new StreamDevourer(dotProcess.getErrorStream()).start();
					new StreamCopier(dotProcess.getInputStream(), fos).start();
				}
			} catch (Exception ex) {
				logger.warn("GraphViewComponent: Could not export diagram to " + file, ex);
				showMessageDialog(null,
						"Problem saving diagram : \n" + ex.getMessage(),
						"Error!", ERROR_MESSAGE);					
			}
		}
	}

	/**
	 * Pop up a save dialogue relating to the given workflow. This method can be
	 * used, for example, for saving the workflow diagram as .png, and will use
	 * the existing workflow title as a base for suggesting a filename.
	 *
	 * @param parentComponent
	 *            Parent component for dialogue window
	 * @param model
	 *            Workflow to save
	 * @param extension
	 *            Extension for filename, such as "jpg"
	 * @param windowTitle
	 *            Title for dialogue box, such as "Save workflow diagram"
	 * @return File instance for the selected abstract filename, or null if the
	 *         dialogue was cancelled.
	 */
	private File saveDialogue(Component parentComponent, Workflow workflow,
			String extension, String windowTitle) {
		JFileChooser fc = new JFileChooser();
		Preferences prefs = Preferences
				.userNodeForPackage(SaveGraphImageSubMenu.class);
		String curDir = prefs
				.get("currentDir", System.getProperty("user.home"));
		String suggestedFileName = "";
		// Get the source the workflow was loaded from - can be File, URL, or InputStream
		Object source = fileManager.getDataflowSource(workflow.getParent());
		if (source instanceof File) {
			suggestedFileName = ((File) source).getName();
			// remove the file extension
			suggestedFileName = suggestedFileName.substring(0,
					suggestedFileName.lastIndexOf("."));
		} else if (source instanceof URL) {
			suggestedFileName = ((URL) source).getPath();
			// remove the file extension
			suggestedFileName = suggestedFileName.substring(0,
					suggestedFileName.lastIndexOf("."));
		} else {
			// We cannot suggest the file name if workflow was read from an InputStream
		}

		fc.setDialogTitle(windowTitle);
		fc.resetChoosableFileFilters();
		fc.setFileFilter(new ExtensionFileFilter(new String[] { extension }));
		if (suggestedFileName.isEmpty())
			// No file suggestion, just the directory
			fc.setCurrentDirectory(new File(curDir));
		else
			// Suggest a filename from the workflow file name
			fc.setSelectedFile(new File(curDir, suggestedFileName + "." + extension));

		while (true) {
			if (fc.showSaveDialog(parentComponent) != APPROVE_OPTION) {
				logger.info("GraphViewComponent: Aborting diagram export to "
						+ suggestedFileName);
				return null;
			}

			File file = fixExtension(fc.getSelectedFile(), extension);
			logger.debug("GraphViewComponent: Selected " + file + " as export target");
			prefs.put("currentDir", fc.getCurrentDirectory().toString());

			// If file doesn't exist, we may write it! (Well, probably...)
			if (!file.exists())
				return file;

			// Ask the user if they want to overwrite the file
			String msg = file.getAbsolutePath()
					+ " already exists. Do you want to overwrite it?";
			if (showConfirmDialog(null, msg, "File already exists",
					YES_NO_OPTION) == JOptionPane.YES_OPTION)
				return file;
		}
	}

	/**
	 * Make sure given File has the given extension. If it has no extension,
	 * a new File instance will be returned. Otherwise, the passed instance is
	 * returned unchanged.
	 *
	 * @param file
	 *            File which extension is to be checked
	 * @param extension
	 *            Extension desired, example: "xml"
	 * @return file parameter if the extension was OK, or a new File instance
	 *         with the correct extension
	 */
	private File fixExtension(File file, String extension) {
		if (file.getName().endsWith("." + extension))
			return file;
		// Append the extension (keep the existing one)
		String name = file.getName();
		return new File(file.getParent(), name + "." + extension);
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setWorkbenchConfiguration(
			WorkbenchConfiguration workbenchConfiguration) {
		this.workbenchConfiguration = workbenchConfiguration;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setGraphViewComponent(GraphViewComponent graphViewComponent) {
		this.graphViewComponent = graphViewComponent;
	}

	private static final String DESIGN_PERSPECTIVE_ID = "net.sf.taverna.t2.ui.perspectives.design.DesignPerspective";

	@SuppressWarnings("unused")
	private final class SelectionManagerObserver extends
			SwingAwareObserver<SelectionManagerEvent> {
		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender,
				SelectionManagerEvent message) {
			if (!(message instanceof PerspectiveSelectionEvent))
				return;
			PerspectiveSelectionEvent event = (PerspectiveSelectionEvent) message;

			saveDiagramMenu.setEnabled((DESIGN_PERSPECTIVE_ID.equals(event
					.getSelectedPerspective().getID())));
		}
	}
}
