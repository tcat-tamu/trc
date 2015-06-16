package edu.tamu.tcat.trc.notes.repo;

import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.notes.Notes;

/**
 * An event notification sent from a {@link NotesRepository} due to a data change.
 */
public interface NoteChangeEvent extends UpdateEvent
{
   /**
    * Retrieves the notes that changed.
    *
    * @return the notes that changed.
    * @throws CatalogRepoException If the notes cannot be retrieved (for example,
    *       if the record was deleted).
    */
   /*
    * See the note on RelationshipChangeEvent
    */
   Notes getNotes() throws CatalogRepoException;
}
