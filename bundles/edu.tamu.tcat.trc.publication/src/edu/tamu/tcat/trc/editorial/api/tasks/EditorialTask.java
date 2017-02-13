package edu.tamu.tcat.trc.editorial.api.tasks;

import java.util.Optional;
import java.util.function.Supplier;

import edu.tamu.tcat.trc.editorial.api.workflow.StageTransition;
import edu.tamu.tcat.trc.editorial.api.workflow.Workflow;
import edu.tamu.tcat.trc.editorial.api.workflow.WorkflowStage;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryReference;

/**
 *  Defines a general editorial task such as adding an article or updating digital copies.
 *  Tasks are intended to be accomplished in a multi-stage workflow in which each stage
 *  may be delegated to account holders with different assigned roles (for example,
 *  assigning an author, writing the article, reviewing the article, copy-editing).
 *
 *  <p>Tasks are ongoing work-processes that are performed on specific {@link WorkItem}s.
 *
 *  <p>To simplify the API, individual {@code EditorialTask} instances are intended to be scoped
 *  to the authorized user who instantiated them. All operations will operate within the scope
 *  of the permissions and access controls of that individual user.
 */
public interface EditorialTask
{
   /**
    * @return The unique identifier for this task.
    */
   String getId();

   /**
    * @return The name of this task.
    */
   String getName();

   /**
    * @return A short description for this task.
    */
   String getDescription();

   /**
    * @return The workflow used to transition items through this task.
    */
   Workflow getWorkflow();

   /**
    * Retrieve items in a given stage.
    *
    * @param stage The desired stage of items to retrieve
    * @param start The index of the first item to retrieve
    * @param ct The number of items to be returned. Implementations are expected to impose
    *       and document an upper bound on this value.
    * @return
    * @apiNote This is provided as a convenience method to return items from a stage
    */
   PartialWorkItemSet getItems(WorkflowStage stage, int start, int ct);

   /**
    * Adds a {@link WorkItem} for the supplied entity.
    *
    * @param entity The entity to be added.
    * @return The created work item.
    * @throws IllegalArgumentException If the supplied entity is not valid for this task.
    */
   WorkItem addItem(EntryId entity) throws IllegalArgumentException;

   /**
    * Retrieves a {@link WorkItem} by its identifier.
    *
    * @param id The id of the work item to retrieve
    * @return The identified work item if it exists
    * @throws IllegalArgumentException If no work item with the given id exists.
    */
   Optional<WorkItem> getItem(String id) throws IllegalArgumentException;

   /**
    * Adds work items for all entities provided by a supplier as new tasks. This returns
    * immediately and processes the items to be added asynchronously. All supplied items
    * will be consumed. Callers will be notified of any items that cannot be added to the
    * work list via the {@link TaskSubmissionMonitor} API.
    *
    * @param entitySupplier An entity supplier. May supply many entities. Should return
    *       <code>null</code> when no more entities are available. When a <code>null</code>
    *       value is encountered, processing will stop, no more entities will be added
    *       and {@link TaskSubmissionMonitor#finished()} will be called.
    * @param monitor A monitor to be notified as items are added to the task or errors are
    *       encountered.
    */
   void addItems(Supplier<EntryReference<?>> entitySupplier, TaskSubmissionMonitor monitor);

   /**
    * Transition an item from its current stage to another via a specific
    * {@link StageTransition} instance.
    *
    * @param item The item to be transitioned.
    * @param transition The transition to executes.
    *
    * @return The supplied item, reflecting updated stage info.
    * TODO define exception(s) to be thrown.
    */
   WorkItem transition(WorkItem item, StageTransition transition);

   // TODO entity reference
}
