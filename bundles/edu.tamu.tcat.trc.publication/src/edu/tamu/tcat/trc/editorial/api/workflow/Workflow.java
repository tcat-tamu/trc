package edu.tamu.tcat.trc.editorial.api.workflow;

import java.util.List;

/**
 * Defines a series of tasks (workflow stages) through which an item may be processed.
 * Items are moved through the workflow by a series of transitions. This system is designed
 * to support end-user extensible editorial or management processes.
 */
public interface Workflow
{
   /**
    * @return A unique identifier for this workflow.
    */
   String getId();

   /**
    * @return A display name for this workflow.
    */
   String getName();

   /**
    * @return A description of this workflow.
    */
   String getDescription();

   /**
    * @return All stages associated with this workflow.
    */
   List<WorkflowStage> getStages();

   /**
    * @param stageId The id of a workflow stage to retrieve.
    * @return The identified stage.
    *
    * @throws IllegalArgumentException
    */
   WorkflowStage getStage(String stageId) throws IllegalArgumentException;

   /**
    * @return The initial stage for items within this workflow.
    */
   WorkflowStage getInitialStage();
}
