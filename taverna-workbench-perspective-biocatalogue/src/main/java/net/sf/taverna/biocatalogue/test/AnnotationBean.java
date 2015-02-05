package net.sf.taverna.biocatalogue.test;


public class AnnotationBean
{
  public AnnotationBean() { }
  
  public String self;
  private int version;
  private String created;
  public Annotatable annotatable;
  private Source source;
  private Attribute attribute;
  private Value value;
  
  
  public static class Annotatable
  {
    private Annotatable() { }
    
    private String name;
    public String resource;
    private String type;
  }
  
  public static class Source
  {
    private Source() { }
    
    private String name;
    private String resource;
    private String type;
  }
  
  public static class Attribute
  {
    private Attribute() { }
    
    private String name;
    private String resource;
    private String identifier;
  }
  
  public static class Value
  {
    private Value() { }
    
    private String resource;
    private String type;
    private String content;
  }
}
