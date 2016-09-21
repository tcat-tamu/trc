package edu.tamu.tcat.trc.entries.types.biblio.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.BibliographicSearchStrategy;
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.WorkSolrQueryCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.biblio.rest.v1.WorkCollectionResource;
import edu.tamu.tcat.trc.search.solr.QueryService;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;

@Path("/")
public class BiblioRestApiService
{
   private final static Logger logger = Logger.getLogger(BiblioRestApiService.class.getName());

   private EntryRepositoryRegistry registry;
   private SearchServiceManager searchMgr;

   private QueryService<WorkSolrQueryCommand> queryService;

   public void setRepoRegistry(EntryRepositoryRegistry registry)
   {
      this.registry = registry;
   }

   public void setSearchSvcMgr(SearchServiceManager searchMgr)
   {
      this.searchMgr = searchMgr;
   }


   /**
    * Lifecycle management callback (usually called by framework service layer)
    * This method should be called once all dependent services have been provided and the resource
    * is ready to start responding to requests.
    */
   public void activate()
   {
      logger.info("Activating " + getClass().getSimpleName());
      if (searchMgr == null)
      {
         logger.warning("No search service has provided to " + getClass().getSimpleName());
         return;
      }

      try
      {
         BibliographicSearchStrategy indexCfg = new BibliographicSearchStrategy();
         queryService = searchMgr.getQueryService(indexCfg);
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to load query service for bibographical entries REST servivce", ex);
         throw ex;
      }
   }

   /**
    * Lifecycle management callback (usually called by framework service layer)
    * This method should be called when the services provided by this class are no longer needed.
    */
   public void dispose()
   {
   }

   @Path("v1/works")
   public WorkCollectionResource getV1Endpoint()
   {
      Account account = null;    // TODO get this from the request if this is possible here.

      BibliographicEntryRepository repo = registry.getRepository(account, BibliographicEntryRepository.class);
      return new WorkCollectionResource(repo, queryService);
   }

   @Path("works")
   public WorkCollectionResource getDefaultEndpoint()
   {
      Account account = null;    // TODO get this from the request if this is possible here.

      BibliographicEntryRepository repo = registry.getRepository(account, BibliographicEntryRepository.class);
      return new WorkCollectionResource(repo, queryService);
   }


}
