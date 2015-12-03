package edu.tamu.tcat.trc.entries.types.article.postgres;

import edu.tamu.tcat.trc.entries.types.article.dto.ArticleAuthorDTO;

public class AuthorChangeSet
{

   public String name;
   public String affiliation;
   public String email;
   public ArticleAuthorDTO original;
   public String id;

   public AuthorChangeSet(String id)
   {
      this.id = id;
   }

}
