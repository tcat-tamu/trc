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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.core.repo.NoSuchEntryException;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;


public class ArticleResource
{
   private final static Logger logger = Logger.getLogger(ArticleResource.class.getName());

   private static final String ERR_NOT_FOUND = "Could not find an article with the supplied id {0}";
   private static final String ERR_UNKNOWN_GET = "That's embrassing. Something went wrong while trying to retrieve {0}.";

   private final EntryRepositoryRegistry repo;
   private final ObjectMapper mapper;

   private final String articleId;


   public ArticleResource(EntryRepositoryRegistry repoSvc, String articleId)
   {
      this.repo = repoSvc;
      this.articleId = articleId;

      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Article get()
   {
      try
      {
         ArticleRepository articleRepo = repo.getRepository(null, ArticleRepository.class);
         Article article = articleRepo.get(articleId);
         return ModelAdapter.adapt(article, repo.getResolverRegistry());
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

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Article update(RestApiV1.Article article)
   {
      // TODO add support for partial updates
      ArticleRepository articleRepo = repo.getRepository(null, ArticleRepository.class);
      try
      {
         EditArticleCommand editCmd = articleRepo.edit(articleId);

         apply(editCmd, article);
         editCmd.execute().get(10, TimeUnit.SECONDS);

         // we get the current version from the repo in order to ensure that we have
         // both the changes that were applied in this update (already present in article)
         // and any changes that may have been applied prior to this.
         Article current = articleRepo.get(articleId);
         return ModelAdapter.adapt(current, repo.getResolverRegistry());
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
      ArticleRepository articleRepo = repo.getRepository(null, ArticleRepository.class);
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
      editCmd.setAbstract(article.articleAbstract);
      editCmd.setBody(article.body);

//      editCmd.setPublicationInfo(getPublication(article.pubInfo));
//      editCmd.setAuthors(getAuthors(article.authors));
//      editCmd.setFootnotes(getFootnotes(article.footnotes));
//      editCmd.setCitations(getCitations(article.citations));
//      editCmd.setBibliography(getBibliographies(article.bibliography));
//      editCmd.setLinks(getLinks(article.links));
   }

//   private List<ArticleAuthorDTO> getAuthors(List<RestApiV1.ArticleAuthor> authors)
//   {
//      List<ArticleAuthorDTO> authorDTO = new ArrayList<>();
//      if (authors != null)
//      {
//         authors.forEach((a) ->
//         {
//            ArticleAuthorDTO authDto = new ArticleAuthorDTO();
//            authDto.id = a.id;
//            authDto.name = a.name;
//            authDto.affiliation = a.affiliation;
//            authDto.contact = ArticleAuthorDTO.ContactInfoDTO.create(a.contact.email, a.contact.phone);
//            authorDTO.add(authDto);
//         });
//      }
//
//      return authorDTO;
//   }
//
//   private List<LinkDTO> getLinks(List<RestApiV1.LinkedResource> links)
//   {
//      List<LinkDTO> dto = new ArrayList<>();
//      if(links != null)
//      {
//         links.forEach((link)->
//         {
//            LinkDTO linkDTO = new LinkDTO();
//            linkDTO.id = link.id;
//            linkDTO.type = link.type;
//            linkDTO.title = link.title;
//            linkDTO.uri = link.uri;
//            linkDTO.rel = link.rel;
//            dto.add(linkDTO);
//         });
//      }
//      return dto;
//   }
//
//   private List<BibliographyDTO> getBibliographies(List<RestApiV1.Bibliography> bibliography)
//   {
//      List<BibliographyDTO> bibDTOs = new ArrayList<>();
//      if (bibliography != null)
//      {
//         bibliography.forEach((bib) ->
//         {
//            BibliographyDTO dto = new BibliographyDTO();
//            dto.id = bib.id;
//            dto.type = bib.type;
//            dto.title = bib.title;
//            dto.edition = bib.edition;
//            dto.publisher = bib.publisher;
//            dto.publisherPlace = bib.publisherPlace;
//            dto.containerTitle = bib.containerTitle;
//            dto.url = bib.URL;
//
//            dto.author = getAuthor(bib.author);
//            dto.translator = getTranslator(bib.translator);
//            dto.issued = getIssued(bib.issued);
//
//            bibDTOs.add(dto);
//         });
//      }
//
//      return bibDTOs;
//   }
//
//   private IssuedBiblioDTO getIssued(RestApiV1.Issued issued)
//   {
//      IssuedBiblioDTO dto = new IssuedBiblioDTO();
//      if (issued == null)
//         dto.dateParts = new ArrayList<>();
//      else
//         dto.dateParts = new ArrayList<>(issued.dateParts);
//      return dto;
//   }
//
//   private List<BibTranslatorDTO> getTranslator(List<RestApiV1.Translator> translator)
//   {
//      List<BibTranslatorDTO> transDTOs = new ArrayList<>();
//      if (translator != null)
//      {
//         translator.forEach((t)->
//         {
//            BibTranslatorDTO dto = new BibTranslatorDTO();
//            dto.family = t.family;
//            dto.given = t.given;
//            dto.literal = t.literal;
//            transDTOs.add(dto);
//         });
//      }
//
//      return transDTOs;
//   }
//
//   private List<BibAuthorDTO> getAuthor(List<RestApiV1.Author> authors)
//   {
//      List<BibAuthorDTO> authDTOs = new ArrayList<>();
//      if (authors != null)
//      {
//         authors.forEach((auth)->
//         {
//            BibAuthorDTO dto = new BibAuthorDTO();
//            dto.family = auth.family;
//            dto.given = auth.given;
//            authDTOs.add(dto);
//         });
//      }
//      return authDTOs;
//   }
//
//   private List<CitationDTO> getCitations(List<RestApiV1.Citation> citations)
//   {
//      List<CitationDTO> citeDTOs = new ArrayList<>();
//      if (citations != null)
//      {
//         citations.forEach((cite) ->
//         {
//            CitationDTO dto = new CitationDTO();
//            dto.id = cite.citationID;
//            dto.citationItems = new ArrayList<>();
//            cite.citationItems.forEach((item)->
//            {
//               CitationItemDTO itemDTO = new CitationItemDTO();
//               itemDTO.id = item.id;
//               itemDTO.label = item.label;
//               itemDTO.locator = item.locator;
//               itemDTO.suppressAuthor = item.suppressAuthor;
//               dto.citationItems.add(itemDTO);
//            });
//            citeDTOs.add(dto);
//         });
//      }
//      return citeDTOs;
//   }
//
//   private List<FootnoteDTO> getFootnotes(List<RestApiV1.Footnote> footnotes)
//   {
//      List<FootnoteDTO> footnoteDTOs = new ArrayList<>();
//      if (footnotes != null)
//      {
//         footnotes.forEach((ftn) ->
//         {
//            FootnoteDTO dto = new FootnoteDTO();
//            dto.id = ftn.id;
//            dto.text = ftn.text;
//
//            footnoteDTOs.add(dto);
//         });
//      }
//      return footnoteDTOs;
//   }
}
