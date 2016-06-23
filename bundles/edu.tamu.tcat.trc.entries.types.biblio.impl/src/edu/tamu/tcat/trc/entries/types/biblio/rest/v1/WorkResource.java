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
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.repo.CopyReferenceMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.entries.types.biblio.rest.EntityCollectionPersistenceAdapter;
import edu.tamu.tcat.trc.entries.types.biblio.rest.EntityPersistenceAdapter;

public class WorkResource
{
   static final Logger logger = Logger.getLogger(WorkResource.class.getName());

   private EntityPersistenceAdapter<Work, EditWorkCommand> workPersistenceAdapter;


   public WorkResource(EntityPersistenceAdapter<Work, EditWorkCommand> workPersistenceAdapter)
   {
      this.workPersistenceAdapter = workPersistenceAdapter;
   }

   /**
    * Get entire work info hierarchy.
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Work getWork()
   {
      Work work = workPersistenceAdapter.get();
      return RepoAdapter.toDTO(work);
   }

   /**
    * Save an updated work entity back to the persistence layer.
    *
    * @param work
    */
   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Work updateWork(RestApiV1.Work updatedWork)
   {
      workPersistenceAdapter.edit(command -> {
         if (!Objects.equals(updatedWork.id, command.getId()))
         {
            throw new BadRequestException("Work ID mismatch");
         }

         RepoAdapter.apply(updatedWork, command);
      });

      // HACK rely on synchronous behavior of above method
      // return updated work to client
      return getWork();
   }

   /**
    * Delete the work from persistence
    */
   @DELETE
   public void deleteWork()
   {
      workPersistenceAdapter.delete();
   }

   @Path("editions")
   public EditionCollectionResource getEditions()
   {
      EntityCollectionPersistenceAdapter<Edition, EditionMutator> helper = new EditionCollectionPersistenceAdapter();
      return new EditionCollectionResource(helper);
   }

   @Path("copies")
   public CopyReferenceCollectionResource getCopyReferences()
   {
      EntityCollectionPersistenceAdapter<CopyReference, CopyReferenceMutator> helper = new CopyReferenceCollectionPersistenceAdapter();
      return new CopyReferenceCollectionResource(helper);
   }

   private class EditionCollectionPersistenceAdapter implements EntityCollectionPersistenceAdapter<Edition, EditionMutator>
   {
      @Override
      public Collection<Edition> get()
      {
         return workPersistenceAdapter.get().getEditions();
      }

      @Override
      public EntityPersistenceAdapter<Edition, EditionMutator> get(String id)
      {
         return new EditionPersistenceAdapter(id);
      }

      @Override
      public String create(Consumer<EditionMutator> editionModifier)
      {
         // HACK the internals may or may not be synchronous, but we don't have API to wait for
         //      their result. This will release the latch once the mutator has been submitted to
         //      the parent, but this may or may not correspond to final execution within the
         //      persistence layer. Technically, the REST API should respond with ACCEPTED and a
         //      reference of where to find the accepted object once creation is finished.
         CountDownLatch latch = new CountDownLatch(1);
         AtomicReference<String> idRef = new AtomicReference<>();

         workPersistenceAdapter.edit(command -> {
            EditionMutator mutator = command.createEdition();
            idRef.set(mutator.getId());
            editionModifier.accept(mutator);
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

   private class EditionPersistenceAdapter implements EntityPersistenceAdapter<Edition, EditionMutator>
   {
      private final String id;

      public EditionPersistenceAdapter(String id)
      {
         this.id = id;
      }

      @Override
      public Edition get()
      {
         Edition edition = workPersistenceAdapter.get().getEdition(id);

         if (edition == null)
         {
            String message = "Unable to find edition with id {" + id + "}.";
            logger.log(Level.WARNING, message);
            throw new NotFoundException(message);
         }

         return edition;
      }

      @Override
      public void edit(Consumer<EditionMutator> modifier)
      {
         workPersistenceAdapter.edit(command -> doEdit(command, modifier));
      }

      private void doEdit(EditWorkCommand command, Consumer<EditionMutator> modifier)
      {
         EditionMutator mutator;

         try
         {
            mutator = command.editEdition(id);
         }
         catch (IllegalArgumentException e)
         {
            String message = "Unable to edit edition with id {" + id + "}.";
            logger.log(Level.WARNING, message, e);
            throw new NotFoundException(message, e);
         }
         catch (Exception e)
         {
            String message = "Encountered an unexpected error while trying to edit edition {" + id + "}.";
            logger.log(Level.SEVERE, message, e);
            throw new InternalServerErrorException(message, e);
         }

         modifier.accept(mutator);
      }

      @Override
      public void delete()
      {
         workPersistenceAdapter.edit(this::doDelete);
      }

      private void doDelete(EditWorkCommand command)
      {
         try
         {
            command.removeEdition(id);
         }
         catch (IllegalArgumentException e)
         {
            String message = "Unable to delete edition with id {" + id + "}.";
            logger.log(Level.WARNING, message, e);
            throw new NotFoundException(message, e);
         }
         catch (Exception e)
         {
            String message = "Encountered an unexpected error while trying to delete edition {" + id + "}.";
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
         return workPersistenceAdapter.get().getCopyReferences();
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

         workPersistenceAdapter.edit(editWorkCommand -> {
            CopyReferenceMutator copyReferenceMutator = editWorkCommand.createCopyReference();
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
         return workPersistenceAdapter.get().getCopyReferences().stream()
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
         workPersistenceAdapter.edit(command -> doEdit(command, modifier));
      }

      private void doEdit(EditWorkCommand command, Consumer<CopyReferenceMutator> modifier)
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
         workPersistenceAdapter.edit(this::doDelete);
      }

      private void doDelete(EditWorkCommand command)
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