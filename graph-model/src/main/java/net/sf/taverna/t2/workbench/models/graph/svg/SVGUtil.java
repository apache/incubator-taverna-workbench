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
package net.sf.taverna.t2.workbench.models.graph.svg;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.taverna.t2.lang.io.StreamDevourer;
import net.sf.taverna.t2.workbench.models.graph.GraphShapeElement.Shape;
import net.sf.taverna.t2.workbench.ui.impl.configuration.WorkbenchConfiguration;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMPoint;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGMatrix;

/**
 * Utility methods.
 * 
 * @author David Withers
 */
public class SVGUtil {

	public static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

	private static Logger logger = Logger.getLogger(SVGUtil.class);

	private static SAXSVGDocumentFactory docFactory;

	static {
		String parser = XMLResourceDescriptor.getXMLParserClassName();
		logger.info("Using XML parser " + parser);
		docFactory = new SAXSVGDocumentFactory(parser);
	}

	public static SVGDocument createSVGDocument() {
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		return (SVGDocument) impl.createDocument(svgNS, "svg", null);
	}
	
	/**
	 * Converts a point in screen coordinates to a point in document coordinates.
	 * 
	 * @param locatable
	 * @param screenPoint
	 * @return
	 */
	public static SVGOMPoint screenToDocument(SVGLocatable locatable, SVGOMPoint screenPoint) {
        SVGMatrix mat = ((SVGLocatable) locatable.getFarthestViewportElement()).getScreenCTM().inverse();
        return (SVGOMPoint) screenPoint.matrixTransform(mat);
	}
	
	/**
	 * Writes SVG to the console. For debugging only.
	 *
	 * @param svgDocument
	 */
	public static void writeSVG(SVGDocument svgDocument) {
		try {
			TransformerFactory tranFact = TransformerFactory.newInstance(); 
			Transformer transfor = tranFact.newTransformer(); 
			Node node = svgDocument.getDocumentElement();
			Source src = new DOMSource(node); 
			Result dest = new StreamResult(System.out);
			transfor.transform(src, dest);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generates an SVGDocument from DOT text by calling out to GraphViz.
	 * 
	 * @param dotText
	 * @return
	 * @throws IOException
	 */
	public static SVGDocument getSVG(String dotText) throws IOException {
		String dotLocation = (String)WorkbenchConfiguration.getInstance().getProperty("taverna.dotlocation");
		if (dotLocation == null) {
			dotLocation = "dot";
		}
		logger.debug("Invoking dot...");
		Process dotProcess = Runtime.getRuntime().exec(
				new String[] { dotLocation, "-Tsvg" });
		StreamDevourer devourer = new StreamDevourer(dotProcess
				.getInputStream());
		devourer.start();
		// Must create an error devourer otherwise stderr fills up and the
		// process stalls!
		StreamDevourer errorDevourer = new StreamDevourer(dotProcess
				.getErrorStream());
		errorDevourer.start();
		PrintWriter out = new PrintWriter(dotProcess.getOutputStream(), true);
		out.print(dotText);
		out.flush();
		out.close();
		
		String svgText = devourer.blockOnOutput();
		// Avoid TAV-424, replace buggy SVG outputted by "modern" GraphViz versions.
		// http://www.graphviz.org/bugs/b1075.html
		// Contributed by Marko Ullgren
		svgText = svgText.replaceAll("font-weight:regular","font-weight:normal");
System.out.println(svgText);
		// Fake URI, just used for internal references like #fish
		return docFactory.createSVGDocument("http://taverna.sf.net/diagram/generated.svg", 
			new StringReader(svgText));
	}

	/**
	 * Generates DOT text with layout information from DOT text by calling out to GraphViz.
	 * 
	 * @param dotText
	 * @return
	 * @throws IOException
	 */
	public static String getDot(String dotText) throws IOException {
		String dotLocation = (String)WorkbenchConfiguration.getInstance().getProperty("taverna.dotlocation");
		if (dotLocation == null) {
			dotLocation = "dot";
		}
		logger.debug("Invoking dot...");
		Process dotProcess = Runtime.getRuntime().exec(
				new String[] { dotLocation, "-Tdot" });
		StreamDevourer devourer = new StreamDevourer(dotProcess
				.getInputStream());
		devourer.start();
		// Must create an error devourer otherwise stderr fills up and the
		// process stalls!
		StreamDevourer errorDevourer = new StreamDevourer(dotProcess
				.getErrorStream());
		errorDevourer.start();
		PrintWriter out = new PrintWriter(dotProcess.getOutputStream(), true);
		out.print(dotText);
		out.flush();
		out.close();
		
		String dot = devourer.blockOnOutput();
//		System.out.println(dot);
		return dot;
}

	public static String getHexValue(Color color) {
		if (color == null) {
			return "none";
		} else {
			return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
		}
	}

	public static double calculateAngle(Element line) {
		float x1 = Float.parseFloat(line.getAttribute(SVGConstants.SVG_X1_ATTRIBUTE));
		float y1 = Float.parseFloat(line.getAttribute(SVGConstants.SVG_Y1_ATTRIBUTE));
		float x2 = Float.parseFloat(line.getAttribute(SVGConstants.SVG_X2_ATTRIBUTE));
		float y2 = Float.parseFloat(line.getAttribute(SVGConstants.SVG_Y2_ATTRIBUTE));		
		return calculateAngle(x1, y1, x2, y2);
	}
	
	public static double calculateAngle(float x1, float y1, float x2, float y2) {
		float dx = x2-x1;
		float dy = y2-y1;
		double angle = Math.atan2(dy, dx);		
		return angle * 180 / Math.PI;
	}

	public static String calculatePoints(Shape shape, int x, int y) {
		StringBuilder sb = new StringBuilder();
		if (Shape.BOX.equals(shape) || Shape.RECORD.equals(shape)) {
			addPoint(sb, 0, 0);
			addPoint(sb, x, 0);
			addPoint(sb, x, y);
			addPoint(sb, 0, y);
		} else if (Shape.HOUSE.equals(shape)) {
			addPoint(sb, x/2f, 0);
			addPoint(sb, x, y/3f);
			addPoint(sb, x, y-3);
			addPoint(sb, 0, y-3);			
			addPoint(sb, 0, y/3f);
		} else if (Shape.INVHOUSE.equals(shape)) {
			addPoint(sb, 0, 3);
			addPoint(sb, x, 3);
			addPoint(sb, x, y/3f*2f);
			addPoint(sb, x/2f, y);			
			addPoint(sb, 0, y/3f*2f);			
		} else if (Shape.TRIANGLE.equals(shape)) {
			addPoint(sb, x/2f, 0);
			addPoint(sb, x, y);
			addPoint(sb, 0, y);
		} else if (Shape.INVTRIANGLE.equals(shape)) {
			addPoint(sb, 0, 0);
			addPoint(sb, x, 0);
			addPoint(sb, x/2f, y);
		} 
		return sb.toString();
	}	

	public static void addPoint(StringBuilder stringBuilder, float x, float y) {
		stringBuilder.append(x + "," + y + " ");
	}

	public static String getPath(List<Point> pointList) {
		StringBuilder sb = new StringBuilder();
		Point firstPoint = pointList.remove(0);
		sb.append("M");
		sb.append(firstPoint.x + "," + firstPoint.y);
		sb.append("C");
		for (Point point : pointList) {
			sb.append(" " + point.x + "," + point.y);
		}
		return sb.toString();
	}
	
}
