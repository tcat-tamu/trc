package edu.tamu.tcat.trc.entries.types.biblio.rest.v1;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
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
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.types.biblio.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceMutator;
import edu.tamu.tcat.trc.entries.types.biblio.rest.EntityCollectionPersistenceAdapter;
import edu.tamu.tcat.trc.entries.types.biblio.rest.EntityPersistenceAdapter;

public class WorkResource
{
   private static final Logger logger = Logger.getLogger(WorkResource.class.getName());

   private EntityPersistenceAdapter<Work, EditWorkCommand> repoHelper;


   public WorkResource(EntityPersistenceAdapter<Work, EditWorkCommand> repoHelper)
   {
      this.repoHelper = repoHelper;
   }

   /**
    * Get entire work info hierarchy.
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Work getWork()
   {
      Work work = repoHelper.get();
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
   public void updateWork(RestApiV1.Work updatedWork)
   {
      repoHelper.edit(command -> {
         if (!Objects.equals(updatedWork.id, command.getId()))
         {
            throw new BadRequestException("Work ID mismatch");
         }

         RepoAdapter.save(updatedWork, command);
      });
   }

   /**
    * Delete the work from persistence
    */
   @DELETE
   public void deleteWork()
   {
      repoHelper.delete();
   }

   @Path("editions")
   public EditionCollectionResource getEditions()
   {
      EntityCollectionPersistenceAdapter<Edition, EditionMutator> helper = new EditionCollectionRepoHelper();
      return new EditionCollectionResource(helper);
   }

   @Path("copies")
   public CopyReferenceCollectionResource getCopyReferences()
   {
      EntityCollectionPersistenceAdapter<CopyReference, CopyReferenceMutator> helper = new CopyReferenceCollectionRepoHelper();
      return new CopyReferenceCollectionResource(helper);
   }

   private static class EditionCollectionResource
   {
      private final EntityCollectionPersistenceAdapter<Edition, EditionMutator> repoHelper;

      public EditionCollectionResource(EntityCollectionPersistenceAdapter<Edition, EditionMutator> repoHelper)
      {
         this.repoHelper = repoHelper;
      }

      /**
       * List all editions on the current work object
       *
       * @return
       */
      @GET
      @Produces(MediaType.APPLICATION_JSON)
      public List<RestApiV1.Edition> listEditions()
      {
         return repoHelper.get().stream()
               .map(RepoAdapter::toDTO)
               .collect(Collectors.toList());
      }

      /**
       * Create a new edition on the current work object
       *
       * @param edition
       */
      @POST
      @Consumes(MediaType.APPLICATION_JSON)
      @Produces(MediaType.APPLICATION_JSON)
      public RestApiV1.EditionId createEdition(RestApiV1.Edition edition)
      {
         RestApiV1.EditionId eid = new RestApiV1.EditionId();
         eid.id = repoHelper.create(mutator -> RepoAdapter.save(edition, mutator));
         return eid;
      }

      /**
       * Fetch a specific edition from the work and perform operations on it
       *
       * @param editionId
       * @return
       */
      @Path("{id}")
      public EditionResource getEdition(@PathParam("id") String editionId)
      {
         return new EditionResource(repoHelper.get(editionId));
      }
   }

   private class EditionCollectionRepoHelper implements EntityCollectionPersistenceAdapter<Edition, EditionMutator>
   {
      @Override
      public Collection<Edition> get()
      {
         return repoHelper.get().getEditions();
      }

      @Override
      public EntityPersistenceAdapter<Edition, EditionMutator> get(String id)
      {
         return new EntityPersistenceAdapter<Edition, EditionMutator>()
         {
            @Override
            public Edition get()
            {
               Edition edition = repoHelper.get().getEdition(id);

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
               repoHelper.edit(command -> {
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
               });
            }

            @Override
            public void delete()
            {
               repoHelper.edit(command -> {
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
               });
            }
         };
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

         repoHelper.edit(command -> {
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
         class CreateCopyReferenceConsumer implements Consumer<EditWorkCommand>
         {
            private String id;

            @Override
            public void accept(EditWorkCommand command)
            {
               CopyReferenceMutator mutator = command.createCopyReference();
               id = mutator.getId();
               modifier.accept(mutator);
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