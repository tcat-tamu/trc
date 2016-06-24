package edu.tamu.tcat.trc.entries.types.article.repo;

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

      // public List<ArticleAuthorDTO> authors;
      // public PublicationDTO info;
      // public List<CitationDTO> citation;
      // public List<FootnoteDTO> footnotes;
      // public List<BibliographyDTO> bibliographies;
      // public List<LinkDTO> links;

   }

}
