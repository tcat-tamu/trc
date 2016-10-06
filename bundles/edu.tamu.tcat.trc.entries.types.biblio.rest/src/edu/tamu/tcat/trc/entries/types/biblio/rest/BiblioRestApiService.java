package edu.tamu.tcat.trc.entries.types.biblio.rest;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.BibliographicSearchStrategy;
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.WorkSolrQueryCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.biblio.rest.v1.WorkCollectionResource;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.search.solr.QueryService;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;
import edu.tamu.tcat.trc.services.TrcServiceManager;

@Path("/")
public class BiblioRestApiService
{
   private final static Logger logger = Logger.getLogger(BiblioRestApiService.class.getName());

   private EntryRepositoryRegistry registry;
   private SearchServiceManager searchMgr;

   private QueryService<WorkSolrQueryCommand> queryService;

   private TrcServiceManager serviceMgr;

   public void setRepoRegistry(EntryRepositoryRegistry registry)
   {
      this.registry = registry;
   }

   public void setSearchSvcMgr(SearchServiceManager searchMgr)
   {
      this.searchMgr = searchMgr;
   }

   public void setServiceMgr(TrcServiceManager serviceMgr)
   {
      this.serviceMgr = serviceMgr;
   }


   /**
    * Lifecycle management callback (usually called by framework service layer)
    * This method should be called once all dependent services have been provided and the resource
    * is ready to start responding to requests.
    */
   public void activate()
   {
      logger.info("Activating " + getClass().getSimpleName());

      try
      {
         Objects.requireNonNull(searchMgr, "No search service configured");
         Objects.requireNonNull(serviceMgr, "No service manager configured");

         BibliographicSearchStrategy indexCfg = new BibliographicSearchStrategy();
         queryService = searchMgr.getQueryService(indexCfg);
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to activate " + getClass().getSimpleName(), ex);
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
      EntryResolverRegistry resolverRegistry = registry.getResolverRegistry();
      return new WorkCollectionResource(repo, queryService, serviceMgr, resolverRegistry);
   }

   @Path("works")
   public WorkCollectionResource getDefaultEndpoint()
   {
      Account account = null;    // TODO get this from the request if this is possible here.

      BibliographicEntryRepository repo = registry.getRepository(account, BibliographicEntryRepository.class);
      EntryResolverRegistry resolverRegistry = registry.getResolverRegistry();
      return new WorkCollectionResource(repo, queryService, serviceMgr, resolverRegistry);
   }


}
