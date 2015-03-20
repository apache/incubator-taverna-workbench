package net.sf.taverna.biocatalogue.model;

/**
 * Trivial class to represent a generic pair of objects.
 * Any types of objects can be used.
 * 
 * @author Sergejs Aleksejevs
 *
 * @param <T1> Type of the first object.
 * @param <T2> Type of the second object.
 */
public class Pair<T1,T2>
{
  private final T1 firstObject;
  private final T2 secondObject;

  public Pair(T1 firstObject, T2 secondObject) {
    this.firstObject = firstObject;
    this.secondObject = secondObject;
  }
  
  public T1 getFirstObject() {
    return firstObject;
  }
  
  public T2 getSecondObject() {
    return secondObject;
  }
  
}
