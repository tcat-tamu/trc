package edu.tamu.tcat.trc.entries.types.article.rest;

import java.net.URI;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.rest.v1.ArticlesCollectionResource;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchService;

/**
 * The primary REST end point for articles. This is intended to be
 * registered as an OSGi declarative service and to return the specific
 * elements of the REST API as sub-resources.
 */
@Path("/")
public class RestApiService
{
   private final static Logger logger = Logger.getLogger(RestApiService.class.getName());

   private URI endpoint;
   private EntryRepositoryRegistry repoSvc;
   private ArticleSearchService searchSvc;


   public void bindSearchService(ArticleSearchService searchSvc)
   {
      this.searchSvc = searchSvc;
   }

   public void bindRepoRegistry(EntryRepositoryRegistry repoSvc)
   {
      URI endpoint = repoSvc.getApiEndpoint();

      this.repoSvc = repoSvc;
      this.endpoint = repoSvc.getApiEndpoint();
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
      return new ArticlesCollectionResource(repoSvc, searchSvc, endpoint.resolve(ArticleRepository.ENTRY_URI_BASE));
   }
}
