package edu.tamu.tcat.trc.entries.types.article.search.solr;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.tamu.tcat.trc.entries.types.article.search.ArticleQuery;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchProxy;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchResult;

/**
 * @since 1.1
 */
public class SolrArticleResults implements ArticleSearchResult
{
   private List<ArticleSearchProxy> articles;
   private ArticleQuery query;
   private long numMatched;
   private Map<String, Map<String, List<String>>> highlighting;

   public SolrArticleResults(ArticleQuery query, List<ArticleSearchProxy> articles, Map<String, Map<String, List<String>>> highlighting, long numMatched)
   {
      this.query = new ArticleQuery(query);
      this.articles = articles;
      this.highlighting = highlighting;
      this.numMatched = numMatched;

   }

   @Override
   public ArticleQuery getQuery()
   {
      return query;
   }

   @Override
   public long getNumberMatched()
   {
      return numMatched;
   }

   @Override
   public List<ArticleSearchProxy> getResults()
   {
      return Collections.unmodifiableList(articles);
   }

   @Override
   public Map<String, Map<String, List<String>>> getHits()
   {
      return highlighting;
   }

}
