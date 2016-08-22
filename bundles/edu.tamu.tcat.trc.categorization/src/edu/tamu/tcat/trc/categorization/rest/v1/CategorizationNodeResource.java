package edu.tamu.tcat.trc.categorization.rest.v1;

import static java.text.MessageFormat.format;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.trc.categorization.CategorizationNode;
import edu.tamu.tcat.trc.categorization.CategorizationRepo;
import edu.tamu.tcat.trc.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.categorization.strategies.tree.EditTreeCategorizationCommand;
import edu.tamu.tcat.trc.categorization.strategies.tree.TreeCategorization;
import edu.tamu.tcat.trc.categorization.strategies.tree.TreeNode;
import edu.tamu.tcat.trc.categorization.strategies.tree.TreeNodeMutator;

/**
 *  Represents a single entry within a categorization.
 *
 */
public abstract class CategorizationNodeResource<SchemeType extends CategorizationScheme>
{
   protected final CategorizationRepo repo;
   protected final SchemeType scheme;
   protected final String nodeId;

   public CategorizationNodeResource(CategorizationRepo repo, SchemeType scheme, String nodeId)
   {
      this.repo = repo;
      this.scheme = scheme;
      this.nodeId = nodeId;
   }

   protected abstract CategorizationNode resolveNode();

   /**
    * @return An instance of this entry.
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public abstract RestApiV1.BasicNode getNode();

   public void remove() {

   }

   public void update() {

   }

   @PUT
   @Path("articleRef")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Categorization associateArticle(RestApiV1.EntryReference article)
   {
      return null;
   }

   @GET
   @Path("article")
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Categorization getArticle()
   {
      return null;
   }

   public static class TreeNodeResource extends CategorizationNodeResource<TreeCategorization>
   {
      public TreeNodeResource(CategorizationRepo repo, TreeCategorization scheme, String nodeId)
      {
         super(repo, scheme, nodeId);
      }

      @Override
      protected TreeNode resolveNode()
      {
         try
         {
            return scheme.getNode(nodeId);
         }
         catch (IllegalArgumentException iae)
         {
            throw ModelAdapterV1.raise(Response.Status.NOT_FOUND, format("Not Found"), Level.WARNING, iae);
         }
      }

      /**
       * @return An instance of this entry.
       */
      @Override
      @GET
      @Produces(MediaType.APPLICATION_JSON)
      public RestApiV1.BasicTreeNode getNode()
      {
         return ModelAdapterV1.adapt(resolveNode());
      }

      @GET
      @Path("parent")
      @Produces(MediaType.APPLICATION_JSON)
      public RestApiV1.BasicTreeNode getParent()
      {
         TreeNode node = resolveNode();
         if (node.getParentId() == null)
         {
            String template = "This node {0} [{1}] is the root node.";
            throw ModelAdapterV1.raise(Response.Status.NOT_FOUND, format(template, node.getLabel(), node.getId()), null, null);
         }

         TreeNode parent = scheme.getNode(node.getParentId());
         return ModelAdapterV1.adapt(parent);
      }

      @GET
      @Path("children")
      @Produces(MediaType.APPLICATION_JSON)
      public List<RestApiV1.BasicTreeNode> getChildren()
      {
         TreeNode node = resolveNode();
         return node.getChildren().stream()
               .map(ModelAdapterV1::adapt)
               .collect(Collectors.toList());
      }

      @POST
      @Path("children")
      @Consumes(MediaType.APPLICATION_JSON)
      @Produces(MediaType.APPLICATION_JSON)
      public RestApiV1.BasicTreeNode createChild(RestApiV1.BasicNode entry)
      {
         TreeNode parent = resolveNode();

         try
         {
            normalize(entry);
            preventDuplicateLabels(parent, entry);
            doCreateChild(parent, entry);

            return loadNewChild(entry.label);
         }
         catch (Exception ex)
         {
            String template = "Failed to create child node {0} for parent {1} [{2}]";
            String msg = format(template, entry.label, parent.getLabel(), parent.getId());
            throw ModelAdapterV1.raise(Response.Status.CONFLICT, msg, Level.SEVERE, ex);
         }
      }

      /**
       * Updates submitted information to ensure proper creation of nodes.
       */
      private void normalize(RestApiV1.BasicNode entry)
      {
         if (entry.label == null || entry.label.isEmpty())
            entry.label = UUID.randomUUID().toString();

         if (entry.description == null)
            entry.description = "";
      }

      private RestApiV1.BasicTreeNode loadNewChild(String label)
      {
         TreeCategorization updated = (TreeCategorization)repo.getById(scheme.getId());
         TreeNode node = updated.getNode(nodeId);
         TreeNode child = node.getChildren().parallelStream()
               .filter(n -> Objects.equals(n.getLabel(), label))
               .findFirst()
               .orElseThrow(() -> {
                  String template = "Failed to retrieve newly created child category {0} for node {1} [{2}].";
                  String msg = format(template, label, node.getLabel(), node.getId());
                  throw ModelAdapterV1.raise(Response.Status.INTERNAL_SERVER_ERROR, msg, Level.SEVERE, null);
                });
         return ModelAdapterV1.adapt(child);
      }

      private void preventDuplicateLabels(TreeNode node, RestApiV1.BasicNode entry)
      {
         // HACK in theory duplicates should be discouraged, but not disallowed. However, given
         //      the need to retrieve the created node by label rather than ID, we need to
         //      make an effort at uniqueness. This won't work in call cases (duplicate entry
         //      with conflicting updates) but should be adequate for most cases.

         boolean hasDuplicate = node.getChildren().stream()
                     .anyMatch(n -> Objects.equals(n.getLabel(), entry.label));
         if (!hasDuplicate)
            return;

         String template = "The node {0} [{1}] already has a child with label {2}. Please choose a new lable.";
         String msg = format(template, node.getLabel(), node.getId(), entry.label);
         throw ModelAdapterV1.raise(Response.Status.CONFLICT, msg, Level.SEVERE, null);
      }

      private void doCreateChild(TreeNode parent, RestApiV1.BasicNode entry) throws InterruptedException, ExecutionException, TimeoutException
      {
         EditTreeCategorizationCommand command;
         try
         {
            command = (EditTreeCategorizationCommand)repo.edit(scheme.getId());
         }
         catch (IllegalArgumentException ex)
         {
            // shouldn't happen
            throw ModelAdapterV1.raise(Response.Status.NOT_FOUND, "", Level.SEVERE, ex);
         }

         TreeNodeMutator mutator = command.edit(nodeId);
         TreeNodeMutator childMutator = mutator.add(entry.label);
         childMutator.setDescription(entry.description);

         command.execute().get(10, TimeUnit.SECONDS);
      }
   }
}
