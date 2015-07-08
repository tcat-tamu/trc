package edu.tamu.tcat.trc.notes.dto;

import java.net.URI;
import java.util.UUID;

import edu.tamu.tcat.trc.notes.Note;
import edu.tamu.tcat.trc.notes.internal.dto.BasicNote;

public class NoteDTO
{
   public UUID id;
   public URI associatedEntity;
   public String authorId;
   public String mimeType;
   public String content;

   public static Note instantiate(NoteDTO dto)
   {
      return new BasicNote(dto.id, dto.associatedEntity, dto.authorId, dto.mimeType, dto.content);
   }

   public static NoteDTO create(Note notes)
   {
      NoteDTO dto = new NoteDTO();

      dto.id = notes.getId();
      dto.associatedEntity = notes.getEntity();
      dto.authorId = notes.getAuthorId().toString();
      dto.mimeType = notes.getMimeType();
      dto.content = notes.getContent();

      return dto;
   }

   public static NoteDTO copy(NoteDTO orig)
   {
      NoteDTO dto = new NoteDTO();

      dto.id = orig.id;
      dto.associatedEntity = orig.associatedEntity;
      dto.authorId = orig.authorId;
      dto.mimeType = orig.mimeType;
      dto.content = orig.content;

      return dto;
   }

}
