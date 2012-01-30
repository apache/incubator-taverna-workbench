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
import java.awt.Desktop;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.renderers.Renderer;
import net.sf.taverna.t2.renderers.RendererException;

/**
 * Web browser renderer for MIME type text/html.
 * 
 * @author Peter Li
 */
public class HTMLBrowserRenderer implements Renderer
{
    private Logger logger = Logger.getLogger(HTMLBrowserRenderer.class);
    private Pattern pattern;

    public HTMLBrowserRenderer()
    {
        pattern = Pattern.compile(".*text/html.*");
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
        return "HTML (in Web browser)";
    }

    public boolean canHandle(ReferenceService referenceService,
            T2Reference reference, String mimeType) throws RendererException
    {
        return canHandle(mimeType);
    }

    public JComponent getComponent(ReferenceService referenceService,
            T2Reference reference) throws RendererException
    {
        JEditorPane editorPane = null;
        String resolve = null;
        try
        {
            // Resolve it as a string
            resolve = (String) referenceService.renderIdentifier(reference,
                    String.class, null);
        } catch (Exception e)
        {
            logger.error("Reference Service failed to render data as string",  e);
            return new JEditorPane("text/html",
                    "Reference Service failed to render data as string\n"
                            + e.toString());
        }

        //Save html as temp file
        File htmlFile = null;
        try
        {
            htmlFile = this.toTempFile(resolve);
        } catch (IOException ioe)
        {
            logger.error("Problem writing HTML data to temporary file");
            return new JEditorPane("text/html",
                  "Problem writing HTML data to temporary file");
        }
        
        // Start Web browser
        try
        {
            Desktop.getDesktop().open(htmlFile);
        } catch (HeadlessException e)
        {
            logger.error("Error attempting to launch Web browser", e);
            return new JEditorPane("text/html",
                    "Error attempting to launch Web browser\n" + e.toString());
        } catch (IOException e) {
           logger.error("Error attempting to launch Web browser", e);
           return new JEditorPane("text/html",
                    "Error attempting to launch Web browser\n" + e.toString());
			
		}

        editorPane = new JEditorPane("text/plain",
        "Launching a Web browser ...");
        
        return editorPane;
    }

    public File toTempFile(String data) throws IOException
    {
        File file = File.createTempFile("temp", ".html");
        PrintWriter out = new PrintWriter(new FileWriter(file));
        out.print(data);
        out.flush();
        out.close();
        return file;
    }
}
