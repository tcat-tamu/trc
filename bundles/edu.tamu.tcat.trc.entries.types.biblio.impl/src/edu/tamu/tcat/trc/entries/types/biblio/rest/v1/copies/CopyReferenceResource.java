package edu.tamu.tcat.trc.entries.types.biblio.rest.v1.copies;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.EditCopyReferenceCommand;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.RepositoryException;

public class CopyReferenceResource
{
   private static final Logger logger = Logger.getLogger(CopyReferenceResource.class.getName());

   private final String id;
   private final DocumentRepository<CopyReference, EditCopyReferenceCommand> repo;

   public CopyReferenceResource(String id, DocumentRepository<CopyReference, EditCopyReferenceCommand> repo)
   {
      this.id = id;
      this.repo = repo;
   }

   /**
    * Get entire copy reference
    *
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.CopyReference getCopyReference()
   {
      CopyReference reference = loadCopyReference();
      return RepoAdapter.toDTO(reference);
   }

   /**
    * Updates an existing copy reference.
    *
    * @param refId
    */
   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public void updateCopyReference(RestApiV1.CopyReference updatedCopyReference)
   {
      if (updatedCopyReference.id != id)
      {
         throw new BadRequestException("Copy reference ID mismatch.");
      }

      EditCopyReferenceCommand command = editCopyReference();
      RepoAdapter.save(updatedCopyReference, command);

      try
      {
         command.execute().get();
      }
      catch (Exception e)
      {
         String message = "Unable to update copy reference {" + id + "}.";
         logger.log(Level.SEVERE, message, e);
         throw new InternalServerErrorException(message, e);
         // TODO: check ExecutionException to see what underlying issue is
      }
   }

   /**
    * Delete the copy reference from persistence
    */
   @DELETE
   public void deleteCopyReference()
   {
      try
      {
         repo.delete(id);
      }
      catch (Exception e)
      {
         String message = "Unable to delete copy reference {" + id + "}.";
         logger.log(Level.SEVERE, message, e);
         throw new NotFoundException(message, e);
         // TODO: error might be related to something other than "not found"
      }
   }

   /**
    * Helper method to load a copy resource from persistence, handling any checked exceptions that
    * arise and passing them as HTTP messages.
    *
    * @return
    * @throws NotFoundException if the copy reference identified by the given ID cannot be found
    */
   private CopyReference loadCopyReference()
   {
      try
      {
         return repo.get(id);
      }
      catch (RepositoryException e)
      {
         throw new NotFoundException("Could not find copy [" + id +"]");
      }
   }

   /**
    * Helper method to start editing a copy resource, handling any checked exceptions that arise and
    * passing them as HTTP messages.
    * @return
    *
    * @return
    * @throws NotFoundException if the copy reference identified by the given ID cannot be found
    */
   private EditCopyReferenceCommand editCopyReference()
   {
      try
      {
         return repo.edit(id);
      }
      catch (RepositoryException e)
      {
         String message = "Unable to update copy reference {" + id + "}";
         logger.log(Level.WARNING, message, e);
         throw new NotFoundException(message, e);
      }
   }
}
