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
    *
    * @param noteId The id of the Note to retrieve
    * @return The identified note.
    * @throws NoSuchCatalogRecordException If the requested note does not exist.
    */
   Note get(UUID noteId) throws NoSuchCatalogRecordException;

   /**
    * Retrieves a list of {@link Note} associated with a particular URI.
    *
    * @param entityURI URI that may contain {@link Note}.
    * @return Collection of Notes
    */
   // NOTE that this should, perhaps, be done through the search API.
   List<Note> getNotes(URI entityURI) throws NoSuchCatalogRecordException;

   /**
    * Builds a new {@link EditNoteCommand} to create a new {@link Note}.
    * @return
    */
   EditNoteCommand create();

   /**
    * Modifies a {@link EditNoteCommand} to allow editing a {@link Note}.
    * @return
    */
   EditNoteCommand edit(UUID noteId) throws NoSuchCatalogRecordException;

   /**
    * Removes a {@link Note} entry from the database.
    */
   Future<Boolean> remove(UUID noteId);

   /**
    * Register a listener that will be notified when a note changes.
    *
    * @param ears The listener to be notified.
    * @return A registration that allows the client to stop listening for changes. The returned
    *       registration <em>must</em> be closed by the caller.
    */
   AutoCloseable register(UpdateListener<NoteChangeEvent> ears);
}
