package edu.tamu.tcat.trc.entries.types.article.rest;

import java.net.URI;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.entries.types.article.ArticleRepoFacade;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.rest.v1.ArticlesCollectionResource;

/**
 * The primary REST end point for articles. This is intended to be
 * registered as an OSGi declarative service and to return the specific
 * elements of the REST API as sub-resources.
 */
@Path("/")
public class RestApiService
{
   private final static Logger logger = Logger.getLogger(RestApiService.class.getName());

   private ArticleRepoFacade repoSvc;
   private EntryResolverRegistry resolvers;
   private URI endpoint;

   @Deprecated // should use EntryRepoRegistry instead. Bump access to SearchSvc into API for repo?
   public void bindArticleRepository(ArticleRepoFacade repoSvc)
   {
      this.repoSvc = repoSvc;
   }

   public void bindRepoRegistry(EntryRepositoryRegistry repos)
   {
      URI endpoint = repos.getApiEndpoint();
      this.endpoint = endpoint.resolve(ArticleRepository.ENTRY_URI_BASE);
      this.resolvers = repos.getResolverRegistry();
   }

   public void activate()
   {
      try
      {
         Objects.requireNonNull(repoSvc, "Article repository service is not configured.");
         if (endpoint == null)
            logger.warning("No API endpoint for TRC is configured. Links returned via the REST API will not function correctly.");
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
      return new ArticlesCollectionResource(repoSvc, resolvers, endpoint);
   }
}
