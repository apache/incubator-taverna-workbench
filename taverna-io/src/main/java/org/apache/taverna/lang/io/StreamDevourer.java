/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.lang.io;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

/**
 * Devours an input stream and allows the contents to be read as a String once
 * the stream has completed.
 * 
 * @author Tom Oinn
 * @author Alan R Williams
 */
public class StreamDevourer extends Thread {
	
	private static Logger logger = Logger.getLogger(StreamDevourer.class);

	private static byte[] newLine = System.getProperty("line.separator").getBytes();

	BufferedReader br;

	ByteArrayOutputStream output;

	/**
	 * Returns the current value of the internal ByteArrayOutputStream
	 */
	@Override
	public String toString() {
		return output.toString();
	}

	/**
	 * Waits for the stream to close then returns the String representation of
	 * its contents (this is equivalent to doing a join then calling toString)
	 */
	public String blockOnOutput() {
		try {
			this.join();
			return output.toString();
		} catch (InterruptedException ie) {
			logger.error("Interrupted", ie);
			interrupt();
			return "";
		}
	}

	/**
	 * Create the StreamDevourer and point it at an InputStream to consume
	 */
	public StreamDevourer(InputStream is) {
		super("StreamDevourer");
		this.br = new BufferedReader(new InputStreamReader(is));
		this.output = new ByteArrayOutputStream();
	}

	/**
	 * When started this Thread will copy all data from the InputStream into a
	 * ByteArrayOutputStream via a BufferedReader. Because of the use of the
	 * BufferedReader this is only really appropriate for streams of textual
	 * data
	 */
	@Override
	public void run() {
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				// && line.endsWith("</svg>") == false) {
				if (line.endsWith("\\") && !line.endsWith("\\\\")) {
					line = line.substring(0, line.length() - 1);
					output.write(line.getBytes());
				} else {
					output.write(line.getBytes());
					output.write(newLine);
				}
			}
			br.close();
		} catch (IOException ioe) {
			logger.error(ioe);
		}
	}

}
