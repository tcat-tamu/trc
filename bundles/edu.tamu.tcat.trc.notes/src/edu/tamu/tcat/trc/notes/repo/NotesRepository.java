package edu.tamu.tcat.trc.notes.repo;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import edu.tamu.tcat.trc.notes.Notes;

public interface NotesRepository
{
   /**
    * Retrieves a specific {@link Notes}
    * @param noteId The id of the specific Notes to be returned
    * @return Notes
    */
   Notes getNote(UUID noteId);

   /**
    * Retrieves a {@code List} of {@link Notes} provided a valid URI.
    * @param entityURI URI that may contain {@link Notes}.
    * @return Collection of Notes
    */
   List<Notes> getNotes(URI entityURI);

   /**
    * Builds a new {@link EditNotesCommand} to create a new {@link Notes}.
    * @return
    */
   EditNotesCommand create();

   /**
    * Modifies a {@link EditNotesCommand} to allow editing a {@link Notes}.
    * @return
    */
   EditNotesCommand edit();

   /**
    * Removes a {@link Notes} entry from the database.
    */
   void delete();

}
