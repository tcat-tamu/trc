package edu.tamu.tcat.trc.categorization.strategies.tree;

import edu.tamu.tcat.trc.categorization.CategorizationScheme;

/**
 * Categorization strategy that supports hierarchical organization of entities.
 */
public interface TreeCategorization extends CategorizationScheme
{
   /**
    * @return The root node of the tree.
    */
   TreeNode getRootNode();
}