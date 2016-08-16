package edu.tamu.tcat.trc.categorization.rest;

import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.categorization.rest.RestApiV1.HierarchyEntry;
import edu.tamu.tcat.trc.categorization.strategies.tree.TreeCategorization;
import edu.tamu.tcat.trc.categorization.strategies.tree.TreeNode;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;

public class ModelAdapterV1
{
   public static RestApiV1.Categorization adapt(TreeCategorization scheme)
   {
      RestApiV1.Categorization dto = adaptBaseScheme(scheme);

      dto.type = HierarchicalCategorizationResource.TYPE;
      dto.entries = adapt(scheme.getRootNode());

      return dto;
   }

   private static RestApiV1.Categorization adaptBaseScheme(CategorizationScheme scheme)
   {
      RestApiV1.Categorization dto = new RestApiV1.Categorization();

      // TODO add additional metadata information
      dto.meta.id = scheme.getId();
      dto.key = scheme.getKey();
      dto.description = scheme.getDescription();

      return dto;
   }

   public static RestApiV1.HierarchyEntry adapt(TreeNode node)
   {
      HierarchyEntry dto = new RestApiV1.HierarchyEntry();

      dto.schemeId = node.getCategorization().getId();
      dto.id = node.getId();
      dto.label = node.getLabel();
      dto.description = node.getDescription();

      dto.articleReference = adapt(node.getAssociatedEntryRef());
      List<TreeNode> children = node.getChildren();
      dto.childIds = children.stream().map(TreeNode::getId).collect(Collectors.toList());
      dto.children = children.stream().map(ModelAdapterV1::adapt).collect(Collectors.toList());

      return dto;
   }

   private static RestApiV1.EntryReference adapt(EntryReference ref)
   {
      if (ref == null)
         return null;

      RestApiV1.EntryReference dto = new RestApiV1.EntryReference();
      dto.id = ref.id;
      dto.type = ref.type;
      dto.version = 0;

      return dto;
   }
}
