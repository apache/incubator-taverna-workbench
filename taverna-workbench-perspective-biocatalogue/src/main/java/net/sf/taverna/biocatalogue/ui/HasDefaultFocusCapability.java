package net.sf.taverna.biocatalogue.ui;

import java.awt.Component;

/**
 * Indicates that the class which implements this interface will focus default
 * component (as if the component represented by that class was activated).
 * 
 * @author Sergejs Aleksejevs
 */
public interface HasDefaultFocusCapability
{
  public void focusDefaultComponent();
  public Component getDefaultComponent();
}
