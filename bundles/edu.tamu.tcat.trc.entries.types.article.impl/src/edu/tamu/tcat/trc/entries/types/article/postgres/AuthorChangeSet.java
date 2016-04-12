package edu.tamu.tcat.trc.entries.types.article.postgres;

import edu.tamu.tcat.trc.entries.types.article.dto.ArticleAuthorDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.ArticleAuthorDTO.ContactInfoDTO;

public class AuthorChangeSet
{

   public String name;
   public String affiliation;
   public ContactInfoDTO contact;
   public ArticleAuthorDTO original;
   public String id;

   public AuthorChangeSet(String id)
   {
      this.id = id;
   }

}
