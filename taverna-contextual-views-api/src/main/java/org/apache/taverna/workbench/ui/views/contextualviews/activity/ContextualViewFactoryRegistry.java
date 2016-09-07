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

package org.apache.taverna.workbench.ui.views.contextualviews.activity;

import java.util.List;

/**
 * A registry for discovering ActivityViewFactories for a given object,
 * like an {@link org.apache.taverna.workflowmodel.processor.activity.Activity}.
 *
 * @author David Withers
 */
public interface ContextualViewFactoryRegistry {
	/**
	 * Discover and return the ContextualViewFactory associated to the provided
	 * object. This is accomplished by returning the discovered
	 * {@link ContextualViewFactory#canHandle(Object)} that returns true for
	 * that Object.
	 *
	 * @param object
	 * @return
	 * @see ContextualViewFactory#canHandle(Object)
	 */
	public <T> List<ContextualViewFactory<? super T>> getViewFactoriesForObject(T object);
}
