package edu.tamu.tcat.trc.entries.types.article.rest;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.types.article.impl.search.ArticleSearchStrategy;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.rest.v1.ArticlesCollectionResource;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQueryCommand;
import edu.tamu.tcat.trc.search.solr.QueryService;
import edu.tamu.tcat.trc.services.ServiceContext;
import edu.tamu.tcat.trc.services.bibref.repo.RefCollectionService;

/**
 * The primary REST end point for articles. This is intended to be
 * registered as an OSGi declarative service and to return the specific
 * elements of the REST API as sub-resources.
 */
@Path("/")
public class ArticleRestApiService
{
   private final static Logger logger = Logger.getLogger(ArticleRestApiService.class.getName());

   private QueryService<ArticleQueryCommand> queryService;

   private TrcApplication trcCtx;

   public void setTrcContext(TrcApplication trcCtx)
   {
      this.trcCtx = trcCtx;
   }

   public void activate()
   {
      try
      {
         logger.info(() -> "Activating " + getClass().getSimpleName());

         try
         {
            ArticleSearchStrategy indexCfg = new ArticleSearchStrategy(trcCtx.getResolverRegistry());
            queryService = trcCtx.getQueryService(indexCfg);
         }
         catch (Exception ex)
         {
            logger.log(Level.SEVERE, "Failed to load query service for articlesREST servivce", ex);
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
      ServiceContext<RefCollectionService> ctx = RefCollectionService.makeContext(null);

      RefCollectionService refRepo = trcCtx.getService(ctx);
      URI resolve = trcCtx.getApiEndpoint().resolve(ArticleRepository.ENTRY_URI_BASE);

      // TODO HACK need to simplify this.
      return new ArticlesCollectionResource(trcCtx.getEntryRepositoryManager(), queryService, refRepo, resolve);
   }
}
