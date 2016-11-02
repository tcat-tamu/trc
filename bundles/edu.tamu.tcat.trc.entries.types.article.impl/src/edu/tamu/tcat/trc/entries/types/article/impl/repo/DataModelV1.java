package edu.tamu.tcat.trc.entries.types.article.impl.repo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

         if (original.authors != null)
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

      public List<ArticleAuthor> authors = new ArrayList<>();
      public final Map<String, Footnote> footnotes = new HashMap<>();
   }

   public static class ArticleAuthor
   {
      public String id;
      public String name;
      public String first;
      public String last;

      public Map<String, String> properties = new HashMap<>();

      public static ArticleAuthor copy(ArticleAuthor orig)
      {
         ArticleAuthor author = new ArticleAuthor();
         author.id = orig.id;
         author.name = orig.name;
         author.first = orig.first;
         author.last = orig.last;
         author.properties = new HashMap<>(orig.properties);

         return author;
      }
   }

   public static class Footnote
   {
      public String id;
      public String backlinkId;
      public String content;
      public String mimeType;
   }

}
