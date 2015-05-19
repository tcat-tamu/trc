package edu.tamu.tcat.trc.entries.reln;


public interface RelationshipChangeEvent
{
   // TODO may factor this into a shared API across catalog services

   enum ChangeType {
      CREATED, MODIFIED, DELETED;
   }

   /**
    * @return The type of change that occurred.
    */
   ChangeType getChangeType();

   /**
    * @return The persistent identifier for the relationship that changed.
    */
   String getRelationshipId();

   /**
    * Retrieves the relationship that was changed.
    *
    * @return the relationship that was changed.
    * @throws RelationshipNotAvailableException If the relationship cannot be retrieved (for example,
    *       if the record was deleted).
    */
   Relationship getRelationship() throws RelationshipNotAvailableException;
}
