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

package org.apache.taverna.lang.io;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * Copies an InputStream to an OutputStream.
 * 
 * @author Tom Oinn
 */
public class StreamCopier extends Thread {

	private static Logger logger = Logger
	.getLogger(StreamCopier.class);

	InputStream is;

	OutputStream os;

	/**
	 * Create a new StreamCopier which will, when started, copy the specified
	 * InputStream to the specified OutputStream
	 */
	public StreamCopier(InputStream is, OutputStream os) {
		super("StreamCopier");
		this.is = is;
		this.os = os;
	}

	/**
	 * Start copying the stream, exits when the InputStream runs out of data
	 */
	public void run() {
		try {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.flush();
			os.close();
		} catch (Exception ex) {
			logger.error("Could not copy stream", ex);
		}
	}

}
