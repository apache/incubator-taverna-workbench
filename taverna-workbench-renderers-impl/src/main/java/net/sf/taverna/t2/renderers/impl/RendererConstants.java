package net.sf.taverna.t2.renderers.impl;

interface RendererConstants {
	String SEE_LOG_MSG = "(see error log for more details):\n";
	String NO_DATA_MSG = "Failed to obtain the data to render: "
			+ "data is not a value or reference";
	String NO_SIZE_MSG = "Failed to get the size of the data " + SEE_LOG_MSG;
	String BIG_DATA_MSG = "%s is approximately %d MB in size, "
			+ "there could be issues with rendering this inside Taverna\n"
			+ "Do you want to continue?";
	String CANCELLED_MSG = "Rendering cancelled due to size of image. "
			+ "Try saving and viewing in an external application.";
}
