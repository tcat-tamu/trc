package edu.tamu.tcat.trc.categorization.rest;

import static java.text.MessageFormat.format;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.tamu.tcat.trc.categorization.CategorizationRepo;
import edu.tamu.tcat.trc.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.categorization.CategorizationScope;
import edu.tamu.tcat.trc.categorization.EditCategorizationCommand;
import edu.tamu.tcat.trc.categorization.rest.CategorizationNodeResource.TreeNodeResource;
import edu.tamu.tcat.trc.categorization.strategies.tree.TreeCategorization;

/**
 *  REST API for working with a categorization scheme.
 */
public abstract class CategorizationResource
{
   private final static Logger logger = Logger.getLogger(CategorizationResource.class.getName());

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
      catch (Exception e)
      {
         String errMsg = "Failed to delete category {0} from scope {1} for user {2}";
         String id = scheme.getId();
         CategorizationScope scope = repo.getScope();

         String formatedMsg = format(errMsg, id, scope.getScopeId(), scope.getAccount().getDisplayName());
         throw ModelAdapterV1.raise(Status.INTERNAL_SERVER_ERROR, formatedMsg, Level.SEVERE, e);
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
      // TODO should throw 404 if not found .. can we do that?
      EditCategorizationCommand command = repo.edit(scheme.getId());

      setIfDefined("key", fields, key -> {
         checkKeyNotEmpty(key);
         command.setKey(key);
      });
      setIfDefined("label", fields, label -> {
         label = label.isEmpty() ? "Unlabled" : label;
         command.setLabel(label);
      });
      setIfDefined("description", fields, desc -> command.setDescription(desc));

      return null;
   }

   private void checkKeyNotEmpty(String key)
   {
      if (!key.isEmpty())
         return;

      String msg = "Cannot set empty value for categorization scheme key.";
      throw ModelAdapterV1.raise(Response.Status.BAD_REQUEST, msg, Level.WARNING, null);

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

   @Path("nodes/{id}")
   public abstract CategorizationNodeResource getNode(@PathParam("id") String nodeId);

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
