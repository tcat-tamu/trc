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

import static java.text.MessageFormat.format;

import java.text.MessageFormat;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.core.repo.NoSuchEntryException;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.AuthorMutator;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.entries.types.article.repo.FootnoteMutator;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.services.bibref.repo.RefCollectionService;
import edu.tamu.tcat.trc.services.rest.bibref.ReferenceCollectionResource;


public class ArticleResource
{
   private final static Logger logger = Logger.getLogger(ArticleResource.class.getName());

   private static final String ERR_NOT_FOUND = "Could not find an article with the supplied id {0}";
   private static final String ERR_UNKNOWN_GET = "That's embrassing. Something went wrong while trying to retrieve {0}.";

   private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
   private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

   private final String articleId;
   private final TrcApplication trcCtx;

   private final RestApiV1Adapter adapter;

   public ArticleResource(TrcApplication trcCtx, String articleId)
   {
      this.trcCtx = trcCtx;
      this.articleId = articleId;
      this.adapter = new RestApiV1Adapter(trcCtx);
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Article get()
   {
      try
      {
         ArticleRepository articleRepo = trcCtx.getRepository(null, ArticleRepository.class);
         Article article = articleRepo.get(articleId);

         return adapter.adapt(article);
      }
      catch (NoSuchEntryException e)
      {
         throw new NotFoundException(format(ERR_NOT_FOUND, articleId));
      }
      catch (Exception ex)
      {
         String msg = MessageFormat.format(ERR_UNKNOWN_GET, articleId);
         logger.log(Level.SEVERE, msg, ex);
         throw new InternalServerErrorException(msg);
      }
   }

   @Path("references")
   public ReferenceCollectionResource getReferences()
   {
      EntryId entryRef = new EntryId(articleId, ArticleRepository.ENTRY_TYPE_ID);
      RefCollectionService recCollectionSvc = trcCtx.getService(RefCollectionService.makeContext(null));
      return new ReferenceCollectionResource(recCollectionSvc, entryRef);
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Article update(RestApiV1.Article article)
   {
      // TODO add support for partial updates
      ArticleRepository articleRepo = trcCtx.getRepository(null, ArticleRepository.class);
      try
      {
         EditArticleCommand editCmd = articleRepo.edit(articleId);

         apply(editCmd, article);
         editCmd.execute().get(10, TimeUnit.SECONDS);

         // we get the current version from the repo in order to ensure that we have
         // both the changes that were applied in this update (already present in article)
         // and any changes that may have been applied prior to this.
         Article current = articleRepo.get(articleId);
         return adapter.adapt(current);
      }
      catch (NoSuchEntryException noEx)
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
   public void delete()
   {
      ArticleRepository articleRepo = trcCtx.getRepository(null, ArticleRepository.class);
      try
      {
         // TODO send appropriate response.
         articleRepo.remove(articleId).get();
      }
      catch (Exception e) {
         // TODO: handle exception
      }
   }

   public static void apply(EditArticleCommand editCmd, RestApiV1.Article article)
   {
      // TODO only apply things that have been updated. . . .
      editCmd.setContentType(article.contentType);
      editCmd.setArticleType(article.articleType);
      editCmd.setTitle(article.title);

      // HACK: clearing and re-adding all authors ensures any deleted authors are removed
      editCmd.clearAuthors();
      if (article.authors != null && !article.authors.isEmpty())
      {
         article.authors.forEach(dto -> {
            if (dto.id == null || dto.id.trim().isEmpty())
               dto.id = UUID.randomUUID().toString();

            AuthorMutator authorMutator = editCmd.addAuthor(dto.id);
            authorMutator.setDisplayName(dto.name);
            authorMutator.setFirstname(dto.firstname);
            authorMutator.setLastname(dto.lastname);

            if (dto.properties != null)
               dto.properties.forEach(authorMutator::setProperty);
         });
      }

      editCmd.setAbstract(article.articleAbstract);
      editCmd.setBody(article.body);

      editCmd.clearFootnotes();
      if (article.footnotes != null && !article.footnotes.isEmpty())
      {
         article.footnotes.forEach((key, dto) -> {
            if (!Objects.equals(key, dto.id))
            {
               throw new BadRequestException(MessageFormat.format("Footnote key {0} must match footnote id {1}.", key, dto.id));
            }

            FootnoteMutator footnoteMutator = editCmd.editFootnote(dto.id);
            footnoteMutator.setBacklinkId(dto.backlinkId);
            footnoteMutator.setContent(dto.content);
            footnoteMutator.setMimeType(dto.mimeType);
         });
      }
   }

   public static String createSlug(String input)
   {
      // see http://stackoverflow.com/questions/1657193/java-code-library-for-generating-slugs-for-use-in-pretty-urls
      String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
      String normalized = Normalizer.normalize(nowhitespace, Form.NFD);
      String slug = NONLATIN.matcher(normalized).replaceAll("");
      return slug.toLowerCase(Locale.ENGLISH);
   }
}
