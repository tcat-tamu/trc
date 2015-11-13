package edu.tamu.tcat.trc.entries.types.article.dto;

import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;

public class ArticleAuthorDTO
{
   public String id;
   public String label;
   
   public static ArticleAuthorDTO create(ArticleAuthor author)
   {
      ArticleAuthorDTO a = new ArticleAuthorDTO();
      a.id = author.getId();
      a.label = author.getLabel();
      return a;
   }
   
   public static ArticleAuthorDTO copy(ArticleAuthorDTO orig)
   {
      ArticleAuthorDTO dto = new ArticleAuthorDTO();
      dto.id = orig.id;
      dto.label = orig.label;
      return dto;
   }
}
