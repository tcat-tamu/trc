package edu.tamu.tcat.trc.refman.postgres;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import edu.tamu.tcat.trc.refman.postgres.EditCollectionCmdFactory.EditCollectionCommand;
import edu.tamu.tcat.trc.repo.CommitHook;
import edu.tamu.tcat.trc.repo.EditCommandFactory;


public class EditCollectionCmdFactory implements EditCommandFactory<RefCollectionMeta, EditCollectionCommand>
{

   @Override
   public EditCollectionCommand create(String id, CommitHook<RefCollectionMeta> commitHook)
   {
      return new EditCollectionCommand(id, null, commitHook);
   }

   @Override
   public EditCollectionCommand edit(String id, Supplier<RefCollectionMeta> currentState, CommitHook<RefCollectionMeta> commitHook)
   {
      return new EditCollectionCommand(id, currentState, commitHook);
   }


   public static class EditCollectionCommand
   {
      private RefCollectionMeta changes;

      private final Supplier<RefCollectionMeta> currentState;
      private final CommitHook<RefCollectionMeta> hook;

      private String id;

      public EditCollectionCommand(String id, Supplier<RefCollectionMeta> currentState, CommitHook<RefCollectionMeta> commitHook)
      {
         this.id = id;
         this.currentState = currentState;
         this.hook = commitHook;
         this.changes = new RefCollectionMeta();
      }

      public void setName(String name)
      {
         changes.name = name;
      }

      public void setDescription(String desc)
      {
         changes.description = desc;
      }

      public void setProvider(String providerId)
      {
         changes.providerId = providerId;
      }

      public Future<String> execute()
      {
         Objects.requireNonNull(this.id, "No id supplied for this collection.");

         RefCollectionMeta data = new RefCollectionMeta();
         data.name = changes.name;
         data.description = changes.description;
         data.providerId = changes.providerId;

         // apply original values if appropriate
         RefCollectionMeta original = currentState != null ? currentState.get() : null;
         if (original != null)
         {
            if (data.name == null)
               data.name = original.name;
            if (data.description == null)
               data.description= original.description;
            if (data.providerId == null)
               data.providerId = original.providerId;
         }

         if (original != null && !original.id.equals(this.id))
         {
            String msg = "The current id for this collection {0} does not match the previous id {1}";
            throw new IllegalStateException(MessageFormat.format(msg, this.id, original.id));
         }

         data.id = this.id;
         return hook.submit(data, changes);
      }
   }
}
