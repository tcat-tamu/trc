package edu.tamu.tcat.trc.notes.repo;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.notes.dto.NoteDTO;

public interface EditNoteCommand
{
   UUID getId();

   void update(NoteDTO updateDTO);

   void setAll(NoteDTO note);

   void setEntity(URI entityURI);

   void setAuthorId(String authorId);

   void setMimeType(String mimeType);

   void setContent(String content);

   Future<UUID> execute();
}
