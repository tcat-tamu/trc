package edu.tamu.tcat.trc.services.types.bibref.repo;

public interface BibliographicItemReferenceMutator
{
   /**
    * @return The id of the bibliographic item referenced by the item reference currently being edited.
    */
   String getItemId();

   /**
    * Sets the locator type for this item reference.
    * @param locatorType
    */
   void setLocatorType(String locatorType);

   /**
    * Set the locator for this item reference.
    * @param locator
    */
   void setLocator(String locator);

   /**
    * Sets the display label for this item reference.
    * @param label
    */
   void setLabel(String label);
}
