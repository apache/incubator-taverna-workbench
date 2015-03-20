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
package net.sf.taverna.t2.workbench.ui.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import static org.junit.Assert.*;

import net.sf.taverna.t2.workbench.ui.impl.UserRegistrationForm;

import org.junit.Ignore;
import org.junit.Test;

public class UserRegistrationTest {

	@Ignore
	@Test
	public void postUserRegistrationDataToServer() {

		String parameters = "";

		// The 'submit' parameter - to let the server-side script know we are
		// submitting
		// the user's registration form - all other requests will be silently
		// ignored
		try {
			parameters = URLEncoder
					.encode(
							UserRegistrationForm.TAVERNA_REGISTRATION_POST_PARAMETER_NAME,
							"UTF-8")
					+ "=" + URLEncoder.encode("submit", "UTF-8"); // value does
																	// not
																	// matter

			parameters += "&"
					+ URLEncoder
							.encode(
									UserRegistrationForm.TAVERNA_VERSION_POST_PARAMETER_NAME,
									"UTF-8") + "="
					+ URLEncoder.encode("snapshot", "UTF-8");
			parameters += "&"
					+ URLEncoder
							.encode(
									UserRegistrationForm.FIRST_NAME_POST_PARAMETER_NAME,
									"UTF-8") + "="
					+ URLEncoder.encode("Alex", "UTF-8");
			parameters += "&"
					+ URLEncoder.encode(
							UserRegistrationForm.LAST_NAME_POST_PARAMETER_NAME,
							"UTF-8") + "="
					+ URLEncoder.encode("Nenadic", "UTF-8");
			parameters += "&"
					+ URLEncoder
							.encode(
									UserRegistrationForm.EMAIL_ADDRESS_POST_PARAMETER_NAME,
									"UTF-8") + "="
					+ URLEncoder.encode("alex@alex.com", "UTF-8");
			parameters += "&"
					+ URLEncoder
							.encode(
									UserRegistrationForm.KEEP_ME_INFORMED_POST_PARAMETER_PROPERTY_NAME,
									"UTF-8") + "="
					+ URLEncoder.encode("true", "UTF-8");
			parameters += "&"
					+ URLEncoder
							.encode(
									UserRegistrationForm.INSTITUTION_OR_COMPANY_POST_PARAMETER_NAME,
									"UTF-8") + "="
					+ URLEncoder.encode("Uni of Manchester", "UTF-8");
			parameters += "&"
					+ URLEncoder
							.encode(
									UserRegistrationForm.INDUSTRY_TYPE_POST_PARAMETER_NAME,
									"UTF-8") + "="
					+ URLEncoder.encode("Academia", "UTF-8");
			parameters += "&"
					+ URLEncoder.encode(
							UserRegistrationForm.FIELD_POST_PARAMETER_NAME,
							"UTF-8") + "="
					+ URLEncoder.encode("Research", "UTF-8");
			parameters += "&"
					+ URLEncoder.encode(
							UserRegistrationForm.PURPOSE_POST_PARAMETER_NAME,
							"UTF-8") + "=" + URLEncoder.encode("None", "UTF-8");
		} catch (UnsupportedEncodingException ueex) {
			System.out
					.println("Failed to url encode post parameters when sending user registration data.");
		}
		String server = "http://cactus.cs.man.ac.uk/~alex/taverna_registration/registration.php";
		server = "http://localhost/~alex/taverna_registration/registration.php";
		// server = "https://somehost.co.uk";

		System.out.println("Posting user registartion to " + server
				+ " with parameters: " + parameters);
		String response = "";
		try {
			URL url = new URL(server);
			URLConnection conn = url.openConnection();
			System.out.println("Opened a connection");
			// Set timeout for connection, otherwise we might hang too long
			// if server is not responding and it will block Taverna
			conn.setConnectTimeout(7000);
			// Set connection parameters
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			// Make server believe we are HTML form data...
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			System.out
					.println("Trying to get an output stream from the connection");
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			// Write out the bytes of the content string to the stream.
			out.writeBytes(parameters);
			out.flush();
			out.close();
			// Read response from the input stream.
			BufferedReader in = new BufferedReader(new InputStreamReader(conn
					.getInputStream()));
			String temp;
			while ((temp = in.readLine()) != null) {
				response += temp + "\n";
			}
			// Remove the last \n character
			if (!response.equals("")) {
				response = response.substring(0, response.length() - 1);
			}
			in.close();
			System.out.println(response);
			if (!response.equals("Registration successful!")) {
				System.out
						.println("Registration failed. Response form server was: "
								+ response);
			}
			assertTrue(response.equals("Registration successful!"));
		}
		// Catch some runtime exceptions
		catch (ConnectException ceex) { // the connection was refused remotely
										// (e.g. no process is listening on the
										// remote address/port).
			System.out
					.println("User registration failed: Registration server is not listening of the specified url.");
			ceex.printStackTrace();
		}
		// Catch some runtime exceptions
		catch (SocketTimeoutException stex) { // timeout has occurred on a
												// socket read or accept.
			System.out
					.println("User registration failed: Socket timeout occurred.");
			stex.printStackTrace();
		} catch (MalformedURLException muex) {
			System.out
					.println("User registration failed: Registartion server's url is malformed.");
			muex.printStackTrace();
		} catch (IOException ioex) {
			System.out
					.println("User registration failed: Failed to open url connection to registration server or writing to it or reading from it.");
			ioex.printStackTrace();
		}
	}

}
