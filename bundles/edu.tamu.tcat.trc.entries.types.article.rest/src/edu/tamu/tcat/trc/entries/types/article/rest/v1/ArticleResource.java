/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.entries.types.article.rest.v1;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.article.dto.ArticleDTO;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQueryCommand;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchResult;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchService;
import edu.tamu.tcat.trc.search.SearchException;


@Path("/articles")
public class ArticleResource
{
   private final static Logger logger = Logger.getLogger(ArticleResource.class.getName());

   private ArticleRepository repo;
   private ObjectMapper mapper;

   private ArticleSearchService articleSearchService;

   public void setRepository(ArticleRepository repo)
   {
      this.repo = repo;
   }

   public void setArticleService(ArticleSearchService service)
   {
      this.articleSearchService = service;
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
   public RestApiV1.ArticleSearchResultSet
   search(@Context UriInfo uriInfo,
          @QueryParam(value="q") String q,
          @QueryParam(value = "offset") @DefaultValue("0")   int offset,
          @QueryParam(value = "max") @DefaultValue("100") int numResults)
   throws SearchException
   {

      try
      {
         ArticleQueryCommand articleQryCmd = articleSearchService.createQuery();

         if (q != null && !q.trim().isEmpty())
            articleQryCmd.setQuery(q);

         articleQryCmd.setOffset(offset);
         articleQryCmd.setMaxResults(numResults);
         ArticleSearchResult results = articleQryCmd.execute();

         RestApiV1.ArticleSearchResultSet rs = new RestApiV1.ArticleSearchResultSet();
         rs.items = ArticleSearchAdapter.toDTO(results);
         rs.query = ArticleSearchAdapter.toQueryDetail(uriInfo.getAbsolutePath(), results);

         return rs;

      }
      catch (SearchException e)
      {
         logger.log(Level.SEVERE, "Error", e);
         // TODO throw REST API exception, not internal.
         throw new SearchException(e);
      }
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

         // TODO supply link
         RestApiV1.ArticleId articleId = new RestApiV1.ArticleId();
         articleId.id = id.toString();
         return articleId;
      }
      catch (ExecutionException ex)
      {
         // TODO what about client supplied errors? Surely these aren't all internal (e.g., no title supplied).
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
