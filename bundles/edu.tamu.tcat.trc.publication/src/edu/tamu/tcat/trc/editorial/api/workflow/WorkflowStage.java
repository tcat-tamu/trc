package edu.tamu.tcat.trc.editorial.api.workflow;

import java.util.List;

/**
 *  A defined stage within a workflow. Stages
 */
public interface WorkflowStage
{
   /**
    * @return A unique identifier for this stage.
    */
   String getId();

   /**
    * @return The display label for this stage.
    */
   String getLabel();

   /**
    * @return A description of this stage.
    */
   String getDescription();

   /**
    * TODO need a concrete type. Intended to represent notions like 'completed',
    * 'active', 'deferred' states.
    *
    * @return
    */
   String getType();

   /**
    *
    * @return A list of defined transitions from this stage to other stages within the
    *    workflow. This will be in the order in which available transitions will be
    *    displayed in user interfaces.
    */
   List<StageTransition> getTransitions();
}
