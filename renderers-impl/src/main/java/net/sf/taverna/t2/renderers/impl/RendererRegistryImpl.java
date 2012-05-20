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
package net.sf.taverna.t2.renderers.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.renderers.Renderer;
import net.sf.taverna.t2.renderers.RendererException;
import net.sf.taverna.t2.renderers.RendererRegistry;

import org.apache.log4j.Logger;

public class RendererRegistryImpl implements RendererRegistry {

	private static Logger logger = Logger.getLogger(RendererRegistryImpl.class);

	private List<Renderer> renderers;

	@Override
	public List<Renderer> getRenderersForMimeType(InvocationContext context, T2Reference reference,
			String mimeType) {
		ArrayList<Renderer> list = new ArrayList<Renderer>();
		for (Renderer renderer : renderers) {
			try {
				if (renderer.canHandle(context.getReferenceService(), reference, mimeType)) {
					list.add(renderer);
				}
			} catch (RendererException e) {
				logger.warn("Problem with renderer for " + renderer.getType(), e);
			}
		}
		return list;
	}

	@Override
	public List<Renderer> getRenderersForMimeType(ReferenceService refernceService,
			T2Reference reference, String mimeType) {
		ArrayList<Renderer> list = new ArrayList<Renderer>();
		for (Renderer renderer : renderers) {
			try {
				if (renderer.canHandle(refernceService, reference, mimeType)) {
					list.add(renderer);
				}
			} catch (RendererException e) {
				logger.warn("Problem with renderer for " + renderer.getType(), e);
			}
		}
		return list;
	}

	public List<Renderer> getRenderers() {
		return renderers;
	}

	public void setRenderers(List<Renderer> renderers) {
		this.renderers = renderers;
	}

}
