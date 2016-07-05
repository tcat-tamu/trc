package edu.tamu.tcat.trc.entries.types.article.rest;

import java.net.URI;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.types.article.docrepo.ArticleRepoService;
import edu.tamu.tcat.trc.entries.types.article.rest.v1.ArticlesCollectionResource;

/**
 * The primary REST end point for articles. This is intended to be
 * registered as an OSGi declarative service and to return the specific
 * elements of the REST API as sub-resources.
 */
@Path("/")
public class RestApiService
{
   private static final String CONFIG_API_ENDPOINT = "edu.tamu.tcat.trc.rest.endpoint";

   private final static Logger logger = Logger.getLogger(RestApiService.class.getName());

   private static final String ARTICLES_PATH = "articles";
//   private static final String AUTHORS_PATH = "article_authors";

   private ArticleRepoService repoSvc;
   private ConfigurationProperties config;
   private URI endpoint;

   public void bindArticleRepository(ArticleRepoService repoSvc)
   {
      this.repoSvc = repoSvc;
   }

   public void bindConfig(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void activate()
   {
      try
      {
         Objects.requireNonNull(repoSvc, "Article repository service is not configured.");

         String apiEndpoint = null;
         if (config != null)
            apiEndpoint = config.getPropertyValue(CONFIG_API_ENDPOINT, String.class);
         endpoint = apiEndpoint != null ? URI.create(apiEndpoint) : URI.create("/");
         if (apiEndpoint == null)
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

   @Path(ARTICLES_PATH)
   public ArticlesCollectionResource getArticles()
   {
      return new ArticlesCollectionResource(repoSvc, endpoint.resolve(ARTICLES_PATH));
   }
}
