package edu.tamu.tcat.trc.entries.types.bio.repo;

import edu.tamu.tcat.trc.entries.types.bio.Person;

/**
 * An event notification sent from a {@link PeopleRepository} due to a data change.
 */
public interface PeopleChangeEvent
{

   enum ChangeType {
      CREATED, MODIFIED, DELETED;
   }

   /**
    * @return The type of change that occurred.
    */
   ChangeType getChangeType();

   /**
    * @return The persistent identifier for the person that changed.
    */
   String getPersonId();

   /**
    * Retrieves the person that was changed.
    *
    * @return the person that was changed.
    * @throws PersonNotAvailableException If the person cannot be retrieved (for example,
    *       if the record was deleted).
    */
   Person getPerson() throws PersonNotAvailableException;
}
