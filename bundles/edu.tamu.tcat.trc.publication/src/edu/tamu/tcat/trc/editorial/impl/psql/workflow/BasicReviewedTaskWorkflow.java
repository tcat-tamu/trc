package edu.tamu.tcat.trc.editorial.impl.psql.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import edu.tamu.tcat.trc.editorial.api.workflow.StageTransition;
import edu.tamu.tcat.trc.editorial.api.workflow.Workflow;
import edu.tamu.tcat.trc.editorial.api.workflow.WorkflowStage;

/**
 * Defines a simple workflow for tasks that require editorial approval prior to completion.
 *
 * <p>This is used as a stopgap measure pending a more flexible implementation of
 * workflow support.
 */
public class BasicReviewedTaskWorkflow implements Workflow
{

   private final Map<String, WorkflowStageImpl> stages = new HashMap<>();
   private final Map<String, List<BasicWorkflowStageTransition>> transitions = new HashMap<>();

   public BasicReviewedTaskWorkflow()
   {
      initialize();
   }

   @Override
   public String getId()
   {
      return "reviewed";
   }

   @Override
   public String getName()
   {
      return "Basic Reviewed Tasks";
   }

   @Override
   public String getDescription()
   {
      return "A simple workflow for tasks that require editorial approval prior to completion. "
            + "Work items may also be pinned for priority attention or deferred.";
   }

   @Override
   public List<WorkflowStage> getStages()
   {
      List<WorkflowStage> stageList = new ArrayList<>(stages.values());
      return Collections.unmodifiableList(stageList);
   }

   @Override
   public WorkflowStage getStage(String stageId) throws IllegalArgumentException
   {
      if (!stages.containsKey(stageId))
      {
         throw new IllegalArgumentException();
      }

      return stages.get(stageId);
   }

   @Override
   public WorkflowStage getInitialStage()
   {
      return stages.get("pending");
   }

   private void initialize()
   {
      // setup core workflow stages
      WorkflowStageImpl pending = new WorkflowStageImpl("pending", "Not Started", "Not yet started.");
      WorkflowStageImpl pinned = new WorkflowStageImpl("pinned", "Pinned", "Task pinned for urgent attention.");
      WorkflowStageImpl inprogress = new WorkflowStageImpl("inprogress", "In Progress", "Work has begun on this task.");
      WorkflowStageImpl review = new WorkflowStageImpl("review", "Under Review", "Work has been completed for this task and should be reviewed.");
      WorkflowStageImpl complete = new WorkflowStageImpl("complete", "Completed", "All work has been completed and reviews for this task.");
      WorkflowStageImpl deferred = new WorkflowStageImpl("deferred", "Deferred", "Task deferred for later work.");

      // setup transitions -- pending
      addTransition(pending, inprogress, "Start");
      addTransition(pending, deferred, "Defer");
      addTransition(pending, pinned, "Pin");

      // setup transitions -- pinned
      addTransition(pinned, review, "Mark Completed");
      addTransition(pinned, inprogress, "Unpin");
      addTransition(pinned, deferred, "Defer");
      addTransition(pinned, complete, "Approved");

      // setup transitions -- inprogress
      addTransition(inprogress, review, "Mark Completed");
      addTransition(inprogress, pinned, "Pin");
      addTransition(inprogress, deferred, "Defer");
      addTransition(inprogress, complete, "Approved");

      // setup transitions -- review
      addTransition(review, complete, "Approved");
      addTransition(review, inprogress, "Reject");

      // setup transitions -- completed
      addTransition(complete, inprogress, "Reopen");

      // setup transitions -- deferred
      addTransition(deferred, inprogress, "Restart");
      addTransition(deferred, review, "Mark Completed");
      addTransition(deferred, complete, "Approved");

      Stream.of(pending, pinned, inprogress, review, complete, deferred)
         .forEach(stage -> stages.put(stage.getId(), stage));
   }

   private void addTransition(WorkflowStageImpl from, WorkflowStageImpl dest, String label)
   {
      BasicWorkflowStageTransition transition = new BasicWorkflowStageTransition(label, label, from, dest);
      transitions.computeIfAbsent(from.getId(), (key) -> new ArrayList<>()).add(transition);
   }

   private class WorkflowStageImpl implements WorkflowStage
   {

      private final String id;
      private final String label;
      private final String description;

      public WorkflowStageImpl(String id, String label, String description)
      {
         this.id = id;
         this.label = label;
         this.description = description;
      }


      @Override
      public String getId()
      {
         return id;
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
      public String getType()
      {
         return "";
      }

      @Override
      public List<StageTransition> getTransitions()
      {
         List<BasicWorkflowStageTransition> list = transitions.get(id);
         return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
      }
   }
}
