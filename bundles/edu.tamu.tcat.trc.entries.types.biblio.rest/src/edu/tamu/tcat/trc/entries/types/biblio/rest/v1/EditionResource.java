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
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.repo.CopyReferenceMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.VolumeMutator;
import edu.tamu.tcat.trc.entries.types.biblio.rest.EntityCollectionPersistenceAdapter;
import edu.tamu.tcat.trc.entries.types.biblio.rest.EntityPersistenceAdapter;

public class EditionResource
{
   private static final Logger logger = Logger.getLogger(EditionResource.class.getName());

   private final EntityPersistenceAdapter<Edition, EditionMutator> editionPersistenceAdapter;

   public EditionResource(EntityPersistenceAdapter<Edition, EditionMutator> editionPersistenceAdapter)
   {
      this.editionPersistenceAdapter = editionPersistenceAdapter;
   }

   /**
    * Get specific edition info hierarchy from the current work
    *
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public BiblioRestApiV1.Edition getEdition()
   {
      Edition edition = editionPersistenceAdapter.get();
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
   public BiblioRestApiV1.Edition updateEdition(BiblioRestApiV1.Edition edition)
   {
      editionPersistenceAdapter.edit(mutator -> {
         if (!Objects.equals(edition.id, mutator.getId()))
         {
            throw new BadRequestException("Edition ID mismatch.");
         }

         RepoAdapter.apply(edition, mutator);
      });

      // HACK rely on synchronous behavior of above method
      return getEdition();
   }

   /**
    * Delete the edition from persistence
    */
   @DELETE
   public void deleteEdition()
   {
      editionPersistenceAdapter.delete();
   }

   @Path("volumes")
   public VolumeCollectionResource getVolumes()
   {
      EntityCollectionPersistenceAdapter<Volume, VolumeMutator> helper = new VolumeCollectionPersistenceAdapter();
      return new VolumeCollectionResource(helper);
   }

   @Path("copies")
   public CopyReferenceCollectionResource getCopyReferences()
   {
      EntityCollectionPersistenceAdapter<CopyReference, CopyReferenceMutator> helper = new CopyReferenceCollectionPersistenceAdapter();
      return new CopyReferenceCollectionResource(helper);
   }

   private class VolumeCollectionPersistenceAdapter implements EntityCollectionPersistenceAdapter<Volume, VolumeMutator>
   {
      @Override
      public Collection<Volume> get()
      {
         return editionPersistenceAdapter.get().getVolumes();
      }

      @Override
      public EntityPersistenceAdapter<Volume, VolumeMutator> get(String id)
      {
         return new VolumePersistenceAdapter(id);
      }

      @Override
      public String create(Consumer<VolumeMutator> volumeModifier)
      {
         // HACK the internals may or may not be synchronous, but we don't have API to wait for
         //      their result. This will release the latch once the mutator has been submitted to
         //      the parent, but this may or may not correspond to final execution within the
         //      persistence layer. Technically, the REST API should respond with ACCEPTED and a
         //      reference of where to find the accepted object once creation is finished.
         CountDownLatch latch = new CountDownLatch(1);
         AtomicReference<String> idRef = new AtomicReference<>();

         editionPersistenceAdapter.edit(editionMutator -> {
            VolumeMutator volumeMutator = editionMutator.createVolume();
            idRef.set(volumeMutator.getId());
            volumeModifier.accept(volumeMutator);
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

   private class VolumePersistenceAdapter implements EntityPersistenceAdapter<Volume, VolumeMutator>
   {
      private final String id;

      public VolumePersistenceAdapter(String id)
      {
         this.id = id;
      }

      @Override
      public String getId()
      {
         return id;
      }

      @Override
      public Volume get()
      {
         Volume volume = editionPersistenceAdapter.get().getVolume(id);

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
         editionPersistenceAdapter.edit(editionMutator -> doEdit(editionMutator, modifier));
      }

      private void doEdit(EditionMutator editionMutator, Consumer<VolumeMutator> modifier)
      {
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
      }

      @Override
      public void delete()
      {
         editionPersistenceAdapter.edit(this::doDelete);
      }

      private void doDelete(EditionMutator editionMutator)
      {
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
      }
   }

   private class CopyReferenceCollectionPersistenceAdapter implements EntityCollectionPersistenceAdapter<CopyReference, CopyReferenceMutator>
   {
      @Override
      public Collection<CopyReference> get()
      {
         return editionPersistenceAdapter.get().getCopyReferences();
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

         editionPersistenceAdapter.edit(editionMutator -> {
            CopyReferenceMutator copyReferenceMutator = editionMutator.createCopyReference();
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
      public String getId()
      {
         return id;
      }

      @Override
      public CopyReference get()
      {
         return editionPersistenceAdapter.get().getCopyReferences().stream()
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
         editionPersistenceAdapter.edit(command -> doEdit(command, modifier));
      }

      private void doEdit(EditionMutator command, Consumer<CopyReferenceMutator> modifier)
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
         editionPersistenceAdapter.edit(this::doDelete);
      }

      private void doDelete(EditionMutator command)
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