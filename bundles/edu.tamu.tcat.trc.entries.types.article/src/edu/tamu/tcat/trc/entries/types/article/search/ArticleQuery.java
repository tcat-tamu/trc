package edu.tamu.tcat.trc.entries.types.article.search;

/**
 * A simple data vehicle to be used to represent an article query.
 */
public class ArticleQuery
{
   /**
    * Free-text query string to be matched against the article title, text and other
    * properties. May be {@code null} or empty string in which case all articles will be
    * returned, respecting the result paging limits.
    */
   public String q;

   /**
    * Indicates whether hit highlighting should be enabled.
    */
   public boolean highlighting = true;

   /**
    * The index of the first result to return. Used to support result paging.
    */
   public int offset;

   /**
    * The index of the last result to return.
    */
   public int max;

   public ArticleQuery()
   {

   }

   public ArticleQuery(ArticleQuery orig)
   {
      this.q = orig.q;
      this.highlighting = orig.highlighting;
      this.offset = orig.offset;
      this.max = orig.max;
   }
}
