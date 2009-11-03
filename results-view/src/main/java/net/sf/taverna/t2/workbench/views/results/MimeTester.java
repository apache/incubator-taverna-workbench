package net.sf.taverna.t2.workbench.views.results;

import org.clapper.util.misc.MIMETypeUtil;

import eu.medsea.mimeutil.MimeUtil2;

public class MimeTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		List<String> extension = ResultsUtils.getExtension("text/xml");
		String mimeTypeForFileExtension = MIMETypeUtil.fileExtensionForMIMEType("text/xml");
		System.out.println(mimeTypeForFileExtension);
		MimeUtil2 mimeUtil = new MimeUtil2();
		mimeUtil
				.registerMimeDetector("eu.medsea.mimeutil.detector.ExtraMimeTypes");
		boolean mimeTypeKnown = MimeUtil2.isMimeTypeKnown("chemical/x-pdb");
		System.out.println(mimeTypeKnown);
	}

}
