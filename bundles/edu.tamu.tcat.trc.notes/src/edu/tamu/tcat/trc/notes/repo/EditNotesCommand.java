package edu.tamu.tcat.trc.notes.repo;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.notes.Note;
import edu.tamu.tcat.trc.notes.UpdateNotesCanceledException;
import edu.tamu.tcat.trc.notes.dto.NotesDTO;

public interface EditNotesCommand
{
   UUID getId();

   void update(NotesDTO updateDTO);

   EditNotesCommand setAll(NotesDTO note);

   EditNotesCommand setEntity(URI entityURI);

   EditNotesCommand setAuthorId(UUID authorId);

   EditNotesCommand setMimeType(String mimeType);

   EditNotesCommand setContent(String content);

   Future<Note> execute() throws UpdateNotesCanceledException;
}
