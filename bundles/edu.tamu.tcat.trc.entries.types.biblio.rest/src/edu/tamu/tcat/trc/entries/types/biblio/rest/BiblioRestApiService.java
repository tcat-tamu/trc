package edu.tamu.tcat.trc.entries.types.biblio.rest;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.BibliographicSearchStrategy;
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.WorkSolrQueryCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.biblio.rest.v1.WorkCollectionResource;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.search.solr.QueryService;

@Path("/")
public class BiblioRestApiService
{
   private final static Logger logger = Logger.getLogger(BiblioRestApiService.class.getName());


   private QueryService<WorkSolrQueryCommand> queryService;


   private TrcApplication trcCtx;

   public void setTrcContext(TrcApplication trcCtx)
   {
      this.trcCtx = trcCtx;
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
         Objects.requireNonNull(trcCtx, "No TRC context configured");

         BibliographicSearchStrategy indexCfg = new BibliographicSearchStrategy(trcCtx);
         queryService = trcCtx.getQueryService(indexCfg);
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
      Account account = null;

      BibliographicEntryRepository repo = trcCtx.getRepository(account, BibliographicEntryRepository.class);
      EntryResolverRegistry resolverRegistry = trcCtx.getResolverRegistry();
      return new WorkCollectionResource(repo, queryService, trcCtx.getServiceManager(), resolverRegistry);
   }

   @Path("works")
   public WorkCollectionResource getDefaultEndpoint()
   {
      return getV1Endpoint();
   }


}
