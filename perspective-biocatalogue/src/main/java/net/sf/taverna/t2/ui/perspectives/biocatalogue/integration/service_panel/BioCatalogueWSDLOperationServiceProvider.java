/*******************************************************************************
 * Copyright (C) 2008-2010 The University of Manchester   
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
package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.service_panel;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.wsdl.Operation;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import net.sf.taverna.biocatalogue.model.SoapOperationIdentity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityHealthChecker;
import net.sf.taverna.t2.servicedescriptions.AbstractConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionRegistryImpl;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

/**
 * Service provider for WSDL operations added to the Service Panel through the
 * BioCatalogue perspective.
 * 
 * @author Alex Nenadic
 */
public class BioCatalogueWSDLOperationServiceProvider extends
	AbstractConfigurableServiceProvider<WSDLOperationFromBioCatalogueServiceDescription> {

	public BioCatalogueWSDLOperationServiceProvider(
			WSDLOperationFromBioCatalogueServiceDescription wsdlOperationDescription) {
		super(wsdlOperationDescription);
	}

	public BioCatalogueWSDLOperationServiceProvider() {
		super(new WSDLOperationFromBioCatalogueServiceDescription(new SoapOperationIdentity("", "", "")));
	}
	
	public static final String PROVIDER_NAME = "BioCatalogue - selected services";
	  
	private static final URI providerId = URI
	.create("http://taverna.sf.net/2010/service-provider/biocatalogue/wsdl");
	
	private static Logger logger = Logger.getLogger(BioCatalogueRESTServiceProvider.class);

	@Override
	protected List<? extends Object> getIdentifyingData() {
		return getConfiguration().getIdentifyingData();
	}

	@Override
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
	    callBack.status("Starting BioCatalogue WSDL Service Provider");
		registerWSDLOperation(getConfiguration(), callBack);
	}

	@Override
	public Icon getIcon() {
		return getConfiguration().getIcon();
	}

	@Override
	public String getId() {
		return providerId.toString();
	}

	@Override
	public String getName() {
		return "BioCatalogue WSDL";
	}
	
	@Override
	public String toString() {
		return "BioCatalogue WSDL service " + getConfiguration().getName();
	}
	
	public static boolean registerWSDLOperation(
			WSDLOperationFromBioCatalogueServiceDescription wsdlOperationDescription,
			FindServiceDescriptionsCallBack callBack)	{
		
		if (callBack == null) {
			// We are not adding service through Taverna service registry's callback and
			// findServiceDescriptionsAsync() -
			// we are adding directly from the BioCatalogue perspective.
			ServiceDescriptionRegistryImpl serviceDescriptionRegistry = ServiceDescriptionRegistryImpl
					.getInstance();
			serviceDescriptionRegistry
					.addServiceDescriptionProvider(new BioCatalogueWSDLOperationServiceProvider(
							wsdlOperationDescription));
			return true;
		} else {
			// Add the WSDL operation to the Service Panel through the callback
			callBack.partialResults(Collections
					.singletonList(wsdlOperationDescription));
			callBack.finished();
			return (true);
		}
	}

	/**
	 * Adds a SOAP/WSDL service and all of its operations into the Taverna's Service Panel.
	 */
	public static boolean registerWSDLService(String wsdlURL, FindServiceDescriptionsCallBack callBack)
	{
		String errorMessage = null;
		Exception ex = null;
		
		List<Operation> operations = null;
		List<WSDLOperationFromBioCatalogueServiceDescription> items = null;
		
		// Do the same thing as in the WSDL service provider
		WSDLParser parser = null;
		try {
			parser = new WSDLParser(wsdlURL);
			operations = parser.getOperations();
			items = new ArrayList<WSDLOperationFromBioCatalogueServiceDescription>();
			for (Operation operation : operations) {
				WSDLOperationFromBioCatalogueServiceDescription item;
				try {
					String operationName = operation.getName();
					String operationDesc = parser.getOperationDocumentation(operationName);
					String use = parser.getUse(operationName);
					String style = parser.getStyle();
					if (!WSDLActivityHealthChecker.checkStyleAndUse(style, use)) {
						logger.warn("Unsupported style and use combination " + style + "/" + use + " for operation " + operationName + " from " + wsdlURL);
						continue;
					}
					item = new WSDLOperationFromBioCatalogueServiceDescription(wsdlURL, operationName, operationDesc);
					items.add(item);					
				} catch (UnknownOperationException e) {
					errorMessage = "Encountered an unexpected operation name:"
							+ operation.getName();
					ex = e;
				}
			}
		} catch (ParserConfigurationException e) {
			errorMessage = "Error configuring the WSDL parser";
			ex = e;
		} catch (WSDLException e) {
			errorMessage = "There was an error with the wsdl: " + wsdlURL;
			ex = e;
		} catch (IOException e) {
			errorMessage = "There was an IO error parsing the wsdl: " + wsdlURL
					+ " Possible reason: the wsdl location was incorrect.";
			ex = e;
		} catch (SAXException e) {
			errorMessage = "There was an error with the XML in the wsdl: "
					+ wsdlURL;
			ex = e;
		} catch (IllegalArgumentException e) { // a problem with the wsdl url
			errorMessage = "There was an error with the wsdl: " + wsdlURL + " "
					+ "Possible reason: the wsdl location was incorrect.";
			ex = e;
		} catch (Exception e) { // anything else we did not expect
			errorMessage = "There was an error with the wsdl: " + wsdlURL;
			ex = e;
		}
		
		if (callBack == null) {
			if (errorMessage != null){
				logger.error(errorMessage, ex);
				return false;
			}
			else{
				// We are not adding service through Taverna service registry's callback and
				// findServiceDescriptionsAsync() -
				// we are adding directly from the BioCatalogue perspective.
				ServiceDescriptionRegistryImpl serviceDescriptionRegistry = ServiceDescriptionRegistryImpl
						.getInstance();
				for (WSDLOperationFromBioCatalogueServiceDescription item : items) {
					serviceDescriptionRegistry
							.addServiceDescriptionProvider(new BioCatalogueWSDLOperationServiceProvider(
									item));
				}
				return true;
			}
		} else {
			if (errorMessage != null){
				callBack.fail(errorMessage, ex);
				return false;
			}
			else{
				callBack.status("Found " + operations.size() + " WSDL operations of service "
						+ wsdlURL);
				callBack.partialResults(items);
				callBack.finished();
				return true;
			}
		}   
	}
}
