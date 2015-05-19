package edu.tamu.tcat.trc.entries.bio;

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
