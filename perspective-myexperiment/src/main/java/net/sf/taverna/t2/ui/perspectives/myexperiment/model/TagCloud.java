// Copyright (C) 2008 The University of Manchester, University of Southampton
// and Cardiff University
package net.sf.taverna.t2.ui.perspectives.myexperiment.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jiten Bhagat
 */
public class TagCloud {

  private List<Tag> tags = new ArrayList<Tag>();

  public List<Tag> getTags() {
	return this.tags;
  }

  /**
   * Removes all tags from the current tag cloud.
   */
  public void clear() {
	this.tags.clear();
  }

  /**
   * Adds all tags from the supplied collection into the tag cloud.
   */
  public void addAll(List<Tag> newTags) {
	this.tags.addAll(newTags);
  }

  /**
   * @return Count of the occurrences of the most popular tag in the cloud data.
   */
  public int getMaxTagCount() {
	int iMax = 0;

	for (Tag t : tags) {
	  if (t.getCount() > iMax) {
		iMax = t.getCount();
	  }
	}

	return (iMax);
  }
}
