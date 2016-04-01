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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.VolumeMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;

public class VolumeResource
{
   private static final Logger logger = Logger.getLogger(VolumeResource.class.getName());

   private final String workId;
   private final String editionId;
   private final String volumeId;
   private final WorkRepository repo;

   public VolumeResource(String workId, String editionId, String volumeId, WorkRepository repo)
   {
      this.workId = workId;
      this.editionId = editionId;
      this.volumeId = volumeId;
      this.repo = repo;
   }


   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Volume getVolume()
   {
      Volume volume = loadVolume();
      return RepoAdapter.toDTO(volume);
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.VolumeId updateVolume(RestApiV1.Volume volume)
   {
      EditWorkCommand editWorkCommand = editWork();
      VolumeMutator volumeMutator = editVolume(editWorkCommand);
      volumeMutator.setAll(RepoAdapter.toRepo(volume));
      editWorkCommand.execute();
      RestApiV1.VolumeId vid = new RestApiV1.VolumeId();
      vid.id = volumeMutator.getId();
      return vid;
   }

   @DELETE
   public void deleteVolume()
   {
      EditWorkCommand command = editWork();
      try
      {
         command.removeVolume(volumeId);
      }
      catch (NoSuchCatalogRecordException e)
      {
         String message = "Unable to remove volume {" + volumeId + "} from work {" + workId + "}.";
         logger.log(Level.WARNING, message, e);
         throw new NotFoundException(message, e);
      }

      try
      {
         command.execute().get();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   /**
    * Helper method to load a work from persistence, handling any checked exceptions that arise and
    * passing them as HTTP messages.
    *
    * @return
    * @throws NotFoundException if the work identified by the given ID cannot be found
    */
   private Volume loadVolume()
   {
      try
      {
         return repo.getVolume(workId, editionId, volumeId);
      }
      catch (NoSuchCatalogRecordException e)
      {
         String message = "Unable to find volume {" + volumeId + "} on edition {" + editionId + "} on work {" + workId + "}.";
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
   private VolumeMutator editVolume(EditWorkCommand editWorkCommand)
   {
      EditionMutator editionMutator;

      try
      {
         editionMutator = editWorkCommand.editEdition(editionId);
      }
      catch (NoSuchCatalogRecordException e)
      {
         String message = "Unable to modify edition {" + editionId + "} on work {" + workId + "}.";
         logger.log(Level.WARNING, message, e);
         throw new NotFoundException(message, e);
      }

      try
      {
         return editionMutator.editVolume(volumeId);
      }
      catch (NoSuchCatalogRecordException e)
      {
         String message = "Unable to modify volume {" + volumeId + "} on edition {" + editionId + "} on work {" + workId + "}.";
         logger.log(Level.WARNING, message, e);
         throw new NotFoundException(message, e);
      }
   }
}
