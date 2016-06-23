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
         article.type = original.type;
         article.articleAbstract = original.articleAbstract;
         article.body = original.body;

         return article;

      }

      public String id;
      public String title;
      public String type;

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
