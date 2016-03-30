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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.VolumeDV;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.VolumeMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;

public class EditionResource
{
   private static final Logger logger = Logger.getLogger(EditionResource.class.getName());

   private final String workId;
   private final String editionId;
   private final WorkRepository repo;

   public EditionResource(String workId, String editionId, WorkRepository repo)
   {
      this.workId = workId;
      this.editionId = editionId;
      this.repo = repo;
   }

   /**
    * Get specific edition info hierarchy from the current work
    *
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Edition getEdition()
   {
      Edition edition = loadEdition();
      return RepoAdapter.toDTO(edition);
   }

   /**
    * Save an updated edition back to the persistence layer
    *
    * @param edition
    * @return
    */
   @PUT
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.EditionId updateEdition(EditionDV edition)
   {
      EditWorkCommand command = editWork();
      EditionMutator editionMutator = editEdition(command);
      editionMutator.setAll(edition);

      try
      {
         command.execute().get();
      }
      catch (Exception e)
      {
         String message = "Unable to update edition {" + editionId + "} on work {" + workId + "}.";
         logger.log(Level.SEVERE, message, e);
         throw new InternalServerErrorException(message, e);
         // TODO: check ExecutionException to see what underlying issue is
      }

      RestApiV1.EditionId eid = new RestApiV1.EditionId();
      eid.id = editionMutator.getId();
      return eid;
   }

   /**
    * Delete the edition from persistence
    */
   @DELETE
   public void deleteEdition()
   {
      EditWorkCommand command = editWork();

      try
      {
         command.removeEdition(editionId);
      }
      catch (NoSuchCatalogRecordException e)
      {
         String message = "Unable to find edition {" + editionId + "} on work {" + workId + "}.";
         logger.log(Level.WARNING, message, e);
         throw new NotFoundException(message, e);
      }

      try
      {
         command.execute().get();
      }
      catch (Exception e)
      {
         String message = "Unable to remove edition {" + editionId + "} from work {" + workId + "}.";
         logger.log(Level.SEVERE, message, e);
         throw new InternalServerErrorException(message, e);
         // TODO: check ExecutionException to see what underlying issue is
      }
   }

   /**
    * List all volumes on the current edition object
    *
    * @return
    */
   @GET
   @Path("/volumes")
   @Produces(MediaType.APPLICATION_JSON)
   public List<RestApiV1.Volume> listVolumes()
   {
      Edition edition = loadEdition();
      List<Volume> volumes = edition.getVolumes();
      return volumes.stream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());
   }

   /**
    * Create a new volume on the current edition object
    *
    * @param volume
    */
   @POST
   @Path("/volumes")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.VolumeId createVolume(RestApiV1.Volume volume)
   {
      EditWorkCommand command = editWork();
      EditionMutator editionMutator = editEdition(command);
      VolumeMutator volumeMutator = editionMutator.createVolume();
      VolumeDV repoDto = RepoAdapter.toRepo(volume);
      volumeMutator.setAll(repoDto);

      try
      {
         command.execute().get();
      }
      catch (Exception e)
      {
         String message = "Unable to save work {" + workId + "} after adding volume to edition {" + editionId + "}.";
         logger.log(Level.SEVERE, message, e);
         throw new InternalServerErrorException(message, e);
      }

      RestApiV1.VolumeId vid = new RestApiV1.VolumeId();
      vid.id = volumeMutator.getId();
      return vid;
   }

   /**
    * Fetch a specific volume from the edition and perform operations on it
    *
    * @param volumeId
    * @return
    */
   @Path("/volumes/{id}")
   public VolumeResource getVolume(@PathParam("id") String volumeId)
   {
      return new VolumeResource(workId, editionId, volumeId, repo);
   }

   /**
    * Helper method to load a work from persistence, handling any checked exceptions that arise and
    * passing them as HTTP messages.
    *
    * @return
    * @throws NotFoundException if the work identified by the given ID cannot be found
    */
   private Edition loadEdition()
   {
      try
      {
         return repo.getEdition(workId, editionId);
      }
      catch (NoSuchCatalogRecordException e)
      {
         String message = "Unable to find edition {" + editionId + "} on work {" + workId + "}.";
         logger.log(Level.WARNING, message, e);
         throw new NotFoundException(message, e);
      }
   }

   /**
    * Helper method to start editing a work, handling any checked exceptions that arise and passing
    * them as HTTP messages.
    *
    * @return
    * @throws NotFoundException if the work identified by the given ID cannot be found
    */
   private EditWorkCommand editWork()
   {
      try
      {
         return repo.edit(workId);
      }
      catch (NoSuchCatalogRecordException e)
      {
         String message = "Unable to modify work {" + workId + "}.";
         logger.log(Level.WARNING, message, e);
         throw new NotFoundException(message, e);
      }
   }

   /**
    * Helper method to start editing an edition, handling any checked exceptions that arise and
    * passing them as HTTP messages.
    *
    * @return
    * @throws NotFoundException if the edition identified by the given ID cannot be found
    */
   private EditionMutator editEdition(EditWorkCommand editWorkCommand)
   {
      try
      {
         return editWorkCommand.editEdition(editionId);
      }
      catch (NoSuchCatalogRecordException e)
      {
         String message = "Unable to modify edition {" + editionId + "} on work {" + workId + "}.";
         logger.log(Level.WARNING, message, e);
         throw new NotFoundException(message, e);
      }
   }
}