package edu.tamu.tcat.trc.entries.reln.rest.v1;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Joiner;

import edu.tamu.tcat.trc.entries.reln.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.reln.Relationship;
import edu.tamu.tcat.trc.entries.reln.RelationshipDirection;
import edu.tamu.tcat.trc.entries.reln.RelationshipQueryCommand;
import edu.tamu.tcat.trc.entries.reln.RelationshipRepository;
import edu.tamu.tcat.trc.entries.reln.RelationshipSearchService;
import edu.tamu.tcat.trc.entries.reln.model.RelationshipDV;
import edu.tamu.tcat.trc.entries.reln.rest.v1.model.RelationshipId;

@Path("/relationships")
public class RelationshipsCollectionService
{
   private static final Logger logger = Logger.getLogger(RelationshipsCollectionService.class.getName());

   private RelationshipRepository repo;
   private RelationshipSearchService relnSearchService;

   public void setRepository(RelationshipRepository repo)
   {
      this.repo = repo;
   }

   public void setRelationshipService(RelationshipSearchService service)
   {
      this.relnSearchService = service;
   }

   public void activate()
   {
   }

   public void dispose()
   {
   }

   // /relationships?entity=<uri>      return all entities related to the supplied entity
   // /relationships?entity=<uri>[&type=<type_id>][&direction=from|to|any]
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<RelationshipDV> getRelationships(
         @QueryParam(value="entity") URI entity,
         @QueryParam(value="type") String type,
         @QueryParam(value="direction") String d)
   {
      if (entity == null) {
         throw new BadRequestException("No \"entity\" parameter value was provided.");
      }

      RelationshipDirection direction = parseDirection(d);

      Objects.requireNonNull(relnSearchService, "relationship search service is not defined");
      RelationshipQueryCommand queryCommand = relnSearchService.createQueryCommand();
      queryCommand.forEntity(entity, direction);

      if (type != null) {
         queryCommand.byType(type);
      }

      List<RelationshipDV> relnDV = new ArrayList<>();
      for (Relationship reln : queryCommand.getResults())
      {
         relnDV.add(RelationshipDV.create(reln));
      }

      return relnDV;
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RelationshipId createRelationship(RelationshipDV relationship)
   {
      EditRelationshipCommand createCommand;
      try
      {
         RelationshipId result = new RelationshipId();
         createCommand = repo.create();
         createCommand.setAll(relationship);

         result.id = createCommand.execute().get();
         return result;
      }
      catch (Exception e)
      {
         logger.severe("An error occured during the creating relationship process. Exception: " + e);
         throw new WebApplicationException("Failed to create a new relationship:", e.getCause(), 500);
      }

   }

   /**
    * Parse a RelationshipDirection from a string
    *
    * @param d A string representation of the relationship direction. This value may be null
    * @return Corresponding relationship direction. This value will not be null.
    */
   private RelationshipDirection parseDirection(String d)
   {
      if (d == null)
         return RelationshipDirection.any;

      try
      {
         return RelationshipDirection.valueOf(d.toLowerCase());
      }
      catch (IllegalArgumentException iea)
      {
         Joiner joiner = Joiner.on(", ");
         throw new BadRequestException("Invalid value for query parameter 'direction' [" + d + "]. Must be one of the following: " + joiner.join(RelationshipDirection.values()));
      }
   }
}
