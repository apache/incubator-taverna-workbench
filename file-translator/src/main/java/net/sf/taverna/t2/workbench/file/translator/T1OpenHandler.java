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
package net.sf.taverna.t2.workbench.file.translator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.compatibility.WorkflowModelTranslator;
import net.sf.taverna.t2.compatibility.WorkflowTranslationException;
import net.sf.taverna.t2.workbench.file.AbstractDataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.DataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.FileDataflowInfo;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;

public class T1OpenHandler extends AbstractDataflowPersistenceHandler implements
		DataflowPersistenceHandler {

	private static Logger logger = Logger.getLogger(T1OpenHandler.class);

	private ApplicationRuntime applicationRuntime = ApplicationRuntime
			.getInstance();

	private final ScuflFileType SCUFL_FILE_TYPE = new ScuflFileType();

	@Override
	public List<FileType> getOpenFileTypes() {
		return Arrays.<FileType> asList(SCUFL_FILE_TYPE);
	}

	@Override
	public List<Class<?>> getOpenSourceTypes() {
		return Arrays.<Class<?>> asList(InputStream.class, URL.class,
				File.class);
	}

	@Override
	public DataflowInfo openDataflow(FileType fileType, Object source)
			throws OpenException {

		TavernaSPIRegistry.setRepository(applicationRuntime
				.getRavenRepository());

		if (!getOpenFileTypes().contains(fileType)) {
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		}
		InputStream inputStream;
		Date lastModified = null;
		Object canonicalSource = source;
		if (source instanceof InputStream) {
			inputStream = (InputStream) source;
		} else if (source instanceof File) {
			try {
				inputStream = new FileInputStream((File) source);
			} catch (FileNotFoundException e) {
				throw new OpenException("Could not open file " + source, e);
			}
		} else if (source instanceof URL) {
			URL url = ((URL) source);
			try {
				URLConnection connection = url.openConnection();
				connection.setRequestProperty("Accept", "text/xml");
				inputStream = connection.getInputStream();
				if (connection.getLastModified() != 0) {
					lastModified = new Date(connection.getLastModified());
				}
			} catch (IOException e) {
				throw new OpenException("Could not open connection to URL "
						+ source, e);
			}
			if (url.getProtocol().equalsIgnoreCase("file")) {
				try {
					canonicalSource = new File(url.toURI());
				} catch (URISyntaxException e) {
					logger.warn("Invalid file URI created from " + url);
				}
			}
		} else {
			throw new IllegalArgumentException("Unsupported source type "
					+ source.getClass());
		}

		final Dataflow dataflow;
		try {
			dataflow = openDataflowStream(inputStream);
		} finally {
			if (!(source instanceof InputStream)) {
				// We created the stream, we'll close it
				try {
					inputStream.close();
				} catch (IOException ex) {
					logger.warn("Could not close inputstream " + inputStream,
							ex);
				}
			}
		}
		if (canonicalSource instanceof File) {
			return new FileDataflowInfo(SCUFL_FILE_TYPE,
					(File) canonicalSource, dataflow);
		}
		return new DataflowInfo(SCUFL_FILE_TYPE, canonicalSource, dataflow,
				lastModified);
	}

	protected Dataflow openDataflowStream(InputStream inputStream)
			throws OpenException {
		ScuflModel scuflModel = new ScuflModel();
		try {
			XScuflParser.populate(inputStream, scuflModel, null);
		} catch (ScuflException e) {
			throw new OpenException("Could not parse Scufl file", e);
		}
		Dataflow dataflow;
		try {
			dataflow = WorkflowModelTranslator.doTranslation(scuflModel);
		} catch (WorkflowTranslationException e) {
			throw new OpenException("Could not translate Scufl file:\n " + e.getMessage().toString(), e);
		}
		return dataflow;
	}

}
