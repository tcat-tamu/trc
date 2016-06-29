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

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.docrepo.ArticleRepoService;
import edu.tamu.tcat.trc.entries.types.article.dto.ArticleAuthorDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.BibliographyDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.BibliographyDTO.BibAuthorDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.BibliographyDTO.BibTranslatorDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.BibliographyDTO.IssuedBiblioDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.CitationDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.CitationItemDTO;
//import edu.tamu.tcat.trc.entries.types.article.dto.CitationDTO.CitationPropertiesDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.FootnoteDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.LinkDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.PublicationDTO;
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

   private ArticleRepoService repo;
   private ObjectMapper mapper;

   private ArticleSearchService searchSvc;

   // called by OSGi
   public void setRepository(ArticleRepoService repo)
   {
      this.repo = repo;
   }

   // called by OSGi
   public void setArticleService(ArticleSearchService service)
   {
      this.searchSvc = service;
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
      searchSvc = null;
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

   @GET
   @Path("{articleid}")
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Article get(@PathParam(value="articleid") String articleId)
   {
      try
      {
         ArticleRepository articleRepo = repo.getArticleRepo(null);
         Article article = articleRepo.get(articleId);
         return ArticleSearchAdapter.adapt(article);
      }
      catch (NoSuchCatalogRecordException e)
      {
         // FIXME make sure we throw this
         throw new NotFoundException(MessageFormat.format("Could not find an article with the supplied id {0}", articleId));
      }
      catch (Exception ex)
      {
         String msg = MessageFormat.format("That's embrassing. Something went wrong while trying to retrieve {0}.", articleId);
         logger.log(Level.SEVERE, msg, ex);
         throw new InternalServerErrorException(msg);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public Response create(@Context UriInfo uriInfo, RestApiV1.Article article)
   {
      // TODO need to asses and fix error handling.
      ArticleRepository articleRepo = repo.getArticleRepo(null);
      try
      {
         EditArticleCommand editCmd = articleRepo.create();
         apply(editCmd, article);

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

   private Response buildResponse(RestApiV1.Article article, UriInfo uriInfo)
   {
      // TODO this is not correct - internally we don't know what our URL is.
      URI uri = uriInfo.getAbsolutePathBuilder().path(article.id).build();

      Link.Builder linkBuilder = Link.fromUri(uri);
      linkBuilder.rel("self");
      linkBuilder.title(article.title);
      Link link = linkBuilder.build();

      // might be created on creation, but I think it is good to return the created article
      // rather than just the ID
      return Response.ok(article).links(link).build();
   }

   @PUT
   @Path("{articleid}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Article update(@PathParam(value="articleid") String articleId, RestApiV1.Article article) throws InterruptedException, ExecutionException, NoSuchCatalogRecordException
   {
      // TODO add support for partial updates
      ArticleRepository articleRepo = repo.getArticleRepo(null);
      try
      {
         EditArticleCommand editCmd = articleRepo.edit(articleId);

         apply(editCmd, article);
         String id = editCmd.execute().get();

         // we get the current version from the repo in order to ensure that we have
         // both the changes that were applied in this update (already present in article)
         // and any changes that may have been applied prior to this.
         Article current = articleRepo.get(articleId);
         return ArticleSearchAdapter.adapt(current);
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
   @Path("{id}")
   public void delete(@PathParam(value="id") String articleId)
   {
      ArticleRepository articleRepo = repo.getArticleRepo(null);
      try
      {
         // TODO send appropriate response.
         articleRepo.remove(articleId).get();
      }
      catch (Exception e) {
         // TODO: handle exception
      }
   }

   private void apply(EditArticleCommand editCmd, RestApiV1.Article article)
   {
      editCmd.setContentType(article.contentType);
      editCmd.setArticleType(article.articleType);
      editCmd.setTitle(article.title);
      editCmd.setPublicationInfo(getPublication(article.pubInfo));
      editCmd.setAuthors(getAuthors(article.authors));
      editCmd.setAbstract(article.articleAbstract);
      editCmd.setBody(article.body);
      editCmd.setFootnotes(getFootnotes(article.footnotes));
      editCmd.setCitations(getCitations(article.citations));
      editCmd.setBibliography(getBibliographies(article.bibliography));
      editCmd.setLinks(getLinks(article.links));
   }

   private PublicationDTO getPublication(RestApiV1.Publication pubInfo)
   {
      PublicationDTO pubDTO = new PublicationDTO();
      pubDTO.dateCreated = pubInfo.dateCreated;
      pubDTO.dateModified = pubInfo.dateModified;
      return pubDTO;
   }

   private List<ArticleAuthorDTO> getAuthors(List<RestApiV1.ArticleAuthor> authors)
   {
      List<ArticleAuthorDTO> authorDTO = new ArrayList<>();
      if (authors != null)
      {
         authors.forEach((a) ->
         {
            ArticleAuthorDTO authDto = new ArticleAuthorDTO();
            authDto.id = a.id;
            authDto.name = a.name;
            authDto.affiliation = a.affiliation;
            authDto.contact = ArticleAuthorDTO.ContactInfoDTO.create(a.contact.email, a.contact.phone);
            authorDTO.add(authDto);
         });
      }

      return authorDTO;
   }

   private List<LinkDTO> getLinks(List<RestApiV1.LinkedResource> links)
   {
      List<LinkDTO> dto = new ArrayList<>();
      if(links != null)
      {
         links.forEach((link)->
         {
            LinkDTO linkDTO = new LinkDTO();
            linkDTO.id = link.id;
            linkDTO.type = link.type;
            linkDTO.title = link.title;
            linkDTO.uri = link.uri;
            linkDTO.rel = link.rel;
            dto.add(linkDTO);
         });
      }
      return dto;
   }

   private List<BibliographyDTO> getBibliographies(List<RestApiV1.Bibliography> bibliography)
   {
      List<BibliographyDTO> bibDTOs = new ArrayList<>();
      if (bibliography != null)
      {
         bibliography.forEach((bib) ->
         {
            BibliographyDTO dto = new BibliographyDTO();
            dto.id = bib.id;
            dto.type = bib.type;
            dto.title = bib.title;
            dto.edition = bib.edition;
            dto.publisher = bib.publisher;
            dto.publisherPlace = bib.publisherPlace;
            dto.containerTitle = bib.containerTitle;
            dto.url = bib.URL;

            dto.author = getAuthor(bib.author);
            dto.translator = getTranslator(bib.translator);
            dto.issued = getIssued(bib.issued);

            bibDTOs.add(dto);
         });
      }

      return bibDTOs;
   }

   private IssuedBiblioDTO getIssued(RestApiV1.Issued issued)
   {
      IssuedBiblioDTO dto = new IssuedBiblioDTO();
      if (issued == null)
         dto.dateParts = new ArrayList<>();
      else
         dto.dateParts = new ArrayList<>(issued.dateParts);
      return dto;
   }

   private List<BibTranslatorDTO> getTranslator(List<RestApiV1.Translator> translator)
   {
      List<BibTranslatorDTO> transDTOs = new ArrayList<>();
      if (translator != null)
      {
         translator.forEach((t)->
         {
            BibTranslatorDTO dto = new BibTranslatorDTO();
            dto.family = t.family;
            dto.given = t.given;
            dto.literal = t.literal;
            transDTOs.add(dto);
         });
      }

      return transDTOs;
   }

   private List<BibAuthorDTO> getAuthor(List<RestApiV1.Author> authors)
   {
      List<BibAuthorDTO> authDTOs = new ArrayList<>();
      if (authors != null)
      {
         authors.forEach((auth)->
         {
            BibAuthorDTO dto = new BibAuthorDTO();
            dto.family = auth.family;
            dto.given = auth.given;
            authDTOs.add(dto);
         });
      }
      return authDTOs;
   }

   private List<CitationDTO> getCitations(List<RestApiV1.Citation> citations)
   {
      List<CitationDTO> citeDTOs = new ArrayList<>();
      if (citations != null)
      {
         citations.forEach((cite) ->
         {
            CitationDTO dto = new CitationDTO();
            dto.id = cite.citationID;
            dto.citationItems = new ArrayList<>();
            cite.citationItems.forEach((item)->
            {
               CitationItemDTO itemDTO = new CitationItemDTO();
               itemDTO.id = item.id;
               itemDTO.label = item.label;
               itemDTO.locator = item.locator;
               itemDTO.suppressAuthor = item.suppressAuthor;
               dto.citationItems.add(itemDTO);
            });
            citeDTOs.add(dto);
         });
      }
      return citeDTOs;
   }

   private List<FootnoteDTO> getFootnotes(List<RestApiV1.Footnote> footnotes)
   {
      List<FootnoteDTO> footnoteDTOs = new ArrayList<>();
      if (footnotes != null)
      {
         footnotes.forEach((ftn) ->
         {
            FootnoteDTO dto = new FootnoteDTO();
            dto.id = ftn.id;
            dto.text = ftn.text;

            footnoteDTOs.add(dto);
         });
      }
      return footnoteDTOs;
   }
}
