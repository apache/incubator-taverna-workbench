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

package org.apache.taverna.renderers.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.taverna.renderers.Renderer;
import org.apache.taverna.renderers.RendererRegistry;

/**
 * Implementation of a RendererRegistry.
 *
 * @author David Withers
 */
public class RendererRegistryImpl implements RendererRegistry {
	private List<Renderer> renderers;

	@Override
	public List<Renderer> getRenderersForMimeType(String mimeType) {
		ArrayList<Renderer> list = new ArrayList<>();
		for (Renderer renderer : renderers)
			if (renderer.canHandle(mimeType))
				list.add(renderer);
		return list;
	}

	@Override
	public List<Renderer> getRenderers() {
		return renderers;
	}

	public void setRenderers(List<Renderer> renderers) {
		this.renderers = renderers;
	}
}
