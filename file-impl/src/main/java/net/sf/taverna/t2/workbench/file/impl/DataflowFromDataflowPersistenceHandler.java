/**
 * 
 */
package net.sf.taverna.t2.workbench.file.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workbench.file.impl.FileDataflowInfo;
import net.sf.taverna.t2.workbench.file.impl.T2DataflowOpener;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workbench.file.DataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.AbstractDataflowPersistenceHandler;

/**
 * @author alanrw
 *
 */
public class DataflowFromDataflowPersistenceHandler extends AbstractDataflowPersistenceHandler implements DataflowPersistenceHandler {
	
	private static final T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();
	private static Logger logger = Logger.getLogger(DataflowFromDataflowPersistenceHandler.class);

	@Override
	public DataflowInfo openDataflow(FileType fileType, Object source)
			throws OpenException {
		if (!getOpenFileTypes().contains(fileType)) {
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		}
		Dataflow d = (Dataflow) source;
		Date lastModified = null;
		Object canonicalSource = null;

		return new DataflowInfo(T2_FLOW_FILE_TYPE, canonicalSource, d,
				lastModified);
	}

	
	@Override
	public List<FileType> getOpenFileTypes() {
		return Arrays.<FileType> asList(T2_FLOW_FILE_TYPE);
	}

	@Override
	public List<Class<?>> getOpenSourceTypes() {
		return Arrays.<Class<?>> asList(Dataflow.class);
	}

}
