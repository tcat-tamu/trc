package edu.tamu.tcat.trc.editorial.impl.psql.tasks;

import static java.text.MessageFormat.format;

import java.util.Optional;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.editorial.api.tasks.EditWorkItemCommand;
import edu.tamu.tcat.trc.editorial.api.tasks.EditorialTask;
import edu.tamu.tcat.trc.editorial.api.workflow.WorkflowStage;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.ExecutableUpdateContext;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

public class EditItemCommandFactory implements EditCommandFactory<DataModelV1.WorkItem, EditWorkItemCommand>
{
   private final EntryResolverRegistry resolvers;

   EditItemCommandFactory(EntryResolverRegistry resolvers)
   {
      this.resolvers = resolvers;
   }

   @Override
   public DataModelV1.WorkItem initialize(String id, Optional<DataModelV1.WorkItem> original)
   {
      return original.map(DataModelV1.WorkItem::copy)
            .orElseGet(() -> {
               DataModelV1.WorkItem dto = new DataModelV1.WorkItem();
               dto.id = id;
               return dto;
            });
   }

   @Override
   public EditWorkItemCommand create(ExecutableUpdateContext<DataModelV1.WorkItem> ctx)
   {
      return new EditWorkItemCmdImpl(ctx, resolvers);
   }

   public static class EditWorkItemCmdImpl implements EditWorkItemCommand
   {
      private final EntryResolverRegistry resolvers;
      private final ExecutableUpdateContext<DataModelV1.WorkItem> ctx;
      private final ApplicableChangeSet<DataModelV1.WorkItem> changes = new BasicChangeSet<>();

      public EditWorkItemCmdImpl(ExecutableUpdateContext<DataModelV1.WorkItem> ctx, EntryResolverRegistry resolvers)
      {
         this.ctx = ctx;
         this.resolvers = resolvers;
      }

      public void setTaskId(EditorialTask task)
      {
         changes.add("taskId", dto -> dto.taskId = task.getId());
      }

      @Override
      public void setEntityRef(EntryId entryId)
      {
         String token = resolvers.tokenize(entryId);
         changes.add("entryRefToken", dto -> dto.entryRefToken = token);
      }

      @Override
      public void setLabel(String label)
      {
         changes.add("label", dto -> dto.label = label);
      }

      @Override
      public void setDescription(String description)
      {
         changes.add("description", dto -> dto.description = description);
      }

      @Override
      public void setProperty(String key, String value)
      {
         changes.add(format("properties.{0}", key), dto -> dto.properties.put(key, value));
      }

      @Override
      public void clearProperty(String key)
      {
         changes.add(format("properties.{0} [CLEAR]", key), dto -> dto.properties.remove(key));
      }

      @Override
      public void setStage(WorkflowStage stage)
      {
         changes.add("stageId", dto -> dto.stageId = stage.getId());
      }

      @Override
      public Future<String> execute()
      {
         return ctx.update(changes::apply).thenApply(dto -> dto.id);
      }
   }

}
