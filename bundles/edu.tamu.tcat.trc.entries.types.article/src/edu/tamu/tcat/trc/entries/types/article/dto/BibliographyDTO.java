package edu.tamu.tcat.trc.entries.types.article.dto;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.article.Bibliography;
import edu.tamu.tcat.trc.entries.types.article.Bibliography.BiblioAuthor;
import edu.tamu.tcat.trc.entries.types.article.Bibliography.BiblioTranslator;
import edu.tamu.tcat.trc.entries.types.article.Bibliography.IssuedDate;

public class BibliographyDTO
{
   public String id;
   public String type;
   public String title;
   public String edition;
   public List<BibAuthorDTO> author;
   public List<BibTranslatorDTO> translator;
   public String publisher;
   public String publisherPlace;
   public String containerTitle;
   public String url;
   public IssuedBiblioDTO issued;
   
   
   public static BibliographyDTO create(Bibliography b)
   {
      BibliographyDTO dto = new BibliographyDTO();
      dto.id = b.getId();
      dto.type = b.getType();
      dto.title = b.getTitle();
      dto.edition =
      dto.publisher = b.getPublisher();
      dto.publisherPlace = b.getPublishLocation();
      dto.url = b.getUrl();
      dto.author = BibAuthorDTO.create(b.getAuthors());
      dto.translator = BibTranslatorDTO.create(b.getTranslators());
      dto.issued = IssuedBiblioDTO.create(b.getIssuedDate());
      return null;
   }
   
   public static class BibAuthorDTO
   {
      public String family;
      public String given;
      
      public static List<BibAuthorDTO> create(List<BiblioAuthor> authors)
      {
         List<BibAuthorDTO> auths = new ArrayList<>();
         authors.forEach((author)->
         {
            BibAuthorDTO dto = new BibAuthorDTO();
            dto.family = author.getFamily();
            dto.given = author.getGiven();
            auths.add(dto);
         });
         return auths;
      }
   }
   
   public static class BibTranslatorDTO
   {
      public String family;
      public String given;
      public String literal;
      
      public static List<BibTranslatorDTO> create(List<BiblioTranslator> translators)
      {
         List<BibTranslatorDTO> transDTO = new ArrayList<>();
         translators.forEach((translator)->
         {
            BibTranslatorDTO dto = new BibTranslatorDTO();
            dto.family = translator.getFamily();
            dto.given = translator.getGiven();
            dto.literal = translator.getLiteral();
            transDTO.add(dto);
         });
         return transDTO;
      }
   }
   
   public static class IssuedBiblioDTO
   {
      public List<List<String>> dateParts;

      public static IssuedBiblioDTO create(IssuedDate issuedDate)
      {
         IssuedBiblioDTO dto = new IssuedBiblioDTO();
         dto.dateParts = new ArrayList<List<String>>(issuedDate.getDateParts());
         return dto;
      }
   }
}
