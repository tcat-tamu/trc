package edu.tamu.tcat.trc.editorial.jaxrs.tasks;

import static java.util.stream.Collectors.toList;

import java.util.function.Function;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.editorial.api.tasks.EditorialTask;
import edu.tamu.tcat.trc.editorial.api.tasks.WorkItem;
import edu.tamu.tcat.trc.editorial.api.workflow.StageTransition;
import edu.tamu.tcat.trc.editorial.api.workflow.Workflow;
import edu.tamu.tcat.trc.editorial.api.workflow.WorkflowStage;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

public class ModelAdapter
{
   public static RestApiV1.EditorialTask adapt(EditorialTask task)
   {
      RestApiV1.EditorialTask dto = new RestApiV1.EditorialTask();
      dto.id = task.getId();
      dto.name = task.getName();
      dto.description = task.getDescription();
      dto.workflowId = task.getWorkflow().getId();

      return dto;
   }

   public static RestApiV1.Workflow adapt(Workflow workflow)
   {
      RestApiV1.Workflow dto = new RestApiV1.Workflow();
      dto.id = workflow.getId();
      dto.name = workflow.getName();
      dto.description = workflow.getDescription();

      WorkflowStage stage = workflow.getInitialStage();
      dto.initialStageId = stage != null ? stage.getId() : "";

      dto.stages = workflow.getStages().stream()
            .map(ModelAdapter::adapt)
            .collect(toList());
      return dto;
   }

   public static RestApiV1.WorkflowStage adapt(WorkflowStage stage)
   {
      RestApiV1.WorkflowStage dto = new RestApiV1.WorkflowStage();
      dto.id = stage.getId();
      dto.label = stage.getLabel();
      dto.description = stage.getDescription();
      dto.type = stage.getType();

      dto.transitions = stage.getTransitions().stream()
            .map(ModelAdapter::adapt)
            .collect(toList());

      return dto;
   }

   public static RestApiV1.StageTransition adapt(StageTransition transition)
   {
      RestApiV1.StageTransition dto = new RestApiV1.StageTransition();
      dto.id = transition.getId();
      dto.label = transition.getLabel();
      dto.description = transition.getDescription();

      dto.sourceId = transition.getSource().getId();
      dto.targetId = transition.getTarget().getId();

      return dto;
   }

   public static RestApiV1.WorkItem adapt(EntryResolverRegistry resolvers, WorkItem item)
   {
      RestApiV1.WorkItem dto = new RestApiV1.WorkItem();

      dto.id = item.getId();
      dto.taskId = item.getTaskId();
      dto.label = item.getLabel();
      dto.description = item.getDescription();
      dto.properties = item.getPropertyKeys().stream()
            .collect(Collectors.toMap(
                  Function.identity(),
                  key -> item.getProperty(key)));
      dto.stageId = item.getStage().getId();
      dto.entryRefToken = resolvers.tokenize(item.getEntryId());

      return dto;
   }
}
