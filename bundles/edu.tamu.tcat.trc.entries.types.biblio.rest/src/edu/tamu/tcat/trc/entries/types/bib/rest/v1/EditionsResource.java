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
package edu.tamu.tcat.trc.entries.types.bib.rest.v1;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDV;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;

@Path("/works/{workId}/editions")
public class EditionsResource
{
   private WorkRepository repo;


   // Called by DS
   public void setRepository(WorkRepository repo)
   {
      this.repo = repo;
   }

   // called by DS
   public void activate()
   {
   }

   // called by DS
   public void dispose()
   {
   }


   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Collection<RestApiV1.Edition> listEditions(@PathParam(value = "workId") String workId) throws NumberFormatException, NoSuchCatalogRecordException
   {
      Work work = repo.getWork(workId);

      Collection<Edition> editions = work.getEditions();

      return editions.parallelStream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toSet());
   }

   @GET
   @Path("{editionId}")
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Edition getEdition(@PathParam(value = "workId") String workId,
                                 @PathParam(value = "editionId") String editionId) throws NumberFormatException, NoSuchCatalogRecordException
   {
      Edition edition = repo.getEdition(workId, editionId);
      return RepoAdapter.toDTO(edition);
   }

   @PUT
   @Path("{editionId}")
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.EditionId updateEdition(@PathParam(value = "workId") String workId,
                                        @PathParam(value = "editionId") String editionId,
                                        EditionDV edition)
                                              throws NoSuchCatalogRecordException
   {
      EditWorkCommand command = repo.edit(workId);
      EditionMutator editionMutator = command.editEdition(editionId);
      editionMutator.setAll(edition);
      command.execute();
      RestApiV1.EditionId eid = new RestApiV1.EditionId();
      eid.id = editionMutator.getId();
      return eid;
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.EditionId createEdition(@PathParam(value = "workId") String workId, RestApiV1.Edition edition) throws NoSuchCatalogRecordException
   {
      EditWorkCommand command = repo.edit(workId);
      EditionMutator editionMutator = command.createEdition();
      editionMutator.setAll(RepoAdapter.toRepo(edition));
      command.execute();
      RestApiV1.EditionId eid = new RestApiV1.EditionId();
      eid.id = editionMutator.getId();
      return eid;
   }

   @DELETE
   @Path("{editionId}")
   public void deleteEdition(@PathParam(value = "workId") String workId,
                             @PathParam(value = "editionId") String editionId) throws NoSuchCatalogRecordException
   {
      EditWorkCommand command = repo.edit(workId);
      command.removeEdition(editionId);
      command.execute();
   }
}