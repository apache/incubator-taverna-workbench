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

package org.apache.taverna.workbench.ui.credentialmanager.password;

import org.apache.taverna.security.credentialmanager.MasterPasswordProvider;

/**
 * A simple implementation of {@link MasterPasswordProvider} that just provides
 * a master password that can be obtained and set from outside the provider.
 * 
 * @author Alex Nenadic
 */
public class SimpleMasterPasswordProvider implements MasterPasswordProvider {
	private String masterPassword;
	private int priority = 200;
	
	@Override
	public String getMasterPassword(boolean firstTime) {
		return masterPassword;
	}
	
	@Override
	public void setMasterPassword(String masterPassword){
		this.masterPassword = masterPassword;
	}

	@Override
	public int getProviderPriority() {
		return priority;
	}

//	@Override
//	public void setProviderPriority(int priority) {
//		this.priority = priority;		
//	}
}
