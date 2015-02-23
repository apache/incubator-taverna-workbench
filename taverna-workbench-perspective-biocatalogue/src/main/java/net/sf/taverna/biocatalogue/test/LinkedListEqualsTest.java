package net.sf.taverna.biocatalogue.test;

import java.util.LinkedList;

public class LinkedListEqualsTest
{
  public static void main(String[] args)
  {
    LinkedList l = new LinkedList();
    
    String a = new String("test1");
    String b = new String("test2");
    
    System.out.println(a == b);
    System.out.println(a.equals(b));
    
    l.add(a);
    l.add(b);
    
    System.out.println(l);
    System.out.println(l.indexOf(a));
    System.out.println(l.indexOf(b));
    
  }
}
