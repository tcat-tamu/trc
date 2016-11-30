package edu.tamu.tcat.trc.impl.psql.services.notes;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.account.store.AccountStore;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.ExecutableUpdateContext;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.services.notes.EditNoteCommand;
import edu.tamu.tcat.trc.services.notes.Note;

public class EditNoteCommandFactory implements EditCommandFactory<DataModelV1.Note, EditNoteCommand>
{
   private final EntryResolverRegistry resolvers;
   private final AccountStore acctStore;

   public EditNoteCommandFactory(EntryResolverRegistry resolvers, AccountStore acctStore)
   {
      this.resolvers = resolvers;
      this.acctStore = acctStore;
   }

   @Override
   public EditNoteCommand create(ExecutableUpdateContext<DataModelV1.Note> ctx)
   {
      // TODO Auto-generated method stub
      return new EditNoteCmdImpl(ctx);
   }

   @Override
   public DataModelV1.Note initialize(String id, Optional<DataModelV1.Note> original)
   {
      String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
      DataModelV1.Note note = original.map(this::copy)
         .orElseGet(() -> {
            DataModelV1.Note dto = new DataModelV1.Note();
            dto.id = id;
            dto.dateCreated = timestamp;
            return dto;
         });

      note.dateModified = timestamp;
      return note;
   }

   public DataModelV1.Note copy(DataModelV1.Note original)
   {
      DataModelV1.Note copy = new DataModelV1.Note();
      copy.id = original.id;
      copy.dateCreated = original.dateCreated;
      copy.dateModified = original.dateModified;
      copy.entryRef = original.entryRef;
      copy.authorId = original.authorId;
      copy.mimeType = original.mimeType;
      copy.content = original.content;

      return copy;
   }

   private class EditNoteCmdImpl implements EditNoteCommand
   {
      private final String id;
      private final ApplicableChangeSet<DataModelV1.Note> changes = new BasicChangeSet<>();
      private final ExecutableUpdateContext<DataModelV1.Note> ctx;

      private EditNoteCmdImpl(ExecutableUpdateContext<DataModelV1.Note> ctx)
      {
         this.ctx = ctx;
         this.id = ctx.getId();
      }

      @Override
      public String getId()
      {
         return id;
      }

      @Override
      public void setAssociatedEntry(EntryId ref)
      {
         String token = resolvers.tokenize(ref);
         changes.add("entryRef", dto -> dto.entryRef = token);
      }

      @Override
      public void setAuthor(Account account)
      {
         changes.add("authorId", dto -> dto.authorId = account.getId());
      }

      @Override
      public void setMimeType(String mimeType)
      {
         changes.add("mimeType", dto -> dto.mimeType = mimeType);
      }

      @Override
      public void setContent(String content)
      {
         changes.add("content", dto -> dto.content = content);
      }

      @Override
      public CompletableFuture<Note> exec()
      {
         return ctx.update(changes::apply)
               .thenApply(dto -> new NoteImpl(dto, acctStore, resolvers));
      }
   }
}
