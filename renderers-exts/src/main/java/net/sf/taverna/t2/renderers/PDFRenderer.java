package net.sf.taverna.t2.renderers;

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

import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.renderers.Renderer;
import net.sf.taverna.t2.renderers.RendererException;

/**
 * PDF renderer for MIME type application/pdf using the ICE PDF Java viewer.
 * 
 * @author Peter Li
 */
public class PDFRenderer implements Renderer
{
    private Logger logger = Logger.getLogger(PDFRenderer.class);

	private Pattern pattern;

	private float MEGABYTE = 1024 * 1024;
    private int meg = 1048576;

    public PDFRenderer()
    {
		pattern = Pattern.compile(".*application/pdf.*");
    }

    public boolean canHandle(String mimeType)
    {
		return pattern.matcher(mimeType).matches();
    }

    public boolean isTerminal()
    {
        return true;
    }

    public String getType()
    {
        return "PDF";
    }

    public boolean canHandle(ReferenceService referenceService,
            T2Reference reference, String mimeType) throws RendererException
    {
        return canHandle(mimeType);
    }

    public JComponent getComponent(ReferenceService referenceService,
            T2Reference reference) throws RendererException
    {
        byte[] resolve = null;
        try
        {
            // Resolve into a byte[]
            resolve = (byte[]) referenceService.renderIdentifier(reference,
                    byte[].class, null);
        } catch (Exception e)
        {
            logger.error(
                    "Reference Service failed to render data as byte array", e);
            return new JEditorPane("application/pdf",
                    "Reference Service failed to render data as byte array\n"
                            + e.toString());
        }

        if (resolve.length > meg)
        {
            int response = JOptionPane
                    .showConfirmDialog(
                            null,
                            "Result is approximately "
                                    + bytesToMeg(((byte[]) resolve).length)
                                    + " Mb in size, there could be issues with rendering this inside Taverna\nDo you want to continue?",
                            "Render this as text/html?",
                            JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.NO_OPTION)
            {
                return new JTextArea(
                        "Rendering cancelled due to size of file. Try saving and viewing in an external application");
            }
        }

        // Build a controller
        SwingController controller = new SwingController();
        // Build a SwingViewFactory configured with the controller
        SwingViewBuilder factory = new SwingViewBuilder(controller);
        // Use the factory to build a JPanel that is pre-configured
        // with a complete, active Viewer UI.
        JPanel viewerComponentPanel = factory.buildViewerPanel();
        // Open a PDF document to view
        controller.openDocument(resolve, 0, resolve.length, "PDF Viewer", null);
        return viewerComponentPanel;
    }

    /**
     * Work out size of file in megabytes to 1 decimal place
     * 
     * @param bytes
     * @return
     */
    private int bytesToMeg(long bytes)
    {
        float f = bytes / MEGABYTE;
        Math.round(f);
        return Math.round(f);
    }
}
