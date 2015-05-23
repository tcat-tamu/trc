package edu.tamu.tcat.trc.entries.types.reln.rest.v1;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.reln.RelationshipException;
import edu.tamu.tcat.trc.entries.reln.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;

@Path("/relationships/types")
public class RelationshipTypeService
{
   private static final Logger logger = Logger.getLogger(RelationshipTypeService.class.getName());

   private RelationshipTypeRegistry registry;

   public void setRegistry(RelationshipTypeRegistry registry)
   {
      this.registry = registry;
   }

   public void clearRegistry(RelationshipTypeRegistry reg)
   {
      this.registry = null;
   }

   public void activate()
   {
      Objects.requireNonNull(registry, "No type registry provided");
   }

   public void dispose()
   {
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{typeId}")
   public RestApiV1.RelationshipType getType(@PathParam(value = "typeId") String id)
   {
      // HACK: handle threading issues
      if (registry == null)
         throw new ServiceUnavailableException("Relationship types are currently unavailable.");

      try
      {
         RelationshipType relnType = registry.resolve(id);
         return RepoAdapter.toDTO(relnType);
      }
      catch (RelationshipException e)
      {
         throw new NotFoundException("The relationship type [" + id + "] is not defined.");
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Collection<RestApiV1.RelationshipType> listDefinedTypes()
   {
      // NOTE: for now, we'll return the full list since we assume this is a fairly limited set
      //       if/when that changes, we'll need a more fully featured paged listing and some
      //       more advanced query options.

      if (registry == null)
         throw new ServiceUnavailableException("Relationship types are currently unavailable.");

      Set<RestApiV1.RelationshipType> results = new HashSet<>();
      Set<String> typeIds = registry.list();

      typeIds.forEach((id) -> {
         try {
            results.add(getType(id));
         } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error retrieving reln type [" + id + "]", ex);
         }
      });

      return results;
   }
}
