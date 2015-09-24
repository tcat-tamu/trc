package edu.tamu.tcat.trc.entries.types.article.search;

import edu.tamu.tcat.trc.search.SearchException;

/**
 * A command-based API for parameterizing and executing a search for articles.
 */
public interface ArticleQueryCommand
{
   /**
    * Executes the configured query.
    *
    * @return The search results.
    * @throws SearchException If there were problems executing the search.
    */
   ArticleSearchResult execute() throws SearchException;

   /**
    * @param q A free-text query to be executed over the title and content of the article. This
    *       corresponds to the common notion of text entered into a search box.
    */
   void setQuery(String q);

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
