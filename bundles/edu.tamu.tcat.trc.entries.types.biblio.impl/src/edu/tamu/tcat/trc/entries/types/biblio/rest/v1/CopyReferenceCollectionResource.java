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
package edu.tamu.tcat.trc.entries.types.biblio.rest.v1;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.types.biblio.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.repo.CopyReferenceMutator;
import edu.tamu.tcat.trc.entries.types.biblio.rest.EntityCollectionPersistenceAdapter;

@Path("/copies")
public class CopyReferenceCollectionResource
{
   private final EntityCollectionPersistenceAdapter<CopyReference, CopyReferenceMutator> repoHelper;

   public CopyReferenceCollectionResource(EntityCollectionPersistenceAdapter<CopyReference, CopyReferenceMutator> repoHelper)
   {
      this.repoHelper = repoHelper;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<RestApiV1.CopyReference> getByWorkId()
   {
      return repoHelper.get().stream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());
   }

   @Path("{id}")
   public CopyReferenceResource getCopy(@PathParam("id") String id)
   {
      return new CopyReferenceResource(repoHelper.get(id));
   }

   /**
    * Add a new copy reference
    *
    * @param entityId
    * @return
    * @throws UpdateCanceledException
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.CopyReference createCopyReference(RestApiV1.CopyReference dto)
   {
      String id = repoHelper.create(mutator -> RepoAdapter.apply(dto, mutator));
      CopyReference copyReference = repoHelper.get(id).get();
      return RepoAdapter.toDTO(copyReference);
   }


}
