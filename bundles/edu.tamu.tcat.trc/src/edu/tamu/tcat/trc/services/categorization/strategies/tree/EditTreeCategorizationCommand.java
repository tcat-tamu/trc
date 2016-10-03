package edu.tamu.tcat.trc.services.categorization.strategies.tree;

import edu.tamu.tcat.trc.services.categorization.EditCategorizationCommand;

public interface EditTreeCategorizationCommand extends EditCategorizationCommand
{

   /**
    * @param nodeId The id of the node to edit.
    * @return a {@link TreeNodeMutator} to use for modifying the indicated node.
    */
   @Override
   TreeNodeMutator editNode(String nodeId);

   /**
    * Removes the indicated node.
    *
    * @param nodeId The id of the node to be removed.
    * @param removeRefs Indicates whether associated entry references should be removed.
    *       Note that this will make a good-faith effort to remove associated entry
    *       references, but will not fail if those reference cannot be removed and provides
    *       no transactional guarantees about their removal.
    */
   @Override
   void removeNode(String nodeId, boolean removeRefs);

   /**
    * Removes the indicated node. Associated references will not be removed.
    *
    * @param nodeId The id of the node to be removed.
    */
   default void removeNode(String nodeId)
   {
      this.removeNode(nodeId, false);
   }

   /**
    * Moves the indicated node (and all descendants) to be a child of a new parent
    * at the indicated index.
    *
    * @param nodeId The id of the node to move.
    * @param newParentId The new parent for the node being moved.
    * @param index The index position within the list of children for the new node. If
    *       this is a negative value, the node will be appended to the end of the list.
    */
   void move(String nodeId, String newParentId, int index);
}