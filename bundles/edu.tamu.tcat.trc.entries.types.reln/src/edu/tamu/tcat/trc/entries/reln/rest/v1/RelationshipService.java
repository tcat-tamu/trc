package edu.tamu.tcat.trc.entries.reln.rest.v1;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.reln.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.reln.Relationship;
import edu.tamu.tcat.trc.entries.reln.RelationshipNotAvailableException;
import edu.tamu.tcat.trc.entries.reln.RelationshipPersistenceException;
import edu.tamu.tcat.trc.entries.reln.RelationshipRepository;
import edu.tamu.tcat.trc.entries.reln.model.RelationshipDV;
import edu.tamu.tcat.trc.entries.reln.rest.v1.model.RelationshipId;

@Path("/relationships/{id}")
public class RelationshipService
{
   private static final Logger logger = Logger.getLogger(RelationshipService.class.getName());

   private RelationshipRepository repo;

   public void setRepository(RelationshipRepository repo)
   {
      this.repo = repo;
   }

   public void activate()
   {
   }

   public void dispose()
   {
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RelationshipDV get(@PathParam(value = "id") String id)
   {
      logger.fine(() -> "Retrieving relationship [relationship/" + id + "]");
      try {
         Relationship reln = repo.get(id);
         return RelationshipDV.create(reln);
      }
      catch (RelationshipNotAvailableException nae)
      {
         String msg = "Relationship does not exist [relationship/" + id + "]";
         logger.info(msg);
         throw new NotFoundException(msg);
      }
      catch (RelationshipPersistenceException perEx)
      {
         logger.log(Level.SEVERE, "Data access error trying to retrieve relationship [relationship/" + id + "]", perEx);
         throw new InternalServerErrorException("Failed to retrive relationship [relationship/" + id + "]");
      }
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RelationshipId update(@PathParam(value = "id") String id, RelationshipDV relationship)
   {
      logger.fine(() -> "Updating relationship [relationship/" + id + "]\n" + relationship);

      checkRelationshipValidity(relationship, id);
      try
      {
         EditRelationshipCommand updateCommand = repo.edit(id);
         updateCommand.setAll(relationship);
         updateCommand.execute().get();

         RelationshipId result = new RelationshipId();
         result.id = id;
         return result;
      }
      catch (Exception e)
      {
         // TODO Might check underlying cause of the exception and ensure that this isn't
         //      the result of malformed data.
         logger.log(Level.SEVERE, "An error occured during the udpating process.", e);
         throw new WebApplicationException("Failed to update relationship [" + id + "]", e.getCause(), 500);
      }
   }

   private void checkRelationshipValidity(RelationshipDV reln, String id)
   {
      if (!reln.id.equals(id))
      {
         String msg = "The id of the supplied relationship data [" + reln.id + "] does not match the id component of the URI [" + id + "]";
         logger.info("Bad Request: " + msg);
         throw new WebApplicationException(msg, 400);
      }

      // TODO need to supply additional checks for constraints on validity.
   }

   @DELETE
   public void remove(@PathParam(value = "id") String id) throws RelationshipNotAvailableException, RelationshipPersistenceException
   {
      repo.delete(id);
   }

}
