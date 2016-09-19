/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.entries.types.reln.rest.v1;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipSearchService;

@Path("/relationships/{id}")
public class RelationshipResource
{
   // FIXME improve error handling
   private static final Logger logger = Logger.getLogger(RelationshipResource.class.getName());

   private RelationshipRepository repo;
   private RelationshipSearchService svcSearch;

   public void setRepository(RelationshipRepository repo)
   {
      this.repo = repo;
   }

   public void setSearch(RelationshipSearchService svc)
   {
      svcSearch = svc;
   }

   public void activate()
   {
   }

   public void dispose()
   {
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Relationship get(@PathParam(value = "id") String id)
   {
      logger.fine(() -> "Retrieving relationship [relationship/" + id + "]");
      try {
         Relationship reln = repo.get(id);
         return RepoAdapter.toDTO(reln);
      }
      catch (Exception perEx)
      {
         logger.log(Level.SEVERE, "Data access error trying to retrieve relationship [relationship/" + id + "]", perEx);
         throw new InternalServerErrorException("Failed to retrive relationship [relationship/" + id + "]");
      }

   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.RelationshipId update(@PathParam(value = "id") String id, RestApiV1.Relationship relationship)
   {
      logger.fine(() -> "Updating relationship [relationship/" + id + "]\n" + relationship);

      checkRelationshipValidity(relationship, id);
      try
      {
         EditRelationshipCommand updateCommand = repo.edit(id);
         updateCommand.setAll(RepoAdapter.toRepo(relationship));
         updateCommand.execute().get();

         RestApiV1.RelationshipId result = new RestApiV1.RelationshipId();
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

   private void checkRelationshipValidity(RestApiV1.Relationship reln, String id)
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
   public void remove(@PathParam(value = "id") String id)
   {
      try
      {
         repo.remove(id).get(10, TimeUnit.SECONDS);
      }
      catch (InterruptedException | ExecutionException | TimeoutException e)
      {
         throw new InternalServerErrorException("Failed to remove relations " + id, e);
      }
   }
}
