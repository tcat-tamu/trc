package edu.tamu.tcat.trc.entries.types.article.docrepo;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class DataModelV1
{

   public static class Article
   {
      public static Article copy(Article original)
      {
         Article article = new Article();
         article.id = original.id;
         article.title = original.title;
         article.slug = original.slug;
         article.contentType = original.contentType;
         article.articleType = original.articleType;
         article.articleAbstract = original.articleAbstract;
         article.body = original.body;

         article.authors = original.authors.stream()
               .map(ArticleAuthor::copy)
               .collect(Collectors.toList());
         return article;
      }

      public String id;
      public String title;
      public String slug;
      public String contentType;
      public String articleType;

      @JsonProperty("abstract")
      public String articleAbstract;
      public String body;

      public List<ArticleAuthor> authors;
      // public PublicationDTO info;
      // public List<CitationDTO> citation;
      // public List<FootnoteDTO> footnotes;
      // public List<BibliographyDTO> bibliographies;
      // public List<LinkDTO> links;

   }

   public static class ArticleAuthor
   {
      public String id;
      public String name;
      public String first;
      public String last;
      public String affiliation;
      public ContactInfo contact = new ContactInfo();

      public static ArticleAuthor copy(ArticleAuthor orig)
      {
         ArticleAuthor author = new ArticleAuthor();
         author.id = orig.id;
         author.name = orig.name;
         author.first = orig.first;
         author.last = orig.last;
         author.affiliation = orig.affiliation;
         author.contact = ContactInfo.copy(orig.contact);

         return author;
      }
   }

   public static class ContactInfo
   {
      public String email;
      public String phone;

      public static ContactInfo copy(ContactInfo orig)
      {
         ContactInfo info = new ContactInfo();
         info.email = orig.email;
         info.phone = orig.phone;

         return info;
      }
   }

}
