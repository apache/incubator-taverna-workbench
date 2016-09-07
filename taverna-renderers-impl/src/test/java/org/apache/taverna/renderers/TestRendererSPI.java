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

package org.apache.taverna.renderers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;


@SuppressWarnings("unused")
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
