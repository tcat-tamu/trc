package edu.tamu.tcat.trc.entries.types.article.rest.v1;

import static java.text.MessageFormat.format;

import java.net.URI;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.trc.entries.types.article.docrepo.ArticleRepoService;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQueryCommand;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchResult;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchService;
import edu.tamu.tcat.trc.search.SearchException;

/**
 *  REST API sub-resource that represents a collection of articles and the actions that
 *  can be performed on that collection.
 */
public class ArticlesCollectionResource
{
   private final static Logger logger = Logger.getLogger(ArticlesCollectionResource.class.getName());

   private final ObjectMapper mapper;
   private final ArticleRepoService repoSvc;
   private final URI endpoint;

   public ArticlesCollectionResource(ArticleRepoService repoSvc, URI endpoint)
   {
      this.repoSvc = repoSvc;
      // HACK FIXME should not hard code endpoint API. Must be configured
      this.endpoint = endpoint != null ? endpoint : URI.create("http://localhost/articles/");

      this.mapper = new ObjectMapper();
      this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.ArticleSearchResultSet
   search(@Context UriInfo uriInfo,
          @QueryParam(value="q") String q,
          @QueryParam(value = "offset") @DefaultValue("0")   int offset,
          @QueryParam(value = "max") @DefaultValue("100") int numResults)
   throws SearchException
   {
      ArticleSearchService searchSvc = repoSvc.getSearchService();
      if (searchSvc == null)
         throw new InternalServerErrorException("Searching for articles is not currently supported.");

      try
      {
         ArticleQueryCommand articleQryCmd = searchSvc.createQuery();

         if (q != null && !q.trim().isEmpty())
            articleQryCmd.setQuery(q);

         articleQryCmd.setOffset(offset);
         articleQryCmd.setMaxResults(numResults);
         ArticleSearchResult results = articleQryCmd.execute();

         RestApiV1.ArticleSearchResultSet rs = new RestApiV1.ArticleSearchResultSet();
         rs.articles = ArticleSearchAdapter.toDTO(results);
         rs.query = ArticleSearchAdapter.toQueryDetail(uriInfo.getAbsolutePath(), results);

         return rs;
      }
      catch (SearchException e)
      {
         String msg = MessageFormat.format("That's embrassing. Something went wrong while trying "
               + "to search for articles.\n\tQuery: {0}.", q);

         logger.log(Level.SEVERE, msg, e);
         throw new InternalServerErrorException(msg);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public Response create(@Context UriInfo uriInfo, RestApiV1.Article article)
   {
      // TODO need to assess and fix error handling.
      try
      {
         ArticleRepository articleRepo = repoSvc.getArticleRepo(null);
         EditArticleCommand editCmd = articleRepo.create();
         ArticleResource.apply(editCmd, article);

         article.id = editCmd.execute().get();

         return buildResponse(article, uriInfo);
      }
      catch (ExecutionException ex)
      {
         // TODO what about client supplied errors? Surely these aren't all internal (e.g., no title supplied).
         logger.log(Level.SEVERE, format("Failed to create the supplied article {0}.", article.title), ex);
         throw new InternalServerErrorException("Failed to update the supplied article.");
      }
      catch (Exception ie)
      {
         logger.log(Level.SEVERE, "Failed to update the supplied article.", ie);
         throw new InternalServerErrorException("Failed to update the supplied article.");
      }
   }

   @Path("{articleId}")
   public ArticleResource get(@PathParam(value="articleId") String articleId)
   {
      return new ArticleResource(repoSvc, articleId);
   }

   private Response buildResponse(RestApiV1.Article article, UriInfo uriInfo)
   {
      URI uri = endpoint.resolve(article.id);

      Link.Builder linkBuilder = Link.fromUri(uri);
      linkBuilder.rel("self");
      linkBuilder.title(article.title);
      Link link = linkBuilder.build();

      // might be created on creation, but I think it is good to return the created article
      // rather than just the ID
      return Response.ok(article).links(link).build();
   }
}
