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

import java.util.Collection;
import java.util.HashSet;
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

import edu.tamu.tcat.trc.entries.types.reln.RelationshipException;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;

public class RelationshipTypeResource
{
   private static final Logger logger = Logger.getLogger(RelationshipTypeResource.class.getName());

   private final RelationshipTypeRegistry registry;

   public RelationshipTypeResource(RelationshipTypeRegistry registry)
   {
      this.registry = registry;
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
         return RepoAdapter.toDto(relnType);
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
