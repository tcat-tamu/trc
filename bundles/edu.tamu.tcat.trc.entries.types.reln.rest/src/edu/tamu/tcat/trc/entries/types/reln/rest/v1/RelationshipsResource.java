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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.repo.AnchorMutator;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipDirection;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipQueryCommand;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipSearchResult;
import edu.tamu.tcat.trc.search.solr.QueryService;
import edu.tamu.tcat.trc.search.solr.SearchException;
import edu.tamu.tcat.trc.services.rest.ApiUtils;

public class RelationshipsResource
{
   private static final Logger debug = Logger.getLogger(RelationshipsResource.class.getName());

   private final RelationshipRepository repo;
   private final QueryService<RelationshipQueryCommand> queryService;

   public RelationshipsResource(RelationshipRepository repo, QueryService<RelationshipQueryCommand> queryService)
   {
      this.repo = repo;
      this.queryService = queryService;
   }


   // /relationships?entity=<uri>      return all entities related to the supplied entity
   // /relationships?entity=<uri>[&type=<type_id>][&direction=from|to|any]
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<RestApiV1.RelationshipSearchResult>
   searchRelationships(@QueryParam(value="entity") URI entity,
                       @QueryParam(value="type") String type,
                       @QueryParam(value="direction") RestApiV1.RelDirection direction,
                       @QueryParam(value = "off") @DefaultValue("0")   int offset,
                       @QueryParam(value = "max") @DefaultValue("100") int numResults)
   throws Exception
   {
      if (entity == null)
         throw new BadRequestException("No \"entity\" parameter value was provided.");

      try
      {
         RelationshipQueryCommand cmd = queryService.createQuery();
         RelationshipDirection dir = RelationshipDirection.any;
         if (direction != null)
            dir = direction.dir;
         cmd.forEntity(entity, dir);

         if (type != null) {
            cmd.byType(type);
         }

         cmd.setOffset(offset);
         cmd.setMaxResults(numResults);

         RelationshipSearchResult results = cmd.execute();
         RestApiV1.RelationshipSearchResultSet rs = new RestApiV1.RelationshipSearchResultSet();
         rs.items = SearchAdapter.toDTO(results.get());

         StringBuilder sb = new StringBuilder();
         try
         {
            app(sb, "entity", entity.toString());
            app(sb, "type", type);
            if (direction != null)
               app(sb, "direction", direction.toValue());
         }
         catch (Exception e)
         {
            throw new SearchException("Failed building querystring", e);
         }

         rs.qs = "off="+offset+"&max="+numResults+"&"+sb.toString();
         // TODO: does this depend on the number of results returned
         //      (i.e. whether < numResults), or do we assume there are infinite results?
         rs.qsNext = "off="+(offset + numResults)+"&max="+numResults+"&"+sb.toString();
         if (offset >= numResults)
            rs.qsPrev = "off="+(offset - numResults)+"&max="+numResults+"&"+sb.toString();
         // first page got off; reset to zero offset
         else if (offset > 0 && offset < numResults)
            rs.qsPrev = "off="+(0)+"&max="+numResults+"&"+sb.toString();

         //HACK: until the JS is ready to accept this data vehicle, just send the list of results
         return rs.items;
      }
      catch (Exception e)
      {
         debug.log(Level.SEVERE, "Error", e);
         throw new SearchException(e);
      }
   }

   private static void app(StringBuilder sb, String p, String v)
   {
      if (v == null)
         return;
      if (sb.length() > 0)
         sb.append("&");
      try {
         sb.append(p).append("=").append(URLEncoder.encode(v, "UTF-8"));
         // suppress exception so this method can be used in lambdas
      } catch (UnsupportedEncodingException e) {
         throw new IllegalArgumentException("Failed encoding ["+v+"]", e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Relationship createRelationship(RestApiV1.Relationship relationship)
   {
      String id;
      try
      {
         RelationshipType type = repo.getTypeRegistry().resolve(relationship.typeId);

         EditRelationshipCommand createCommand = repo.create();
         createCommand.setType(type);
         createCommand.setDescription(relationship.description);

         relationship.related.forEach(anchor -> editRelatedEntry(createCommand, anchor));
         relationship.target.forEach(anchor -> editTargetEntry(createCommand, anchor));

         id = createCommand.execute().get(10, TimeUnit.SECONDS);

         // TODO use optional and handle appropriately.
         return RepoAdapter.toDTO(repo.get(id));
      }
      catch (Exception e)
      {
         String msg = "Failed to create a new relationship";
         throw ApiUtils.raise(Response.Status.INTERNAL_SERVER_ERROR, msg, Level.SEVERE, e);
      }
   }

   private void editRelatedEntry(EditRelationshipCommand createCommand, RestApiV1.Anchor anchor)
   {
      AnchorMutator mutator = createCommand.editRelatedEntry(anchor.ref);
      anchor.properties.keySet().forEach(key -> {
         String value = anchor.properties.get(key);
         if (value == null)
            mutator.setProperty(key, value);
      });
   }

   private void editTargetEntry(EditRelationshipCommand createCommand, RestApiV1.Anchor anchor)
   {
      AnchorMutator mutator = createCommand.editTargetEntry(anchor.ref);
      anchor.properties.keySet().forEach(key -> {
         String value = anchor.properties.get(key);
         if (value == null)
            mutator.setProperty(key, value);
      });
   }

   @Path("/{id}")
   public RelationshipResource getRelationship(@PathParam("id") String relnId)
   {
      return new RelationshipResource(relnId, repo);
   }
}
