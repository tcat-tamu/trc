package edu.tamu.tcat.trc.entries.types.article.dto;

import java.net.URI;
import java.util.UUID;

import edu.tamu.tcat.trc.entries.types.article.Article;

public class ArticleDTO
{
   public UUID id;
   public String title;
   public URI associatedEntity;
   public String authorId;
   public String mimeType;
   public String content;


   public static ArticleDTO create(Article article)
   {
      ArticleDTO dto = new ArticleDTO();

      dto.id = article.getId();
      dto.title = article.getTitle();
      dto.associatedEntity = article.getEntity();
      dto.authorId = article.getAuthorId().toString();
      dto.mimeType = article.getMimeType();
      dto.content = article.getContent();

      return dto;
   }

   public static ArticleDTO copy(ArticleDTO orig)
   {
      ArticleDTO dto = new ArticleDTO();

      dto.id = orig.id;
      dto.title = orig.title;
      dto.associatedEntity = orig.associatedEntity;
      dto.authorId = orig.authorId;
      dto.mimeType = orig.mimeType;
      dto.content = orig.content;

      return dto;
   }
}
