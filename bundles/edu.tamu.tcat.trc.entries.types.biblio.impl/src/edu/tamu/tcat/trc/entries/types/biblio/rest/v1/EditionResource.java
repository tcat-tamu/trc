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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
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

import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.VolumeMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceMutator;
import edu.tamu.tcat.trc.entries.types.biblio.rest.CollectionRepoHelper;
import edu.tamu.tcat.trc.entries.types.biblio.rest.RepoHelper;
import edu.tamu.tcat.trc.entries.types.biblio.rest.v1.copies.CopyReferenceCollectionResource;

public class EditionResource
{
   private static final Logger logger = Logger.getLogger(EditionResource.class.getName());

   private final RepoHelper<Edition, EditionMutator> repoHelper;

   public EditionResource(RepoHelper<Edition, EditionMutator> repoHelper)
   {
      this.repoHelper = repoHelper;
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
      Edition edition = repoHelper.get();
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
   public void updateEdition(RestApiV1.Edition edition)
   {
      repoHelper.edit(mutator -> {
         if (!Objects.equals(edition.id, mutator.getId()))
         {
            throw new BadRequestException("Edition ID mismatch.");
         }

         RepoAdapter.save(edition, mutator);
      });
   }

   /**
    * Delete the edition from persistence
    */
   @DELETE
   public void deleteEdition()
   {
      repoHelper.delete();
   }

   @Path("volumes")
   public VolumeCollectionResource getVolumes()
   {
      CollectionRepoHelper<Volume, VolumeMutator> helper = new VolumeCollectionResourceRepoHelper();
      return new VolumeCollectionResource(helper);
   }

   @Path("copies")
   public CopyReferenceCollectionResource getCopyReferences()
   {
      CollectionRepoHelper<CopyReference, CopyReferenceMutator> helper = new CopyReferenceCollectionRepoHelper();
      return new CopyReferenceCollectionResource(helper);
   }

   private static class VolumeCollectionResource
   {

      private final CollectionRepoHelper<Volume, VolumeMutator> repoHelper;

      public VolumeCollectionResource(CollectionRepoHelper<Volume, VolumeMutator> helper)
      {
         this.repoHelper = helper;
      }

      /**
       * List all volumes on the current edition object
       *
       * @return
       */
      @GET
      @Produces(MediaType.APPLICATION_JSON)
      public List<RestApiV1.Volume> listVolumes()
      {
         return repoHelper.get().stream()
               .map(RepoAdapter::toDTO)
               .collect(Collectors.toList());
      }

      /**
       * Create a new volume on the current edition object
       *
       * @param volume
       */
      @POST
      @Consumes(MediaType.APPLICATION_JSON)
      @Produces(MediaType.APPLICATION_JSON)
      public RestApiV1.VolumeId createVolume(RestApiV1.Volume volume)
      {
         RestApiV1.VolumeId vid = new RestApiV1.VolumeId();
         vid.id = repoHelper.create(mutator -> RepoAdapter.save(volume, mutator));
         return vid;
      }

      /**
       * Fetch a specific volume from the edition and perform operations on it
       *
       * @param volumeId
       * @return
       */
      @Path("{id}")
      public VolumeResource getVolume(@PathParam("id") String volumeId)
      {
         return new VolumeResource(repoHelper.get(volumeId));
      }
   }

   private class VolumeCollectionResourceRepoHelper implements CollectionRepoHelper<Volume, VolumeMutator>
   {
      @Override
      public Collection<Volume> get()
      {
         return repoHelper.get().getVolumes();
      }

      @Override
      public RepoHelper<Volume, VolumeMutator> get(String id)
      {
         return new RepoHelper<Volume, VolumeMutator>()
         {
            @Override
            public Volume get()
            {
               Volume volume = repoHelper.get().getVolume(id);

               if (volume == null)
               {
                  String message = "Unable to find volume with id {" + id + "}.";
                  logger.log(Level.WARNING, message);
                  throw new NotFoundException(message);
               }

               return volume;
            }

            @Override
            public void edit(Consumer<VolumeMutator> modifier)
            {
               repoHelper.edit(editionMutator -> {
                  VolumeMutator volumeMutator;

                  try
                  {
                     volumeMutator = editionMutator.editVolume(id);
                  }
                  catch (IllegalArgumentException e)
                  {
                     String message = "Unable to edit volume with id {" + id + "}.";
                     logger.log(Level.WARNING, message, e);
                     throw new NotFoundException(message, e);
                  }
                  catch (Exception e)
                  {
                     String message = "Encountered an unexpected error while trying to edit volume {" + id + "}.";
                     logger.log(Level.SEVERE, message, e);
                     throw new InternalServerErrorException(message, e);
                  }

                  modifier.accept(volumeMutator);
               });
            }

            @Override
            public void delete()
            {
               repoHelper.edit(editionMutator -> {
                  try
                  {
                     editionMutator.removeVolume(id);
                  }
                  catch (IllegalArgumentException e)
                  {
                     String message = "Unable to delete volume with id {" + id + "}.";
                     logger.log(Level.WARNING, message, e);
                     throw new NotFoundException(message, e);
                  }
                  catch (Exception e)
                  {
                     String message = "Encountered an unexpected error while trying to delete volume {" + id + "}.";
                     logger.log(Level.SEVERE, message, e);
                     throw new InternalServerErrorException(message, e);
                  }
               });
            }
         };
      }

      @Override
      public String create(Consumer<VolumeMutator> modifier)
      {
         class CreateVolumeConsumer implements Consumer<EditionMutator>
         {
            private String id;

            @Override
            public synchronized void accept(EditionMutator editionMutator)
            {
               VolumeMutator volumeMutator = editionMutator.createVolume();
               id = volumeMutator.getId();
               modifier.accept(volumeMutator);
            }

            public String getId()
            {
               return id;
            }
         }

         // HACK the only reason this works is because repoHelper.edit() is synchronous.
         CreateVolumeConsumer consumer = new CreateVolumeConsumer();
         repoHelper.edit(consumer);
         return consumer.getId();
      }
   }

   private class CopyReferenceCollectionRepoHelper implements CollectionRepoHelper<CopyReference, CopyReferenceMutator>
   {
      @Override
      public Collection<CopyReference> get()
      {
         return repoHelper.get().getCopyReferences();
      }

      @Override
      public RepoHelper<CopyReference, CopyReferenceMutator> get(String id)
      {
         return new RepoHelper<CopyReference, CopyReferenceMutator>()
         {
            @Override
            public CopyReference get()
            {
               return repoHelper.get().getCopyReferences().stream()
                  .filter(copyReference -> Objects.equals(copyReference.getId(), id))
                  .findFirst()
                  .orElseThrow(() -> {
                     String message = "Unable to find copy reference with id {" + id + "}.";
                     logger.log(Level.WARNING, message);
                     return new NotFoundException(message);
                  });

            }

            @Override
            public void edit(Consumer<CopyReferenceMutator> modifier)
            {
               repoHelper.edit(command -> {
                  CopyReferenceMutator mutator;

                  try
                  {
                     mutator = command.editCopyReference(id);
                  }
                  catch (IllegalArgumentException e)
                  {
                     String message = "Unable to edit copy reference with id {" + id + "}.";
                     logger.log(Level.WARNING, message, e);
                     throw new NotFoundException(message, e);
                  }
                  catch (Exception e)
                  {
                     String message = "Encountered an unexpected error while trying to edit copy reference {" + id + "}.";
                     logger.log(Level.SEVERE, message, e);
                     throw new InternalServerErrorException(message, e);
                  }

                  modifier.accept(mutator);
               });
            }

            @Override
            public void delete()
            {
               repoHelper.edit(command -> {
                  try
                  {
                     command.removeCopyReference(id);
                  }
                  catch (IllegalArgumentException e)
                  {
                     String message = "Unable to delete copy reference with id {" + id + "}.";
                     logger.log(Level.WARNING, message, e);
                     throw new NotFoundException(message, e);
                  }
                  catch (Exception e)
                  {
                     String message = "Encountered an unexpected error while trying to delete copy reference {" + id + "}.";
                     logger.log(Level.SEVERE, message, e);
                     throw new InternalServerErrorException(message, e);
                  }
               });
            }
         };
      }

      @Override
      public String create(Consumer<CopyReferenceMutator> modifier)
      {
         class CreateCopyReferenceConsumer implements Consumer<EditionMutator>
         {
            private String id;

            @Override
            public void accept(EditionMutator editionMutator)
            {
               CopyReferenceMutator copyReferenceMutator = editionMutator.createCopyReference();
               id = copyReferenceMutator.getId();
               modifier.accept(copyReferenceMutator);
            }

            public String getId()
            {
               return id;
            }
         }

         // HACK the only reason this works is because repoHelper.edit() is synchronous.
         CreateCopyReferenceConsumer consumer = new CreateCopyReferenceConsumer();
         repoHelper.edit(consumer);
         return consumer.getId();
      }
   }
}