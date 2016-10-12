package edu.tamu.tcat.trc.entries.types.article.rest.v1;

import static java.text.MessageFormat.format;

import java.net.URI;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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

import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQueryCommand;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchResult;
import edu.tamu.tcat.trc.search.solr.QueryService;
import edu.tamu.tcat.trc.search.solr.SearchException;
import edu.tamu.tcat.trc.services.bibref.repo.RefCollectionService;

/**
 *  REST API sub-resource that represents a collection of articles and the actions that
 *  can be performed on that collection.
 */
public class ArticlesCollectionResource
{
   private final static Logger logger = Logger.getLogger(ArticlesCollectionResource.class.getName());

   private final ObjectMapper mapper;
   private final EntryRepositoryRegistry repoSvc;
   private final QueryService<ArticleQueryCommand> queryService;

   private final URI endpoint;

   private RefCollectionService refRepo;

   public ArticlesCollectionResource(EntryRepositoryRegistry repoSvc,
                                     QueryService<ArticleQueryCommand> queryService,
                                     RefCollectionService refRepo,
                                     URI endpoint)
   {
      this.repoSvc = repoSvc;
      this.queryService = queryService;
      this.refRepo = refRepo;
      this.endpoint = endpoint != null ? endpoint : URI.create("http://localhost/articles/");

      this.mapper = new ObjectMapper();
      this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.ArticleSearchResultSet
   search(@Context UriInfo uriInfo,
          @QueryParam(value="q") String q,
          @QueryParam(value = "offset") @DefaultValue("0") int offset,
          @QueryParam(value = "max") @DefaultValue("100") int numResults)
   {
      if (queryService == null)
         throw new InternalServerErrorException("Searching for articles is not currently supported.");

      try
      {
         ArticleQueryCommand articleQryCmd = queryService.createQuery();

         if (q != null && !q.trim().isEmpty())
            articleQryCmd.setQuery(q);

         articleQryCmd.setOffset(offset);
         articleQryCmd.setMaxResults(numResults);
         ArticleSearchResult results = articleQryCmd.execute();

         RestApiV1.ArticleSearchResultSet rs = new RestApiV1.ArticleSearchResultSet();
         rs.articles = ModelAdapter.toDTO(results);
         rs.query = ModelAdapter.toQueryDetail(uriInfo.getAbsolutePath(), results);

         return rs;
      }
      catch (SearchException e)
      {
         String msg = MessageFormat.format("That's embrassing. Something went wrong while trying "
               + "to search for articles.\n\tQuery: {0}.", q);

         logger.log(Level.SEVERE, msg, e);
         throw new InternalServerErrorException(msg);
      }
      catch (Exception ex)
      {
         String msg = MessageFormat.format("That's embrassing. Something went wrong while trying "
               + "to search for articles.\n\tQuery: {0}.", q);

         logger.log(Level.SEVERE, msg, ex);
         throw new InternalServerErrorException(msg);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public Response create(RestApiV1.Article article)
   {
      // TODO need to assess and fix error handling.
      try
      {
         ArticleRepository articleRepo = repoSvc.getRepository(null, ArticleRepository.class);
         EditArticleCommand editCmd = articleRepo.create();
         ArticleResource.apply(editCmd, article);

         article.id = editCmd.execute().get(10, TimeUnit.SECONDS);
         Article stored = articleRepo.get(article.id);

         return buildResponse(ModelAdapter.adapt(stored, repoSvc.getResolverRegistry()));
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
      return new ArticleResource(repoSvc, refRepo, articleId);
   }

   private Response buildResponse(RestApiV1.Article dto)
   {
      URI uri = endpoint.resolve(dto.id);

      Link.Builder linkBuilder = Link.fromUri(uri);
      linkBuilder.rel("self");
      linkBuilder.title(dto.title);
      Link link = linkBuilder.build();
      dto.self = ModelAdapter.makeLink(uri, "self", dto.title);

      // might be created on creation, but I think it is good to return the created article
      // rather than just the ID
      return Response.ok(dto).links(link).build();
   }
}
