package edu.tamu.tcat.trc.entries.types.bio.search;

import java.util.List;

/**
 * The result set of person entries matched by a {@link PeopleQueryCommand}.
 * A result set has no functionality other than retrieving matched results from an executed
 * query. It should be considered "stale" as soon as it is acquired due to the inherently
 * unstable nature of a search framework.
 */
public interface PersonSearchResult
{
   /**
    * Get the {@link PeopleQueryCommand} which executed to provide this result.
    */
   PeopleQueryCommand getCommand();

   /**
    * @return Proxies for the people that match the current search.
    */
   List<BioSearchProxy> get();

   //TODO: add support for retrieving facet information
}
