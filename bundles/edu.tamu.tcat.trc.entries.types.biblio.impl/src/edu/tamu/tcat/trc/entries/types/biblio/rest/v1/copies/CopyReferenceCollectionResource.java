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
package edu.tamu.tcat.trc.entries.types.biblio.rest.v1.copies;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.EditCopyReferenceCommand;
import edu.tamu.tcat.trc.repo.DocumentRepository;

@Path("/copies")
public class CopyReferenceCollectionResource
{
   private static final Logger logger = Logger.getLogger(CopyReferenceCollectionResource.class.getName());

   private final DocumentRepository<CopyReference, EditCopyReferenceCommand> repo;

   public CopyReferenceCollectionResource(DocumentRepository<CopyReference, EditCopyReferenceCommand> repo)
   {
      this.repo = repo;
   }

//   @GET
//   @Produces(MediaType.APPLICATION_JSON)
//   public List<RestApiV1.CopyReference> getByWorkId(@QueryParam("uri") String entityUri,
//                                                    @QueryParam("deep") @DefaultValue("false") boolean deep)
//   {
//      URI uri;
//      try
//      {
//         uri = new URI(entityUri);
//      }
//      catch (URISyntaxException e)
//      {
//         throw new BadRequestException("Malformed query URI", e);
//      }
//
//      List<CopyReference> matchedCopies = repo.getCopies(uri, deep);
//      return matchedCopies.parallelStream()
//                          .map(RepoAdapter::toDTO)
//                          .collect(Collectors.toList());
//   }

   @Path("{id}")
   public CopyReferenceResource getCopy(@PathParam("id") String id)
   {
      return new CopyReferenceResource(id, repo);
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
   public RestApiV1.CopyReferenceId createCopyReference(RestApiV1.CopyReference dto)
   {
      EditCopyReferenceCommand command = repo.create();
      RepoAdapter.save(dto, command);

      try
      {
         RestApiV1.CopyReferenceId copyReferenceId = new RestApiV1.CopyReferenceId();
         copyReferenceId.id = command.execute().get();
         return copyReferenceId;
      }
      catch (Exception e)
      {
         String message = "Unable to save new copy reference.";
         logger.log(Level.SEVERE, message, e);
         throw new InternalServerErrorException(message, e);
      }
   }


}
