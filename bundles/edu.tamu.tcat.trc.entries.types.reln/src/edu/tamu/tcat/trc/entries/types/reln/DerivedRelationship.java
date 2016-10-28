package edu.tamu.tcat.trc.entries.types.reln;

import java.util.Collection;

/**
 *  Extends a relationship to support the notion of relationships that have been
 *  inferred or aggregated from the original relationships in the collection. For
 *  example, relationships between people that are inferred based on books that
 *  they have written.
 */
public interface DerivedRelationship extends Relationship
{
   /**
    * @return A collection of the original relationships from which this relationship
    *       has been derived. May be empty if this is the original relationship.
    */
   Collection<Relationship> getSourceRelationships();
}
