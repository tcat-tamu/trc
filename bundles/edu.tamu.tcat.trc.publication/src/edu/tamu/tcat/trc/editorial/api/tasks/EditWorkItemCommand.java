package edu.tamu.tcat.trc.editorial.api.tasks;

import java.util.concurrent.Future;

import edu.tamu.tcat.trc.editorial.api.workflow.WorkflowStage;
import edu.tamu.tcat.trc.resolver.EntryId;

/**
 * Supports modification of a given {@link WorkItem}.
 */
public interface EditWorkItemCommand
{
   /**
    * @param label The display label for the item.
    */
   void setLabel(String label);

   /**
    * @param description Sets a description for this work item.
    */
   void setDescription(String description);

   /**
    * Sets an application defined property on this item.
    *
    * @param key The key for the property to set.
    * @param value The value the property should be set to.
    */
   void setProperty(String key, String value);

   /**
    * Clears the indicated property.
    *
    * @param key The property to clear.
    */
   void clearProperty(String key);

   /**
    * Sets the entry associated with this work item.
    *
    * @param entryId
    */
   void setEntityRef(EntryId entryId);

   /**
    * @param stage The current stage of the work item. Note that if the supplied stage does
    *    not represent a defined transition from the current stage, this will fail.
    */
   void setStage(WorkflowStage stage);
   // TODO This is ugly API. Should manage transitions through an appropriate controller

   /**
    * @return Applies the requested changes.
    */
   Future<String> execute();
}
