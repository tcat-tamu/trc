package edu.tamu.tcat.trc.impl.psql.services.categorization.repo;

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.impl.psql.services.categorization.repo.PersistenceModelV1.TreeCategorizationStrategy;
import edu.tamu.tcat.trc.impl.psql.services.categorization.repo.PersistenceModelV1.TreeNode;
import edu.tamu.tcat.trc.repo.ChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.ExecutableUpdateContext;
import edu.tamu.tcat.trc.repo.id.IdFactory;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolver;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.services.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.EditTreeCategorizationCommand;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.TreeNodeMutator;

public class EditHeirarchyCommandFactory
      implements EditCommandFactory<PersistenceModelV1.TreeCategorizationStrategy, EditTreeCategorizationCommand>
{

   private final static Logger logger = Logger.getLogger(EditHeirarchyCommandFactory.class.getName());

   // TODO need more consistent change labels.
   private final IdFactory nodeIds;
   private final EntryResolverRegistry resolvers;

   public EditHeirarchyCommandFactory(EntryResolverRegistry resolvers, IdFactory idFactory)
   {
      this.resolvers = resolvers;
      this.nodeIds = idFactory;
   }

   public static EntryId copy(EntryId ref)
   {
      return (ref != null) ? new EntryId(ref.getId(), ref.getType()) : null;
   }

   @Override
   public EditTreeCategorizationCommand create(ExecutableUpdateContext<PersistenceModelV1.TreeCategorizationStrategy> ctx)
   {
      return new EditHierarchyCmdImpl(nodeIds, ctx);
   }

   @Override
   public PersistenceModelV1.TreeCategorizationStrategy initialize(String id, Optional<PersistenceModelV1.TreeCategorizationStrategy> original)
   {
      return original.map(EditHeirarchyCommandFactory::copy)
               .orElseGet(() -> create(id));
   }

   private static PersistenceModelV1.TreeNode copy(PersistenceModelV1.TreeNode original)
   {
      PersistenceModelV1.TreeNode node = new PersistenceModelV1.TreeNode();
      node.id = original.id;
      node.label = original.label;
      node.description = original.description;
      node.parentId = original.parentId;

      if (original.ref != null)
      {
         node.ref = new HashMap<>(original.ref);
      }

      if (original.children != null)
      {
         node.children = new ArrayList<>(original.children);
      }

      return node;
   }

   private static PersistenceModelV1.TreeCategorizationStrategy copy(PersistenceModelV1.TreeCategorizationStrategy original)
   {
      PersistenceModelV1.TreeCategorizationStrategy dto = new PersistenceModelV1.TreeCategorizationStrategy();
      dto.id = original.id;
      dto.scopeId = original.scopeId;
      dto.key = original.key;
      dto.strategy = original.strategy;     // this had better be TREE

      dto.title = original.title;
      dto.description = original.description;

      dto.root = original.root;
      dto.nodes = original.nodes.keySet().stream()
            .collect(Collectors.toMap(
                  Function.identity(),
                  id -> copy(original.nodes.get(id))));
      return dto;
   }

   private PersistenceModelV1.TreeCategorizationStrategy create(String id)
   {
      PersistenceModelV1.TreeCategorizationStrategy dto = new PersistenceModelV1.TreeCategorizationStrategy();
      dto.id = id;
      dto.strategy = CategorizationScheme.Strategy.TREE.name();

      PersistenceModelV1.TreeNode root = new PersistenceModelV1.TreeNode();
      root.id = id;
      root.parentId = null;
      root.label = "Root Node";
      root.description = "Root Node for " + dto.id;

      dto.nodes.put(root.id, root);
      dto.root = root.id;

      return dto;
   }

   public class EditHierarchyCmdImpl
         extends BaseEditCommand<PersistenceModelV1.TreeCategorizationStrategy>
         implements EditTreeCategorizationCommand
   {

      public EditHierarchyCmdImpl(IdFactory nodeIds, ExecutableUpdateContext<PersistenceModelV1.TreeCategorizationStrategy> ctx)
      {
         super(nodeIds, ctx);
      }

      @Override
      public TreeNodeMutator editNode(String nodeId)
      {
         ChangeSet<PersistenceModelV1.TreeNode> changeSet = changes.partial("node@" + nodeId, selectNode(nodeId));
         return new TreeNodeMutatorImpl(nodeId, this, changeSet);
      }

      @Override
      public void move(String nodeId, String parentId, int index)
      {
         // TODO this API is ugly and brittle to conflicting structural changes.
         //      Would be better to 'moveBefore' or 'moveUnder' or else
         changes.add("move node#" + nodeId, dto -> {
            PersistenceModelV1.TreeNode child = dto.nodes.get(nodeId);
            PersistenceModelV1.TreeNode oldParent = dto.nodes.get(child.parentId);
            PersistenceModelV1.TreeNode newParent = dto.nodes.get(parentId);

            // sanity checks
            Objects.requireNonNull(child, format("Cannot move node {0}: No such node.", nodeId));
            if (getDescendents(dto.nodes, nodeId).anyMatch(id -> parentId.equals(id)))
            {
               String msg = "Cannot move node {0} to be a child of its descendant node {1}";
               throw new IllegalStateException(format(msg, nodeId, parentId));
            }

            oldParent.children.remove(child.id);
            if (index < 0 || index >= newParent.children.size())
               newParent.children.add(nodeId);
            else
               newParent.children.add(index, nodeId);

            child.parentId = parentId;
         });
      }

      @Override
      public void removeNode(String nodeId, boolean removeRef)
      {
         changes.add("delete child#" + nodeId, dto -> {
            doRemoveNode(dto, nodeId, removeRef);
         });
      }

      private void doRemoveNode(TreeCategorizationStrategy dto, String nodeId, boolean removeRef)
      {
         String childNotFoundErr = "Failed to delete node {0}. Not found as child of parent {1}";
         String rootNodeErr = "Cannot delete the root node of a TreeCategorization. id: {0}";
         String noParentErr = "Failed to delete node {0}. This node's parent could not be found.";

         Map<String, TreeNode> nodes = dto.nodes;

         PersistenceModelV1.TreeNode toRemove = nodes.get(nodeId);
         Objects.requireNonNull(toRemove.parentId, () -> format(rootNodeErr, nodeId));

         PersistenceModelV1.TreeNode parent = nodes.get(toRemove.parentId);
         Objects.requireNonNull(parent, () -> format(noParentErr, nodeId));

         Consumer<String> remover = makeRemoveFn(dto, removeRef);
         if (parent.children.remove(nodeId))
         {
            getDescendents(nodes, nodeId).forEach(remover::accept);
            remover.accept(nodeId);
         }
         else
         {
            // log the fact that we are missing the node that we're trying to delete.
            // this is likley a consistency error that results from multiple simultaneous
            // edits, with the end result being that the node is no longer here.

            // TODO need a way to record warnings and errors in progress.
            logger.log(Level.INFO, () -> format(childNotFoundErr, nodeId, parent.id));
         }
      }

      /**
       *
       * @param dto
       * @param removeRef Indicates whether the assoicated reference should be removed.
       * @return
       */
      private Consumer<String> makeRemoveFn(PersistenceModelV1.TreeCategorizationStrategy dto, boolean removeRef)
      {
         return nodeId -> {
            PersistenceModelV1.TreeNode removed = dto.nodes.remove(nodeId);
            if (removeRef && removed.ref != null)
               removeReferencedEntry(removed, dto);
         };
      }

      /**
       * Removes the entry associated with a deleted tree node.
       * @param removed The node that was removed.
       * @param dto For logging purposes
       */
      private void removeReferencedEntry(PersistenceModelV1.TreeNode removed,
                                         PersistenceModelV1.TreeCategorizationStrategy dto)
      {
         String noResolverErr = "Failed to remove associated reference for node {0} [{1}]. No resolver found:\n{2}";
         String unknownErr = "Failed to remove associated reference for node {0} [{1}]:\n{2}";
         try
         {
            Account account = svcContext.getAccount().orElse(null);
            EntryResolver<Object> resolver = resolvers.getResolver(removed.ref);
            if (resolver == null)
            {
               logger.log(Level.WARNING, format(noResolverErr, removed.label, removed.id, removed.ref));
               return;
            }

            // NOTE calling #get on the Future returned by resolversetResolvermay deadlock
            //      would be nice to capture this information for logging.
            resolver.remove(account, EntryId.fromMap(removed.ref));
         }
         catch (Exception ex)
         {
            logger.log(Level.WARNING, format(unknownErr, removed.label, removed.id, removed.ref), ex);
         }
      }

      /**
       * @param nodes
       * @param rootId The id of root node of the subtree to return.
       * @return the ids of all descendant nodes of the root, excluding the root.
       */
      private Stream<String> getDescendents(Map<String, PersistenceModelV1.TreeNode> nodes, String rootId)
      {
         TreeNode root = nodes.get(rootId);
         if (root == null || root.children.isEmpty())
            return Stream.empty();

         return root.children.stream()
                    .flatMap(childId -> Stream.concat(
                          Stream.of(childId),
                          getDescendents(nodes, childId)));
      }

      private TreeNodeMutator createChild(String parentId, int index)
      {
         String ERR_PARENT_NOT_FOUND = "Failed to create node. Could not find parent node {0}.";
         String id = nodeIds.get();
         changes.add("new child#" + id, dto -> {

            PersistenceModelV1.TreeNode parent = dto.nodes.get(parentId);
            Objects.requireNonNull(parent, format(ERR_PARENT_NOT_FOUND, parentId));

            PersistenceModelV1.TreeNode node = new PersistenceModelV1.TreeNode();

            node.id = id;
            node.parentId = parentId;
            dto.nodes.put(id, node);

            if (index < 0 || index >= parent.children.size())
               parent.children.add(id);
            else
               parent.children.add(index, id);
         });

         ChangeSet<PersistenceModelV1.TreeNode> changeSet = changes.partial("create child id: " + id, selectNode(id));
         return new TreeNodeMutatorImpl(id, this, changeSet);
      }

      private Function<PersistenceModelV1.TreeCategorizationStrategy, PersistenceModelV1.TreeNode> selectNode(String id)
      {
         return (dto) -> dto.nodes.get(id);  // NOTE potential NPE in the event of a null return value.
      }
   }

   private static class TreeNodeMutatorImpl extends BaseCategorizationNodeMutator implements TreeNodeMutator
   {
      // TODO should we ensure child nodes actually belongs to the expected parent?

      private final EditHierarchyCmdImpl cmd;

      public TreeNodeMutatorImpl(String id, EditHierarchyCmdImpl cmd, ChangeSet<PersistenceModelV1.TreeNode> changes)
      {
         super(id, changes);

         this.cmd = cmd;
      }

      @Override
      public TreeNodeMutator add(String label)
      {
         TreeNodeMutator mutator = cmd.createChild(this.id, -1);
         mutator.setLabel(label);

         return mutator;
      }

      @Override
      public TreeNodeMutator insert(String label, int index)
      {
         TreeNodeMutator mutator = cmd.createChild(this.id, index);
         mutator.setLabel(label);

         return mutator;
      }

      @Override
      public TreeNodeMutator edit(String id)
      {
         return cmd.editNode(id);
      }

      @Override
      public void removeChild(String id)
      {
         cmd.removeNode(id);
      }
   }
}
