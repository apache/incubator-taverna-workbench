package org.apache.taverna.renderers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.taverna.renderers.impl.RendererRegistryImpl;
import org.junit.Before;
import org.junit.Test;

public class TestRendererSPI {

	private RendererRegistryImpl rendererRegistry;

	@Before
	public void makeRendererRegistry() {
		// For the test we'll look up
		// META-INF/services/org.apache.taverna.renderers.Renderer
		ServiceLoader<Renderer> sl = ServiceLoader.load(Renderer.class, getClass().getClassLoader());
		rendererRegistry = new RendererRegistryImpl();
		List<Renderer> renderers = new ArrayList<>();
		for (Renderer r : sl) {
			renderers.add(r);
		}
		assertFalse("No renderers found", renderers.isEmpty());
		rendererRegistry.setRenderers(renderers);
	}

	@Test
	public void checkTextHtmlMimeType() {
		String mimeType = "text/html";
		String html = "<HTML><HEAD></HEAD><BODY>hello</BODY></HTML>";
		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(mimeType);
		assertEquals(2, renderersForMimeType.size());
		assertEquals("HTMLBrowserRenderer", renderersForMimeType.get(0).getClass().getSimpleName());
		assertEquals("TextRenderer", renderersForMimeType.get(1).getClass().getSimpleName());
		assertTrue(renderersForMimeType.get(0).canHandle("text/html"));
	}

	@Test
	public void checkSVGMimeType() {
		String mimeType = "image/svg+xml";
		String type = "SVG";
		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(mimeType);
		assertEquals("SVGRenderer", renderersForMimeType.get(0).getClass().getSimpleName());
	}

	@Test
	public void checkPDFMimeType() throws Exception {
		String mimeType = "application/pdf";
		String type = "PDF";
		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(mimeType);
		assertEquals("PDFRenderer", renderersForMimeType.get(0).getClass().getSimpleName());
	}

	@Test
	public void checkTextMimeType() {
		String mimeType = "text/text";
		String type = "text";
		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(mimeType);
		assertEquals(renderersForMimeType.size(), 1);
		assertEquals("TextRenderer", renderersForMimeType.get(0).getClass().getSimpleName());
	}

	@Test
	public void checkTextRtfMimeType() {
		String mimeType = "text/rtf";
		String type = "textRTF";
		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(mimeType);
		assertEquals(renderersForMimeType.size(), 2);
		assertEquals("TextRtfRenderer", renderersForMimeType.get(1).getClass().getSimpleName());
	}

	@Test
	public void checkTextXMLMimeType() {
		String mimeType = "text/xml";
		String type = "textXML";
		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(mimeType);
		assertEquals(renderersForMimeType.size(), 2);
		assertEquals("TextXMLRenderer", renderersForMimeType.get(1).getClass().getSimpleName());
	}

}
