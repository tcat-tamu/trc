package edu.tamu.tcat.trc.entries.types.reln.search;

import edu.tamu.tcat.trc.search.SearchException;

public interface RelationshipSearchService
{
   RelationshipQueryCommand createQueryCommand() throws SearchException;
}
