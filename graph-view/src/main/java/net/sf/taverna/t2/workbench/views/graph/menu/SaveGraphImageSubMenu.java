/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.views.graph.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.workbench.ui.zaria.WorkflowPerspective;
import net.sf.taverna.t2.lang.io.StreamCopier;
import net.sf.taverna.t2.lang.io.StreamDevourer;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ExtensionFileFilter;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.models.graph.DotWriter;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGUtil;
import net.sf.taverna.t2.workbench.ui.impl.configuration.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.views.graph.GraphViewComponent;
import net.sf.taverna.t2.workbench.views.graph.actions.SaveGraphImageAction;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * An action that saves graph diagram image.
 * 
 * @author Alex Nenadic
 * @author Tom Oinn
 *
 */
@SuppressWarnings("serial")
public class SaveGraphImageSubMenu extends AbstractMenuCustom{

	private static Logger logger = Logger.getLogger(SaveGraphImageAction.class);
	
	private static ModelMap modelMap = ModelMap.getInstance();
	
	// Perspective switch observer
	private CurrentPerspectiveObserver perspectiveObserver = new CurrentPerspectiveObserver();
	
	private JMenu saveDiagramMenu;
	
	private String[] saveTypes = { "dot", "png", "svg", "ps", "ps2" };
	private String[] saveExtensions = { "dot", "png", "svg", "ps", "ps" };
	private String[] saveTypeNames = { "dot text", "PNG bitmap",
			"scalable vector graphics", "postscript", "postscript for PDF" };
	
	public static final URI SAVE_GRAPH_IMAGE_MENU_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/menu#graphMenuSaveGraphImage");
	
	public SaveGraphImageSubMenu(){
		super(DiagramSaveMenuSection.DIAGRAM_SAVE_MENU_SECTION, 70, SAVE_GRAPH_IMAGE_MENU_URI);
	}
	
	@SuppressWarnings("unchecked")
	protected Component createCustomComponent() {
		saveDiagramMenu = new JMenu("Save diagram");
		saveDiagramMenu.setToolTipText("Open this menu to save diagram in various formats");
		for (int i = 0; i < saveTypes.length; i++) {
			String type = saveTypes[i];
			String extension = saveExtensions[i];
			ImageIcon icon = new ImageIcon(WorkbenchIcons.class
					.getResource("graph/saveAs" + type.toUpperCase() + ".png"));
			JMenuItem item = new JMenuItem("Save as " + saveTypeNames[i],
					icon);
			item.addActionListener(new DotInvoker(type, extension));
			modelMap.addObserver(perspectiveObserver);
			saveDiagramMenu.add(item);
		}
		return saveDiagramMenu;
	}
	
	class DotInvoker implements ActionListener {
		String type = "dot";
		String extension = "dot";

		public DotInvoker() {
			//
		}

		public DotInvoker(String type, String extension) {
			this.type = type;
			this.extension = extension;
		}

		public void actionPerformed(ActionEvent e) {
			Dataflow dataflow = FileManager.getInstance().getCurrentDataflow();
			if (dataflow == null) {
				JOptionPane
				.showMessageDialog(
						null,
						"Can't save an empty diagram.",
						"Warning", JOptionPane.WARNING_MESSAGE);
			}
			else{
				File file = saveDialogue(null, dataflow, extension,
						"Save workflow diagram");
				if (file != null) {// User did not cancel
					try {
						
						SVGGraphController graphController = GraphViewComponent.graphControllerMap.get(dataflow);
						
						if (type.equals("dot")) {
							// Just write out the dot text, no processing required
							PrintWriter out = new PrintWriter(new FileWriter(file));
							DotWriter dotWriter = new DotWriter(out);
							dotWriter.writeGraph(graphController.generateGraph());
							out.flush();
							out.close();
						} 
						else{
							String dotLocation = (String)WorkbenchConfiguration.getInstance().getProperty("taverna.dotlocation");
							if (dotLocation == null) {
								dotLocation = "dot";
							}
							logger.debug("GraphViewComponent: Invoking dot...");
							Process dotProcess = Runtime.getRuntime().exec(
									new String[] { dotLocation, "-T" + type });
						
							FileOutputStream fos = new FileOutputStream(file);
						
							StringWriter stringWriter = new StringWriter();
							DotWriter dotWriter = new DotWriter(stringWriter);
							dotWriter.writeGraph(graphController.generateGraph());
							
							OutputStream dotOut = dotProcess.getOutputStream();
							dotOut.write(SVGUtil.getDot(stringWriter.toString()).getBytes());
							dotOut.flush();
							dotOut.close();
							new StreamDevourer(dotProcess.getErrorStream()).start();
							new StreamCopier(dotProcess.getInputStream(), fos).start();
							
						}
					} catch (Exception ex) {
						logger.warn("GraphViewComponent: Could not save diagram to " + file, ex);
						JOptionPane.showMessageDialog(null,
								"Problem saving diagram : \n" + ex.getMessage(),
								"Error!", JOptionPane.ERROR_MESSAGE);
					}
				}
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
	private File saveDialogue(Component parentComponent,
			Dataflow dataflow, String extension, String windowTitle) {

		JFileChooser fc = new JFileChooser();
		Preferences prefs = Preferences
				.userNodeForPackage(SaveGraphImageAction.class);
		String curDir = prefs
				.get("currentDir", System.getProperty("user.home"));
		String suggestedFileName = "";
		// Get the source the workflow was loaded from - can be File, URL, or InputStream
		Object source  = FileManager.getInstance().getDataflowSource(dataflow);
		if (source instanceof File){
			suggestedFileName = ((File)source).getName();
			// remove the file extension
			suggestedFileName = suggestedFileName.substring(0,suggestedFileName.lastIndexOf("."));
		}
		else if (source instanceof URL){
			suggestedFileName = ((URL)source).getPath();
			// remove the file extension
			suggestedFileName = suggestedFileName.substring(0,suggestedFileName.lastIndexOf("."));
		}
		else{
			// We cannot suggest the file name if workflow was read from an InputStream
		}
		
		fc.setDialogTitle(windowTitle);
		fc.resetChoosableFileFilters();
		fc.setFileFilter(new ExtensionFileFilter(new String[] { extension }));
		if (suggestedFileName.equals("")) {
			// No file suggestion, just the directory
			fc.setCurrentDirectory(new File(curDir));
		} else {
			// Suggest a filename from the workflow file name
			fc.setSelectedFile(new File(curDir, suggestedFileName + "." + extension));
		}
		
		// Do the "Do you want to overwrite?" if user selected an already existing file
		boolean tryAgain = true;
		while (tryAgain) {
			tryAgain = false;
			
			int returnVal = fc.showSaveDialog(parentComponent);
			if (returnVal != JFileChooser.APPROVE_OPTION) {
				logger.info("GraphViewComponent: Aborting diagram save of " + suggestedFileName);
				return null;
			}
			else{
				File file = fixExtension(fc.getSelectedFile(), extension);
				logger.debug("GraphViewComponent: Selected " + file + " for save");
				prefs.put("currentDir", fc.getCurrentDirectory().toString());
				
				if (file.exists()){ // File already exists
					// Ask the user if they want to overwrite the file
					String msg = file.getAbsolutePath() + " already exists. Do you want to overwrite it?";
					int ret = JOptionPane.showConfirmDialog(
							null, msg, "File already exists",
							JOptionPane.YES_NO_OPTION);
					
					if (ret == JOptionPane.YES_OPTION) {
						return file;
					}
					else{
						tryAgain = true;
					}
				}
				else{ 
					return file;
				}
			}
		}
		return null; // should not get to here, but java was complaining
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
		if (file.getName().endsWith("." + extension)) {
			return file;
		}
		// Append the extension (keep the existing one)
		String name = file.getName();
		return new File(file.getParent(), name + "." + extension);
	}

	/**
	 * Modify the enabled/disabled state of the action when ModelMapConstants.CURRENT_PERSPECTIVE has been
	 * modified (i.e. when perspective has been switched).
	 */
	public class CurrentPerspectiveObserver implements Observer<ModelMapEvent> {
		public void notify(Observable<ModelMapEvent> sender,
				ModelMapEvent message) throws Exception {
			if (message.getModelName().equals(
					ModelMapConstants.CURRENT_PERSPECTIVE)) {
				if (message.getNewModel() instanceof WorkflowPerspective) {
					saveDiagramMenu.setEnabled(true);
				}
				else{
					saveDiagramMenu.setEnabled(false);
				}
			}
		}
	}
}
