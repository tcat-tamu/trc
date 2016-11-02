package edu.tamu.tcat.trc.entries.types.article.impl.repo;

import java.util.function.Function;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;

public abstract class ModelAdapter
{
   public DataModelV1.Article adapt(Article article)
   {
      DataModelV1.Article dto = new DataModelV1.Article();
      dto.id = article.getId();
      dto.title = article.getTitle();
      dto.slug = article.getSlug();
      dto.authors =  article.getAuthors().stream()
            .map(ModelAdapter::adapt)
            .collect(Collectors.toList());
      dto.contentType = article.getContentType();
      dto.articleType = article.getArticleType();
      dto.articleAbstract = article.getAbstract();
      dto.body = article.getBody();

      return dto;
   }

   private static DataModelV1.ArticleAuthor adapt(ArticleAuthor author)
   {
      DataModelV1.ArticleAuthor dto = new DataModelV1.ArticleAuthor();
      dto.id = author.getId();
      dto.name = author.getDisplayName();
      dto.first = author.getFirstname();
      dto.last = author.getLastname();

      dto.properties = author.getProperties().stream()
            .collect(Collectors.toMap(
                  Function.identity(),
                  key -> author.getProperty(key).orElse("")
            ));

      return dto;
   }
}
