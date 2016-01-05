package edu.tamu.tcat.trc.entries.types.article;

import java.util.List;

public interface Bibliography
{
   String getId();
   String getType();
   String getTitle();
   String getEdition();
   List<BiblioAuthor> getAuthors();
   List<BiblioTranslator> getTranslators();
   String getPublisher();
   String getPublishLocation();
   String getContainerTitle();
   String getUrl();
   IssuedDate getIssuedDate();
   
   public interface BiblioAuthor
   {
      String getFamily();
      String getGiven();
   }
   
   public interface BiblioTranslator
   {
      String getFamily();
      String getGiven();
      String getLiteral();
   }
   
   public interface IssuedDate
   {
      List<List<String>> getDateParts();
   }
}
