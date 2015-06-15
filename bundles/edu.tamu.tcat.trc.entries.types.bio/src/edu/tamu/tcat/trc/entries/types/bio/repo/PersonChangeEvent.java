package edu.tamu.tcat.trc.entries.types.bio.repo;

import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.types.bio.Person;

/**
 * An event notification sent from a {@link PeopleRepository} due to a data change.
 */
public interface PersonChangeEvent extends UpdateEvent
{
   /**
    * Retrieves the person that was changed.
    *
    * @return the person that was changed.
    * @throws PersonNotAvailableException If the person cannot be retrieved (for example,
    *       if the record was deleted).
    */
   /*
    * See the note on RelationshipChangeEvent
    */
   Person getPerson() throws PersonNotAvailableException;
}
