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
package net.sf.taverna.t2.activities.stringconstant.servicedescriptions;

import javax.swing.Icon;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.activities.stringconstant.query.StringConstantActivityIcon;
import net.sf.taverna.t2.servicedescriptions.AbstractTemplateService;

public class StringConstantTemplateService extends AbstractTemplateService<StringConstantConfigurationBean>{
	
	private static final String STRINGCONSTANT = "String constant";

	@Override
	public Class<StringConstantActivity> getActivityClass() {
		return StringConstantActivity.class;
	}

	@Override
	public StringConstantConfigurationBean getActivityConfiguration() {
		StringConstantConfigurationBean stringConstantConfigurationBean = new StringConstantConfigurationBean();
		stringConstantConfigurationBean.setValue("Add your own value here");
		return stringConstantConfigurationBean;
	}

	@Override
	public Icon getIcon() {
		return StringConstantActivityIcon.getStringConstantIcon();
	}

	public String getName() {
		return STRINGCONSTANT;
	}

	public String getDescription() {
		return "A string value that you can set";
	}
}
