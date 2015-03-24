/**
 *
 */
package net.sf.taverna.t2.workbench.file.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.sf.taverna.t2.workbench.file.AbstractDataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.DataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Workflow;

/**
 * @author alanrw
 */
public class DataflowFromDataflowPersistenceHandler extends
		AbstractDataflowPersistenceHandler implements
		DataflowPersistenceHandler {
	private static final WorkflowBundleFileType WORKFLOW_BUNDLE_FILE_TYPE = new WorkflowBundleFileType();

	@Override
	public DataflowInfo openDataflow(FileType fileType, Object source)
			throws OpenException {
		if (!getOpenFileTypes().contains(fileType))
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);

		WorkflowBundle workflowBundle = (WorkflowBundle) source;
		Date lastModified = null;
		Object canonicalSource = null;
		return new DataflowInfo(WORKFLOW_BUNDLE_FILE_TYPE, canonicalSource,
				workflowBundle, lastModified);
	}

	@Override
	public List<FileType> getOpenFileTypes() {
		return Arrays.<FileType> asList(WORKFLOW_BUNDLE_FILE_TYPE);
	}

	@Override
	public List<Class<?>> getOpenSourceTypes() {
		return Arrays.<Class<?>> asList(Workflow.class);
	}
}
