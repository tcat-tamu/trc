package edu.tamu.tcat.trc.services.types.bibref.impl.model;

import edu.tamu.tcat.trc.services.types.bibref.BibliographicItemMeta;

public class BibliographicItemMetaImpl implements BibliographicItemMeta
{
   private String key;
   private String creatorSummary;
   private String parsedDate;
   private String dateAdded;
   private String dateModified;

   public BibliographicItemMetaImpl()
   {
   }

   public BibliographicItemMetaImpl(String key, String creatorSummary, String parsedDate, String dateAdded, String dateModified)
   {
      this.key = key;
      this.creatorSummary = creatorSummary;
      this.parsedDate = parsedDate;
      this.dateAdded = dateAdded;
      this.dateModified = dateModified;
   }

   public BibliographicItemMetaImpl(BibliographicItemMeta other)
   {
      if (other == null)
         return;

      this.key = other.getKey();
      this.creatorSummary = other.getCreatorSummary();
      this.parsedDate = other.getParsedDate();
      this.dateAdded = other.getDateAdded();
      this.dateModified = other.getDateModified();
   }

   @Override
   public String getKey()
   {
      return key;
   }

   @Override
   public String getCreatorSummary()
   {
      return creatorSummary;
   }

   @Override
   public String getParsedDate()
   {
      return parsedDate;
   }

   @Override
   public String getDateAdded()
   {
      return dateAdded;
   }

   @Override
   public String getDateModified()
   {
      return dateModified;
   }

}
