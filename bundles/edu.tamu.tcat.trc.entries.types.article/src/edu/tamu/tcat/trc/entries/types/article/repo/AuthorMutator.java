package edu.tamu.tcat.trc.entries.types.article.repo;

/**
 *  Obtained from an {@link EditArticleCommand} in order to update properties of
 *  one of the author's of the article.
 */
public interface AuthorMutator
{
   /**
    * @return The unique identifier for the author being edited.
    */
   String getId();

   /**
    * @param name The first name of the author.
    */
   void setFirstname(String name);

   /**
    * @param name The last name of the author. This field will be used to
    *       determine sort-order, where appropriate.
    */
   void setLastname(String name);

   /**
    * @param affiliation The affiliation of this author.
    */
   void setAffiliation(String affiliation);

   /**
    * @param email An email address to use to contact this author.
    */
   void setEmailAddress(String email);

}
