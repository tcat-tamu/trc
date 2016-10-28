package edu.tamu.tcat.trc.services.rest.categorizations.v1;

import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.services.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.TreeCategorization;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.TreeNode;
import edu.tamu.tcat.trc.services.rest.categorizations.v1.CategorizationResource.TreeCategorizationResource;
import edu.tamu.tcat.trc.services.rest.categorizations.v1.RestApiV1.BasicTreeNode;

public class ModelAdapterV1
{
   public static RestApiV1.Categorization adapt(TreeCategorization scheme)
   {
      RestApiV1.Categorization dto = adaptBaseScheme(scheme);

      dto.type = TreeCategorizationResource.TYPE;
      dto.root = adapt(scheme.getRootNode());

      return dto;
   }

   private static RestApiV1.Categorization adaptBaseScheme(CategorizationScheme scheme)
   {
      RestApiV1.Categorization dto = new RestApiV1.Categorization();

      // TODO add additional metadata information
      dto.meta.id = scheme.getId();
      dto.key = scheme.getKey();
      dto.label = scheme.getLabel();
      dto.description = scheme.getDescription();

      return dto;
   }

   public static RestApiV1.BasicTreeNode adapt(TreeNode node)
   {
      BasicTreeNode dto = new RestApiV1.BasicTreeNode();

      dto.schemeId = node.getCategorization().getId();
      dto.id = node.getId();
      dto.label = node.getLabel();
      dto.description = node.getDescription();

      dto.entryRef = adapt(node.getAssociatedEntryRef());
      List<TreeNode> children = node.getChildren();
      dto.childIds = children.stream().map(TreeNode::getId).collect(Collectors.toList());
      dto.children = children.stream().map(ModelAdapterV1::adapt).collect(Collectors.toList());

      return dto;
   }

   private static RestApiV1.EntryReference adapt(EntryId ref)
   {
      if (ref == null)
         return null;

      RestApiV1.EntryReference dto = new RestApiV1.EntryReference();
      dto.id = ref.getId();
      dto.type = ref.getType();
      dto.version = 0;

      return dto;
   }
}
