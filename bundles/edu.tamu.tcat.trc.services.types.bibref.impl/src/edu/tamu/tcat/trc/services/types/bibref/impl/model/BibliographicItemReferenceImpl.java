package edu.tamu.tcat.trc.services.types.bibref.impl.model;

import edu.tamu.tcat.trc.services.types.bibref.BibliographicItemReference;

public class BibliographicItemReferenceImpl implements BibliographicItemReference
{
   private String itemId;
   private String label;
   private String locatorType;
   private String locator;

   public BibliographicItemReferenceImpl()
   {
   }

   public BibliographicItemReferenceImpl(String itemId, String label, String locatorType, String locator)
   {
      this.itemId = itemId;
      this.label = label;
      this.locatorType = locatorType;
      this.locator = locator;
   }

   public BibliographicItemReferenceImpl(BibliographicItemReference other)
   {
      if (other == null)
         return;

      this.itemId = other.getItemId();
      this.label = other.getLabel();
      this.locatorType = other.getLocatorType();
      this.locator = other.getLocator();
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

}
