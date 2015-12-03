package edu.tamu.tcat.trc.entries.types.article;

/**
 *  An identifier for an author of the article.
 * @since 1.1
 */
public interface ArticleAuthor
{
   // TODO need to deal with corporate authors, authors who don't have system accounts, etc.

   /**
    * @return A unique, application-defined identifier for this author.
    */
   String getId();

   /**
    * @return The author's name.
    */
   String getName();

   /**
    * 
    * @return The affiliation of the author
    */
   String getAffiliation();
   
   /**
    * 
    * @return The email address of the author
    */
   String getEmail();
}
