package net.sf.taverna.t2.ui.perspectives.myexperiment;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class StyledHTMLEditorKit extends HTMLEditorKit {

	private final StyleSheet styleSheet;

	public StyledHTMLEditorKit(StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}
	
	@Override
	public StyleSheet getStyleSheet() {
		return styleSheet;
	}

}
