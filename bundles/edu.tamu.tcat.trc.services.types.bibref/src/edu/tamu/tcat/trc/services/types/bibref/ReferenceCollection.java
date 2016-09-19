package edu.tamu.tcat.trc.services.types.bibref;

import java.util.Collection;

public interface ReferenceCollection
{
   /**
    * @return all citations
    */
   Collection<Citation> getCitations();

   /**
    * Finds an individual bibliographic item.
    * @param id
    * @return The bibliographic item or {@code null} if not found.
    */
   BibliographicItem getItem(String id);

   /**
    * @return All bibliographic items in this bibliography.
    */
   Collection<BibliographicItem> getItems();
}
