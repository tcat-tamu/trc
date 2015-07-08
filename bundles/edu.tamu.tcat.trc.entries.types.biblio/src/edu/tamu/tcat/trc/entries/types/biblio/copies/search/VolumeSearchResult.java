package edu.tamu.tcat.trc.entries.types.biblio.copies.search;

import java.util.List;

public interface VolumeSearchResult
{

   /**
    * Get the {@link PageSearchCommand} which executed to provide this result.
    */
   VolumeSearchCommand getCommand();

   /**
    * @return Proxies for the works that match the current search.
    */
   List<VolumeSearchProxy> get();
}
