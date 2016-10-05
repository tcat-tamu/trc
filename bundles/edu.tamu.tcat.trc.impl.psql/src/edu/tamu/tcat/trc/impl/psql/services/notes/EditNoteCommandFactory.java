package edu.tamu.tcat.trc.impl.psql.services.notes;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.account.store.AccountStore;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.UpdateContext;
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
   public EditNoteCommand create(String id, EditCommandFactory.UpdateStrategy<DataModelV1.Note> strategy)
   {
      return new EditNoteCmdImpl(id, strategy);
   }

   @Override
   public EditNoteCommand edit(String id, EditCommandFactory.UpdateStrategy<DataModelV1.Note> strategy)
   {
      return new EditNoteCmdImpl(id, strategy);
   }

   private class EditNoteCmdImpl implements EditNoteCommand
   {
      private final String id;
      private final EditCommandFactory.UpdateStrategy<DataModelV1.Note> strategy;
      private final ApplicableChangeSet<DataModelV1.Note> changes = new BasicChangeSet<>();

      private EditNoteCmdImpl(String id, EditCommandFactory.UpdateStrategy<DataModelV1.Note> strategy)
      {
         this.id = id;
         this.strategy = strategy;
      }

      @Override
      public String getId()
      {
         return id;
      }

      @Override
      public void setAssociatedEntry(EntryReference ref)
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
         CompletableFuture<DataModelV1.Note> modified = strategy.update(ctx -> {
            DataModelV1.Note dto = prepModifiedData(ctx);
            return this.changes.apply(dto);
         });

         return modified
               .thenApply(dto -> new NoteImpl(dto, acctStore, resolvers));
      }

      private DataModelV1.Note prepModifiedData(UpdateContext<DataModelV1.Note> ctx)
      {
         DataModelV1.Note dto = null;
         DataModelV1.Note original = ctx.getOriginal();
         if (original == null)
         {
            String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME);

            dto = new DataModelV1.Note();
            dto.id = this.id;
            dto.dateCreated = timestamp;
            dto.dateModified = timestamp;
         }
         else
         {
            dto = copy(original);
            dto.dateModified = ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
         }

         return dto;
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
   }
}
