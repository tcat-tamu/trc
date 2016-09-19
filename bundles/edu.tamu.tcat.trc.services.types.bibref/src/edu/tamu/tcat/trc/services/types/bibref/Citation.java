package edu.tamu.tcat.trc.services.types.bibref;

import java.util.List;

public interface Citation
{
   /**
    * @return a unique identifier for this citation. Must never be {@code null}.
    */
   String getId();

   /**
    * @return References to bibliographic items cited in this citation.
    */
   List<BibliographicItemReference> getCitedItems();
}
