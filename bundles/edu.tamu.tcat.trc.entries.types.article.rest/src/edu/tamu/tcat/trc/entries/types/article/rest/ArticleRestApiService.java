package edu.tamu.tcat.trc.entries.types.article.rest;

import java.net.URI;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.rest.v1.ArticlesCollectionResource;

/**
 * The primary REST end point for articles. This is intended to be
 * registered as an OSGi declarative service and to return the specific
 * elements of the REST API as sub-resources.
 */
@Path("/")
public class ArticleRestApiService
{
   private final static Logger logger = Logger.getLogger(ArticleRestApiService.class.getName());

   private TrcApplication trcCtx;

   public void setTrcContext(TrcApplication trcCtx)
   {
      this.trcCtx = trcCtx;
   }

   public void activate()
   {
      logger.info(() -> "Activating " + getClass().getSimpleName());
   }

   @Path(ArticleRepository.ENTRY_URI_BASE)
   public ArticlesCollectionResource getArticles()
   {
      URI endpoint = trcCtx.getApiEndpoint().resolve(ArticleRepository.ENTRY_URI_BASE);
      return new ArticlesCollectionResource(trcCtx, endpoint);
   }
}
