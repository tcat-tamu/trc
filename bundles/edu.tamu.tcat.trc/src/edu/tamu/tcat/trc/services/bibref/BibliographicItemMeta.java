package edu.tamu.tcat.trc.services.bibref;

public interface BibliographicItemMeta
{
   /**
    * @return A vendor-specific key for identifying the bibliographic item within its source.
    */
   public String getKey();

   /**
    * @return A human-readable label to summarize the creators on the bibliographic item.
    */
   public String getCreatorSummary();

   /**
    * @return A human-readable label to summarize the creation date field on the bibliographic item.
    */
   public String getParsedDate();

   /**
    * @return The date and time of this bibliographic item's creation.
    */
   public String getDateAdded();

   /**
    * @return The date and time of this bibliographic item's last modification. May be {@code null} or equal to {@link BibliographicItemMeta#dateAdded()} if the item has never been modified.
    */
   public String getDateModified();
}
