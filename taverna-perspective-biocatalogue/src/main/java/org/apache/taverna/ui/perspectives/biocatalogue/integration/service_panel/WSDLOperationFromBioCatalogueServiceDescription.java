package org.apache.taverna.ui.perspectives.biocatalogue.integration.service_panel;
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

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import org.apache.taverna.biocatalogue.model.SoapOperationIdentity;
import org.apache.taverna.activities.wsdl.WSDLActivity;
import org.apache.taverna.activities.wsdl.WSDLActivityConfigurationBean;
import org.apache.taverna.activities.wsdl.servicedescriptions.WSDLActivityIcon;
import org.apache.taverna.lang.beans.PropertyAnnotation;
import org.apache.taverna.servicedescriptions.ServiceDescription;

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
