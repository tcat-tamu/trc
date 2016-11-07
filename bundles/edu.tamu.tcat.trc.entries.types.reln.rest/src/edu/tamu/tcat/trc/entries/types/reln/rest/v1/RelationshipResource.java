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
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

@Path("/relationships/{id}")
public class RelationshipResource
{
   // FIXME improve error handling
   private static final Logger logger = Logger.getLogger(RelationshipResource.class.getName());

   private final String relnId;
   private final RelationshipRepository repo;
   private final EntryResolverRegistry resolvers;

   public RelationshipResource(String relnId, RelationshipRepository repo, EntryResolverRegistry resolvers)
   {
      this.relnId = relnId;
      this.repo = repo;
      this.resolvers = resolvers;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Relationship get()
   {
      logger.fine(() -> "Retrieving relationship [relationship/" + relnId + "]");
      try {
         Relationship reln = repo.get(relnId);
         return RepoAdapter.toDTO(reln, resolvers);
      }
      catch (Exception perEx)
      {
         logger.log(Level.SEVERE, "Data access error trying to retrieve relationship [relationship/" + relnId + "]", perEx);
         throw new InternalServerErrorException("Failed to retrive relationship [relationship/" + relnId + "]");
      }

   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Relationship update(RestApiV1.SimpleRelationship relationship)
   {
      logger.fine(() -> "Updating relationship [relationship/" + relnId + "]\n" + relationship);

      UpdateHelper.checkValidity(relationship, relnId);
      try
      {
         UpdateHelper helper = new UpdateHelper(repo, resolvers);
         EditRelationshipCommand cmd = helper.applyChanges(repo.edit(relnId), relationship);

         cmd.execute().get(10, TimeUnit.SECONDS);

         Relationship reln = repo.get(relnId);
         return RepoAdapter.toDTO(reln, resolvers);
      }
      catch (Exception e)
      {
         // TODO Might check underlying cause of the exception and ensure that this isn't
         //      the result of malformed data.
         logger.log(Level.SEVERE, "An error occured during the udpating process.", e);
         throw new WebApplicationException("Failed to update relationship [" + relnId + "]", e.getCause(), 500);
      }
   }

   @DELETE
   public void remove()
   {
      try
      {
         repo.remove(relnId).get(10, TimeUnit.SECONDS);
      }
      catch (InterruptedException | ExecutionException | TimeoutException e)
      {
         throw new InternalServerErrorException("Failed to remove relations " + relnId, e);
      }
   }


}
