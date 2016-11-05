package edu.tamu.tcat.trc.entries.types.article.search;

import java.util.concurrent.CompletableFuture;

/**
 * A command-based API for parameterizing and executing a search for articles.
 * @since 1.1
 */
public interface ArticleQueryCommand
{
   /**
    * Executes the configured query.
    *
    * @return The search results.
    * @throws SearchException If there were problems executing the search.
    */
   CompletableFuture<ArticleSearchResult> execute();

   /**
    * @param q A free-text query to be executed over the title and content of the article. This
    *       corresponds to the common notion of text entered into a search box.
    */
   void query(String q);

   // TODO add author query by name and/or id

   /**
    * @param start The offset for the first article to be returned. Used to support paged
    *       search results.
    */
   void setOffset(int start);

   /**
    * @param max The maximum number of results to be returned. Used to support paged results.
    *       Defaults to 25.
    */
   void setMaxResults(int max);

}
