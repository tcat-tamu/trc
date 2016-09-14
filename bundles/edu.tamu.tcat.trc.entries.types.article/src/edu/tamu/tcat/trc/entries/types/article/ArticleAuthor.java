package edu.tamu.tcat.trc.entries.types.article;

/**
 *  An identifier for an author of the article.
 *
 * @since 1.1
 */
public interface ArticleAuthor
{
   // TODO need to deal with corporate authors, authors who don't have system accounts, etc.
   //      NOTE that, without authoritative management of authors, this information cannot
   //      be systematically updated for all authors.

   /**
    * @return An internal identifier for this author. Note that currently, this references
    *       an author within an individual work. The same author will have different id's
    *       when used across different works.
    */
   String getId();

   /**
    * @return The author's name.
    */
   String getName();

   /**
    * @return The first or given name of this individual.
    */
   String getFirstname();

   /**
    * @return The last or family name of this individual. Used for alphabetizing by author.
    */
   String getLastname();

   /**
    * @return The affiliation of the author
    */
   String getAffiliation();

   /**
    * @return Structured contact information for this author.
    */
   ContactInfo getContactInfo();

   public interface ContactInfo
   {
      // NOTE using an object to support future expansion.
      //      May use Map<String, String> instead.
      /**
       * @return Email for this author if available.
       *    May be {@code null} or empty string.
       */
      String getEmail();

      /**
       * @return phone number for this author if available.
       *    May be {@code null} or empty string.
       */
      String getPhone();
   }
}
