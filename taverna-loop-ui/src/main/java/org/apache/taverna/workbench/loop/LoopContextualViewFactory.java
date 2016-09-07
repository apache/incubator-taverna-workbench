package org.apache.taverna.workbench.loop;

import java.util.Arrays;
import java.util.List;

import org.apache.taverna.scufl2.api.core.Processor;

import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

public class LoopContextualViewFactory implements ContextualViewFactory<Processor> {

	private EditManager editManager;
	private FileManager fileManager;

	public boolean canHandle(Object selection) {
		return selection instanceof Processor;
	}

	public List<ContextualView> getViews(Processor selection) {
		return Arrays.asList(new ContextualView[] {new LoopContextualView(selection, editManager, fileManager)});
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
}
