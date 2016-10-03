package edu.tamu.tcat.trc.services.categorization.strategies.tree;

import java.util.List;

import edu.tamu.tcat.trc.services.categorization.CategorizationNode;

public interface TreeNode extends CategorizationNode
{
   /**
    * @return The id of the parent of the node.
    */
   String getParentId();

   /**
    * @return A list of nodes are have been associated to the node.
    */
   List<TreeNode> getChildren();
}
