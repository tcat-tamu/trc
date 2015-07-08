package edu.tamu.tcat.trc.notes.repo;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.notes.Note;

public interface NotesRepository
{
   /**
    * Retrieves a specific {@link Note}
    * @param noteId The id of the specific Notes to be returned
    * @return Notes
    */
   Note get(UUID noteId) throws NoSuchCatalogRecordException;

   /**
    * Retrieves a {@code List} of {@link Note} provided a valid URI.
    * @param entityURI URI that may contain {@link Note}.
    * @return Collection of Notes
    */
   List<Note> getNotes(URI entityURI) throws NoSuchCatalogRecordException;

   /**
    * Builds a new {@link EditNotesCommand} to create a new {@link Note}.
    * @return
    */
   EditNotesCommand create();

   /**
    * Modifies a {@link EditNotesCommand} to allow editing a {@link Note}.
    * @return
    */
   EditNotesCommand edit(UUID noteId) throws NoSuchCatalogRecordException;

   /**
    * Removes a {@link Note} entry from the database.
    */
   Future<Boolean> remove(UUID noteId);

   AutoCloseable register(UpdateListener<NoteChangeEvent> ears);
}
