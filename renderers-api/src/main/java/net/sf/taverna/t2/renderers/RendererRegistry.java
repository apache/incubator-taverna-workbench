/*******************************************************************************
 * Copyright (C) 2011 The University of Manchester
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
package net.sf.taverna.t2.renderers;

import java.util.List;

/**
 * Manages the collection of {@linkplain Renderer renderers}.
 * 
 * @author David Withers
 */
public interface RendererRegistry {
	/**
	 * Get all of the available renderers for a specific MIME type. If there is
	 * a problem with one then catch the exception and log the problem but carry
	 * on since there is probably more than one way to render the data
	 * 
	 * @param path
	 * @param mimeType
	 * @return
	 */
	List<Renderer> getRenderersForMimeType(String mimeType);

	/**
	 * Return all the renderers.
	 * 
	 * @return all the renderers
	 */
	List<Renderer> getRenderers();
}
