package net.sf.taverna.t2.workbench.views.results;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;

public class ExtraMimeTypes extends MagicMimeMimeDetector {
	
	static {
		defaultLocations = new String[] {getMimeLocation()};
		magicMimeFileLocations = Arrays
		.asList(defaultLocations);
	}

	/**
	 * Load the mime-type file, copy to a temporary directory and return the
	 * path to this new file
	 * 
	 * @return
	 */
	private static String getMimeLocation() {
		InputStream resourceAsStream = ExtraMimeTypes.class
				.getResourceAsStream("/extra-mimes");

		File mimeTempFile = null;
		try {
			mimeTempFile = File.createTempFile("mime-types", null);
			OutputStream outputStream = new FileOutputStream(mimeTempFile);
			IOUtils.copy(resourceAsStream, outputStream);
		} catch (IOException e) {

		}
		return mimeTempFile.getAbsolutePath();
	}

}
