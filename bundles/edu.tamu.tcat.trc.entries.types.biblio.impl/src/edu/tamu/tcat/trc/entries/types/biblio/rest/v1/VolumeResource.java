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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
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
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.types.biblio.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.repo.VolumeMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceMutator;
import edu.tamu.tcat.trc.entries.types.biblio.rest.EntityCollectionPersistenceAdapter;
import edu.tamu.tcat.trc.entries.types.biblio.rest.EntityPersistenceAdapter;

public class VolumeResource
{
   private static final Logger logger = Logger.getLogger(VolumeResource.class.getName());

   private final EntityPersistenceAdapter<Volume, VolumeMutator> volumePersistenceAdapter;

   public VolumeResource(EntityPersistenceAdapter<Volume, VolumeMutator> repoHelper)
   {
      this.volumePersistenceAdapter = repoHelper;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Volume getVolume()
   {
      Volume volume = volumePersistenceAdapter.get();
      return RepoAdapter.toDTO(volume);
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Volume updateVolume(RestApiV1.Volume volume)
   {
      volumePersistenceAdapter.edit(mutator -> {
         if (!Objects.equals(volume.id, mutator.getId()))
         {
            throw new BadRequestException("Volume ID mismatch.");
         }

         RepoAdapter.apply(volume, mutator);
      });

      // HACK rely on synchronous behavior of above method
      return getVolume();
   }

   @DELETE
   public void deleteVolume()
   {
      volumePersistenceAdapter.delete();
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
         return volumePersistenceAdapter.get().getCopyReferences();
      }

      @Override
      public EntityPersistenceAdapter<CopyReference, CopyReferenceMutator> get(String id)
      {
         return new CopyReferencePersistenceAdapter(id);
      }

      @Override
      public String create(Consumer<CopyReferenceMutator> copyReferenceModifier)
      {
         // HACK the internals may or may not be synchronous, but we don't have API to wait for
         //      their result. This will release the latch once the mutator has been submitted to
         //      the parent, but this may or may not correspond to final execution within the
         //      persistence layer. Technically, the REST API should respond with ACCEPTED and a
         //      reference of where to find the accepted object once creation is finished.
         CountDownLatch latch = new CountDownLatch(1);
         AtomicReference<String> idRef = new AtomicReference<>();

         volumePersistenceAdapter.edit(volumeMutator -> {
            CopyReferenceMutator copyReferenceMutator = volumeMutator.createCopyReference();
            idRef.set(copyReferenceMutator.getId());
            copyReferenceModifier.accept(copyReferenceMutator);
            latch.countDown();
         });

         try
         {
            latch.await(2, TimeUnit.MINUTES);
            return idRef.get();
         }
         catch (InterruptedException e)
         {
            String msg = "This seems to be taking longer than expected. Failed to create the new edition within two minutes.";
            logger.log(Level.SEVERE, msg, e);
            throw new ServiceUnavailableException(msg);
         }
      }
   }

   private class CopyReferencePersistenceAdapter implements EntityPersistenceAdapter<CopyReference, CopyReferenceMutator>
   {
      private final String id;

      public CopyReferencePersistenceAdapter(String id)
      {
         this.id = id;
      }

      @Override
      public CopyReference get()
      {
         return volumePersistenceAdapter.get().getCopyReferences().stream()
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
         volumePersistenceAdapter.edit(command -> doEdit(command, modifier));
      }

      private void doEdit(VolumeMutator command, Consumer<CopyReferenceMutator> modifier)
      {
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
      }

      @Override
      public void delete()
      {
         volumePersistenceAdapter.edit(this::doDelete);
      }

      private void doDelete(VolumeMutator command)
      {
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
      }
   }
}
