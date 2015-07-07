package edu.tamu.tcat.trc.entries.types.bio.search;

import edu.tamu.tcat.trc.search.SearchException;

/**
 * Command for use in querying the associated {@link PeopleSearchService} which provides
 * search proxy instances.
 * <p>
 * A {@link PeopleQueryCommand} is intended to be initialized, executed a single time, provide results,
 * and be discarded.
 * <p>
 * The various "query" methods are intended to be for user-entered criteria which results in "like",
 * wildcard, or otherwise interpreted query criteria which may apply to multiple fields of the index.
 * Alternately, the various "filter" methods are intended for specific criteria which typically
 * applies to faceted searching or to known criteria for specific stored data.
 */
public interface PeopleQueryCommand
{
   PersonSearchResult execute() throws SearchException;

   void query(String q) throws SearchException;

   void queryFamilyName(String familyName) throws SearchException;

   void setOffset(int start);

   void setMaxResults(int max);
}
