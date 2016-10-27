package edu.tamu.tcat.trc.impl.psql.services.notes;

import java.time.ZonedDateTime;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.account.store.AccountStore;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.services.notes.Note;

public class NoteImpl implements Note
{
   private final String id;
   private final ZonedDateTime dateCreated;
   private final ZonedDateTime dateModified;
   private final EntryId entryRef;
   private final Account author;
   private final String mimeType;
   private final String content;

   public NoteImpl(DataModelV1.Note dto, AccountStore accountStore, EntryResolverRegistry resolvers)
   {
      this.id = dto.id;
      this.dateCreated = dto.dateCreated != null
            ? ZonedDateTime.parse(dto.dateCreated) : null;
      this.dateModified = dto.dateModified != null
            ? ZonedDateTime.parse(dto.dateModified) : null;

      this.entryRef = (dto.entryRef != null) ? resolvers.decodeToken(dto.entryRef) : null;
      this.author = (dto.authorId != null) ? accountStore.getAccount(dto.authorId) : null;

      this.content = dto.content;
      this.mimeType = dto.mimeType;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public ZonedDateTime getDateCreated()
   {
      return dateCreated;
   }

   @Override
   public ZonedDateTime getDateModified()
   {
      return dateModified;
   }

   @Override
   public EntryId getAssociatedEntry()
   {
      return entryRef;
   }

   @Override
   public Account getAuthor()
   {
      return author;
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
