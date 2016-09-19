package edu.tamu.tcat.trc.services.types.bibref.repo;

public interface BibliographicItemMetaMutator
{
   /**
    * Sets the vendor-specific key.
    * @param key
    */
   void setKey(String key);

   /**
    * Sets a creator summary label.
    * @param creatorSummary
    */
   void setCreatorSummary(String creatorSummary);

   /**
    * Sets a creation date label. Note this should represent the creation date of the work referenced by the bibliographic item and not the bibliographic item itself.
    * @param parsedDate
    */
   void setParsedDate(String parsedDate);

   /**
    * Sets the creation date of this bibliographic item. Values should be supplied in ISO-8601 format.
    * @param dateAdded
    */
   void setDateAdded(String dateAdded);

   /**
    * Sets the modification date of this bibliographic item. Values should be supplied in ISO-8601 format.
    * @param dateModified
    */
   void setDateModified(String dateModified);
}
