package edu.tamu.tcat.trc.entries.types.article;

public interface Citation
{
   String getId();
   CitationItem getItems();
   CitationProperties getProperties();
   String getSupressAuthor();
   
   public interface CitationItem
   {
      String getId();
      String getLocator();
      String getLabel();
   }
   
   public interface CitationProperties
   {
   }
}
