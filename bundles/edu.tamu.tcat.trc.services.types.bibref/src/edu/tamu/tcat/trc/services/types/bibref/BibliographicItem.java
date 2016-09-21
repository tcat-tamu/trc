package edu.tamu.tcat.trc.services.types.bibref;

import java.util.List;
import java.util.Map;

/**
 * Represents a single rendered line in a bibliography.
 */
public interface BibliographicItem
{
   /**
    * @return A unique identifier for this bibliographic item to be used in citations.
    */
   String getItemId();

   /**
    * Identifies the type of item referenced in this bibliographic item, e.g. a book, an article, or an audio recording.
    * This value is vendor-specific to the source of this item's data (e.g. Zotero, Mendeley, or something internal tothe TRC framework).
    * @return
    */
   String getType();

   /**
    * @return Various metadata about this bibliographic item
    */
   BibliographicItemMeta getMetadata();

   /**
    * @return The original creators of the work referenced by this bibliographic item.
    */
   List<Creator> getCreators();

   /**
    * @return Arbitrary, vendor-specific field-value pairs containing information necessary for rendering this bibliographic item in a bibliography.
    */
   Map<String, String> getFields();
}
