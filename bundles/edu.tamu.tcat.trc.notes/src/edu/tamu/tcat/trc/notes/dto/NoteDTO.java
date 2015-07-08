package edu.tamu.tcat.trc.notes.dto;

import java.net.URI;
import java.util.UUID;

import edu.tamu.tcat.trc.notes.Note;

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

   private static class BasicNote implements Note
   {
      private final UUID id;
      private final URI associatedEntity;
      private final String authorId;
      private final String mimeType;
      private final String content;

      public BasicNote(UUID id, URI associatedEntity, String authorId, String mimeType, String content)
      {
         this.id = id;
         this.associatedEntity = associatedEntity;
         this.authorId = authorId;
         this.mimeType = mimeType;
         this.content = content;
      }

      @Override
      public UUID getId()
      {
         return id;
      }

      @Override
      public URI getEntity()
      {
         return associatedEntity;
      }

      @Override
      public UUID getAuthorId()
      {
         return UUID.fromString(authorId);
      }

      @Override
      public String getMimeType()
      {
         return mimeType;
      }

      @Override
      public String getContent()
      {
         return content;
      }
   }

}
