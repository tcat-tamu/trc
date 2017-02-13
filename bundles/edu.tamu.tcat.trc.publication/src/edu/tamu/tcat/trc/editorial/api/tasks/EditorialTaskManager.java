package edu.tamu.tcat.trc.editorial.api.tasks;

import java.util.List;
import java.util.Optional;

import edu.tamu.tcat.trc.auth.account.TrcAccount;
import edu.tamu.tcat.trc.services.BasicServiceContext;
import edu.tamu.tcat.trc.services.ServiceContext;

/**
 *  Provides CRUD operations and other support for interacting with defined editorial tasks.
 */
public interface EditorialTaskManager
{
   /**
    * Factory method to create a new {@link ServiceContext} that will obtain an
    * {@link EditorialTaskManager} for the supplied account.
    *
    * @param account The account associated with the user requesting access.
    * @return A {@link ServiceContext} bound to the supplied account.
    */
   public static ServiceContext<EditorialTaskManager> makeContext(TrcAccount account)
   {
      return new BasicServiceContext<>(EditorialTaskManager.class, account);
   }

   /**
    * Determine if an editorial task exists for the supplied account.
    *
    * @param taskId The id of the task to query.
    * @return <code>true</code> if the identified task exists.
    */
   boolean exists(String taskId);

   /**
    * @return A list of all defined editorial tasks.
    */
   List<EditorialTask> listTasks();

   /**
    * Retrieve the identified editorial task.
    *
    * @param taskId The id of the task to retrieve.
    * @return An optional containing the task if it exists.
    */
   Optional<EditorialTask> get(String taskId);

   /**
    * Creates a new editorial task.
    *
    * @param details The details
    * @return The newly created task.
    */
   EditorialTask create(TaskDescription details);

   /**
    * Updates an existing editorial task. Note that null-valued fields will be ignored.
    *
    * @param details The details
    * @return The updated task.
    */
   EditorialTask update(TaskDescription details);

   // TODO need to support a much more robust model for changing the workflow associated with a task.

   /**
    * Removes an existing editorial task. Note that all work-items currently associated with
    * this task will be removed.
    *
    * @param taskId The id of the task to remove.
    */
   void remove(String taskId);

   /**
    *  A simple data structure for describing the properties associated with an
    *  {@link EditorialTask}.
    */
   public static class TaskDescription
   {
      /** The unique id of the task */
      public String id;

      /**
       * A display name for the task. Should be unique across all tasks that will be
       * accessible for a given account.
       */
      public String name;

      /** A description of this task. */
      public String description;

      /** The ID of the workflow used to manage items within this task. */
      public String workflowId;
   }
}
