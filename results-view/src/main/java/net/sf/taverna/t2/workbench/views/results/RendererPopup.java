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
package net.sf.taverna.t2.workbench.views.results;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTree;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.renderers.Renderer;
import net.sf.taverna.t2.renderers.RendererException;
import net.sf.taverna.t2.renderers.RendererRegistry;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Displays a {@link JPopupMenu} containing the MIME types appropriate to the
 * workflow result. On selecting one it gets the {@link JPanel} from the
 * corresponding {@link Renderer} and renders the results
 * 
 * Inspired by the ScavengerTreePopupHandler in T1
 * 
 * @author Ian Dunlop
 * 
 */
public class RendererPopup extends MouseAdapter {

	private static Logger logger = Logger.getLogger(RendererPopup.class);
	private JTree tree;
	private JPanel renderedResultPanel;
	private RenderedResultComponent renderedResultComponent;
	private InvocationContext context;

	public RendererPopup(JTree tree, InvocationContext context,
			RenderedResultComponent renderedResultComponent) {
		this.tree = tree;
		this.context = context;
		this.renderedResultComponent = renderedResultComponent;
		renderedResultPanel = new JPanel();
	}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			doEvent(e);
		}
	}

	/**
	 * Similarly handle the mouse released event
	 */
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			doEvent(e);
		}
	}

	void doEvent(MouseEvent e) {
		Object lastSelectedPathComponent = tree.getLastSelectedPathComponent();
		T2Reference token = null;
		if (lastSelectedPathComponent != null) {
			JPopupMenu menu = new JPopupMenu();
			menu.setLabel("Available Renderers:");
			menu.addSeparator();
			if (lastSelectedPathComponent instanceof ResultTreeNode) {
				// TODO: Actually get a list of types
				List<String> types = new ArrayList<String>();
				RendererRegistry rendererRegistry = new RendererRegistry();
				token = null;

				List<Renderer> allRenderers = new ArrayList<Renderer>();
				// if there are no renderers then display these MIME types in a
				// dialogue box
				String allMimeTypes = "";
				for (String type : types) {
					allMimeTypes = allMimeTypes + type + "\n";
					if (type != null) {
						List<Renderer> renderersForMimeType = rendererRegistry
								.getRenderersForMimeType(context, token, type);
						for (Renderer renderer : renderersForMimeType) {
							if (!allRenderers.contains(renderer)) {
								allRenderers.add(renderer);
							}
						}
					} else {
						// mime magic since we have no type
						Object renderIdentifier = context.getReferenceService()
								.renderIdentifier(token, InputStream.class,
										null);
						if (renderIdentifier instanceof InputStream) {
							ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
							try {
								IOUtils.copy((InputStream) renderIdentifier,
										byteStream);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							byte[] byteArray = byteStream.toByteArray();
							MagicMatch magicMatch;
							String mimeType = null;
							try {
								// FIXME please please give me annotations. I
								// don't like this horrible kind of hackery,
								// resolving the reference just to get the mime
								// type - yuck
								magicMatch = Magic.getMagicMatch(byteArray);
								mimeType = magicMatch.getMimeType();
							} catch (MagicParseException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (MagicMatchNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (MagicException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							List<Renderer> renderersForMimeType = rendererRegistry
									.getRenderersForMimeType(context, token,
											mimeType);
							for (Renderer renderer : renderersForMimeType) {
								if (!allRenderers.contains(renderer)) {
									allRenderers.add(renderer);
								}
							}
						}
					}
				}
				if (allRenderers.isEmpty()) {
					JOptionPane.showMessageDialog(tree,
							"Unable to display for mime types " + allMimeTypes,
							"Unable to render", JOptionPane.WARNING_MESSAGE);
				} else {
					for (Renderer renderer : allRenderers) {
						menu.add(getMenuForRenderer(renderer, context, token));
						menu.addSeparator();
					}
				}
				menu.add(new SaveAction(context, token));
				menu.show(tree, e.getX(), e.getY());
			}
		}

	}

	/**
	 * When the {@link JMenuItem} is selected in the {@link RendererPopup} the
	 * appropriate {@link Renderer} will be displayed
	 * 
	 * @param renderer
	 * @param dataFacade
	 * @param identifier
	 * @return
	 */
	private JMenuItem getMenuForRenderer(Renderer renderer,
			final InvocationContext context, T2Reference reference) {
		final Renderer guiRenderer = renderer;
		final InvocationContext guiContext = context;
		final T2Reference guiReference = reference;
		String type = renderer.getType();
		JMenuItem menuItem = new JMenuItem("Render as " + type);
		menuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JComponent component = null;
				try {
					component = guiRenderer.getComponent(guiContext
							.getReferenceService(), guiReference);
				} catch (RendererException e1) {// maybe this should be
					// Exception
					// show the user that something unexpected has happened but
					// continue
					component = new JTextArea(
							"Could not render using renderer type "
									+ guiRenderer.getClass().getName()
									+ "\n"
									+ "Please try with a different renderer if available and consult log for details of problem");
					logger.warn("Couln not render using "
							+ guiRenderer.getClass().getName(), e1);
				}
				// RenderedResultComponent rendererComponent =
				// RendererResultComponentFactory.getInstance().getRendererComponent();
				// rendererComponent.setResultComponent(component);
//				renderedResultComponent.setResultComponent(component);
			}

		});
		return menuItem;
	}

	private class SaveAction extends AbstractAction {

		private final InvocationContext context2;
		private final T2Reference reference;

		private SaveAction(InvocationContext context, T2Reference reference) {
			super("Save to file"); // icon?
			context2 = context;
			this.reference = reference;
		}

		public void actionPerformed(ActionEvent ae) {
			Object resolve = null;
			try {
				resolve = context2.getReferenceService().renderIdentifier(
						reference, Object.class, null);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(tree, "Problem saving data : \n"
						+ e.getMessage(), "Exception!",
						JOptionPane.ERROR_MESSAGE);
			}
			JFileChooser fc = new JFileChooser();
			String curDir = System.getProperty("user.home");
			fc.setCurrentDirectory(new File(curDir));
			// Popup a save dialog and allow the user to store
			// the data to disc
			int returnVal = fc.showSaveDialog(tree);
			if (returnVal != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File file = fc.getSelectedFile();
			try {
				FileOutputStream fos = new FileOutputStream(file);
				if (resolve instanceof byte[]) {
					logger.info("saving resolved entity as byte stream");
					fos.write((byte[]) resolve);
					fos.flush();
					fos.close();
				} else if (resolve instanceof InputStream) {
					logger.info("saving resolved entity as input stream");
					IOUtils.copy((InputStream) resolve, fos);
					fos.flush();
					fos.close();
				} else {
					logger.info("saving resolved entity as a string");
					Writer out = new BufferedWriter(new OutputStreamWriter(fos));
					out.write((String) resolve);
					fos.flush();
					out.flush();
					fos.close();
					out.close();
				}
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(tree, "Problem saving data : \n"
						+ ioe.getMessage(), "Exception!",
						JOptionPane.ERROR_MESSAGE);
			}
		}

	}
}
