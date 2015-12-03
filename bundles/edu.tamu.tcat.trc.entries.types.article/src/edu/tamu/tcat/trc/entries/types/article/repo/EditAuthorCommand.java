package edu.tamu.tcat.trc.entries.types.article.repo;

import java.util.concurrent.Future;

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
   void setEmail(String email);
   
   Future<String> execute();
}
