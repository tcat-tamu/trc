package edu.tamu.tcat.trc.entries.types.biblio.copies.search;

import java.util.List;

public interface PageSearchResult
{

   /**
    * Get the {@link PageSearchCommand} which executed to provide this result.
    */
   PageSearchCommand getCommand();

   /**
    * @return Proxies for the works that match the current search.
    */
   List<PageSearchProxy> get();
}
