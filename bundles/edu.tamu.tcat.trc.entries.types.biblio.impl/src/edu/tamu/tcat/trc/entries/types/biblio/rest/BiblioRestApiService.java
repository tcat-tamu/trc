package edu.tamu.tcat.trc.entries.types.biblio.rest;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;
import edu.tamu.tcat.trc.entries.types.biblio.rest.v1.WorkCollectionResource;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkSearchService;

@Path("/")
public class BiblioRestApiService
{

   private final static Logger logger = Logger.getLogger(BiblioRestApiService.class.getName());

   private WorkRepository repo;
   private WorkSearchService searchSvc;

   /**
    * Bind method for persistence component (usually called by framework dependency injection layer)
    *
    * @param repoProvider
    */
   public void setRepository(WorkRepository repo)
   {
      this.repo = repo;
   }

   /**
    * Bind method for search component (usually called by framework dependency injection layer)
    *
    * @param workSearchService
    */
   public void setWorkService(WorkSearchService workSearchService)
   {
      this.searchSvc = workSearchService;
   }

   /**
    * Lifecycle management callback (usually called by framework service layer)
    * This method should be called once all dependent services have been provided and the resource
    * is ready to start responding to requests.
    */
   public void activate()
   {
      try
      {
         logger.fine("Starting Works REST API endpoint.");

         Objects.requireNonNull(repo, "No works repository provided.");
         Objects.requireNonNull(searchSvc, "No work search service provided.");
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to start works REST API", ex);
         throw ex;
      }
   }

   /**
    * Lifecycle management callback (usually called by framework service layer)
    * This method should be called when the services provided by this class are no longer needed.
    */
   public void dispose()
   {
      repo = null;
      searchSvc = null;
   }

   @Path("v1/works")
   public WorkCollectionResource getV1Endpoint()
   {
      return new WorkCollectionResource(repo, searchSvc);
   }

   @Path("works")
   public WorkCollectionResource getDefaultEndpoint()
   {
      return new WorkCollectionResource(repo, searchSvc);
   }


}
