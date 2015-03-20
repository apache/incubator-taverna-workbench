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
package net.sf.taverna.t2.renderers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;


public class TestRendererSPI {
	
	private static final String TEST_NS = "testNS";
	
	@Test
	public void doNothing() {
		//do nothing for the moment
	}
	
//	@Test
//	public void getAllRenderers() {
//		RendererRegistry rendererRegistry = new RendererRegistry();
//		assertEquals(rendererRegistry.getInstances().size(), 10);
//	}
//	
//	@Test
//	public void checkTextHtmlMimeType() throws EmptyListException, MalformedListException, UnsupportedObjectTypeException {
//		String mimeType = "text/html";
//		String html = "<HTML><HEAD></HEAD><BODY>hello</BODY></HTML>";
//		EntityIdentifier entityIdentifier = facade.register(html, "utf-8");
//		RendererRegistry rendererRegistry = new RendererRegistry();
//		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(facade, entityIdentifier, mimeType);
//		assertEquals(renderersForMimeType.size(),2);
//		assertEquals(renderersForMimeType.get(0).getClass().getSimpleName(), "TextRenderer");
//		assertEquals(renderersForMimeType.get(1).getClass().getSimpleName(), "TextHtmlRenderer");
//		assertTrue(renderersForMimeType.get(0).canHandle("text/html"));
//	}
//	
//	@Test
//	public void checkURLMimeType() throws EmptyListException, MalformedListException, UnsupportedObjectTypeException {
//		String mimeType = "text/x-taverna-web-url.text";
//		String url = "http://google.com";
//		EntityIdentifier entityIdentifier = facade.register(url);
//		RendererRegistry rendererRegistry = new RendererRegistry();
//		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(facade, entityIdentifier, mimeType);
//		assertEquals(renderersForMimeType.size(),2);
//		assertEquals(renderersForMimeType.get(0).getClass().getSimpleName(), "TextRenderer");
//		assertEquals(renderersForMimeType.get(1).getClass().getSimpleName(), "TextTavernaWebUrlRenderer");
//		assertTrue(renderersForMimeType.get(1).canHandle("text/x-taverna-web-url.text"));
//	}
//	
//	@Test
//	public void checkJMolMimeType() throws EmptyListException, MalformedListException, UnsupportedObjectTypeException {
//		String mimeType ="chemical/x-pdb";
//		String jmol = "jmol";
//		EntityIdentifier entityIdentifier = facade.register(jmol);
//		RendererRegistry rendererRegistry = new RendererRegistry();
//		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(facade, entityIdentifier, mimeType);
//		assertEquals(renderersForMimeType.size(), 1);
//		assertEquals(renderersForMimeType.get(0).getClass().getSimpleName(), "JMolRenderer");
//		assertTrue(renderersForMimeType.get(0).canHandle("chemical/x-mdl-molfile"));
//		assertTrue(renderersForMimeType.get(0).canHandle("chemical/x-cml"));
//	}
//	
//	@Test
//	public void checkSeqVistaMimeType() throws EmptyListException, MalformedListException, UnsupportedObjectTypeException {
//		String mimeType ="chemical/x-swissprot";
//		String type = "seqvista";
//		EntityIdentifier entityIdentifier = facade.register(type);
//		RendererRegistry rendererRegistry = new RendererRegistry();
//		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(facade, entityIdentifier, mimeType);
//		assertEquals(renderersForMimeType.size(), 1);
//		assertEquals(renderersForMimeType.get(0).getClass().getSimpleName(), "SeqVistaRenderer");
//		assertTrue(renderersForMimeType.get(0).canHandle("chemical/x-embl-dl-nucleotide"));
//		assertTrue(renderersForMimeType.get(0).canHandle("chemical/x-fasta"));
//	}
//	
//	@Test
//	public void checkSVGMimeType() throws EmptyListException, MalformedListException, UnsupportedObjectTypeException {
//		String mimeType ="image/svg+xml";
//		String type = "SVG";
//		EntityIdentifier entityIdentifier = facade.register(type);
//		RendererRegistry rendererRegistry = new RendererRegistry();
//		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(facade, entityIdentifier, mimeType);
//		assertEquals(renderersForMimeType.size(), 2);
//		assertEquals(renderersForMimeType.get(1).getClass().getSimpleName(), "SVGRenderer");
//	}
//	
//	@Test
//	public void checkTextMimeType() throws EmptyListException, MalformedListException, UnsupportedObjectTypeException {
//		String mimeType ="text/text";
//		String type = "text";
//		EntityIdentifier entityIdentifier = facade.register(type);
//		RendererRegistry rendererRegistry = new RendererRegistry();
//		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(facade, entityIdentifier, mimeType);
//		assertEquals(renderersForMimeType.size(), 1);
//		assertEquals(renderersForMimeType.get(0).getClass().getSimpleName(), "TextRenderer");
//	}
//	
//	@Test
//	public void checkTextRtfMimeType() throws EmptyListException, MalformedListException, UnsupportedObjectTypeException {
//		String mimeType ="text/rtf";
//		String type = "textRTF";
//		EntityIdentifier entityIdentifier = facade.register(type);
//		RendererRegistry rendererRegistry = new RendererRegistry();
//		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(facade, entityIdentifier, mimeType);
//		assertEquals(renderersForMimeType.size(), 2);
//		assertEquals(renderersForMimeType.get(1).getClass().getSimpleName(), "TextRtfRenderer");
//	}
//	
//	@Test
//	public void checkTextXMLMimeType() throws EmptyListException, MalformedListException, UnsupportedObjectTypeException {
//		String mimeType ="text/xml";
//		String type = "textXML";
//		EntityIdentifier entityIdentifier = facade.register(type);
//		RendererRegistry rendererRegistry = new RendererRegistry();
//		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(facade, entityIdentifier, mimeType);
//		assertEquals(renderersForMimeType.size(), 2);
//		assertEquals(renderersForMimeType.get(1).getClass().getSimpleName(), "TextXMLRenderer");
//	}
//	
//	@Before
//	public void setDataManager() {
//		// dManager = new FileDataManager("testNS",
//		// new HashSet<LocationalContext>(), new File("/tmp/fish"));
//		dManager = new InMemoryDataManager(TEST_NS,
//				new HashSet<LocationalContext>());
//		facade = new DataFacade(dManager);
//	}

}
