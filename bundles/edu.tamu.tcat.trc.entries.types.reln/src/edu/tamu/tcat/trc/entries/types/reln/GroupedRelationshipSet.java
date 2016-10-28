package edu.tamu.tcat.trc.entries.types.reln;

import java.util.Collection;

import edu.tamu.tcat.trc.resolver.EntryId;

public interface GroupedRelationshipSet
{
   /**
    * A {@code GroupedRelationshipSet} is used to organize a set of relationships for a given
    * entry. For example, all entries related to a particular person or to a particular book.
    * This returns the a reference to the root entry for this set.
    *
    * @return A reference to the entity this set is defined in relationship to.
    */
   EntryId getRootEntity();

   /**
    * @return The relationships associated with the root entity, grouped by relationship type.
    */
   Collection<TypedRelationshipGroup> getRelationshipGroups();

   /**
    * Represents a collection of relationships of a certain type.
    * If the type is directed, then {@code out} and {@code in} will contain all of the referent's
    * outgoing and incoming relationships, respectively.
    * If the type is undirected, then {@code none} will contain all of the referent's relationships.
    */
   public interface TypedRelationshipGroup
   {
      /**
       * @return The entry with respect to which this relationship group has been constructed.
       */
      EntryId getRootEntity();

      /**
       * @return The type of relationship represented by this group.
       */
      RelationshipType getType();

      /**
       * @return Relationships in which which the root entity belongs to set of related
       *       entries. For directed relationships, this will be the relationships from
       *       the root entry to one or more other entries.
       */
      Collection<DerivedRelationship> getRelationshipsFrom();

      /**
       * @return Relationships in which the root entity is a target of the relationship.
       *       For undirected relationships, this will be an empty set.
       */
      Collection<DerivedRelationship> getRelationshipsTo();
   }
}
