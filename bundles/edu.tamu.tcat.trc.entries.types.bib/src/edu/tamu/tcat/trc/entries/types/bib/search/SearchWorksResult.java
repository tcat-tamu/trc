package edu.tamu.tcat.trc.entries.types.bib.search;

import java.util.List;

import edu.tamu.tcat.trc.entries.types.bib.Work;

/**
 * The result set of {@link Work}s matched by a {@link WorkQueryCommand}.
 * A result set has no functionality other than retrieving matched results from an executed
 * query. It should be considered "stale" as soon as it is acquired due to the inherently
 * unstable nature of a search framework.
 */
public interface SearchWorksResult
{
   /**
    * Get the {@link WorkQueryCommand} which executed to provide this result.
    */
   WorkQueryCommand getCommand();
   
   /**
    * @return Proxies for the works that match the current search.
    */
   @Deprecated // why return proxies? what should this return? perhaps Work?
   List<WorkSearchProxy> get();
   
   //TODO: add support for retrieving facet information
}
