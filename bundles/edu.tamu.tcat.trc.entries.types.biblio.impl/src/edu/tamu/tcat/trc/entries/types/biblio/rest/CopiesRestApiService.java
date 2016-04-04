package edu.tamu.tcat.trc.entries.types.biblio.rest;

import java.util.Objects;

import javax.ws.rs.Path;

import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceRepositoryProvider;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.EditCopyReferenceCommand;
import edu.tamu.tcat.trc.entries.types.biblio.rest.v1.copies.CopyReferenceCollectionResource;
import edu.tamu.tcat.trc.repo.DocumentRepository;

@Path("/")
public class CopiesRestApiService
{
   private DocumentRepository<CopyReference, EditCopyReferenceCommand> repo;

   /**
    * Bind method for copy persistence component (usually provided by dependency injection layer)
    *
    * @param repo
    */
   public void setCopyRepository(CopyReferenceRepositoryProvider provider)
   {
      this.repo = provider.getRepository();
   }

   /**
    * Lifecycle management callback (usually called by framework service layer)
    * This method should be called once all dependent services have been provided and the resource
    * is ready to start responding to requests.
    */
   public void activate()
   {
      Objects.requireNonNull(repo, "No copy reference repository configured");
   }

   /**
    * Lifecycle management callback (usually called by framework service layer)
    * This method should be called when the services provided by this class are no longer needed.
    */
   public void dispose()
   {
      repo = null;
   }

   @Path("v1/copies")
   public CopyReferenceCollectionResource getV1Endpoint()
   {
      return new CopyReferenceCollectionResource(repo);
   }

   @Path("copies")
   public CopyReferenceCollectionResource getDefaultEndpoint()
   {
      return new CopyReferenceCollectionResource(repo);
   }
}
