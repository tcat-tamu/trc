package edu.tamu.tcat.trc.entries.types.biblio.rest.v1.copies;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceMutator;
import edu.tamu.tcat.trc.entries.types.biblio.rest.EntityPersistenceAdapter;

public class CopyReferenceResource
{
   private final EntityPersistenceAdapter<CopyReference, CopyReferenceMutator> repoHelper;

   public CopyReferenceResource(EntityPersistenceAdapter<CopyReference, CopyReferenceMutator> repoHelper)
   {
      this.repoHelper = repoHelper;
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
      CopyReference reference = repoHelper.get();
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
      repoHelper.edit(mutator -> {
         if (updatedCopyReference.id != mutator.getId())
         {
            throw new BadRequestException("Copy reference ID mismatch.");
         }

         RepoAdapter.save(updatedCopyReference, mutator);
      });
   }

   /**
    * Delete the copy reference from persistence
    */
   @DELETE
   public void deleteCopyReference()
   {
      repoHelper.delete();
   }
}
