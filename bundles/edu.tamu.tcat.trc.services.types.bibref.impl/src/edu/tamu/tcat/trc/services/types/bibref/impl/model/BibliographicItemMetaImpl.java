package edu.tamu.tcat.trc.services.types.bibref.impl.model;

import edu.tamu.tcat.trc.services.types.bibref.BibliographicItemMeta;
import edu.tamu.tcat.trc.services.types.bibref.impl.repo.DataModelV1;

public class BibliographicItemMetaImpl implements BibliographicItemMeta
{
   private final String key;
   private final String creatorSummary;
   private final String parsedDate;
   private final String dateAdded;
   private final String dateModified;

   public BibliographicItemMetaImpl(DataModelV1.BibliographicItemMeta dto)
   {
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
