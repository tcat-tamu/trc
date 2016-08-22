package edu.tamu.tcat.trc.categorization;

import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;

/**
 * Represents an individual entry within a categorization. The specific categorization
 * strategy will determine the concrete type of node and nodes will typically be referenced
 * using one of these concrete sub-types.
 *
 * <p>Note that, to avoid confusion with the associated TRC entries, these are referred to
 * as 'nodes' rather than 'entries'.
 */
public interface CategorizationNode
{

   // TODO add key/value properties
   // TODO add longer content (could be key-value)
   // TODO add account and audit history

   /**
    * @return The unique id of the node. Will be unique within the context of the
    *    categorization this node belongs to.
    */
   String getId();

   /**
    * @return A user-supplied label that succinctly describes this node.
    */
   String getLabel();

   /**
    * @return A description of this node
    */
   String getDescription();

   /**
    * @return The categorization this node belongs to.
    */
   CategorizationScheme getCategorization();

   /**
    * @return The TRC entity referenced by this node. May be <code>null</code>.
    */
   EntryReference getAssociatedEntryRef();

   /**
    * Resolves and returns the entry associated with this node. Note that if no
    *
    * @param type
    * @return
    */
   <X> X getAssociatedEntry(Class<X> type);
}
