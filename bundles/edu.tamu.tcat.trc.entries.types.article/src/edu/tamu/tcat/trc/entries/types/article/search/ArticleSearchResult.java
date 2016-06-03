package edu.tamu.tcat.trc.entries.types.article.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

   /**
    * @return the Hit Highlight results found.
    */
   Map<String, Map<String, List<String>>> getHits();

   /**
    * @deprecated To be moved into core TRC Search API
    */
   @Deprecated
   public static class FacetValue
   {
      public String filter;
      public String name;
      public long count;
   }

   /**
    * @deprecated To be moved into core TRC Search API
    */
   @Deprecated
   public static class FacetValueList
   {
      public int count;
      public String name;
      public List<FacetValue> items = new ArrayList<>();
   }

   List<FacetValueList> getFacets();
}
