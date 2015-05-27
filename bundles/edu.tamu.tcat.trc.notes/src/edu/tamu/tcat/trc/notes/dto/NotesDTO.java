package edu.tamu.tcat.trc.notes.dto;

import java.net.URI;
import java.util.UUID;

import edu.tamu.tcat.trc.notes.Notes;
import edu.tamu.tcat.trc.notes.repo.basic.BasicNotesImpl;

public class NotesDTO
{
   public UUID id;
   public URI associatedEntity;
   public UUID authorId;
   public String mimeType;
   public String content;

   public static Notes instantiate(NotesDTO dto)
   {
      return new BasicNotesImpl(dto.id, dto.associatedEntity, dto.authorId, dto.mimeType, dto.content);
   }

   public static NotesDTO create(Notes notes)
   {
      NotesDTO dto = new NotesDTO();

      dto.id = notes.getId();
      dto.associatedEntity = notes.getEntity();
      dto.authorId = notes.getAuthorId();
      dto.mimeType = notes.getMimeType();
      dto.content = notes.getContent();

      return dto;
   }

   public static NotesDTO copy(NotesDTO orig)
   {
      NotesDTO dto = new NotesDTO();

      dto.id = orig.id;
      dto.associatedEntity = orig.associatedEntity;
      dto.authorId = orig.authorId;
      dto.mimeType = orig.mimeType;
      dto.content = orig.content;

      return dto;
   }

}
