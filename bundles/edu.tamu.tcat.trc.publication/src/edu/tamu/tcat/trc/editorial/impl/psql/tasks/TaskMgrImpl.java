package edu.tamu.tcat.trc.editorial.impl.psql.tasks;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.editorial.api.tasks.EditorialTask;
import edu.tamu.tcat.trc.editorial.api.tasks.EditorialTaskManager;
import edu.tamu.tcat.trc.editorial.api.tasks.PartialWorkItemSet;
import edu.tamu.tcat.trc.editorial.api.tasks.TaskSubmissionMonitor;
import edu.tamu.tcat.trc.editorial.api.tasks.WorkItem;
import edu.tamu.tcat.trc.editorial.api.workflow.StageTransition;
import edu.tamu.tcat.trc.editorial.api.workflow.Workflow;
import edu.tamu.tcat.trc.editorial.api.workflow.WorkflowManager;
import edu.tamu.tcat.trc.editorial.api.workflow.WorkflowStage;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.services.ServiceContext;

public class TaskMgrImpl implements EditorialTaskManager
{
   private final Account account;
   private final TaskManagerFactory taskMgrFactory;

   public TaskMgrImpl(TaskManagerFactory taskMgrFactory, WorkflowManager workflows, ServiceContext<EditorialTaskManager> ctx)
   {
      this.taskMgrFactory = taskMgrFactory;
      this.account = ctx.getAccount().orElse(null);
   }

   @Override
   public boolean exists(String taskId)
   {
      return taskMgrFactory.exists(taskId);
   }

   @Override
   public List<EditorialTask> listTasks()
   {
      return taskMgrFactory.listTasks().stream()
            .map(EdTask::new)
            .collect(toList());
   }

   @Override
   public Optional<EditorialTask> get(String taskId)
   {
      return taskMgrFactory.get(taskId).map(EdTask::new);

   }

   @Override
   public EditorialTask create(TaskDescription details)
   {
      PartialEditorialTaskImpl partial = taskMgrFactory.create(account, details);
      return new EdTask(partial);
   }

   @Override
   public EditorialTask update(TaskDescription details)
   {
      PartialEditorialTaskImpl partial = taskMgrFactory.update(account, details);
      return new EdTask(partial);
   }

   @Override
   public void remove(String taskId)
   {
      taskMgrFactory.remove(account, taskId);
   }

   /**
    *  HACK:
    *  This is a partial implementation of the {@link EditorialTask} interface.
    *  Unfortunately, the {@link DocumentRepository} API/implementation does not
    *  allow implementations to be scoped relative to the account used to access them
    *  and the {@link EditorialTask}, since it must access a second, work item repository,
    *  requires access to the service context (specifically, the Account) associated with
    *  the {@link EditorialTaskManager} through which it was accessed.
    *
    *  <p>To work around this constraint, the {@link TaskMgrImpl} will wrap instances of
    *  {@link PartialEditorialTaskImpl} with the complete, account-aware implementation.
    *  Instances of this class should never be returned to the client.
    *
    *  <p>As a design note, The underlying repo structure should be updated to manage the
    *  persistence DTOs rather than providing a general framework that supports adapting
    *  those DTOs into Domain Model (DM) objects and Edit Commands. That design pattern can be
    *  added as an optional adapter around the underlying DocumentRepoository IF (and only if)
    *  needed.
    */
   public static class PartialEditorialTaskImpl implements EditorialTask
   {
      private final WorkflowManager workflows;

      private final String id;
      private final String description;
      private final String name;
      private final String workflowId;

      PartialEditorialTaskImpl(DataModelV1.EditorialTask dto, WorkflowManager workflows)
      {
         this.workflows = workflows;

         this.id = dto.id;
         this.name = dto.name;
         this.description = dto.description;
         this.workflowId = dto.workflowId;
      }

      @Override
      public String getId()
      {
         return id;
      }

      @Override
      public String getName()
      {
         return name;
      }

      @Override
      public String getDescription()
      {
         return description;
      }

      @Override
      public Workflow getWorkflow()
      {
         return workflows.getWorkflow(workflowId);
      }

      @Override
      public PartialWorkItemSet getItems(WorkflowStage stage, int start, int ct)
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public WorkItem addItem(EntryId entity) throws IllegalArgumentException
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public Optional<WorkItem> getItem(String id) throws IllegalArgumentException
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public <X> void addItems(Supplier<EntryReference<X>> entitySupplier, TaskSubmissionMonitor monitor)
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public WorkItem transition(WorkItem item, StageTransition transition)
      {
         throw new UnsupportedOperationException();
      }
   }

   private class EdTask implements EditorialTask
   {
      /**
       * A delegate that partially implements the {@link EditorialTask} API but lacks
       * the {@link Account} and other scoping information required to fully implement it.
       */
      private final PartialEditorialTaskImpl delegate;

      public EdTask(PartialEditorialTaskImpl task)
      {
         delegate = task;
      }

      @Override
      public String getId()
      {
         return delegate.getId();
      }

      @Override
      public String getName()
      {
         return delegate.getName();
      }

      @Override
      public String getDescription()
      {
         return delegate.getDescription();
      }

      @Override
      public Workflow getWorkflow()
      {
         return delegate.getWorkflow();
      }

      @Override
      public PartialWorkItemSet getItems(WorkflowStage stage, int start, int ct)
      {
         return taskMgrFactory.getItems(account, this, stage, start, ct);
      }

      @Override
      public WorkItem addItem(EntryId entity) throws IllegalArgumentException
      {
         return taskMgrFactory.addItem(account, this, entity);
      }

      @Override
      public Optional<WorkItem> getItem(String id) throws IllegalArgumentException
      {
         return taskMgrFactory.getItem(id);
      }

      @Override
      public <X> void addItems(Supplier<EntryReference<X>> entitySupplier, TaskSubmissionMonitor monitor)
      {
         taskMgrFactory.addItems(account, this, entitySupplier, monitor);
      }

      @Override
      public WorkItem transition(WorkItem item, StageTransition transition)
      {
         return taskMgrFactory.transition(account, item, transition);
      }

   }
}