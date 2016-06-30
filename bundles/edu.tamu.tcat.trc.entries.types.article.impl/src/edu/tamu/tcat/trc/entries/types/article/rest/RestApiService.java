package edu.tamu.tcat.trc.entries.types.article.rest;

import javax.ws.rs.Path;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;

/**
 * The primary REST end point for articles. This is intended to be
 * registered as an OSGi declarative service and to return the specific
 * elements of the REST API as sub-resources.
 */
@Path("/articles")
public class RestApiService
{
   private ArticleRepository repo;
   private ConfigurationProperties config;

   public void bindArticleRepository(ArticleRepository repo)
   {
      this.repo = repo;
   }

   public void bindConfig(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void activate()
   {

   }

   public void deactivate()
   {

   }
}
