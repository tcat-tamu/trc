package edu.tamu.tcat.trc.editorial.api.workflow;

public interface WorkflowManager
{
   /**
    * @param id The workflow to retrieve.
    * @return The identified workflow. Will not be <code>null</code>
    * @throws IllegalArgumentException If there is no workflow with the supplied id.
    */
   Workflow getWorkflow(String id) throws IllegalArgumentException;
}
