package edu.tamu.tcat.trc.editorial.api.tasks;

import java.util.concurrent.Future;

import edu.tamu.tcat.trc.editorial.api.workflow.Workflow;

public interface EditTaskCommand
{
   /**
    * @param name The display name for this task.
    */
   void setName(String name);

   /**
    * @param description Sets a description for this task
    */
   void setDescription(String description);

   /**
    * @param workflow Sets the workflow associated with this task. Note that updating a
    *       workflow requires that all associated work items be transfered from their
    *       stage in the current workflow to a corresponding stage in the target workflow.
    *       This multi-item update is beyond the scope of the Repository EditCommand system.
    */
   void setWorkflow(Workflow workflow);

   /**
    * @return Applies the requested changes.
    */
   Future<EditorialTask> execute();
}
