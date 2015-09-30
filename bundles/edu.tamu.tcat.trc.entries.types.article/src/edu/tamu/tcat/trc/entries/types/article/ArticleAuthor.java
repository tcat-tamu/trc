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
    * @return A display label, typically the person's name.
    */
   String getLabel();
}
