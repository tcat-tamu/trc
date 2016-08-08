package edu.tamu.tcat.trc.categorization.strategies.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Performs a pre-order traversal of a {@link TreeCategorization} returning all nodes
 * that match a given predicate (or all nodes, if no predicate is supplied).
 */
public class PreOrderTraversal implements Function<TreeCategorization, List<TreeNode>>
{
   private final Predicate<TreeNode> predicate;

   /**
    * @param predicate The predicate used to test nodes for inclusion in the result set.
    *    If <code>null</code>, all nodes will be included.
    */
   public PreOrderTraversal(Predicate<TreeNode> predicate)
   {
      this.predicate = predicate != null ? predicate : (n) -> true;
   }

   @Override
   public List<TreeNode> apply(TreeCategorization scheme)
   {
      List<TreeNode> results = new ArrayList<>();
      visit(scheme.getRootNode(), results);
      return results;
   }

   private void visit(TreeNode node, List<TreeNode> accumulator)
   {
      if (predicate.test(node))
         accumulator.add(node);

      node.getChildren().forEach(child -> visit(child, accumulator));
   }

}