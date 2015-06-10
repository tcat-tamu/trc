package edu.tamu.tcat.trc.entries.types.reln.search;

import java.net.URI;

import edu.tamu.tcat.trc.entries.search.SearchException;

public interface RelationshipQueryCommand
{
   RelationshipSearchResult execute() throws SearchException;

   //TODO: convert this query to use edismax and rename methods to proper pattern
   @Deprecated // rename to "query*" or "filter*"
   void forEntity(URI entity, RelationshipDirection direction) throws SearchException;

   @Deprecated // rename to "query*" or "filter*"
   void byType(String typeId) throws SearchException;

   void setOffset(int start);
   void setMaxResults(int rows);
}
