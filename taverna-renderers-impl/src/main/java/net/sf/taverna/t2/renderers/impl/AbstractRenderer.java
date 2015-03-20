package net.sf.taverna.t2.renderers.impl;

import static java.lang.String.format;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static net.sf.taverna.t2.renderers.RendererUtils.getSizeInBytes;
import static net.sf.taverna.t2.renderers.impl.RendererConstants.BIG_DATA_MSG;
import static net.sf.taverna.t2.renderers.impl.RendererConstants.CANCELLED_MSG;
import static net.sf.taverna.t2.renderers.impl.RendererConstants.NO_DATA_MSG;
import static net.sf.taverna.t2.renderers.impl.RendererConstants.NO_SIZE_MSG;
import static uk.org.taverna.databundle.DataBundles.isValue;
import static uk.org.taverna.databundle.DataBundles.isReference;

import java.nio.file.Path;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import net.sf.taverna.t2.renderers.Renderer;
import net.sf.taverna.t2.renderers.RendererException;

import org.apache.log4j.Logger;

/**
 * Implements some of the common code across the renderers.
 * 
 * @author Donal Fellows
 */
abstract class AbstractRenderer implements Renderer {
	protected Logger logger = Logger.getLogger(AbstractRenderer.class);
	/** Size of a <s>mibibyte</s> megabyte. */
	private int MEGABYTE = 1024 * 1024;

	/**
	 * Work out size of file in megabytes
	 * 
	 * @param bytes
	 *            Number of bytes
	 * @return Number of megabytes
	 */
	private int bytesToMeg(long bytes) {
		float f = bytes / MEGABYTE;
		return Math.round(f);
	}

	/**
	 * Implements basic checks on the entity to render before delegating to
	 * subclasses to actually do the rendering.
	 * 
	 * @see #getRendererComponent(Path)
	 */
	@Override
	public JComponent getComponent(Path path) throws RendererException {
		if (!isValue(path) && !isReference(path)) {
			logger.error("unrenderable: data is not a value or reference");
			return new JTextArea(NO_DATA_MSG);
		}

		// Get the size
		long sizeInBytes;
		try {
			sizeInBytes = getSizeInBytes(path);
		} catch (Exception ex) {
			logger.error("unrenderable: failed to get data size", ex);
			return new JTextArea(NO_SIZE_MSG + ex.getMessage());
		}

		// over the limit for this renderer?
		if (sizeInBytes > MEGABYTE * getSizeLimit()) {
			JComponent alternative = sizeCheck(path, bytesToMeg(sizeInBytes));
			if (alternative != null)
				return alternative;
		}

		return getRendererComponent(path);
	}

	/**
	 * Implements the user-visible part of the size check. Default version just
	 * does a simple yes/no proceed.
	 * 
	 * @return <tt>null</tt> if the normal flow is to continue, or a component
	 *         to show to the user if it is to be used instead (e.g., to show a
	 *         message that the entity was too large).
	 */
	protected JComponent sizeCheck(Path path, int approximateSizeInMB) {
		int response = showConfirmDialog(null,
				format(BIG_DATA_MSG, getResultType(), approximateSizeInMB),
				getSizeQueryTitle(), YES_NO_OPTION);
		if (response != YES_OPTION) // NO_OPTION or ESCAPE key
			return new JTextArea(CANCELLED_MSG);
		return null;
	}

	/**
	 * How should we describe the data to the user? Should be capitalised.
	 */
	protected String getResultType() {
		return "Result";
	}

	/**
	 * At what size (in megabytes) do we query the user?
	 * 
	 * @see #sizeCheck(Path,log)
	 */
	protected int getSizeLimit() {
		return 1;
	}

	/**
	 * What title do we use on the dialog used to query the user?
	 * 
	 * @see #sizeCheck(Path,log)
	 */
	protected String getSizeQueryTitle() {
		return "Render this image?";
	}

	/** Actually get the renderer; the basic checks have passed. */
	protected abstract JComponent getRendererComponent(Path path)
			throws RendererException;
}
