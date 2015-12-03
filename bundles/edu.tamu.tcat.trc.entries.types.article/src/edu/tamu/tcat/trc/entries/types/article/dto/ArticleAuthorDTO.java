package edu.tamu.tcat.trc.entries.types.article.dto;

import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;

public class ArticleAuthorDTO
{
   public String id;
   public String name;
   public String affiliation;
   public String email;
   
   public static ArticleAuthorDTO create(ArticleAuthor author)
   {
      ArticleAuthorDTO a = new ArticleAuthorDTO();
      a.id = author.getId();
      a.name = author.getName();
      a.affiliation = author.getAffiliation();
      a.email = author.getEmail();
      return a;
   }
   
   public static ArticleAuthorDTO copy(ArticleAuthorDTO orig)
   {
      ArticleAuthorDTO dto = new ArticleAuthorDTO();
      dto.id = orig.id;
      dto.name = orig.name;
      dto.affiliation = orig.affiliation;
      dto.email = orig.email;
      return dto;
   }
}
