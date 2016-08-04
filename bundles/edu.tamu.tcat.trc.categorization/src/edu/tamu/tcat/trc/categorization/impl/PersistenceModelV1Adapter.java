package edu.tamu.tcat.trc.categorization.impl;

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.categorization.CategorizationNode;
import edu.tamu.tcat.trc.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.categorization.strategies.tree.TreeCategorization;
import edu.tamu.tcat.trc.categorization.strategies.tree.TreeNode;
import edu.tamu.tcat.trc.entries.core.EntryReference;
import edu.tamu.tcat.trc.entries.core.EntryResolverRegistry;
import edu.tamu.tcat.trc.entries.core.InvalidReferenceException;

public abstract class PersistenceModelV1Adapter
{
   public static EntryReference copy(EntryReference ref)
   {
      if (ref == null)
         return null;

      EntryReference dto = new EntryReference();
      dto.id = ref.id;
      dto.type = ref.type;
      dto.version = ref.version;

      return dto;
   }

   public static TreeCategorization toDomainModel(EntryResolverRegistry registry,
                                                  PersistenceModelV1.TreeCategorizationStrategy dto)
   {
      return new TreeCategorizationImpl(dto, registry);
   }

   public static class CategorizationImpl implements CategorizationScheme
   {
      private final String id;
      private final String scopeId;
      private final String key;
      private final Strategy strategy;
      private final String title;
      private final String description;

      public CategorizationImpl(PersistenceModelV1.CategorizationScheme scheme)
      {
         this.id = scheme.id;
         this.scopeId = scheme.scopeId;
         this.key = scheme.key;
         this.strategy = CategorizationScheme.Strategy.valueOf(scheme.strategy);

         this.title = scheme.title;
         this.description = scheme.description;
      }

      @Override
      public final String getId()
      {
         return id;
      }

      @Override
      public final String getScopeId()
      {
         return scopeId;
      }

      @Override
      public final String getKey()
      {
         return key;
      }

      @Override
      public final Strategy getType()
      {
         return strategy;
      }

      @Override
      public final String getLabel()
      {
         return title;
      }

      @Override
      public final String getDescription()
      {
         return description;
      }
   }

   public static class CategorizationNodeImpl implements CategorizationNode
   {
      protected final String id;
      protected final String label;
      protected final String description;
      protected final EntryReference ref;

      protected final CategorizationScheme scheme;
      protected final EntryResolverRegistry registry;

      public CategorizationNodeImpl(EntryResolverRegistry registry,
                                    CategorizationScheme scheme,
                                    PersistenceModelV1.CategorizationNode dto)
      {
         // FIXME should be registry, not resolver
         this.registry = registry;
         this.scheme = scheme;

         this.id = dto.id;
         this.label = dto.label;
         this.description = dto.description;
         this.ref = dto.ref;
      }

      @Override
      public final String getId()
      {
         return id;
      }

      @Override
      public final String getLabel()
      {
         return label;
      }

      @Override
      public final String getDescription()
      {
         return description;
      }

      @Override
      public final CategorizationScheme getCategorization()
      {
         return scheme;
      }

      @Override
      public final EntryReference getEntityRef()
      {
         return ref;
      }

      @Override
      public final <X> X getEntity(Class<X> type)
      {
         if (ref == null)
            return null;

         Object entry = registry.getResolver(ref).resolve(ref);
         if (!type.isInstance(entry))
            throw new InvalidReferenceException(ref,
                  format("The referenced entry is not an instance the expected type {0}", type));

         return type.cast(entry);
      }
   }

   public static class TreeCategorizationImpl extends CategorizationImpl implements TreeCategorization
   {

      private final String rootId;
      private final Map<String, TreeNodeImpl> nodeMap;

      public TreeCategorizationImpl(PersistenceModelV1.TreeCategorizationStrategy dto, EntryResolverRegistry registry)
      {
         super(dto);

         nodeMap = dto.nodes.values().stream()
               .map(node -> new TreeNodeImpl(registry, this, node))
               .collect(Collectors.toMap(n -> n.getId(),
                        Function.identity()));
         rootId = dto.root;
      }

      @Override
      public TreeNode getRootNode()
      {
         return nodeMap.get(rootId);
      }
   }

   public static class TreeNodeImpl extends CategorizationNodeImpl implements TreeNode
   {
      private final String parentId;
      private final ArrayList<String> children;

      public TreeNodeImpl(EntryResolverRegistry registry,
                          TreeCategorizationImpl scheme,
                          PersistenceModelV1.TreeNode dto)
      {
         super(registry, scheme, dto);

         this.parentId = dto.parentId;
         this.children = new ArrayList<>(dto.children);
      }

      @Override
      public String getParentId()
      {
         return parentId;
      }

      @Override
      public List<TreeNode> getChildren()
      {
         return children.stream()
               .map(this::getNode)
               .collect(Collectors.toList());
      }

      private TreeNode getNode(String id)
      {
         if (!children.contains(id))
            throw new IllegalArgumentException(format("The requested node {0} is not a child of {1}.", id, this.id));

         TreeNodeImpl node = ((TreeCategorizationImpl)scheme).nodeMap.get(id);
         if (node == null)
            throw new IllegalArgumentException(format("Cannot find node {0}.", id));

         return node;
      }
   }
}
