package edu.tamu.tcat.trc.editorial.api.workflow;

/**
 *  A path for an item to to transfer from one stage to another stage.
 */
public interface StageTransition
{

   // TODO add constraints on who is allowed to execute a transition.
   // TODO add a data entry/data capture screen to display upon transition.
   // TODO add triggers that are fired upon stage transition.

   /**
    * @return The unique identifier for this transition.
    */
   String getId();

   /**
    * @return A label for display to the user prompting the transition.
    */
   String getLabel();

   /**
    * @return A brief description of this transition.
    */
   String getDescription();

   /**
    * @return The source workflow stage.
    */
   WorkflowStage getSource();

   /**
    * @return The target workflow stage after completion of this transition.
    */
   WorkflowStage getTarget();
}
