package edu.tamu.tcat.trc.categorization.strategies.tree;

import edu.tamu.tcat.trc.categorization.EditCategorizationCommand;

public interface EditTreeCategorizationCommand extends EditCategorizationCommand
{

   /**
    *
    * @param index
    * @param parentId
    * @return
    */
   TreeNodeMutator edit(String nodeId);

   /**
    *
    * @param index
    * @param parentId
    */
   void remove(String nodeId);

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