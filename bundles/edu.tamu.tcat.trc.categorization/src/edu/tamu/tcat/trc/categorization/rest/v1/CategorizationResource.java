package edu.tamu.tcat.trc.categorization.rest.v1;

import static java.text.MessageFormat.format;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.categorization.CategorizationRepo;
import edu.tamu.tcat.trc.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.categorization.CategorizationScope;
import edu.tamu.tcat.trc.categorization.EditCategorizationCommand;
import edu.tamu.tcat.trc.categorization.rest.v1.CategorizationNodeResource.TreeNodeResource;
import edu.tamu.tcat.trc.categorization.strategies.tree.TreeCategorization;

/**
 *  REST API for working with a categorization scheme.
 */
public abstract class CategorizationResource
{

   // TODO Rename SchemeResrouce

   protected final CategorizationRepo repo;
   protected final CategorizationScheme scheme;

   public CategorizationResource(CategorizationRepo repo, CategorizationScheme scheme)
   {
      this.repo = repo;
      this.scheme = scheme;
   }

   /**
    * @return A representation of this categorization.
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Categorization getCategorization()
   {
      switch (scheme.getType())
      {
         case TREE:
            return ModelAdapterV1.adapt((TreeCategorization)scheme);
         default:
            // TODO add message content
            throw new InternalServerErrorException();
      }
   }

   /**
    * Removes this categorization. Removal of categorizations that do not exist will be
    * considered successful.
    *
    * <p>Note that categorizations may be retained internally for historical purposes
    * and may be returned for some queries but will not be available unless deleted
    * categorizations are specifically requested. through the the main GET methods.
    *
    * @return NoContent on success.
    */
   @DELETE
   public Response removeCategorization()
   {
      // TODO note that this might be OK if the scheme doesn't exist, but will have returned 404 already.
      // NOTE once authorization is added, this will block permission errors.
      try
      {
         repo.remove(scheme.getId()).get(10, TimeUnit.SECONDS);
         return Response.noContent().build();
      }
      catch (ExecutionException e)
      {
         String errMsg = "Failed to delete category {0} from scope {1} for user {2}";
         throw handleExecutionException(errMsg, e);
      }
      catch (InterruptedException | TimeoutException e)
      {
         throw handleExecTimeout(e);
      }
   }

   /**
    * Updates to the core descriptive information about the categorization. Will not
    * affect entries associated with the categorization.
    *
    * <p>Supplied data must match the {@link RestApiV1.CategorizationDesc} data format.
    * If supplied, the type field must match the type of this categorization scheme. All
    * other fields will be updated if and only if they are supplied. Null values (where
    * appropriate will be set to empty strings.
    *
    * @param updated The updated representation of the categorization.
    * @return The updated categorization.
    */
   @PUT     // technically, this is a patch, not a put
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Categorization update(Map<String, String> fields)
   {
      try
      {
         // TODO should throw 404 if not found .. can we do that?
         String id = updateScheme(fields);
         return ModelAdapterV1.adapt((TreeCategorization)repo.getById(id));
      }
      catch (ExecutionException e)
      {
         String errMsg = "Failed to update category {0} from scope {1} for user {2}";
         throw handleExecutionException(errMsg, e);
      }
      catch (InterruptedException | TimeoutException e)
      {
         throw handleExecTimeout(e);
      }
   }

   @Path("nodes/{id}")
   public abstract CategorizationNodeResource<?> getNode(@PathParam("id") String nodeId);

   private WebApplicationException handleExecutionException(String errMsg, ExecutionException e) throws Error
   {
      Throwable cause = e.getCause();
      if (Error.class.isInstance(cause))
         throw (Error)cause;

      String id = scheme.getId();

      CategorizationScope scope = repo.getScope();
      Account account = scope.getAccount();
      String uname = account == null ? "anonymous" : account.getDisplayName();
      String formatedMsg = format(errMsg, id, scope.getScopeId(), uname);

      return ApiUtils.raise(Response.Status.INTERNAL_SERVER_ERROR, formatedMsg, Level.SEVERE, (Exception)e.getCause());
   }

   private WebApplicationException handleExecTimeout(Exception e)
   {
      String msg = "We are currently experiencing heavy load and unable to complete your request in a timely manner.";
      return ApiUtils.raise(Response.Status.SERVICE_UNAVAILABLE, msg, Level.SEVERE, e);
   }

   private String updateScheme(Map<String, String> fields) throws InterruptedException, ExecutionException, TimeoutException
   {
      EditCategorizationCommand command = repo.edit(scheme.getId());

      setIfDefined("key", fields, key -> {
         checkKeyNotEmpty(key);
         ApiUtils.checkUniqueKey(repo, key);
         command.setKey(key);
      });
      setIfDefined("label", fields, label -> {
         label = label.isEmpty() ? "Unlabled" : label;
         command.setLabel(label);
      });
      setIfDefined("description", fields, desc -> command.setDescription(desc));

      String id = command.execute().get(10, TimeUnit.SECONDS);
      return id;
   }

   private void checkKeyNotEmpty(String key)
   {
      if (!key.isEmpty())
         return;

      String msg = "Cannot set empty value for categorization scheme key.";
      throw ApiUtils.raise(Response.Status.BAD_REQUEST, msg, Level.WARNING, null);

   }

   private void setIfDefined(String param, Map<String, String> data, Consumer<String> action)
   {
      if (!data.containsKey(param))
         return;

      String value = data.get(param);
      if (value == null)
         value = "";

      action.accept(value);
   }

   public static class TreeCategorizationResource extends CategorizationResource
   {

      /**
       * The type identifier for hierarchical categorizations.
       */
      public static final String TYPE = "hierarchical";

      public TreeCategorizationResource(CategorizationRepo repo, TreeCategorization scheme)
      {
         super(repo, scheme);
      }

      /**
       * Moves nodes from one position within the entry structure to another.
       *
       * This method is not supported for SetCategorizations. For ListCategorizations,
       * information about the parent entry will be ignored if present.
       *
       * @param updates A list of entry movements to be applied in order.
       * @return
       */
      @POST
      @Consumes(MediaType.APPLICATION_JSON)
      @Produces(MediaType.APPLICATION_JSON)
      public Response moveEntries(List<RestApiV1.MoveEntry> updates)
      {
         return null;
      }

      @Override
      public CategorizationNodeResource<?> getNode(@PathParam("id") String nodeId)
      {
         return new TreeNodeResource(repo, (TreeCategorization)scheme, nodeId);
      }
   }

}
