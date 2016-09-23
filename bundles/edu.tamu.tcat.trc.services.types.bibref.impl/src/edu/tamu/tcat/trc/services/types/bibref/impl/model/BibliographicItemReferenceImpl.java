package edu.tamu.tcat.trc.services.types.bibref.impl.model;

import edu.tamu.tcat.trc.services.types.bibref.BibliographicItemReference;
import edu.tamu.tcat.trc.services.types.bibref.impl.repo.DataModelV1;

public class BibliographicItemReferenceImpl implements BibliographicItemReference
{
   private final String itemId;
   private final String label;
   private final String locatorType;
   private final String locator;
   private final boolean suppressAuthor;

   public BibliographicItemReferenceImpl(DataModelV1.BibliographicItemReference dto)
   {
      itemId = dto.itemId;
      label = dto.label;
      locatorType = dto.locatorType;
      locator = dto.locator;
      suppressAuthor = dto.suppressAuthor;
   }

   @Override
   public String getItemId()
   {
      return itemId;
   }

   @Override
   public String getLabel()
   {
      return label;
   }

   @Override
   public String getLocatorType()
   {
      return locatorType;
   }

   @Override
   public String getLocator()
   {
      return locator;
   }

   @Override
   public boolean isAuthorNameSuppressed()
   {
      return suppressAuthor;
   }
}
