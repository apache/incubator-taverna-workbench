/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

package org.apache.taverna.workbench.ui.views.contextualviews.annotated;

import static java.util.Collections.singletonList;

import java.util.List;

import org.apache.taverna.annotation.Annotated;
import org.apache.taverna.annotation.AnnotationBeanSPI;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import org.apache.taverna.workflowmodel.processor.activity.Activity;

public class AnnotatedContextualViewFactory implements
		ContextualViewFactory<Annotated<?>> {
	private EditManager editManager;
	private List<AnnotationBeanSPI> annotationBeans;
	private SelectionManager selectionManager;

	@Override
	public boolean canHandle(Object selection) {
		return ((selection instanceof Annotated) && !(selection instanceof Activity));
	}

	@Override
	public List<ContextualView> getViews(Annotated<?> selection) {
		return singletonList((ContextualView) new AnnotatedContextualView(
				selection, editManager, selectionManager, annotationBeans));
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setAnnotationBeans(List<AnnotationBeanSPI> annotationBeans) {
		this.annotationBeans = annotationBeans;
	}
}
