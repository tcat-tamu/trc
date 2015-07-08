package edu.tamu.tcat.trc.notes.repo;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.notes.Note;
import edu.tamu.tcat.trc.notes.dto.NoteDTO;

public interface EditNoteCommand
{
   UUID getId();

   void update(NoteDTO updateDTO);

   EditNoteCommand setAll(NoteDTO note);

   EditNoteCommand setEntity(URI entityURI);

   EditNoteCommand setAuthorId(UUID authorId);

   EditNoteCommand setMimeType(String mimeType);

   EditNoteCommand setContent(String content);

   Future<Note> execute();
}
