package edu.tamu.tcat.trc.categorization;

import edu.tamu.tcat.trc.entries.core.EntryReference;

/**
 *  Used to edit properties of a {@link CategorizationNode}. This will
 *  frequently be accessed through strategy specific sub-classes,
 *  particularly when structural changes are needed (e.g., reordering
 *  items in a list or tree).
 */
public interface CategorizationNodeMutator
{
   /**
    * @param label the label for this node.
    */
   void setLabel(String label);

   /**
    * @param description A description for this node.
    */
   void setDescription(String description);

   /**
    * @param ref The entity reference to be associated with this node.
    */
   void associateEntryRef(EntryReference ref);

}
