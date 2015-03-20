package net.sf.taverna.biocatalogue.ui.tristatetree;

/**
 * A simple interface to enable tracking tree checking
 * changes.
 * 
 * @author Sergejs Aleksejevs
 */
public interface TriStateTreeCheckingListener
{
  public void triStateTreeCheckingChanged(JTriStateTree source);
}
