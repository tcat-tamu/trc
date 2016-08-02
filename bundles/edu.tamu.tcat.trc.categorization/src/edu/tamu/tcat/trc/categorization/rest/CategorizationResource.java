package edu.tamu.tcat.trc.categorization.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *  REST API for working with an individual categorization.
 *
 */
public class CategorizationResource
{

   private final String key;     // TODO should be constructed with a specific categorization

   public CategorizationResource(String key)
   {
      this.key = key;
   }

   /**
    * @return A representation of this categorization.
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Categorization getCategorization()
   {

      return null;
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
      // TODO find and delete categorization. If not present, ignore

      return Response.noContent().build();
   }

   /**
    * Updates to the core descriptive information about the categorization. Will not
    * affect entries associated with the categorization. While the entries field will
    * be ignored, if supplied, it is best practice not to submit it in this request
    * in order to minimize data transfer.
    *
    * <p>The meta information associated with the last time data for this categorization
    * was loaded from the server should be supplied and will be used for optimistic
    * locking. If the categorization has been changed since the last seen update, this
    * method will fail with a Conflict.
    *
    * @param updated The updated representation of the categorization.
    * @return The updated categorization.
    */
   @PUT     // TODO technically, this is a patch, not a put
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Categorization update(RestApiV1.Categorization updated)
   {
      return null;
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

   @Path("entries/{entry}")
   public CategorizationEntryResource getEntry(@PathParam("entry") String entryKey)
   {
      return null;
   }
}
