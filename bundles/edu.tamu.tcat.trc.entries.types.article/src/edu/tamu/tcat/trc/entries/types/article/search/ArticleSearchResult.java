package edu.tamu.tcat.trc.entries.types.article.search;

import java.util.List;

public interface ArticleSearchResult
{
   /**
    * @return The search query used to generate these results.
    */
   ArticleQuery getQuery();

   /**
    * @return An estimate of the total number of results that matched the query, excluding the
    *       paging query parameters.
    */
   long getNumberMatched();

   /**
    * @return search proxies for the Articles that matched the supplied results.
    */
   List<ArticleSearchProxy> getResults();
}
