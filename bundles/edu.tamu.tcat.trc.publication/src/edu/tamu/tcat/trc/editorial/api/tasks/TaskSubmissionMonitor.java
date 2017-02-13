package edu.tamu.tcat.trc.editorial.api.tasks;

import java.util.Optional;

/**
 *  Used to monitor the creation of work items within an {@link EditorialTask}.
 */
public interface TaskSubmissionMonitor
{
   /**
    * Called when the creation of an item succeeds.
    *
    * @param record
    */
   <EntityType> void created(TaskSubmissionMonitor.WorkItemCreationRecord<EntityType> record);

   /**
    * Called when the creation of a work item fails.
    * @param error
    */
   <EntityType> void failed(TaskSubmissionMonitor.WorkItemCreationError<EntityType> error);

   /**
    * Called once all items that were provided by a given supplier have been
    */
   void finished();

   public interface WorkItemCreationRecord<EntityType>
   {
      /**
       * @return The object for which the work item was created.
       */
      EntityType getEntity();

      /**
       * @return The id of the newly created work item.
       */
      String getWorkItemId();
   }

   public interface WorkItemCreationError<EntityType>
   {
      /**
       * @return The object for which the creation of a new work item failed.
       */
      EntityType getEntity();

      /**
       * @return A message describing the error.
       */
      String getMessage();

      /**
       * @return The exception that generated this error.
       */
      Optional<Exception> getException();
   }
}