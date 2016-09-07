package org.apache.taverna.workbench.models.graph;

public interface GraphEventManager {
	void mouseClicked(GraphElement graphElement, short button, boolean altKey,
			boolean ctrlKey, boolean metaKey, int x, int y, int screenX,
			int screenY);

	void mouseDown(GraphElement graphElement, short button, boolean altKey,
			boolean ctrlKey, boolean metaKey, int x, int y, int screenX,
			int screenY);

	void mouseUp(GraphElement graphElement, short button, boolean altKey,
			boolean ctrlKey, boolean metaKey, final int x, final int y,
			int screenX, int screenY);

	void mouseMoved(GraphElement graphElement, short button, boolean altKey,
			boolean ctrlKey, boolean metaKey, int x, int y, int screenX,
			int screenY);

	void mouseOver(GraphElement graphElement, short button, boolean altKey,
			boolean ctrlKey, boolean metaKey, int x, int y, int screenX,
			int screenY);

	void mouseOut(GraphElement graphElement, short button, boolean altKey,
			boolean ctrlKey, boolean metaKey, int x, int y, int screenX,
			int screenY);
}
