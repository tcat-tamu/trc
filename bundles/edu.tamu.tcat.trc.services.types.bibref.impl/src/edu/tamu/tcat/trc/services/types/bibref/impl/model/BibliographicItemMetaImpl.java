package edu.tamu.tcat.trc.services.types.bibref.impl.model;

import edu.tamu.tcat.trc.services.types.bibref.BibliographicItemMeta;
import edu.tamu.tcat.trc.services.types.bibref.impl.repo.DataModelV1;

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

      key = other.getKey();
      creatorSummary = other.getCreatorSummary();
      parsedDate = other.getParsedDate();
      dateAdded = other.getDateAdded();
      dateModified = other.getDateModified();
   }

   public BibliographicItemMetaImpl(DataModelV1.BibliographicItemMeta dto)
   {
      if (dto == null)
         return;

      key = dto.key;
      creatorSummary = dto.creatorSummary;
      parsedDate = dto.parsedDate;
      dateAdded = dto.dateAdded;
      dateModified = dto.dateModified;
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
