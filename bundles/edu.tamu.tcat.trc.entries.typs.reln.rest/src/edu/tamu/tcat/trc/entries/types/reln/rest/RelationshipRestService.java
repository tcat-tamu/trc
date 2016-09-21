package edu.tamu.tcat.trc.entries.types.reln.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.types.reln.impl.search.RelnSearchStrategy;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.entries.types.reln.rest.v1.RelationshipTypeResource;
import edu.tamu.tcat.trc.entries.types.reln.rest.v1.RelationshipsResource;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipQueryCommand;
import edu.tamu.tcat.trc.search.solr.QueryService;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;

@Path("/")
public class RelationshipRestService
{
   private final static Logger logger = Logger.getLogger(RelationshipRestService.class.getName());

   private EntryRepositoryRegistry repos;
   private RelationshipTypeRegistry typeRegistry;

   private SearchServiceManager searchMgr;
   private QueryService<RelationshipQueryCommand> queryService;

   public void setRepoRegistry(EntryRepositoryRegistry registry)
   {
      this.repos = registry;
   }

   public void setTypeRegistry(RelationshipTypeRegistry registry)
   {
      this.typeRegistry = registry;
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
         RelnSearchStrategy indexCfg = new RelnSearchStrategy();
         queryService = searchMgr.getQueryService(indexCfg);
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to load query service for relationship entries REST servivce", ex);
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


   @Path("v1/relationships")
   public RelationshipsResource getV1Endpoint()
   {
      Account account = null;    // TODO get this from the request if this is possible here.

      RelationshipRepository repo = repos.getRepository(account, RelationshipRepository.class);
      return new RelationshipsResource(repo, queryService);
   }

   @Path("relationships")
   public RelationshipsResource getDefaultEndpoint()
   {
      return getV1Endpoint();
   }

   @Path("/relationships/types")
   public RelationshipTypeResource getTypes()
   {
      return new RelationshipTypeResource(typeRegistry);
   }
}
