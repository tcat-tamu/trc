package edu.tamu.tcat.trc.services.types.bibref;

public interface BibliographicItemReference
{
   /**
    * @return the identifier of the referenced bibliographic item.
    */
   String getItemId();

   /**
    * @return A human-readable display label to identify this item reference to the user.
    */
   String getLabel();

   /**
    * @return The unit of reference for identifying a specific point within the referenced bibliographic Item.
    */
   String getLocatorType();

   /**
    * @return A scale for the locator to a point within the referenced bibliographic item
    */
   String getLocator();
}