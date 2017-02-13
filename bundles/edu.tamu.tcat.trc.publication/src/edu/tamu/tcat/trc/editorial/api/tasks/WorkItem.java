package edu.tamu.tcat.trc.editorial.api.tasks;

import java.util.Set;

import edu.tamu.tcat.trc.editorial.api.workflow.WorkflowStage;
import edu.tamu.tcat.trc.resolver.EntryId;

/**
 * Represents a unit of work within an {@link EditorialTask}. For example, this might represent
 * a single bibliographic item to which digital copies should be assigned, or a book for which
 * a book review should be provided.
 *
 * <p>Each work items maintains an application-defined set of properties that can be used by
 * interfaces or applications to define custom properties that may be associated with an item.
 */
public interface WorkItem
{

   // TODO add assignee, due date, reporter, etc.
   // TODO track transition history
   // TODO notify monitors by email

   /**
    * @return The unique id of this item.
    */
   String getId();

   /**
    * @return The display label for this item.
    */
   String getLabel();

   /**
    * @return A description of the work to be done
    */
   String getDescription();

   /**
    * @return A set of application defined property keys.
    */
   Set<String> getPropertyKeys();

   /**
    * @param key The propery key to retrieve.
    * @return The value associated with the identified property.
    */
   String getProperty(String key);

   /**
    * @return The workflow stage of this item. Will not be <code>null</code>.
    */
   WorkflowStage getStage();

   /**
    * @return The id of the TRC Entry that this item relates to.
    */
   EntryId getEntryId();

}
