package edu.tamu.tcat.trc.editorial.impl.psql.tasks;

import java.util.Optional;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.editorial.api.tasks.EditTaskCommand;
import edu.tamu.tcat.trc.editorial.api.tasks.EditorialTask;
import edu.tamu.tcat.trc.editorial.api.workflow.Workflow;
import edu.tamu.tcat.trc.editorial.api.workflow.WorkflowManager;
import edu.tamu.tcat.trc.editorial.impl.psql.tasks.TaskMgrImpl.PartialEditorialTaskImpl;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.ExecutableUpdateContext;

public class EditTaskCommandFactory implements EditCommandFactory<DataModelV1.EditorialTask, EditTaskCommand>
{
   private final WorkflowManager workflows;

   public EditTaskCommandFactory(WorkflowManager workflows)
   {
      this.workflows = workflows;
   }

   @Override
   public EditTaskCommand create(ExecutableUpdateContext<DataModelV1.EditorialTask> ctx)
   {
      return new EditTaskCmdImp(ctx);
   }

   @Override
   public DataModelV1.EditorialTask initialize(String id, Optional<DataModelV1.EditorialTask> original)
   {
      return original.map(dto -> this.copy(dto, id)).orElse(create(id));
   }

   private DataModelV1.EditorialTask copy(DataModelV1.EditorialTask orig, String id)
   {
      DataModelV1.EditorialTask dto = new DataModelV1.EditorialTask();
      dto.id = orig.id;
      dto.name = orig.name;
      dto.description = orig.description;
      dto.workflowId = orig.workflowId;

      if (!id.equals(dto.id))
         throw new IllegalStateException("Id of original editorial task does not match supplied id");

      return dto;
   }

   private DataModelV1.EditorialTask create(String id)
   {
      DataModelV1.EditorialTask dto = new DataModelV1.EditorialTask();
      dto.id = id;

      return dto;
   }

   public class EditTaskCmdImp implements EditTaskCommand
   {
      private final ExecutableUpdateContext<DataModelV1.EditorialTask> ctx;
      private final ApplicableChangeSet<DataModelV1.EditorialTask> changes = new BasicChangeSet<>();

      public EditTaskCmdImp(ExecutableUpdateContext<DataModelV1.EditorialTask> ctx)
      {
         this.ctx = ctx;
      }

      @Override
      public void setName(String name)
      {
         changes.add("name", dto -> dto.name = name);

      }

      @Override
      public void setDescription(String description)
      {
         changes.add("description", dto -> dto.description = description);
      }

      @Override
      public void setWorkflow(Workflow workflow)
      {
         changes.add("workflowId", dto -> dto.workflowId = workflow.getId());
      }

      @Override
      public Future<EditorialTask> execute()
      {
         return ctx.update(changes::apply).thenApply(dto -> new PartialEditorialTaskImpl(dto, workflows));
      }

   }
}
