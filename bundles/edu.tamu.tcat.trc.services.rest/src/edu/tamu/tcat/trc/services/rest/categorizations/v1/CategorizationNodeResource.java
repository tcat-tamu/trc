package edu.tamu.tcat.trc.services.rest.categorizations.v1;

import static java.text.MessageFormat.format;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.services.categorization.CategorizationNode;
import edu.tamu.tcat.trc.services.categorization.CategorizationNodeMutator;
import edu.tamu.tcat.trc.services.categorization.CategorizationRepo;
import edu.tamu.tcat.trc.services.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.services.categorization.EditCategorizationCommand;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.EditTreeCategorizationCommand;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.TreeCategorization;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.TreeNode;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.TreeNodeMutator;
import edu.tamu.tcat.trc.services.rest.categorizations.v1.RestApiV1.BasicNode;
import edu.tamu.tcat.trc.services.rest.internal.ApiUtils;

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

   protected abstract RestApiV1.BasicNode adapt(CategorizationNode node);

   /**
    * @return An instance of this entry.
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.BasicNode getNode()
   {
      return adapt(resolveNode());
   }

   @DELETE
   public Response remove(@QueryParam("remove_refs") @DefaultValue("false") boolean removeRefs)
   {
      CategorizationNode node = resolveNode();
      try
      {
         EditCategorizationCommand command = repo.edit(scheme.getId());
         command.removeNode(nodeId, removeRefs);
         command.execute().get(10, TimeUnit.SECONDS);

         return Response.noContent().build();
      }
      catch (InterruptedException | ExecutionException | TimeoutException e)
      {
         String template = "Failed to delete categorization node {0} [{1}]";
         String msg = format(template, node.getLabel(), node.getId());
         throw ApiUtils.raise(Response.Status.INTERNAL_SERVER_ERROR, msg, Level.SEVERE, e);
      }
   }

   /**
    * Designed to accept the basic
    * @param properties
    * @return
    */
   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.BasicTreeNode update(Map<String, Object> properties)
   {
      CategorizationNode original = resolveNode();    // throw exception if the node is undefined.

      try
      {
         EditTreeCategorizationCommand command = (EditTreeCategorizationCommand)repo.edit(scheme.getId());
         TreeNodeMutator mutator = command.editNode(nodeId);

         setIfDefined("label", properties, (String label) -> {
            label = label.isEmpty() ? "Unlabled" : label;
            mutator.setLabel(label);
         });
         setIfDefined("description", properties, (String desc) -> mutator.setDescription(desc));

         command.execute().get(10, TimeUnit.SECONDS);

         TreeNode node = (TreeNode)repo.getById(scheme.getId()).getNode(nodeId);
         return ModelAdapterV1.adapt(node);
      }
      catch (Exception e)
      {
         String template = "Failed to update the categorization node {0} [{1}]";
         String msg = format(template, original.getLabel(), nodeId);
         throw ApiUtils.raise(Response.Status.INTERNAL_SERVER_ERROR, msg, Level.SEVERE, e);
      }
   }

   @PUT
   @Path("entryRef")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.EntryReference associateEntry(RestApiV1.EntryReference entryRef)
   {
      CategorizationNode node = resolveNode();
      EntryReference ref = new EntryReference();
      ref.id = entryRef.id;
      ref.type = entryRef.type;
      // TODO might check to ensure validity?

      EditCategorizationCommand command = repo.edit(scheme.getId());
      CategorizationNodeMutator mutator = command.editNode(nodeId);

      mutator.associateEntryRef(ref);
      try
      {
         command.execute().get(10, TimeUnit.SECONDS);
         return entryRef;
      }
      catch (InterruptedException | ExecutionException | TimeoutException e)
      {
         String template = "Failed to update entry reference for categorization node {0} [{1}].";
         String msg = format(template, node.getLabel(), node.getId());
         throw ApiUtils.raise(Response.Status.INTERNAL_SERVER_ERROR, msg, Level.SEVERE, e);
      }
   }

   @GET
   @Path("entryRef")
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.EntryReference getEnrtyRef()
   {
      BasicNode dto = adapt(resolveNode());
      return dto.entryRef;
   }

   private <T> void setIfDefined(String param, Map<String, Object> data, Consumer<T> action)
   {
      if (!data.containsKey(param))
         return;

      // enforced by out-of-band knowledge in caller
      @SuppressWarnings("unchecked")
      T value = (T)data.get(param);

      action.accept(value);
   }

   /**
    * Updates submitted information to ensure proper creation of nodes.
    */
   protected void normalize(RestApiV1.BasicNode entry)
   {
      if (entry.label == null || entry.label.isEmpty())
         entry.label = UUID.randomUUID().toString();

      if (entry.description == null)
         entry.description = "";
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
            throw ApiUtils.raise(Response.Status.NOT_FOUND, format("Not Found"), Level.WARNING, iae);
         }
      }

      @Override
      protected RestApiV1.BasicNode adapt(CategorizationNode node)
      {
         if (!TreeNode.class.isInstance(node))
            throw ApiUtils.raise(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected node type while adapting", Level.SEVERE, null);

         return ModelAdapterV1.adapt((TreeNode)node);
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
            throw ApiUtils.raise(Response.Status.NOT_FOUND, format(template, node.getLabel(), node.getId()), null, null);
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
            throw ApiUtils.raise(Response.Status.CONFLICT, msg, Level.SEVERE, ex);
         }
      }

      /**
       * Override to ensure that the root node is not removed and to respond with
       * appropriate error message.
       */
      @DELETE
      @Override
      public Response remove(@QueryParam("remove_refs") @DefaultValue("false") boolean removeRefs)
      {
         String rootErrMsg = "The root node [{0}] cannot be deleted.";
         TreeNode node = resolveNode();
         if (node.getParentId() == null)
            ApiUtils.raise(Response.Status.BAD_REQUEST, format(rootErrMsg, nodeId), Level.WARNING, null);

         return super.remove(removeRefs);
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
                  throw ApiUtils.raise(Response.Status.INTERNAL_SERVER_ERROR, msg, Level.SEVERE, null);
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
         throw ApiUtils.raise(Response.Status.CONFLICT, msg, Level.WARNING, null);
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
            throw ApiUtils.raise(Response.Status.NOT_FOUND, format("Could not create new node. Failed to edit scheme [{0}]", scheme.getId()), Level.SEVERE, ex);
         }

         TreeNodeMutator mutator = command.editNode(nodeId);
         TreeNodeMutator childMutator = mutator.add(entry.label);
         childMutator.setDescription(entry.description);

         command.execute().get(10, TimeUnit.SECONDS);
      }
   }
}
