package edu.tamu.tcat.trc.impl.psql.services.categorization;

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.impl.psql.services.categorization.PersistenceModelV1.TreeNode;
import edu.tamu.tcat.trc.repo.ChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.UpdateContext;
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

   public static EntryReference copy(EntryReference ref)
   {
      if (ref == null)
         return null;

      EntryReference dto = new EntryReference();
      dto.id = ref.id;
      dto.type = ref.type;

      return dto;
   }

   @Override
   public EditTreeCategorizationCommand create(String id, EditCommandFactory.UpdateStrategy<PersistenceModelV1.TreeCategorizationStrategy> strategy)
   {
      return new EditHierarchyCmdImpl(id, nodeIds, strategy);
   }

   @Override
   public EditTreeCategorizationCommand edit(String id, EditCommandFactory.UpdateStrategy<PersistenceModelV1.TreeCategorizationStrategy> strategy)
   {
      return new EditHierarchyCmdImpl(id, nodeIds, strategy);
   }

   private static PersistenceModelV1.TreeNode copy(PersistenceModelV1.TreeNode original)
   {
      PersistenceModelV1.TreeNode node = new PersistenceModelV1.TreeNode();
      node.id = original.id;
      node.label = original.label;
      node.description = original.description;
      node.ref = copy(original.ref);


      node.parentId = original.parentId;
      node.children = new ArrayList<>(original.children);

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

   public class EditHierarchyCmdImpl
         extends BaseEditCommand<PersistenceModelV1.TreeCategorizationStrategy>
         implements EditTreeCategorizationCommand
   {

      public EditHierarchyCmdImpl(String id, IdFactory nodeIds, EditCommandFactory.UpdateStrategy<PersistenceModelV1.TreeCategorizationStrategy> context)
      {
         super(id, nodeIds, context);
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
         String pattern = "Failed to delete node {0}. This node's parent could not be found.";
         changes.add("delete child#" + nodeId, dto -> {
            // TODO find all child nodes, collect non-null references and remove them, ignoring errors

            Map<String, TreeNode> nodes = dto.nodes;

            PersistenceModelV1.TreeNode toRemove = nodes.get(nodeId);
            Objects.requireNonNull(toRemove.parentId, "Cannot delete the root node of a TreeCategorization");

            PersistenceModelV1.TreeNode parent = nodes.get(toRemove.parentId);
            Objects.requireNonNull(parent, () -> format(pattern, nodeId));

            Consumer<String> remover = makeRemoveFn(dto, removeRef);
            if (parent.children.remove(nodeId))
            {
               getDescendents(nodes, nodeId).forEach(remover::accept);
               remover.accept(nodeId);
            } // TODO otherwise, something has gone horribly wrong
         });
      }

      @Override
      protected final PersistenceModelV1.TreeCategorizationStrategy prepareData(
                  UpdateContext<PersistenceModelV1.TreeCategorizationStrategy> data)
      {
         PersistenceModelV1.TreeCategorizationStrategy original = data.getOriginal();
         return (original != null) ? copy(original) : create();
      }

      private Consumer<String> makeRemoveFn(PersistenceModelV1.TreeCategorizationStrategy dto, boolean removeRef)
      {
         return nodeId -> {
            PersistenceModelV1.TreeNode removed = dto.nodes.remove(nodeId);
            if (removeRef && removed.ref != null)
            {
               try
               {
                  // NOTE calling #get on the returned Future may deadlock
                  //      would be nice to know if this fails and log that, because that is bad.
                  Account account = scope.getAccount();
                  resolvers.getResolver(removed.ref)
                           .remove(account, removed.ref);
               }
               catch (Exception ex)
               {
                  String errTemplate = "Failed to remove associated reference for {0} [{1}]:\n{2}";
                  logger.log(Level.WARNING, format(errTemplate, dto.title, dto.id, removed.ref), ex);
               }
            }
         };
      }

      private PersistenceModelV1.TreeCategorizationStrategy create()
      {
         PersistenceModelV1.TreeCategorizationStrategy dto = new PersistenceModelV1.TreeCategorizationStrategy();
         dto.id = this.categorizationId;
         dto.strategy = CategorizationScheme.Strategy.TREE.name();

         PersistenceModelV1.TreeNode root = new PersistenceModelV1.TreeNode();
         root.id = this.categorizationId;
         root.parentId = null;
         root.label = "Root Node";
         root.description = "Root Node for " + dto.id;

         dto.nodes.put(root.id, root);
         dto.root = root.id;

         return dto;
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
