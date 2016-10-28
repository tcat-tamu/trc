package edu.tamu.tcat.trc.entries.types.reln;

import java.util.stream.Stream;

import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryReference;

/**
 *  A strategy that obtains relationships associated with a given object. This can be used
 *  to expand and adapt relationships over simple structure into more complex relationship
 *  and to decorate the basic endpoints associated with the relationships.
 *
 *  <p>For example, to retrieve relationships associated with a given person, it might be
 *  appropriate to retrieve both relationships that directly reference that person and to
 *  retrieve relationships that connect that person to other people indirectly via books
 *  that he has written. In this use-case, one strategy might retrieve direct references
 *  while the second would lookup all books authored by a person, retrieve relationships
 *  associated with those books (and the corresponding editions and volumes) and find the
 *  transitively related authors.
 *
 *  <p>In addition to retrieving relationships, a strategy might decorate the basic anchor
 *  properties to include domain specific information such as a display label, birth and
 *  death dates, a brief summary or other relevant information.
 *
 *  <p>Since the relationships returned by this class may have been derived from one or more
 *  direct relationships, a new sub-class, DerivedRelationship is provided that captures the
 *  underlying relationship or relationships that were used to derive the current relationship.
 *
 *  <p>The relationship entry type is designed to provide applications with considerable
 *  flexibility to design relationships that are appropriate with respect to their application
 *  domain. The {@link RelationshipInferenceStrategy} is intended to be defined and configured
 *  at the application layer to facilitate resolution of implicit relationships.
 */
public interface RelationshipInferenceStrategy
{
   /**
    * @return A display label for this strategy.
    */
   String getLabel();

   /**
    * Indicates whether this strategy will accept instances of the supplied
    * {@link EntryReference}.
    *
    * @param ref An entry id to test.
    * @return <code>true</code> if {@link #getRelationships(EntryReference)} can
    *       correctly interpret and return relationships for the referenced entity.
    */
   boolean accepts(EntryId ref);

   /**
    *
    * @param ref An entry for which relationships should be retrieved.
    * @return A collection of relationships associated with the reference entity.
    * @throws IllegalArgumentException If the supplied reference is not accepted
    *       by this strategy.
    */
   Stream<DerivedRelationship> getRelationships(EntryId ref) throws IllegalArgumentException;
}
