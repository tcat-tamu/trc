package edu.tamu.tcat.trc.notes.repo.basic;

import java.net.URI;
import java.util.UUID;

import edu.tamu.tcat.trc.notes.Notes;

public class BasicNotesImpl implements Notes
{
   private final UUID id;
   private final URI associatedEntity;
   private final UUID authorId;
   private final String mimeType;
   private final String content;

   public BasicNotesImpl(UUID id, URI associatedEntity, UUID authorId, String mimeType, String content)
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
      return authorId;
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
