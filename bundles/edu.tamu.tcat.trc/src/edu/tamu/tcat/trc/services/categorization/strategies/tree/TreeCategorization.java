package edu.tamu.tcat.trc.services.categorization.strategies.tree;

import edu.tamu.tcat.trc.services.categorization.CategorizationScheme;

/**
 * Categorization strategy that supports hierarchical organization of entities.
 */
public interface TreeCategorization extends CategorizationScheme
{
   /**
    * @return The root node of the tree.
    */
   TreeNode getRootNode();

   @Override
   TreeNode getNode(String id) throws IllegalArgumentException;
}