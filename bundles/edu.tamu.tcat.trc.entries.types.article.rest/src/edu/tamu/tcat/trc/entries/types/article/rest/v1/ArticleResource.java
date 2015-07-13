package edu.tamu.tcat.trc.entries.types.article.rest.v1;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.article.dto.ArticleDTO;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;


@Path("/articles")
public class ArticleResource
{
   private final static Logger logger = Logger.getLogger(ArticleResource.class.getName());

   private ArticleRepository repo;
   private ObjectMapper mapper;

   public void setRepository(ArticleRepository repo)
   {
      this.repo = repo;

   }

   public void activate()
   {
      Objects.requireNonNull(repo, "Article Repsoitory was not setup correctly.");
      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   public void dispose()
   {
      repo = null;
      mapper = null;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<RestApiV1.ArticleSearchResult> search(@QueryParam(value="q") String q)
   {
      return null;
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.ArticleId create(ArticleDTO articleDTO)
   {
      // TODO need to asses and fix error handling.
      try
      {
         EditArticleCommand articleCommand = repo.create();
         articleCommand.setAll(articleDTO);

         UUID id = articleCommand.execute().get();

         RestApiV1.ArticleId articleId = new RestApiV1.ArticleId();
         articleId.id = id.toString();
         return articleId;
      }
      catch (ExecutionException ex)
      {
         logger.log(Level.SEVERE, "Failed to update the supplied article.", ex);
         throw new InternalServerErrorException("Failed to update the supplied article.");
      }
      catch (Exception ie)
      {
         logger.log(Level.SEVERE, "Failed to update the supplied article.", ie);
         throw new InternalServerErrorException("Failed to update the supplied article.");
      }
   }

   @PUT
   @Path("{articleid}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.ArticleId update(@PathParam(value="articleid") String articleId, ArticleDTO articleDTO) throws InterruptedException, ExecutionException, NoSuchCatalogRecordException
   {
      try
      {
         EditArticleCommand articleCmd = repo.edit(UUID.fromString(articleId));
         articleCmd.setAll(articleDTO);

         UUID id = articleCmd.execute().get();

         RestApiV1.ArticleId result = new RestApiV1.ArticleId();
         result.id = id.toString();
         return result;
      }
      catch (NoSuchCatalogRecordException noEx)
      {
         String msg = MessageFormat.format("Could not edit article [{0}]. No such article exists.", articleId);
         logger.log(Level.WARNING, msg, noEx);
         throw new NotFoundException(msg);
      }
      catch (Exception ie)
      {
         logger.log(Level.SEVERE, "Failed to update the supplied article.", ie);
         throw new InternalServerErrorException("Failed to update the supplied article.");
      }
   }

   @DELETE
   @Path("{articleid}")
   public void delete(@PathParam(value="articleid") String articleId)
   {
      // TODO send appropriate response.
      repo.remove(UUID.fromString(articleId));
   }
}
