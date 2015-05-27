package edu.tamu.tcat.trc.notes.repo;

import java.net.URI;
import java.util.UUID;

public interface EditNotesCommand
{


   UUID getId();

   EditNotesCommand setEntity(URI entityURI);

   EditNotesCommand setAuthorId(UUID authorId);

   EditNotesCommand setMimeType(String mimeType);

   EditNotesCommand setContent(String content);
}
