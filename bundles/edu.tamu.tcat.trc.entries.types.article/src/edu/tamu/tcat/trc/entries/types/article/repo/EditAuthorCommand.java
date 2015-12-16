package edu.tamu.tcat.trc.entries.types.article.repo;

import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.types.article.dto.ArticleAuthorDTO.ContactInfoDTO;

public interface EditAuthorCommand
{
   /**
    * The name of the Author as it appears on the article
    * @param name
    */
   void setName(String name);
   
   /**
    * The entity that the author is associating themselves with. This could be null.
    * @param affiliation
    */
   void setAffiliation(String affiliation);
   
   /**
    * The email of the author. This could be null.
    * @param email
    */
   void setContact(ContactInfoDTO info);
   
   Future<String> execute();
}
