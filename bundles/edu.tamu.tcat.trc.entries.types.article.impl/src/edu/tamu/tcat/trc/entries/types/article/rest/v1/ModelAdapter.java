package edu.tamu.tcat.trc.entries.types.article.rest.v1;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.article.dto.ArticleAuthorDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.BibliographyDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.BibliographyDTO.BibAuthorDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.BibliographyDTO.BibTranslatorDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.BibliographyDTO.IssuedBiblioDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.CitationDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.CitationItemDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.FootnoteDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.LinkDTO;
import edu.tamu.tcat.trc.entries.types.article.rest.v1.RestApiV1.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.rest.v1.RestApiV1.Author;
import edu.tamu.tcat.trc.entries.types.article.rest.v1.RestApiV1.Bibliography;
import edu.tamu.tcat.trc.entries.types.article.rest.v1.RestApiV1.Citation;
import edu.tamu.tcat.trc.entries.types.article.rest.v1.RestApiV1.Issued;
import edu.tamu.tcat.trc.entries.types.article.rest.v1.RestApiV1.Translator;

public class ModelAdapter
{

//   public static PublicationDTO getPublication(Publication pubInfo)
//   {
//      PublicationDTO pubDTO = new PublicationDTO();
//      pubDTO.dateCreated = pubInfo.dateCreated;
//      pubDTO.dateModified = pubInfo.dateModified;
//      return pubDTO;
//   }

   public static List<ArticleAuthorDTO> getAuthors(List<ArticleAuthor> authors)
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

   public static List<LinkDTO> getLinks(List<RestApiV1.LinkedResource> links)
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

   public static List<BibliographyDTO> getBibliographies(List<Bibliography> bibliography)
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

   public static IssuedBiblioDTO getIssued(Issued issued)
   {
      IssuedBiblioDTO dto = new IssuedBiblioDTO();
      if (issued == null)
         dto.dateParts = new ArrayList<>();
      else
         dto.dateParts = new ArrayList<>(issued.dateParts);
      return dto;
   }

   public static List<BibTranslatorDTO> getTranslator(List<Translator> translator)
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

   public static List<BibAuthorDTO> getAuthor(List<Author> authors)
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

   public static List<CitationDTO> getCitations(List<Citation> citations)
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

   public static List<FootnoteDTO> getFootnotes(List<RestApiV1.Footnote> footnotes)
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
