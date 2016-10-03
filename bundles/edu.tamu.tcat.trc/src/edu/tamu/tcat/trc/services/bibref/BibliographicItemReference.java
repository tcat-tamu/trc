package edu.tamu.tcat.trc.services.bibref;

public interface BibliographicItemReference
{
   /**
    * @return the identifier of the referenced bibliographic item.
    */
   String getItemId();

   /**
    * @return A display label to identify this item reference.
    */
   String getLabel();

   /**
    * @return The unit of reference for identifying a specific location within the referenced
    *    item (e.g., page, section, chapter).
    */
   String getLocatorType();

   /**
    * @return An identifier for a specific location within the referenced item that is
    *    pertinent for this citation, such as a page range or chapter. The unit of reference is
    *    given by the locator type.
    */
   String getLocator();

   /**
    * @return Whether the author name should be suppressed in this containing reference's citation.
    */
   boolean isAuthorNameSuppressed();
}