package edu.tamu.tcat.trc.entries.types.reln.search;

import java.net.URI;

import edu.tamu.tcat.trc.entries.types.reln.Relationship;

public interface RelationshipSearchService
{
   /**
    * Retrieves all relationships associated with a particular catalog entry.
    *
    * @param entry
    * @return
    * FIXME belongs in search
    */
   @Deprecated
   Iterable<Relationship> findRelationshipsFor(URI entry);

   RelationshipQueryCommand createQueryCommand();

   Iterable<Relationship> findRelationshipsBy(URI creator);
}
