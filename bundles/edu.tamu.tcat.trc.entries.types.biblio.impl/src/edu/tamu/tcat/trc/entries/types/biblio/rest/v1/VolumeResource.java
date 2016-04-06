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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.repo.VolumeMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceMutator;
import edu.tamu.tcat.trc.entries.types.biblio.rest.EntityCollectionPersistenceAdapter;
import edu.tamu.tcat.trc.entries.types.biblio.rest.EntityPersistenceAdapter;
import edu.tamu.tcat.trc.entries.types.biblio.rest.v1.copies.CopyReferenceCollectionResource;

public class VolumeResource
{
   private static final Logger logger = Logger.getLogger(VolumeResource.class.getName());

   private final EntityPersistenceAdapter<Volume, VolumeMutator> repoHelper;

   public VolumeResource(EntityPersistenceAdapter<Volume, VolumeMutator> repoHelper)
   {
      this.repoHelper = repoHelper;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Volume getVolume()
   {
      Volume volume = repoHelper.get();
      return RepoAdapter.toDTO(volume);
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public void updateVolume(RestApiV1.Volume volume)
   {
      repoHelper.edit(mutator -> {
         if (!Objects.equals(volume.id, mutator.getId()))
         {
            throw new BadRequestException("Volume ID mismatch.");
         }

         RepoAdapter.save(volume, mutator);
      });
   }

   @DELETE
   public void deleteVolume()
   {
      repoHelper.delete();
   }

   @Path("copies")
   public CopyReferenceCollectionResource getCopyReferences()
   {
      EntityCollectionPersistenceAdapter<CopyReference, CopyReferenceMutator> helper = new CopyReferenceCollectionRepoHelper();
      return new CopyReferenceCollectionResource(helper);
   }

   private class CopyReferenceCollectionRepoHelper implements EntityCollectionPersistenceAdapter<CopyReference, CopyReferenceMutator>
   {
      @Override
      public Collection<CopyReference> get()
      {
         return repoHelper.get().getCopyReferences();
      }

      @Override
      public EntityPersistenceAdapter<CopyReference, CopyReferenceMutator> get(String id)
      {
         return new EntityPersistenceAdapter<CopyReference, CopyReferenceMutator>()
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
         class CreateCopyReferenceConsumer implements Consumer<VolumeMutator>
         {
            private String id;

            @Override
            public void accept(VolumeMutator volumeMutator)
            {
               CopyReferenceMutator copyReferenceMutator = volumeMutator.createCopyReference();
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
