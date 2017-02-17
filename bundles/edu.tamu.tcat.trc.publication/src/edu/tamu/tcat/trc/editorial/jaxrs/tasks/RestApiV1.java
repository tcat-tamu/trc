package edu.tamu.tcat.trc.editorial.jaxrs.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestApiV1
{
   public static class EditorialTask
   {
      /** The unique identifier for this task. */
      public String id;

      /** The name of this task. */
      public String name;

      /** A short description for this task. */
      public String description;

      /** The id of the workflow associated with this task. Note that this allows
       *  clients to load commonly used workflows once and cache the workflow. */
      public String workflowId;     // TODO HATEOS
   }

   public static class WorkItem
   {
      /** The unique id of this item. */
      public String id;

      /** The id of the EditorialTask this item is associated with. */
      public String taskId;

      /** The current workflow stage of this item. */
      public String stageId;

      /** The reference token for the TRC entry associated with this item. */
      public String entryRefToken;

      /**
       * A display label for this work item. Will typically be derived from the
       * associated TRC Entry.
       */
      public String label;

      /** An optional description associated with this item. */
      public String description;

      /** A collection of application-defined properties associated with this entry. */
      public Map<String, String> properties = new HashMap<>();
   }

   public static class Workflow
   {
      /** A unique identifier for this workflow. */
      public String id;

      /** A display name for this workflow. */
      public String name;

      /** A description of this workflow. */
      public String description;

      /** All stages associated with this workflow. */
      public List<WorkflowStage> stages;

      /** The ID of the initial stage for new items. */
      public String initialStageId;
   }

   public static class WorkflowStage
   {
      /**  A unique identifier for this stage. */
      public String id;

      /** The display label for this stage. */
      public String label;

      /** A description of this stage. */
      public String description;

      /** Semantic type for this stage (e.g., terminal). */
      public String type;

      /**
       *  A list of defined transitions from this stage to other stages
       *  within the workflow. This will be in the order in which available
       *  transitions will be displayed in user interfaces.
       */
      public List<StageTransition> transitions;

   }

   public static class StageTransition
   {
      /** The unique identifier for this transition. */
      public String id;

      /** A label for display to the user prompting the transition. */
      public String label;

      /**  A brief description of this transition. */
      public String description;

      /** The id of the source workflow stage. */
      public String sourceId;

      /** The the id of the target workflow stage after completion of this transition. */
      public String targetId;
   }
}
