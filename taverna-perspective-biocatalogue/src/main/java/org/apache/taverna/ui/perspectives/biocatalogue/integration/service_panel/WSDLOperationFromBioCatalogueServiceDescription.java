package org.apache.taverna.ui.perspectives.biocatalogue.integration.service_panel;

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import org.apache.taverna.biocatalogue.model.SoapOperationIdentity;
import org.apache.taverna.activities.wsdl.WSDLActivity;
import org.apache.taverna.activities.wsdl.WSDLActivityConfigurationBean;
import org.apache.taverna.activities.wsdl.servicedescriptions.WSDLActivityIcon;
import org.apache.taverna.lang.beans.PropertyAnnotation;
import org.apache.taverna.servicedescriptions.ServiceDescription;

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
public class WSDLOperationFromBioCatalogueServiceDescription extends ServiceDescription<WSDLActivityConfigurationBean>
{
  private static final int SHORT_DESCRIPTION_MAX_LENGTH = 200;
  
  private static final String FULL_DESCRIPTION = "Full description";
  
  
  private final String wsdlLocation;
  private final String operationName;
  private final String description;
  
  
  public WSDLOperationFromBioCatalogueServiceDescription(String wsdlLocation, String operationName, String description)
  {
    this.wsdlLocation = wsdlLocation;
    this.operationName = operationName;
    this.description = description;
  }
  
  public WSDLOperationFromBioCatalogueServiceDescription(SoapOperationIdentity soapOpearationIdentity)
  {
    this.wsdlLocation = soapOpearationIdentity.getWsdlLocation();
    this.operationName = soapOpearationIdentity.getOperationName();
    this.description = soapOpearationIdentity.getDescription();
  }
  
  
	@Override
	public Class getActivityClass() {
		return WSDLActivity.class;
	}

	@Override
	public WSDLActivityConfigurationBean getActivityConfiguration() {
		WSDLActivityConfigurationBean bean = new WSDLActivityConfigurationBean();
		bean.setOperation(operationName);
		bean.setWsdl(wsdlLocation);
		return bean;
	}

	@Override
	public Icon getIcon() {
		return WSDLActivityIcon.getWSDLIcon();
	}

	@Override
	public String getName() {
		return (this.operationName);
	}

	/**
	 * Truncates the description if necessary to {@link WSDLOperationFromBioCatalogueServiceDescription#SHORT_DESCRIPTION_MAX_LENGTH} --
	 * to get full description, use {@link WSDLOperationFromBioCatalogueServiceDescription#getFullDescription()}
	 */
	public String getDescription() {
    if (this.description != null && this.description.length() > SHORT_DESCRIPTION_MAX_LENGTH) {
      return (this.description.substring(0, SHORT_DESCRIPTION_MAX_LENGTH) + "(...)");
    }
    else {
      return this.description;
    }
  }
	
	@PropertyAnnotation(displayName = FULL_DESCRIPTION)
	public String getFullDescription() {
	  return this.description;
	}
	
	@Override
	public List<String> getPath() {
		return Arrays.asList(BioCatalogueWSDLOperationServiceProvider.PROVIDER_NAME, "WSDL @ " + this.wsdlLocation);
	}
	
	@Override
	protected List<? extends Object> getIdentifyingData()
	{
	  // This is important - Taverna won't add identical operations
	  // into the Service Panel. These tokens distinguish added items.
		return Arrays.asList(wsdlLocation, operationName);
	}

}
