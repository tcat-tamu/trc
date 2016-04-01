package edu.tamu.tcat.trc.entries.types.biblio.rest.v1;

import java.util.List;
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

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDV;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;

public class WorkResource
{
   private static final Logger logger = Logger.getLogger(WorkResource.class.getName());

   private final String id;
   private final WorkRepository repo;

   public WorkResource(String id, WorkRepository repo)
   {
      this.id = id;
      this.repo = repo;
   }

   /**
    * Get entire work info hierarchy.
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public WorkDV getWork()
   {
      // FIXME should be returning RestApiV1.Work instead of WorkDV
      Work work = loadWork();
      return WorkDV.create(work);
   }

   /**
    * Save an updated work entity back to the persistence layer.
    *
    * @param work
    * @return
    */
   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public void updateWork(RestApiV1.Work updatedWork)
   {
      if (updatedWork.id != id) {
         throw new BadRequestException("Work ID mismatch");
      }

      EditWorkCommand editWorkCommand = editWork();

      WorkDV repoDto = RepoAdapter.toRepo(updatedWork);
      editWorkCommand.setAll(repoDto);

      try
      {
         editWorkCommand.execute().get();
      }
      catch (Exception e)
      {
         String message = "Unable to update work {" + id + "}.";
         logger.log(Level.SEVERE, message, e);
         throw new InternalServerErrorException(message, e);
         // TODO: check ExecutionException to see what underlying issue is
      }
   }

   /**
    * Delete the work from persistence
    */
   @DELETE
   public void deleteWork()
   {
      try
      {
         repo.delete(id);
      }
      catch (Exception e)
      {
         String message = "Unable to delete work {" + id + "}.";
         logger.log(Level.SEVERE, message, e);
         throw new NotFoundException(message, e);
         // TODO: might be something else
      }
   }

   /**
    * List all editions on the current work object
    *
    * @return
    */
   @GET
   @Path("/editions")
   @Produces(MediaType.APPLICATION_JSON)
   public List<RestApiV1.Edition> listEditions()
   {
      Work work = loadWork();
      List<Edition> editions = work.getEditions();
      return editions.stream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());
   }

   /**
    * Create a new edition on the current work object
    *
    * @param edition
    */
   @POST
   @Path("/editions")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.EditionId createEdition(RestApiV1.Edition edition)
   {
      EditWorkCommand command = editWork();
      EditionMutator editionMutator = command.createEdition();
      EditionDV repoDto = RepoAdapter.toRepo(edition);
      editionMutator.setAll(repoDto);

      try
      {
         command.execute().get();
      }
      catch (Exception e)
      {
         String message = "Unable to save work {" + id + "} after adding edition.";
         logger.log(Level.SEVERE, message, e);
         throw new InternalServerErrorException(message, e);
      }

      RestApiV1.EditionId eid = new RestApiV1.EditionId();
      eid.id = editionMutator.getId();
      return eid;
   }

   /**
    * Fetch a specific edition from the work and perform operations on it
    *
    * @param editionId
    * @return
    */
   @Path("/editions/{id}")
   public EditionResource getEdition(@PathParam("id") String editionId)
   {
      return new EditionResource(id, editionId, repo);
   }

   /**
    * Helper method to load a work from persistence, handling any checked exceptions that arise and
    * passing them as HTTP messages.
    *
    * @return
    * @throws NotFoundException if the work identified by the given ID cannot be found
    */
   private Work loadWork()
   {
      try
      {
         return repo.getWork(id);
      }
      catch (NoSuchCatalogRecordException e)
      {
         String message = "Unable to find work {" + id + "}.";
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
         return repo.edit(id);
      }
      catch (NoSuchCatalogRecordException e)
      {
         logger.log(Level.SEVERE, "unable to edit work {" + id + "}", e);
         throw new NotFoundException("Unable to update work {" + id + "}", e);
      }
   }
}
