package edu.tamu.tcat.trc.notes.internal.dto;

import java.net.URI;
import java.util.UUID;

import edu.tamu.tcat.trc.notes.Notes;

public class BasicNotes implements Notes
{
   private final UUID id;
   private final URI associatedEntity;
   private final String authorId;
   private final String mimeType;
   private final String content;

   public BasicNotes(UUID id, URI associatedEntity, String authorId, String mimeType, String content)
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
