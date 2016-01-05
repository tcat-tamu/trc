package edu.tamu.tcat.trc.entries.types.article;

import java.util.List;

public interface Citation
{
   String getId();
   List<CitationItem> getItems();
   
   public interface CitationItem
   {
      String getId();
      String getLocator();
      String getLabel();
      String getSuppressAuthor();
   }
}
