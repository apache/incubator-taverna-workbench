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
package org.apache.taverna.lang.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * Manager for reading key set file
 * 
 * @author Ingo Wassink
 * @author Ian Dunlop
 * @author Alan R Williams
 * 
 */
public class EditorKeySetUtil {
	
	private static Logger logger = Logger.getLogger(EditorKeySetUtil.class);


	public static Set<String> loadKeySet(InputStream stream) {
		Set<String> result = new TreeSet<String>();
				try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(stream));
			                                                     
			String line;
			while ((line = reader.readLine()) != null) {
				result.add(line.trim());
			}
			reader.close();
		} catch (Exception e) {
			logger.error(e);
		}
		return result;
	}
}
