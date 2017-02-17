package edu.tamu.tcat.trc.editorial.impl.psql.tasks;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.editorial.api.tasks.EditTaskCommand;
import edu.tamu.tcat.trc.editorial.api.tasks.EditWorkItemCommand;
import edu.tamu.tcat.trc.editorial.api.tasks.EditorialTask;
import edu.tamu.tcat.trc.editorial.api.tasks.EditorialTaskManager;
import edu.tamu.tcat.trc.editorial.api.tasks.EditorialTaskManager.TaskDescription;
import edu.tamu.tcat.trc.editorial.api.tasks.PartialWorkItemSet;
import edu.tamu.tcat.trc.editorial.api.tasks.TaskSubmissionMonitor;
import edu.tamu.tcat.trc.editorial.api.tasks.TaskSubmissionMonitor.WorkItemCreationError;
import edu.tamu.tcat.trc.editorial.api.tasks.TaskSubmissionMonitor.WorkItemCreationRecord;
import edu.tamu.tcat.trc.editorial.api.tasks.WorkItem;
import edu.tamu.tcat.trc.editorial.api.workflow.StageTransition;
import edu.tamu.tcat.trc.editorial.api.workflow.Workflow;
import edu.tamu.tcat.trc.editorial.api.workflow.WorkflowManager;
import edu.tamu.tcat.trc.editorial.api.workflow.WorkflowStage;
import edu.tamu.tcat.trc.editorial.impl.psql.tasks.TaskMgrImpl.PartialEditorialTaskImpl;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocRepoBuilderFactory;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.services.ServiceContext;

/**
 *  Service factory class used to manufacture TaskManager implementations for the TRC system.
 *
 */
public class TaskManagerFactory
{

   private final static Logger logger = Logger.getLogger(TaskManagerFactory.class.getName());

//    FIXME load up the WorkItemRepositoryFactory . . . perhaps internalize?
   private final static String TASKS_TABLE_NAME = "editorial_tasks";
   private final static String ITEMS_TABLE_NAME = "editorial_work_items";

   private final WorkflowManager workflows;
   private final EntryResolverRegistry resolvers;
   private final ExecutorService addItemsExec;

   private DocumentRepository<PartialEditorialTaskImpl, EditTaskCommand> taskRepo;
   private DocumentRepository<WorkItem, EditWorkItemCommand> itemRepo;

   public TaskManagerFactory(TrcApplication trcCtx,
                             WorkflowManager workflows,
                             DocRepoBuilderFactory factory)
   {
      this.workflows = workflows;
      this.resolvers = trcCtx.getResolverRegistry();
      this.addItemsExec = Executors.newCachedThreadPool();

      initTaskRepo(factory);
      initItemsRepo(factory);
   }

   public Class<EditorialTaskManager> getType()
   {
      return EditorialTaskManager.class;
   }

   public EditorialTaskManager getService(ServiceContext<EditorialTaskManager> ctx)
   {
      return new TaskMgrImpl(this, workflows, ctx);
   }

   private void initTaskRepo(DocRepoBuilderFactory factory)
   {
      DocRepoBuilder<PartialEditorialTaskImpl, DataModelV1.EditorialTask, EditTaskCommand> repoBuilder = factory.getDocRepoBuilder();
      repoBuilder.setPersistenceId(TASKS_TABLE_NAME);
      repoBuilder.setEnableCreation(true);

      repoBuilder.setEditCommandFactory(new EditTaskCommandFactory(workflows));
      repoBuilder.setDataAdapter(dto -> null);

      repoBuilder.setStorageType(DataModelV1.EditorialTask.class);

      taskRepo = repoBuilder.build();
   }

   private void initItemsRepo(DocRepoBuilderFactory factory)
   {
      DocRepoBuilder<WorkItem, DataModelV1.WorkItem, EditWorkItemCommand> builder =
            factory.getDocRepoBuilder();

      builder.setPersistenceId(ITEMS_TABLE_NAME);
      builder.setEnableCreation(true);

      builder.setEditCommandFactory(new EditItemCommandFactory(resolvers));
      builder.setStorageType(DataModelV1.WorkItem.class);
      builder.setDataAdapter(dto -> new WorkItemImpl(dto));

      itemRepo = builder.build();
   }

   public void shutdown()
   {
      taskRepo.dispose();
      itemRepo.dispose();

      try {
         addItemsExec.shutdown();
         addItemsExec.awaitTermination(10, TimeUnit.SECONDS);
      } catch (Exception ex) {
         String errMsg = "Failed to cleanly shutdown add items executor. {0} tasks remaining.";
         List<Runnable> inProgress = addItemsExec.shutdownNow();
         logger.log(Level.SEVERE, format(errMsg, inProgress.size()), ex);
      }
   }

   boolean exists(String taskId)
   {
      // HACK: add existence check to repo
      return taskRepo.get(taskId)
            .map(task -> Boolean.TRUE)
            .orElse(Boolean.FALSE)
            .booleanValue();
   }

   List<PartialEditorialTaskImpl> listTasks()
   {
      return taskRepo.listAsStream().collect(toList());
   }

   Optional<PartialEditorialTaskImpl> get(String taskId)
   {
      return taskRepo.get(taskId);
   }

   PartialEditorialTaskImpl create(Account account, TaskDescription details)
   {
      EditTaskCommand cmd = (details.id != null) ? taskRepo.create(account, details.id) : taskRepo.create(account);
      cmd.setName(details.name);
      cmd.setDescription(details.description);

      Workflow workflow = workflows.getWorkflow(details.workflowId);
      cmd.setWorkflow(workflow);

      String errMsg = "Failed to create new task description: {0} [{1}].";
      return (PartialEditorialTaskImpl)DocumentRepository.unwrap(cmd.execute(), () -> format(errMsg, details.name, details.id));
   }

   PartialEditorialTaskImpl update(Account account, TaskDescription details)
   {
      if (details.id == null || details.id.trim().isEmpty())
         throw new IllegalArgumentException("Cannut update task description. No id supplied.");

      EditTaskCommand cmd = taskRepo.edit(account, details.id);
      if (details.name != null)
         cmd.setName(details.name);

      if (details.description != null)
         cmd.setDescription(details.description);

      if (details.workflowId != null)
      {
         Workflow workflow = workflows.getWorkflow(details.workflowId);
         cmd.setWorkflow(workflow);
      }

      String errMsg = "Failed to update task description: {0} [{1}].";
      return (PartialEditorialTaskImpl)DocumentRepository.unwrap(cmd.execute(), () -> format(errMsg, details.name, details.id));
   }

   void remove(Account account, String taskId)
   {
      taskRepo.delete(account, taskId);
   }

   public PartialWorkItemSet getItems(Account account, EditorialTask task, WorkflowStage stage, int start, int ct)
   {
      String taskId = Objects.requireNonNull(task, "No editorial task supplied").getId();

      // HACK support filtering of items within the repo rather than list all and filter in memory.
      List<WorkItem> items = itemRepo.listAsStream()
              .filter(item -> Objects.equals(item.getTaskId(), taskId))
              .collect(toList());

      return new ListBackedPartialWorkItemSetImpl(items, start, ct);
   }

   public WorkItem addItem(Account account, EditorialTask task, EntryId entity) throws IllegalArgumentException
   {
      EntryReference<?> ref = resolvers.getReference(entity);
      String name = task.getName();
      String itemId = this.addItem(account, task.getWorkflow(), name, ref);

      String errGetMsg = "Failed to retrieve new {0} item for work item {1} [{2}]";
      return itemRepo.get(itemId)
            .orElseThrow(() -> new IllegalStateException(
                  format(errGetMsg, name, ref.getLabel(), itemId)));
   }

   private String addItem(Account account, Workflow workflow, String taskName, EntryReference<?> ref) throws IllegalArgumentException
   {
      EditWorkItemCommand cmd = itemRepo.create(account, UUID.randomUUID().toString());

      cmd.setEntityRef(ref.getEntryId());
      cmd.setLabel(ref.getHtmlLabel());
      cmd.setDescription("");
      cmd.setStage(workflow.getInitialStage());

      String errMsg = "Failed to create new {0} task for entry {1}";
      Future<String> result = cmd.execute();
      return DocumentRepository.unwrap(result, () -> format(errMsg, taskName, ref.getLabel()));
   }

   public Optional<WorkItem> getItem(String id) throws IllegalArgumentException
   {
      return itemRepo.get(id);
   }

   public <X> void addItems(Account account,
                            EditorialTask task,
                            Supplier<EntryReference<X>> entitySupplier,
                            TaskSubmissionMonitor monitor)
   {
      Workflow workflow = task.getWorkflow();
      String name = task.getName();
      addItemsExec.execute(() -> {
         EntryReference<X> ref = entitySupplier.get();

         while (ref != null)
         {
            try
            {
               String id = this.addItem(account, workflow, name, ref);
               monitor.created(new WorkItemCreationRecordImpl<>(account, ref, id));
            }
            catch (Exception ex)
            {
               monitor.failed(new WorkItemCreationErrorImpl<>(account, ref, ex));

            }

            ref = entitySupplier.get();
         }

         monitor.finished();
      });
   }

   public WorkItem transition(Account account, WorkItem item, StageTransition transition)
   {
      // verify the transition is valid for the item's current state.
      String errNotFound = "The supplied item {0} [{2}] could not be found.";
      WorkItem current = getItem(item.getId())
         .orElseThrow(() -> new IllegalArgumentException(format(errNotFound, item.getLabel(), item.getId())));

      if (!isValidTransition(transition, current))
      {
         // TODO figure out correct exception
         throw new IllegalArgumentException();
      }

      WorkflowStage src = transition.getSource();
      WorkflowStage target = transition.getTarget();

      EditWorkItemCommand cmd = itemRepo.edit(account, current.getId());
      cmd.setStage(target);

      String errMsg = "Failed to transition item {0} [{1}] from stage {2} to stage {3} ";
      Future<String> result = cmd.execute();
      String itemId = DocumentRepository.unwrap(result,
            () -> format(errMsg, item.getLabel(), item.getId(), src.getLabel(), target.getLabel()));

      String errGetMsg = "Failed to retrieve updated work item {1} [{2}]";
      return itemRepo.get(itemId)
            .orElseThrow(() -> new IllegalStateException(
                  format(errGetMsg, item.getLabel(), itemId)));
   }

   private boolean isValidTransition(StageTransition transition, WorkItem current)
   {
      WorkflowStage stage = current.getStage();
      return stage.getTransitions().stream()
         .anyMatch(t -> Objects.equals(t.getId(), transition.getId()));
   }

   public class WorkItemCreationRecordImpl<T> implements WorkItemCreationRecord<T>
   {

      private final Account account;
      private final EntryReference<T> ref;
      private final String id;

      public WorkItemCreationRecordImpl(Account account, EntryReference<T> ref, String id)
      {
         this.account = account;
         this.ref = ref;
         this.id = id;
      }

      @Override
      public T getEntity()
      {
         return ref.getEntry(account);
      }

      @Override
      public String getWorkItemId()
      {
         return id;
      }
   }

   public class WorkItemCreationErrorImpl<T> implements WorkItemCreationError<T>
   {
      private final Account account;
      private final EntryReference<T> ref;
      private final Exception ex;

      public WorkItemCreationErrorImpl(Account account, EntryReference<T> ref, Exception ex)
      {
         this.account = account;
         this.ref = ref;
         this.ex = ex;
      }

      @Override
      public T getEntity()
      {
         return ref.getEntry(account);
      }

      @Override
      public String getMessage()
      {
         return ex.getMessage();
      }

      @Override
      public Optional<Exception> getException()
      {
         return Optional.of(ex);
      }
   }

   /**
    *  A {@link PartialWorkItemSet} backed by the list of all works associated with a given editorial task.
    *
    *  HACK: this requires more robust backend support to be efficient
    */
   public class ListBackedPartialWorkItemSetImpl implements PartialWorkItemSet
   {
      private final List<WorkItem> allWorkItems;

      /** Index of the first item in the partial set. Will be coerced into the range [0, max - 1)*/
      private final int start;
      /**
       * Index of the last item in the partial set, exclusive. Inferred from
       * <code>start + limit</code> with bounds checking.
       */
      private final int end;

      /** Number of items in set. Used for paging. */
      private final int limit;

      /**
       *
       * @param allWorkItems
       * @param start The starting index. Will be coerced into the range [0, max).
       * @param limit The maximum number of items to include. If less than equal to zero,
       *       will be set to 20.
       */
      ListBackedPartialWorkItemSetImpl(List<WorkItem> allWorkItems, int start, int limit)
      {
         if (start < 0)
            start = 0;
         if (start >= allWorkItems.size())
            start = allWorkItems.size() - 1;

         if (limit <= 0)
            limit = 20;

         this.allWorkItems = allWorkItems;
         this.start = start;
         this.limit = limit;
         this.end = Math.min(allWorkItems.size(), start + limit);
      }

      @Override
      public int getTotalMatched()
      {
         return allWorkItems.size();
      }

      @Override
      public int size()
      {
         return end - start;
      }


      @Override
      public int getStart()
      {
         return start;
      }

      @Override
      public int getEnd()
      {
         return end;
      }

      @Override
      public int getLimit()
      {
         return limit;
      }

      @Override
      public List<WorkItem> getItems()
      {
         return allWorkItems.subList(start, end);
      }

      @Override
      public PartialWorkItemSet getNext()
      {
         if (hasNextSet())
            throw new NoSuchElementException("There are no additional item sets");

         return new ListBackedPartialWorkItemSetImpl(allWorkItems, end, end + limit);
      }

      @Override
      public boolean hasNextSet()
      {
         return end >= allWorkItems.size();
      }

   }

   public class WorkItemImpl implements WorkItem
   {
      private final String id;
      private final String taskId;
      private final String stageId;
      private final String token;

      private final String label;
      private final String description;
      private final Map<String, String> properties;

      public WorkItemImpl(DataModelV1.WorkItem dto)
      {
         this.id = dto.id;
         this.taskId = dto.taskId;
         this.stageId = dto.stageId;
         this.token = dto.entryRefToken;

         this.label = dto.label;
         this.description = dto.description;
         this.properties = new HashMap<>(dto.properties);
      }

      @Override
      public String getId()
      {
         return id;
      }

      @Override
      public String getTaskId()
      {
         return taskId;
      }

      @Override
      public String getLabel()
      {
         return label;
      }

      @Override
      public String getDescription()
      {
         return description;
      }

      @Override
      public Set<String> getPropertyKeys()
      {
         return properties.keySet();
      }

      @Override
      public String getProperty(String key)
      {
         if (!properties.containsKey(key))
            throw new IllegalArgumentException(format("Property key '{0}' is not defined.", key));

         return properties.get(key);
      }

      @Override
      public WorkflowStage getStage()
      {
         EditorialTask task = get(this.taskId)
               .orElseThrow(() -> new IllegalStateException());
         return task.getWorkflow().getStage(this.stageId);
      }

      @Override
      public EntryId getEntryId()
      {
         return resolvers.decodeToken(token);
      }

   }
}
