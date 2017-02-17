package edu.tamu.tcat.trc.editorial.impl.psql.tasks;

import java.util.HashMap;
import java.util.Map;

public abstract class DataModelV1
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
      public String workflowId;
   }

   public static class WorkItem
   {
      public String id;
      public String taskId;
      public String stageId;
      public String entryRefToken;

      public String label;
      public String description;

      public Map<String, String> properties = new HashMap<>();

      public static WorkItem copy(WorkItem orig)
      {
         WorkItem result = new WorkItem();
         result.id = orig.id;
         result.taskId = orig.taskId;
         result.label = orig.label;
         result.description = orig.description;
         result.entryRefToken = orig.entryRefToken;
         result.stageId = orig.stageId;
         result.properties = new HashMap<>(orig.properties);

         return result;
      }
   }
}
