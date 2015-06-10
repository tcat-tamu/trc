package edu.tamu.tcat.trc.entries.types.reln.search;

import java.util.List;

/**
 * The result set of entries matched by a {@link RelationshipQueryCommand}.
 * A result set has no functionality other than retrieving matched results from an executed
 * query. It should be considered "stale" as soon as it is acquired due to the inherently
 * unstable nature of a search framework.
 */
public interface RelationshipSearchResult
{
   /**
    * Get the {@link PeopleQueryCommand} which executed to provide this result.
    */
   RelationshipQueryCommand getCommand();

   /**
    * @return Proxies for the people that match the current search.
    */
   List<RelnSearchProxy> get();

   //TODO: add support for retrieving facet information
}
