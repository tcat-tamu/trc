package edu.tamu.tcat.trc.entries.types.article.rest;

import java.net.URI;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.types.article.impl.search.ArticleSearchStrategy;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepositoryFactory;
import edu.tamu.tcat.trc.entries.types.article.rest.v1.ArticlesCollectionResource;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQueryCommand;
import edu.tamu.tcat.trc.search.solr.QueryService;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;

/**
 * The primary REST end point for articles. This is intended to be
 * registered as an OSGi declarative service and to return the specific
 * elements of the REST API as sub-resources.
 */
@Path("/")
public class ArticleRestApiService
{
   private final static Logger logger = Logger.getLogger(ArticleRestApiService.class.getName());

   private URI endpoint;
   private EntryRepositoryRegistry repoSvc;
   private SearchServiceManager searchMgr;

   private QueryService<ArticleQueryCommand> queryService;

   public void setRepoFactory(ArticleRepositoryFactory factory)
   {
      // ensures that the repo has been registered with the core TRC service
   }

   public void setRepoRegistry(EntryRepositoryRegistry repoSvc)
   {
      this.repoSvc = repoSvc;
      this.endpoint = repoSvc.getApiEndpoint();
   }

   public void setSearchSvcMgr(SearchServiceManager searchMgr)
   {
      this.searchMgr = searchMgr;
   }

   public void activate()
   {
      try
      {
         Objects.requireNonNull(repoSvc, "Article repository service is not configured.");
         if (endpoint == null)
         {
            logger.warning("No API endpoint for TRC is configured. Links returned via the REST API will not function correctly.");
            endpoint = URI.create("http://localhost/");
         }

         try
         {
            ArticleSearchStrategy indexCfg = new ArticleSearchStrategy();
            queryService = searchMgr.getQueryService(indexCfg);
         }
         catch (Exception ex)
         {
            logger.log(Level.SEVERE, "Failed to load query service for bibographical entries REST servivce", ex);
            throw ex;
         }
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to activate Article RestApiService", ex);
         throw ex;
      }
   }

   public void deactivate()
   {

   }

   @Path(ArticleRepository.ENTRY_URI_BASE)
   public ArticlesCollectionResource getArticles()
   {
      URI resolve = endpoint.resolve(ArticleRepository.ENTRY_URI_BASE);
      return new ArticlesCollectionResource(repoSvc, queryService, resolve);
   }
}
