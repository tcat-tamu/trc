package edu.tamu.tcat.trc.services.rest.seealso.v1;

import static java.text.MessageFormat.format;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.resolver.InvalidReferenceException;
import edu.tamu.tcat.trc.services.TrcServiceManager;
import edu.tamu.tcat.trc.services.rest.ApiUtils;
import edu.tamu.tcat.trc.services.seealso.Link;
import edu.tamu.tcat.trc.services.seealso.SeeAlsoService;

/**
 *  Intended as a sub-resource for use in accessing SeeAlso links for a given entry.
 *
 *
 *  This is intended that can be instantiated from some other primary object
 *  and used to see also references.
 */
public class SeeAlsoEntryResource
{
   private final EntryReference<Object> sourceRef;
   private final EntryResolverRegistry resolvers;
   private final TrcServiceManager svcManager;
   private final ModelAdapter adapter;

   public SeeAlsoEntryResource(EntryId eId, EntryResolverRegistry resolvers, TrcServiceManager svcManager)
   {
      // TODO replace with TrcApplication
      this.resolvers = resolvers;
      this.svcManager = svcManager;
      this.adapter = new ModelAdapter(resolvers);

      try
      {
         sourceRef = resolvers.getReference(eId);
      }
      catch (InvalidReferenceException ex)
      {
         String msg = format("Invalid entry id {0} [{1}]", eId.getId(), eId.getType());
         throw ApiUtils.raise(BAD_REQUEST, msg, Level.INFO, ex);
      }
   }

   public SeeAlsoEntryResource(EntryId eId, TrcApplication appMgr)
   {
      this.resolvers = appMgr.getResolverRegistry();
      this.svcManager = appMgr.getServiceManager();
      this.adapter = new ModelAdapter(resolvers);

      try
      {
         sourceRef = resolvers.getReference(eId);
      }
      catch (InvalidReferenceException ex)
      {
         String msg = format("Invalid entry id {0} [{1}]", eId.getId(), eId.getType());
         throw ApiUtils.raise(BAD_REQUEST, msg, Level.INFO, ex);
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.SeeAlso getRelatedEntries()
   {
      try
      {
         SeeAlsoService svc = svcManager.getService(SeeAlsoService.makeContext(null));

         Collection<Link> links = svc.getFor(sourceRef.getToken());
         return adapter.adapt(sourceRef.getEntryId(), links);
      }
      catch (Exception ex)
      {
         String msg = format("Failed to retrieve related entries for {0} [{1}]",
               getLabel(sourceRef), sourceRef.getToken());
         throw ApiUtils.raise(INTERNAL_SERVER_ERROR, msg, Level.WARNING, ex);
      }
   }

   /**
    * Creates a link to the supplied entry. The supplied value must be one of the
    * following:
    *
    * <pre>
    * { "token": "[token value]" }
    * </pre>
    *
    * or
    * <pre>
    * {
    *   "id": "[id_value]",
    *   "type": "[type_value]"
    * }
    * </pre>
    *
    * @param data A JSON data vehicle in one of two
    * @throws WebApplicationException If an internal error prevents the creation of
    *       this resource or if a bad request (e.g. invalid token) prevents the request
    *       from being processed.
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.SeeAlsoLink createLink(Map<String, String> data)
   {
      EntryReference<?> tgtRef = data.containsKey("token")
            ? parseToken(data.get("token"))
            : parseEntryId(data);

      String source = sourceRef.getToken();
      String target = tgtRef.getToken();
      try
      {
         SeeAlsoService svc = svcManager.getService(SeeAlsoService.makeContext(null));

         Link created = svc.create(source, target);
         return adapter.adapt(created);
      }
      catch (Exception ex)
      {
         String template = "Failed to create See Also link from {0} [{1}] to {2} [{3}]";
         String msg = formatMessage(template, target);
         throw ApiUtils.raise(INTERNAL_SERVER_ERROR, msg, Level.SEVERE, ex);
      }
   }

   /**
    * Deletes the link from this entry to the specified target entry.
    *
    * @param token The token of the target entry corresponding to the link to delete.
    * @return A response with status code 204 (No Content).
    * @throws WebApplicationException If an internal error prevents the creation of
    *       this resource or if a bad request (e.g. invalid token) prevents the request
    *       from being processed.
    */
   @DELETE
   @Path("{token}")
   @Consumes(MediaType.APPLICATION_JSON)
   public Response deleteLink(@PathParam("token") String token)
   {
      try
      {
         SeeAlsoService svc = svcManager.getService(SeeAlsoService.makeContext(null));
         svc.delete(sourceRef.getToken(), token);

         return Response.noContent().build();
      }
      catch (Exception ex)
      {
         String template = "Failed to delete See Also link from {0} [{1}] to {2} [{3}]";
         String msg = formatMessage(template, token);
         throw ApiUtils.raise(INTERNAL_SERVER_ERROR, msg, Level.SEVERE, ex);
      }
   }

   /**
    * Deletes all links associated with this entry.
    *
    * @return A response with status code 204 (No Content).
    * @throws WebApplicationException If an internal error prevents the creation of
    *       this resource or if a bad request (e.g. invalid token) prevents the request
    *       from being processed.
    */
   @DELETE
   public Response deleteAll()
   {
      try
      {
         SeeAlsoService svc = svcManager.getService(SeeAlsoService.makeContext(null));
         svc.delete(sourceRef.getToken());

         return Response.noContent().build();
      }
      catch (Exception ex)
      {
         String template = "Failed to delete See Also links from {0} [{1}]";
         String msg = format(template, sourceRef.getLabel(), sourceRef.getToken());
         throw ApiUtils.raise(INTERNAL_SERVER_ERROR, msg, Level.SEVERE, ex);
      }
   }

   private EntryReference<?> parseEntryId(Map<String, String> data)
   {
      if (!data.containsKey("id") || data.containsKey("type"))
         throw ApiUtils.raise(BAD_REQUEST, "Invalid entry id. Must contain both id and type fields.", Level.INFO, null);

      String id = data.get("id");
      String type = data.get("type");

      return resolvers.getReference(new EntryId(id, type));
   }

   /**
    *
    * @param token
    * @return
    * @throws WebApplicationException with status BAD REQUEST if the token cannot be resolved.
    */
   private EntryReference<?> parseToken(String token)
   {
      try
      {
         return resolvers.getReference(token);
      }
      catch (Exception ex)
      {
         throw ApiUtils.raise(BAD_REQUEST,
                              format("Invalid entry token {0}", token),
                              Level.WARNING, ex);
      }
   }

   /**
    * "Failed to delete See Also link from {0} [{1}] to {2} [{3}]"
    * @param template An error message template to be formatted using MessageFormat.
    *       Positions 0 and 1 will be the label and token for the source entry respectively.
    *       Positions 2 and 3 will be the label and token for the target entry respectively.
    *       For example <code>"Failed to delete See Also link from {0} [{1}] to {2} [{3}]"</code>.
    *
    * @param source The token for the source entry
    * @param target The token for the target entry
    * @return the formatted message or else a message stating that one of the supplied tokens
    *       could not be resolved.
    */
   private String formatMessage(String template, String target)
   {
      EntryReference<?> targetRef = resolvers.getReference(target);

      return format(template,
            getLabel(sourceRef), sourceRef.getToken(),
            getLabel(targetRef), targetRef.getToken());
   }

   private String getLabel(EntryReference<?> ref)
   {
      try
      {
         return ref.getLabel();
      }
      catch (Exception ex)
      {
         return format("No label for {0} [{1}]", ref.getId(), ref.getType());
      }
   }
}
