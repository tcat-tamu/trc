package edu.tamu.tcat.trc.entries.types.article.postgres;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.article.Bibliography;
import edu.tamu.tcat.trc.entries.types.article.Bibliography.BiblioAuthor;
import edu.tamu.tcat.trc.entries.types.article.Bibliography.BiblioTranslator;
import edu.tamu.tcat.trc.entries.types.article.Bibliography.IssuedDate;
import edu.tamu.tcat.trc.entries.types.article.dto.BibliographyDTO.BibAuthorDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.BibliographyDTO.BibTranslatorDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.BibliographyDTO.IssuedBiblioDTO;

public class PsqlBibliography implements Bibliography
{

   private String id;
   private String type;
   private String title;
   private String edition;
   private List<BiblioAuthor> author;
   private List<BiblioTranslator> translator;
   private String publisher;
   private String publisherPlace;
   private String containerTitle;
   private String url;
   private IssuedDate issued;

   public PsqlBibliography(String id, String type, String title, String edition, List<BibAuthorDTO> authors,
                           List<BibTranslatorDTO> translator, String publisher, String publisherPlace,
                           String containerTitle, String url ,IssuedBiblioDTO issued)
   {
      this.id = id;
      this.type = type;
      this.title = title;
      this.edition = edition;
      this.author = getAuthors(authors);
      this.translator = getTranslators(translator);
      this.publisher = publisher;
      this.publisherPlace = publisherPlace;
      this.containerTitle = containerTitle;
      this.url = url;
      this.issued = new PsqlIssuedDate(issued.dateParts);
   }

   @Override
   public String getId()
   {
      return this.id;
   }

   @Override
   public String getType()
   {
      return this.type;
   }

   @Override
   public String getTitle()
   {
      return this.title;
   }
   
   @Override
   public String getEdition()
   {
      return this.edition;
   }

   @Override
   public List<BiblioAuthor> getAuthors()
   {
      return this.author;
   }

   @Override
   public List<BiblioTranslator> getTranslators()
   {
      return this.translator;
   }

   @Override
   public String getPublisher()
   {
      return this.publisher;
   }

   @Override
   public String getPublishLocation()
   {
      return this.publisherPlace;
   }
   
   @Override
   public String getContainerTitle()
   {
      return this.containerTitle;
   }

   @Override
   public String getUrl()
   {
      return this.url;
   }

   @Override
   public IssuedDate getIssuedDate()
   {
      return this.issued;
   }

   private List<BiblioTranslator> getTranslators(List<BibTranslatorDTO> translators)
   {
      List<BiblioTranslator> trans = new ArrayList<>();
      
      translators.forEach((t) ->
      {
         trans.add(new PsqlBiblioTranslator(t.family, t.given, t.literal));
      });
      
      return trans;
   }

   private List<BiblioAuthor> getAuthors(List<BibAuthorDTO> authors)
   {
      List<BiblioAuthor> bibAuths = new ArrayList<>();
      
      authors.forEach((auth) ->
      {
         bibAuths.add(new PsqlBiblioAuthor(auth.family, auth.given));
      });
      
      return bibAuths;
   }
   
   public static class PsqlBiblioAuthor implements BiblioAuthor
   {

      private String family;
      private String given;

      public PsqlBiblioAuthor(String family, String given)
      {
         this.family = family;
         this.given = given;
      }

      @Override
      public String getFamily()
      {
         return this.family;
      }

      @Override
      public String getGiven()
      {
         return this.given;
      }
   }
   
   public static class PsqlBiblioTranslator implements BiblioTranslator
   {
      

      private String family;
      private String given;
      private String literal;

      public PsqlBiblioTranslator(String family, String given, String literal)
      {
         this.family = family;
         this.given = given;
         this.literal = literal;
      }

      @Override
      public String getFamily()
      {
         return this.family;
      }
   
      @Override
      public String getGiven()
      {
         return this.given;
      }
   
      @Override
      public String getLiteral()
      {
         return this.literal;
      }
   }
   
   public static class PsqlIssuedDate implements IssuedDate
   {

      private List<List<String>> dateParts;

      public PsqlIssuedDate(List<List<String>> dateParts)
      {
         this.dateParts = new ArrayList<>(dateParts);
      }

      @Override
      public List<List<String>> getDateParts()
      {
         return this.dateParts;
      }
      
   }
}
